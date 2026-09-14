package com.rfcoding.vibeplayer.core.player

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.rfcoding.vibeplayer.core.domain.song.Song

private const val KEY_FILE_URI = "file_uri"
private const val KEY_IMAGE_URI = "image_uri"
private const val KEY_IS_FAVORITE = "is_favorite"
private const val KEY_CREATED_AT = "created_at"

/**
 * Every [Song] field travels with the item, so the queue can be rebuilt from the session alone, for
 * example when a controller connects to a service that kept playing without the UI.
 */
internal fun Song.toMediaItem(): MediaItem {
    val fileUri = Uri.parse(fileUri)
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(fileUri)
        // Kept here as well so the service can restore the URI if an item arrives without one.
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(fileUri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistName)
                .setArtworkUri(imageUri?.let(Uri::parse))
                .setDurationMs(durationMillis)
                .setExtras(
                    Bundle().apply {
                        putString(KEY_FILE_URI, this@toMediaItem.fileUri)
                        putString(KEY_IMAGE_URI, imageUri)
                        putBoolean(KEY_IS_FAVORITE, isFavorite)
                        putLong(KEY_CREATED_AT, createdAt)
                    },
                )
                .build(),
        )
        .build()
}

internal fun MediaItem.toSong(): Song {
    val extras = mediaMetadata.extras ?: Bundle.EMPTY
    return Song(
        id = mediaId,
        title = mediaMetadata.title?.toString().orEmpty(),
        artistName = mediaMetadata.artist?.toString(),
        fileUri = extras.getString(KEY_FILE_URI) ?: requestMetadata.mediaUri?.toString().orEmpty(),
        imageUri = extras.getString(KEY_IMAGE_URI),
        durationMillis = mediaMetadata.durationMs ?: 0L,
        isFavorite = extras.getBoolean(KEY_IS_FAVORITE, false),
        createdAt = extras.getLong(KEY_CREATED_AT, 0L),
    )
}
