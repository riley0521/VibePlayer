package com.rfcoding.vibeplayer.core.data.song

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import java.io.IOException

/** The tags of one audio file that the library stores. */
data class MusicFileTags(
    val title: String,
    val artistName: String?,
    val durationMillis: Long,
)

/** A readable audio file, with its embedded cover art copied to [imageUri]. */
data class MusicFile(
    val tags: MusicFileTags,
    val fileUri: String,
    val imageUri: String?,
)

/**
 * Reads one audio file's tags with [MediaMetadataRetriever] and copies its embedded cover art to
 * `filesDir/artwork/<MediaStore id>`. Shared by the Music folder scan and the downloader, so a
 * downloaded song gets the same row a later rescan would produce.
 */
class MusicFileReader(
    context: Context,
) {
    private val appContext = context.applicationContext

    val artworkDir = File(appContext.filesDir, ARTWORK_DIR_NAME)

    /**
     * Returns null when the file can't be read, has no title or duration, or [accept] turns its
     * tags down. [accept] runs before the cover art is read, so rejected files cost no picture copy.
     */
    suspend fun read(
        uri: Uri,
        mediaId: Long,
        accept: suspend (MusicFileTags) -> Boolean = { true },
    ): MusicFile? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)
            currentCoroutineContext().ensureActive()

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: return null

            val artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

            val durationMillis = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: return null

            val tags = MusicFileTags(title, artistName, durationMillis)
            if (!accept(tags)) return null

            val picture = retriever.embeddedPicture
            currentCoroutineContext().ensureActive()

            MusicFile(
                tags = tags,
                fileUri = uri.toString(),
                imageUri = picture?.let { saveArtwork(mediaId, it) },
            )
        } catch (e: CancellationException) {
            // CancellationException is a RuntimeException, so it must escape the catch below.
            throw e
        } catch (_: RuntimeException) {
            // A corrupt or unsupported file.
            null
        } finally {
            retriever.release()
        }
    }

    /** Returns the artwork's file URI, or null when it couldn't be written. */
    private fun saveArtwork(mediaId: Long, bytes: ByteArray): String? {
        val artworkFile = File(artworkDir, artworkFileName(mediaId))
        return try {
            artworkDir.mkdirs()
            // Same length is a cheap "unchanged" check that spares a rewrite on every rescan.
            if (!artworkFile.exists() || artworkFile.length() != bytes.size.toLong()) {
                artworkFile.writeBytes(bytes)
            }
            Uri.fromFile(artworkFile).toString()
        } catch (_: IOException) {
            null
        }
    }

    companion object {
        private const val ARTWORK_DIR_NAME = "artwork"

        fun artworkFileName(mediaId: Long): String = mediaId.toString()
    }
}
