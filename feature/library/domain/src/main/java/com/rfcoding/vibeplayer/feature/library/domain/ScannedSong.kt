package com.rfcoding.vibeplayer.feature.library.domain

/** A music file that passed the scan filters, before it gets an id and is stored. */
data class ScannedSong(
    val title: String,
    val artistName: String?,
    val fileUri: String,
    val imageUri: String?,
    val durationMillis: Long,
)
