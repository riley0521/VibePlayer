package com.rfcoding.vibeplayer.feature.library.presentation.createplaylist

/** [MaxPlaylistNameLength] only drives the counter here; trimming and validation land with the ViewModel. */
const val MaxPlaylistNameLength = 40

data class CreatePlaylistState(
    val name: String = "",
) {
    /** The spec disables Create while the trimmed name is empty. */
    val canCreate: Boolean get() = name.isNotBlank()
}
