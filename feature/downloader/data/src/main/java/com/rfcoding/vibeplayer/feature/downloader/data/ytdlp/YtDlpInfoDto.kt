package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The parts of `yt-dlp -J --flat-playlist` output the downloader reads. A video link gives one
 * video; a playlist link gives `_type: playlist` with flat [entries].
 */
@Serializable
internal data class YtDlpInfoDto(
    @SerialName("_type") val type: String? = null,
    val id: String? = null,
    val url: String? = null,
    @SerialName("webpage_url") val webpageUrl: String? = null,
    val title: String? = null,
    val track: String? = null,
    val artist: String? = null,
    val artists: List<String>? = null,
    val channel: String? = null,
    val uploader: String? = null,
    val duration: Double? = null,
    val thumbnail: String? = null,
    val thumbnails: List<YtDlpThumbnailDto>? = null,
    val entries: List<YtDlpInfoDto?>? = null,
)

@Serializable
internal data class YtDlpThumbnailDto(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)
