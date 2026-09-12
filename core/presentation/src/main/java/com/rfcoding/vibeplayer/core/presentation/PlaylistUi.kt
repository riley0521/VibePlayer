package com.rfcoding.vibeplayer.core.presentation

import androidx.compose.runtime.Immutable

/**
 * A user-created playlist. "Favourites" is virtual (built from `isFavorite = true`) and never a row,
 * so it is not modelled here — screens render it from their own state.
 */
@Immutable
data class PlaylistUi(
    val id: Long,
    val name: String,
    val songCount: Int,
    val imageUri: String?,
)
