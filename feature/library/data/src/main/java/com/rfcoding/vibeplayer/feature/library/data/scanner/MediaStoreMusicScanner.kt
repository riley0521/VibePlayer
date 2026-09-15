package com.rfcoding.vibeplayer.feature.library.data.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.ScanFilters
import com.rfcoding.vibeplayer.feature.library.domain.ScannedSong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Finds `.mp3` files directly inside `Music/` through MediaStore and reads their tags with
 * [MediaMetadataRetriever]. Embedded cover art is copied to `filesDir/artwork/<MediaStore id>`.
 */
class MediaStoreMusicScanner(
    private val context: Context,
) : MusicScanner {

    private val artworkDir = File(context.filesDir, ARTWORK_DIR_NAME)

    override suspend fun scan(filters: ScanFilters): Result<List<ScannedSong>, DataError.Local> {
        return withContext(Dispatchers.IO) {
            val files = try {
                queryMusicFiles(filters)
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                // SecurityException when the permission was revoked, or a provider failure.
                return@withContext Result.Error(DataError.Local.UNKNOWN)
            }
            // Checkpoint after blocking operation.
            ensureActive()

            artworkDir.mkdirs()
            val seenKeys = HashSet<Pair<String, String?>>()
            val keptArtwork = HashSet<String>()
            val songs = files.mapNotNull { file ->
                ensureActive()
                readSong(file, filters, seenKeys)?.also { song ->
                    if (song.imageUri != null) keptArtwork += file.artworkFileName
                }
            }

            // Every kept song rewrote or confirmed its artwork above, so anything else is stale.
            artworkDir.listFiles()
                ?.filter { it.name !in keptArtwork }
                ?.forEach { it.delete() }

            Result.Success(songs)
        }
    }

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
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, file.uri)
            currentCoroutineContext().ensureActive()

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            if (title == null) return null

            val artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

            val durationMillis = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
            if (durationMillis == null || !filters.acceptsDuration(durationMillis)) return null

            if (!seenKeys.add(title to artistName)) return null

            val picture = retriever.embeddedPicture

            ScannedSong(
                title = title,
                artistName = artistName,
                fileUri = file.uri.toString(),
                imageUri = picture?.let { saveArtwork(file, it) },
                durationMillis = durationMillis,
            )
        } catch (e: CancellationException) {
            // CancellationException is a RuntimeException, so it must escape the catch below.
            throw e
        } catch (_: RuntimeException) {
            // A corrupt or unsupported file; skip it and keep scanning.
            null
        } finally {
            retriever.release()
        }
    }

    /** Returns the artwork's file URI, or null when it couldn't be written. */
    private fun saveArtwork(file: MusicFile, bytes: ByteArray): String? {
        val artworkFile = File(artworkDir, file.artworkFileName)
        return try {
            // Same length is a cheap "unchanged" check that spares a rewrite on every rescan.
            if (!artworkFile.exists() || artworkFile.length() != bytes.size.toLong()) {
                artworkFile.writeBytes(bytes)
            }
            Uri.fromFile(artworkFile).toString()
        } catch (_: IOException) {
            null
        }
    }

    private data class MusicFile(
        val mediaId: Long,
        val uri: Uri,
    ) {
        val artworkFileName: String get() = mediaId.toString()
    }

    private companion object {
        const val ARTWORK_DIR_NAME = "artwork"
    }
}
