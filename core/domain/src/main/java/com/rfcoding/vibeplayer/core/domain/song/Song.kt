package com.rfcoding.vibeplayer.core.domain.song

/**
 * A music file from the device's `Music/` folder. [id] is a random UUID; a rescan recognises the
 * same song by its ([title], [artistName]) pair and keeps the id, [isFavorite] and playlist links.
 */
data class Song(
    val id: String,
    val title: String,
    val artistName: String?,
    val fileUri: String,
    val imageUri: String?,
    val durationMillis: Long,
    val isFavorite: Boolean,
    /** Epoch milliseconds. */
    val createdAt: Long,
)
