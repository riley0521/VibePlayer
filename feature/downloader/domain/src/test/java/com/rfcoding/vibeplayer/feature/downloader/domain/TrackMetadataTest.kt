package com.rfcoding.vibeplayer.feature.downloader.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

class TrackMetadataTest {

    @Test
    fun `YouTube Music fields win over the video title and channel`() {
        val metadata = resolveTrackMetadata(
            title = "Artist - Song (Official Video)",
            track = "Song",
            artist = "Artist",
            channel = "ArtistVEVO",
        )

        assertThat(metadata).isEqualTo(TrackMetadata(title = "Song", artistName = "Artist"))
    }

    @Test
    fun `without music fields the video title and channel stand in`() {
        val metadata = resolveTrackMetadata(
            title = "Artist - Song (Official Video)",
            track = null,
            artist = null,
            channel = "ArtistVEVO",
        )

        assertThat(metadata).isEqualTo(
            TrackMetadata(title = "Artist - Song (Official Video)", artistName = "ArtistVEVO"),
        )
    }

    @Test
    fun `the Topic suffix of auto-generated channels is removed`() {
        val metadata = resolveTrackMetadata(title = "Song", track = null, artist = null, channel = "Artist - Topic")

        assertThat(metadata?.artistName).isEqualTo("Artist")
    }

    @Test
    fun `blank fields count as missing and values are trimmed`() {
        val metadata = resolveTrackMetadata(title = "  Song  ", track = " ", artist = "", channel = "  Band ")

        assertThat(metadata).isEqualTo(TrackMetadata(title = "Song", artistName = "Band"))
    }

    @Test
    fun `no channel means no artist`() {
        val metadata = resolveTrackMetadata(title = "Song", track = null, artist = null, channel = null)

        assertThat(metadata).isEqualTo(TrackMetadata(title = "Song", artistName = null))
    }

    @Test
    fun `no title at all gives nothing`() {
        assertThat(resolveTrackMetadata(title = " ", track = null, artist = "Artist", channel = null)).isNull()
    }
}
