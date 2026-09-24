package com.rfcoding.vibeplayer.feature.downloader.data.library

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.rfcoding.vibeplayer.feature.downloader.domain.mp3FileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume

private const val MP3_MIME_TYPE = "audio/mpeg"
private const val MUSIC_RELATIVE_PATH = "Music/"

/** A file now in `Music/`, with the same content URI the library scan builds for it. */
data class MusicFolderFile(
    val mediaId: Long,
    val uri: Uri,
)

/**
 * Copies a finished download into `Music/` as `[title].mp3` (see [mp3FileName] for clashes).
 * API 29+ goes through MediaStore and needs no permission; API 28 writes the file directly, which
 * needs `WRITE_EXTERNAL_STORAGE`, and then has the media scanner index it.
 */
class MusicFolderWriter(
    context: Context,
) {
    private val appContext = context.applicationContext

    suspend fun write(
        source: File,
        title: String,
        artistName: String?,
        fallbackName: String,
    ): Result<MusicFolderFile, DownloadError> = withContext(Dispatchers.IO) {
        try {
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val takenNames = musicFolderNames()
                val name = mp3FileName(title, artistName, fallbackName) { it.lowercase() in takenNames }
                writeToMediaStore(source, name)
            } else {
                @Suppress("DEPRECATION") // The only way to reach the shared Music folder before scoped storage.
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                val name = mp3FileName(title, artistName, fallbackName) { File(directory, it).exists() }
                writeToPublicDirectory(source, directory, name)
            }
            Result.Success(file)
        } catch (e: IOException) {
            Result.Error(e.toDownloadError())
        } catch (_: SecurityException) {
            Result.Error(DownloadError.STORAGE_PERMISSION_DENIED)
        }
    }

    /** Lower-cased names of the audio files directly in `Music/`; shared storage ignores case. */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun musicFolderNames(): Set<String> {
        val cursor = appContext.contentResolver.query(
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            arrayOf(MediaStore.Audio.Media.DISPLAY_NAME),
            "${MediaStore.Audio.Media.RELATIVE_PATH} = ?",
            arrayOf(MUSIC_RELATIVE_PATH),
            null,
        ) ?: return emptySet()
        return cursor.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            buildSet {
                while (it.moveToNext()) {
                    it.getString(nameColumn)?.let { name -> add(name.lowercase()) }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeToMediaStore(source: File, displayName: String): MusicFolderFile {
        val resolver = appContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, MP3_MIME_TYPE)
            put(MediaStore.Audio.Media.RELATIVE_PATH, MUSIC_RELATIVE_PATH)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val insertedUri = resolver.insert(collection, values) ?: throw IOException("MediaStore refused $displayName")
        try {
            resolver.openOutputStream(insertedUri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: throw IOException("No output stream for $insertedUri")
            resolver.update(
                insertedUri,
                ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
                null,
                null,
            )
        } catch (e: IOException) {
            // A half-written pending row would otherwise linger as a broken file.
            resolver.delete(insertedUri, null, null)
            throw e
        }
        val mediaId = ContentUris.parseId(insertedUri)
        return MusicFolderFile(
            mediaId = mediaId,
            uri = ContentUris.withAppendedId(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), mediaId),
        )
    }

    private suspend fun writeToPublicDirectory(source: File, directory: File, fileName: String): MusicFolderFile {
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Can't create $directory")
        val target = File(directory, fileName)
        try {
            source.copyTo(target)
        } catch (e: IOException) {
            target.delete()
            throw e
        }
        val scannedUri = scanFile(target)
        if (scannedUri == null) {
            // Unindexed, the scan would never see it either.
            target.delete()
            throw IOException("The media scanner couldn't index $target")
        }
        val mediaId = ContentUris.parseId(scannedUri)
        return MusicFolderFile(
            mediaId = mediaId,
            uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId),
        )
    }

    private suspend fun scanFile(file: File): Uri? = suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(appContext, arrayOf(file.absolutePath), arrayOf(MP3_MIME_TYPE)) { _, uri ->
            continuation.resume(uri)
        }
    }

    private fun IOException.toDownloadError(): DownloadError {
        val message = message.orEmpty()
        return when {
            "ENOSPC" in message || "No space" in message -> DownloadError.DISK_FULL
            "EACCES" in message || "Permission denied" in message -> DownloadError.STORAGE_PERMISSION_DENIED
            else -> DownloadError.UNKNOWN
        }
    }
}
