package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.rfcoding.vibeplayer.feature.downloader.domain.MediaLinkResolver
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.UUID

/** Lists a link's songs with `yt-dlp -J --flat-playlist`, which reads the playlist page only. */
class YoutubeDlLinkResolver(
    private val engine: YoutubeDlEngine,
) : MediaLinkResolver {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun resolve(url: String): Result<List<RemoteTrack>, DownloadError> {
        val normalizedUrl = url.trim().let { if ("://" in it) it else "https://$it" }
        val output = try {
            engine.execute(infoRequest(normalizedUrl), processId = "resolve-${UUID.randomUUID()}").out
        } catch (e: YoutubeDLException) {
            return Result.Error(e.toDownloadError())
        } catch (_: YoutubeDL.CanceledException) {
            return Result.Error(DownloadError.UNKNOWN)
        }

        val tracks = withContext(Dispatchers.Default) {
            try {
                json.decodeFromString<YtDlpInfoDto>(output).toRemoteTracks()
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        return when {
            tracks == null -> Result.Error(DownloadError.EXTRACTOR_FAILED)
            tracks.isEmpty() -> Result.Error(DownloadError.UNAVAILABLE)
            else -> Result.Success(tracks)
        }
    }

    private fun YoutubeDLException.toDownloadError(): DownloadError {
        val error = classifyYtDlpError(message)
        if (error == DownloadError.EXTRACTOR_FAILED) engine.markOutdated()
        return error
    }
}
