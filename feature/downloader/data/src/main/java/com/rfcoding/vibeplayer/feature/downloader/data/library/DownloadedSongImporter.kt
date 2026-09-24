package com.rfcoding.vibeplayer.feature.downloader.data.library

import com.rfcoding.vibeplayer.core.data.song.MusicFile
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import java.util.UUID
import kotlin.time.Clock

/** Reads a file's tags and cover art; [com.rfcoding.vibeplayer.core.data.song.MusicFileReader] in the app. */
fun interface MusicFileSource {
    suspend fun read(fileUri: String, mediaId: Long): MusicFile?
}

/**
 * Adds one downloaded file to the library straight away, skipping the scan filters: the user asked
 * for this song. The upsert matches on (title, artist) like a scan does, so a later rescan updates
 * this row instead of adding a second one.
 */
class DownloadedSongImporter(
    private val musicFileSource: MusicFileSource,
    private val songDataSource: SongLocalDataSource,
    private val clock: Clock = Clock.System,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {

    suspend fun import(fileUri: String, mediaId: Long): EmptyResult<DownloadError> {
        val file = musicFileSource.read(fileUri, mediaId) ?: return Result.Error(DownloadError.UNKNOWN)
        val song = Song(
            id = newId(),
            title = file.tags.title,
            artistName = file.tags.artistName,
            fileUri = file.fileUri,
            imageUri = file.imageUri,
            durationMillis = file.tags.durationMillis,
            isFavorite = false,
            createdAt = clock.now().toEpochMilliseconds(),
        )
        return when (val upsert = songDataSource.upsertScannedSongs(listOf(song))) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(
                if (upsert.error == DataError.Local.DISK_FULL) DownloadError.DISK_FULL else DownloadError.UNKNOWN,
            )
        }
    }
}
