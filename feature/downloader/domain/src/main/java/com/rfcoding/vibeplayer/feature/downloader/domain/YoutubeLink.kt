package com.rfcoding.vibeplayer.feature.downloader.domain

import java.net.URI
import java.net.URISyntaxException

private val YoutubeHosts = setOf("youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com")
private const val ShortLinkHost = "youtu.be"
private val VideoPathPrefixes = listOf("/shorts/", "/live/")

/**
 * Whether [input] is a YouTube (or YouTube Music) link to a video or a playlist. A missing scheme
 * is fine, since links are often copied without one.
 */
fun isYoutubeLink(input: String): Boolean {
    val text = input.trim()
    if (text.isEmpty() || text.any { it.isWhitespace() }) return false
    val uri = try {
        URI(if ("://" in text) text else "https://$text")
    } catch (_: URISyntaxException) {
        return false
    }
    if (uri.scheme?.lowercase() !in setOf("http", "https")) return false
    val host = uri.host?.lowercase() ?: return false
    val path = uri.path.orEmpty()
    val query = uri.rawQuery.orEmpty().split('&').associate {
        it.substringBefore('=') to it.substringAfter('=', missingDelimiterValue = "")
    }

    return when (host) {
        ShortLinkHost -> path.trim('/').isNotEmpty()
        in YoutubeHosts -> when {
            path == "/watch" -> !query["v"].isNullOrEmpty()
            path == "/playlist" -> !query["list"].isNullOrEmpty()
            else -> VideoPathPrefixes.any { path.startsWith(it) && path.length > it.length }
        }
        else -> false
    }
}
