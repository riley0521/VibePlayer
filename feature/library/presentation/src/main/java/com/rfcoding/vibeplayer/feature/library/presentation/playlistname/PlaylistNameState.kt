package com.rfcoding.vibeplayer.feature.library.presentation.playlistname

/** The same Figma sheet names a new playlist and renames an existing one. */
sealed interface PlaylistNameMode {
    data object Create : PlaylistNameMode
    data class Rename(val playlistId: Long, val currentName: String) : PlaylistNameMode
}

data class PlaylistNameState(
    val mode: PlaylistNameMode = PlaylistNameMode.Create,
    val name: String = "",
    val isSaving: Boolean = false,
) {
    /** The spec disables the confirm button while the trimmed name is empty. */
    val canConfirm: Boolean get() = name.isNotBlank() && !isSaving
}
