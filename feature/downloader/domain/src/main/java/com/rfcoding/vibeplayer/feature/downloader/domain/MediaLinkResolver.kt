package com.rfcoding.vibeplayer.feature.downloader.domain

import com.rfcoding.vibeplayer.core.domain.util.Result

interface MediaLinkResolver {

    /** The songs behind a video or playlist link, in the playlist's order. */
    suspend fun resolve(url: String): Result<List<RemoteTrack>, DownloadError>
}
