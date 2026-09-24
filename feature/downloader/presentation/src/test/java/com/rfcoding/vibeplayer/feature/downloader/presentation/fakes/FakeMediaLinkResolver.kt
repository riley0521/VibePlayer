package com.rfcoding.vibeplayer.feature.downloader.presentation.fakes

import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.rfcoding.vibeplayer.feature.downloader.domain.MediaLinkResolver
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import kotlinx.coroutines.CompletableDeferred

class FakeMediaLinkResolver : MediaLinkResolver {

    /** Returned by [resolve] unless [gate] is set. */
    var result: Result<List<RemoteTrack>, DownloadError> = Result.Success(emptyList())

    /** Set to hold [resolve] until it's completed, to observe the loading state. */
    var gate: CompletableDeferred<Result<List<RemoteTrack>, DownloadError>>? = null

    val resolvedUrls = mutableListOf<String>()

    override suspend fun resolve(url: String): Result<List<RemoteTrack>, DownloadError> {
        resolvedUrls += url
        return gate?.await() ?: result
    }
}
