package com.rfcoding.vibeplayer.feature.downloader.domain

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class YoutubeLinkTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ&list=PL123&index=2",
            "http://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://music.youtube.com/watch?v=dQw4w9WgXcQ&si=abc",
            "https://www.youtube.com/playlist?list=PLx0sYbCqOb8TBPRdmBHs5Iftvv9TPboYG",
            "https://music.youtube.com/playlist?list=OLAK5uy_abc",
            "https://youtu.be/dQw4w9WgXcQ?si=xyz",
            "https://www.youtube.com/shorts/abc123",
            "https://www.youtube.com/live/abc123",
            "youtube.com/watch?v=dQw4w9WgXcQ",
            "  youtu.be/dQw4w9WgXcQ  ",
        ],
    )
    fun `video and playlist links are accepted`(url: String) {
        assertThat(isYoutubeLink(url)).isTrue()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "   ",
            "not a link",
            "https://www.youtube.com/",
            "https://www.youtube.com/watch",
            "https://www.youtube.com/watch?v=",
            "https://www.youtube.com/playlist",
            "https://www.youtube.com/shorts/",
            "https://youtu.be/",
            "https://www.youtube.com/@SomeChannel",
            "https://vimeo.com/123456",
            "https://notyoutube.com/watch?v=abc",
            "ftp://youtube.com/watch?v=abc",
            "https://youtube.com/watch?v=abc def",
        ],
    )
    fun `anything else is rejected`(url: String) {
        assertThat(isYoutubeLink(url)).isFalse()
    }
}
