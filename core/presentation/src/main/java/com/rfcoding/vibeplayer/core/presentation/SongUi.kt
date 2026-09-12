package com.rfcoding.vibeplayer.core.presentation

import androidx.compose.runtime.Immutable

/**
 * A song as the screens need it. The `Song` -> `SongUi` mapper lands with the data layer.
 */
@Immutable
data class SongUi(
    val id: String,
    val title: String,
    val artistName: String?,
    val imageUri: String?,
    val durationMillis: Long,
) {
    val durationText: String
        get() = durationMillis.toDurationText()
}
