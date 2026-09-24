package com.rfcoding.vibeplayer.feature.library.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.rfcoding.vibeplayer.core.data.song.MusicFileReader
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.SCAN_BATCH_SIZE
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.domain.ScannedSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Finds `.mp3` files directly inside `Music/` through MediaStore and reads their tags with
 * [MusicFileReader], which copies embedded cover art to `filesDir/artwork/<MediaStore id>`.
 */
class MediaStoreMusicScanner(
    private val context: Context,
    private val musicFileReader: MusicFileReader,
    private val applicationScope: CoroutineScope
) : MusicScanner {

    private val artworkDir = musicFileReader.artworkDir
    private val seenKeysMutex = Mutex()

    override fun scan(filters: ScanFilters): Flow<Result<List<ScannedSong>, DataError.Local>> = flow {
        val files = try {
            queryMusicFiles(filters)
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            // SecurityException when the permission was revoked, or a provider failure.
            emit(Result.Error(DataError.Local.UNKNOWN))
            return@flow
        }
        // Checkpoint after blocking operation.
        currentCoroutineContext().ensureActive()

        artworkDir.mkdirs()
        // Scan-wide, so duplicates are dropped across batches too.
        val seenKeys = HashSet<Pair<String, String?>>()
        val keptArtwork = HashSet<String>()

        files.chunked(SCAN_BATCH_SIZE).forEach { batch ->
            val songs = coroutineScope {
                batch.map { file ->
                    async { readSong(file, filters, seenKeys) }
                }.awaitAll()
            }
            // Collected after awaitAll, so the set needs no lock.
            batch.zip(songs).forEach { (file, song) ->
                if (song?.imageUri != null) keptArtwork += file.artworkFileName
            }
            emit(Result.Success(songs.filterNotNull()))
        }

        // Every kept song rewrote or confirmed its artwork above, so anything else is stale. A
        // cancelled scan never gets here, so its partial set deletes nothing.
        applicationScope.launch(Dispatchers.IO) {
            artworkDir.listFiles()
                ?.filter { it.name !in keptArtwork }
                ?.forEach { it.delete() }
        }
    }.flowOn(Dispatchers.IO)

    /** Rows that pass the file-name, folder and size rules, oldest first so duplicates resolve stably. */
    private fun queryMusicFiles(filters: ScanFilters): List<MusicFile> {
        val isScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val collection = if (isScopedStorage) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        @Suppress("DEPRECATION")
        val pathColumn = if (isScopedStorage) MediaStore.Audio.Media.RELATIVE_PATH else MediaStore.Audio.Media.DATA
        @Suppress("DEPRECATION")
        val musicDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            pathColumn,
        )
        // LIKE is case-insensitive, matching shared storage. It only narrows the query: subfolders
        // still match on API 28, so the Kotlin rules below make the final call.
        val selection = "$pathColumn LIKE ?"
        val selectionArgs = arrayOf(if (isScopedStorage) MUSIC_RELATIVE_PATH else "$musicDirPath/%")
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} ASC"

        val cursor = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        ) ?: return emptyList()

        return cursor.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val pathColumnIndex = it.getColumnIndexOrThrow(pathColumn)

            buildList {
                while (it.moveToNext()) {
                    val displayName = it.getString(nameColumn) ?: continue
                    val path = it.getString(pathColumnIndex)
                    val isInMusicFolder = if (isScopedStorage) {
                        isDirectlyInMusicFolder(relativePath = path)
                    } else {
                        path != null && isDirectlyInMusicFolder(filePath = path, musicDirPath = musicDirPath)
                    }
                    if (!isMp3(displayName) || !isInMusicFolder || !filters.acceptsSize(it.getLong(sizeColumn))) {
                        continue
                    }
                    val id = it.getLong(idColumn)
                    add(MusicFile(mediaId = id, uri = ContentUris.withAppendedId(collection, id)))
                }
            }
        }
    }

    /**
     * Returns null when the file can't be read, has no title, is too short, or repeats a
     * (title, artist) pair already in [seenKeys].
     */
    private suspend fun readSong(
        file: MusicFile,
        filters: ScanFilters,
        seenKeys: MutableSet<Pair<String, String?>>,
    ): ScannedSong? {
        val read = musicFileReader.read(file.uri, file.mediaId) { tags ->
            filters.acceptsDuration(tags.durationMillis) &&
                seenKeysMutex.withLock { seenKeys.add(tags.title to tags.artistName) }
        } ?: return null

        return ScannedSong(
            title = read.tags.title,
            artistName = read.tags.artistName,
            fileUri = read.fileUri,
            imageUri = read.imageUri,
            durationMillis = read.tags.durationMillis,
        )
    }

    private data class MusicFile(
        val mediaId: Long,
        val uri: Uri,
    ) {
        val artworkFileName: String get() = MusicFileReader.artworkFileName(mediaId)
    }
}
