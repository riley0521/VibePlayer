package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ForcedTagCommandsTest {

    @Test
    fun `title and artist are replaced whole by literals`() {
        assertThat(forcedTagCommands(title = "Song: 100%", artistName = "AC/DC")).containsExactly(
            "--parse-metadata", "title:%(meta_title)s",
            "--replace-in-metadata", "meta_title", "(?s)^.*$", "Song: 100%",
            "--parse-metadata", "title:%(meta_artist)s",
            "--replace-in-metadata", "meta_artist", "(?s)^.*$", "AC/DC",
        )
    }

    @Test
    fun `a missing artist is cleared instead of left to yt-dlp`() {
        assertThat(forcedTagCommands(title = "Song", artistName = null).takeLast(2))
            .containsExactly("--parse-metadata", ":(?P<meta_artist>)")
    }

    @Test
    fun `backslashes are escaped for the regex replacement`() {
        assertThat(regexReplacementLiteral("""A\1 \g<0>""")).isEqualTo("""A\\1 \\g<0>""")
    }
}
