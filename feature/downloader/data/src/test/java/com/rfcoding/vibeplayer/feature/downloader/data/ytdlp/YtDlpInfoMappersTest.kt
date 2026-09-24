package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class YtDlpInfoMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `a single YouTube Music video uses its track and artists fields`() {
        val output = """
            {
              "_type": "video",
              "id": "abc123",
              "title": "Artist - Song (Official Audio)",
              "track": "Song",
              "artists": ["Artist", "Guest"],
              "artist": "Artist, Guest",
              "channel": "Artist - Topic",
              "duration": 215.5,
              "webpage_url": "https://www.youtube.com/watch?v=abc123",
              "thumbnail": "https://i.ytimg.com/vi/abc123/maxresdefault.jpg",
              "formats": [{"format_id": "251"}]
            }
        """.trimIndent()

        val tracks = parse(output)

        assertThat(tracks).containsExactly(
            RemoteTrack(
                videoId = "abc123",
                url = "https://www.youtube.com/watch?v=abc123",
                title = "Song",
                artistName = "Artist, Guest",
                thumbnailUrl = "https://i.ytimg.com/vi/abc123/maxresdefault.jpg",
                durationMillis = 215_500,
            ),
        )
    }

    @Test
    fun `a flat playlist maps every entry and drops removed videos`() {
        val output = """
            {
              "_type": "playlist",
              "id": "PL1",
              "title": "My mix",
              "entries": [
                {
                  "_type": "url",
                  "id": "v1",
                  "url": "https://www.youtube.com/watch?v=v1",
                  "title": "First",
                  "channel": "Band - Topic",
                  "duration": 180,
                  "thumbnails": [
                    {"url": "https://i.ytimg.com/vi/v1/small.jpg", "width": 168, "height": 94},
                    {"url": "https://i.ytimg.com/vi/v1/big.jpg", "width": 686, "height": 386},
                    {"url": "https://i.ytimg.com/vi/v1/medium.jpg", "width": 336, "height": 188}
                  ]
                },
                {"_type": "url", "id": "v2", "url": "https://www.youtube.com/watch?v=v2", "title": "[Private video]"},
                null,
                {"_type": "url", "id": "v3", "url": "https://www.youtube.com/watch?v=v3", "title": "Third", "uploader": "Someone"}
              ]
            }
        """.trimIndent()

        val tracks = parse(output)

        assertThat(tracks.map { it.videoId }).containsExactly("v1", "v3")
        assertThat(tracks[0].artistName).isEqualTo("Band")
        assertThat(tracks[0].thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/v1/medium.jpg")
        assertThat(tracks[0].durationMillis).isEqualTo(180_000L)
        assertThat(tracks[1].artistName).isEqualTo("Someone")
        assertThat(tracks[1].thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/v3/mqdefault.jpg")
        assertThat(tracks[1].durationMillis).isEqualTo(null)
    }

    @Test
    fun `an entry without a web url gets a watch link`() {
        val output = """{"_type": "playlist", "entries": [{"id": "v1", "url": "v1", "title": "Song"}]}"""

        assertThat(parse(output).single().url).isEqualTo("https://www.youtube.com/watch?v=v1")
    }

    @Test
    fun `an empty playlist has no tracks`() {
        assertThat(parse("""{"_type": "playlist", "id": "PL1", "entries": []}""")).isEmpty()
    }

    private fun parse(output: String) = json.decodeFromString<YtDlpInfoDto>(output).toRemoteTracks()
}
