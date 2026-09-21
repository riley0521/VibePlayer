package com.rfcoding.vibeplayer.core.data.image

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.rfcoding.vibeplayer.core.domain.image.ImageGallery
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.domain.util.EmptyResult
import com.rfcoding.vibeplayer.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private const val ALBUM = "VibePlayer"
private const val PNG_MIME_TYPE = "image/png"

/**
 * Saves into `Pictures/VibePlayer`. API 29+ goes through MediaStore and needs no permission; API 28
 * writes the file directly, which needs `WRITE_EXTERNAL_STORAGE`, and then asks the scanner to index it.
 */
class MediaStoreImageGallery(
    private val context: Context,
) : ImageGallery {

    override suspend fun savePng(bytes: ByteArray, displayName: String): EmptyResult<DataError.Local> {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveToMediaStore(bytes, displayName)
                } else {
                    saveToPublicDirectory(bytes, displayName)
                }
                Result.Success(Unit)
            } catch (e: IOException) {
                Result.Error(e.toDataError())
            } catch (_: SecurityException) {
                Result.Error(DataError.Local.UNKNOWN)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(bytes: ByteArray, displayName: String) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, PNG_MIME_TYPE)
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: throw IOException("MediaStore refused $displayName")
        try {
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IOException("No output stream for $uri")
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        } catch (e: IOException) {
            // A half-written pending row would otherwise linger as a broken gallery entry.
            resolver.delete(uri, null, null)
            throw e
        }
    }

    @Suppress("DEPRECATION") // The only way to reach the shared Pictures folder before scoped storage.
    private fun saveToPublicDirectory(bytes: ByteArray, displayName: String) {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM)
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Can't create $directory")
        val file = File(directory, displayName)
        file.writeBytes(bytes)
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(PNG_MIME_TYPE), null)
    }

    private fun IOException.toDataError(): DataError.Local {
        val message = message.orEmpty()
        return if ("ENOSPC" in message || "No space" in message) DataError.Local.DISK_FULL else DataError.Local.UNKNOWN
    }
}
