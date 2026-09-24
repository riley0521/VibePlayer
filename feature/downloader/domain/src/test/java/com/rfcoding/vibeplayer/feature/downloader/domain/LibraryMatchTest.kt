package com.rfcoding.vibeplayer.feature.downloader.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.song.Song
import org.junit.jupiter.api.Test

class LibraryMatchTest {

    @Test
    fun `a track matches a song with the same title and artist ignoring case and spaces`() {
        val keys = listOf(song(title = "Midnight Drive", artist = "The Night Owls")).toLibraryKeys()

        assertThat(track("1", title = " midnight drive", artist = "THE NIGHT OWLS ").isIn(keys)).isTrue()
    }

    @Test
    fun `a different artist is a different song`() {
        val keys = listOf(song(title = "Intro", artist = "Band A")).toLibraryKeys()

        assertThat(track("1", title = "Intro", artist = "Band B").isIn(keys)).isFalse()
    }

    @Test
    fun `no artist matches no artist`() {
        val keys = listOf(song(title = "Intro", artist = null)).toLibraryKeys()

        assertThat(track("1", title = "Intro", artist = null).isIn(keys)).isTrue()
        assertThat(track("2", title = "Intro", artist = "Band").isIn(keys)).isFalse()
    }

    @Test
    fun `download all skips songs in the library, queued or downloading, and retries failures`() {
        val keys = listOf(song(title = "Owned", artist = "A")).toLibraryKeys()
        val tracks = listOf(
            track("owned", title = "Owned", artist = "A"),
            track("queued", title = "Queued"),
            track("running", title = "Running"),
            track("failed", title = "Failed"),
            track("new", title = "New"),
        )
        val statuses = mapOf(
            "queued" to DownloadStatus.Queued,
            "running" to DownloadStatus.Downloading(0.5f),
            "failed" to DownloadStatus.Failed(DownloadError.NO_INTERNET),
        )

        val result = tracksToDownload(tracks, keys, statuses)

        assertThat(result.map { it.videoId }).containsExactly("failed", "new")
    }

    @Test
    fun `download all takes a song listed twice once`() {
        val tracks = listOf(
            track("a", title = "Same", artist = "Band"),
            track("b", title = "same", artist = "band"),
        )

        val result = tracksToDownload(tracks, emptySet(), emptyMap())

        assertThat(result.map { it.videoId }).containsExactly("a")
    }

    private fun song(title: String, artist: String?) = Song(
        id = title,
        title = title,
        artistName = artist,
        fileUri = "content://$title",
        imageUri = null,
        durationMillis = 60_000,
        isFavorite = false,
        createdAt = 0,
    )
}

internal fun track(videoId: String, title: String = "Song $videoId", artist: String? = null) = RemoteTrack(
    videoId = videoId,
    url = "https://www.youtube.com/watch?v=$videoId",
    title = title,
    artistName = artist,
    thumbnailUrl = null,
    durationMillis = null,
)
