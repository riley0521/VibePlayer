package com.rfcoding.vibeplayer.feature.downloader.data.library

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.core.data.song.MusicFile
import com.rfcoding.vibeplayer.core.data.song.MusicFileTags
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.core.testing.FakeSongLocalDataSource
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class DownloadedSongImporterTest {

    private val songDataSource = FakeSongLocalDataSource()
    private val clock = object : Clock {
        override fun now() = Instant.fromEpochMilliseconds(1_000)
    }
    private var readFile: MusicFile? = MusicFile(
        tags = MusicFileTags(title = "Song", artistName = "Band", durationMillis = 200_000),
        fileUri = "content://media/external/audio/media/7",
        imageUri = "file:///artwork/7",
    )
    private val importer = DownloadedSongImporter(
        musicFileSource = { _, _ -> readFile },
        songDataSource = songDataSource,
        clock = clock,
        newId = { "new-id" },
    )

    @Test
    fun `the downloaded file is upserted as a new song`() = runTest {
        val result = importer.import("content://media/external/audio/media/7", mediaId = 7)

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(songDataSource.upsertedBatches).containsExactly(
            listOf(
                Song(
                    id = "new-id",
                    title = "Song",
                    artistName = "Band",
                    fileUri = "content://media/external/audio/media/7",
                    imageUri = "file:///artwork/7",
                    durationMillis = 200_000,
                    isFavorite = false,
                    createdAt = 1_000,
                ),
            ),
        )
    }

    @Test
    fun `an unreadable file is an error and nothing is stored`() = runTest {
        readFile = null

        assertThat(importer.import("content://x", mediaId = 1)).isEqualTo(Result.Error(DownloadError.UNKNOWN))
        assertThat(songDataSource.upsertedBatches).isEmpty()
    }

    @Test
    fun `a full disk while storing is reported as such`() = runTest {
        songDataSource.upsertError = DataError.Local.DISK_FULL

        assertThat(importer.import("content://x", mediaId = 1)).isEqualTo(Result.Error(DownloadError.DISK_FULL))
    }
}
