package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class YtDlpErrorsTest {

    @ParameterizedTest
    @MethodSource("errors")
    fun `stderr maps to the error the user sees`(stderr: String?, expected: DownloadError) {
        assertThat(classifyYtDlpError(stderr)).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun errors() = listOf(
            Arguments.of(
                "ERROR: [youtube] abc: Unable to download webpage: <urlopen error [Errno 7] No address associated with hostname>",
                DownloadError.NO_INTERNET,
            ),
            Arguments.of("ERROR: Unsupported URL: https://example.com", DownloadError.INVALID_LINK),
            Arguments.of("ERROR: [youtube] abc: Video unavailable. This video is private", DownloadError.UNAVAILABLE),
            Arguments.of("ERROR: [youtube] abc: Private video. Sign in if you've been granted access", DownloadError.UNAVAILABLE),
            Arguments.of(
                "ERROR: [youtube] abc: Sign in to confirm you’re not a bot. Use --cookies-from-browser",
                DownloadError.EXTRACTOR_FAILED,
            ),
            Arguments.of("ERROR: Postprocessing: [Errno 28] No space left on device", DownloadError.DISK_FULL),
            Arguments.of("ERROR: [youtube] abc: Requested format is not available", DownloadError.EXTRACTOR_FAILED),
            Arguments.of("Something odd", DownloadError.UNKNOWN),
            Arguments.of(null, DownloadError.UNKNOWN),
        )
    }
}
