package com.rfcoding.vibeplayer.feature.library.presentation.playlistname

/** [MaxPlaylistNameLength] only drives the counter here; trimming and validation land with the ViewModel. */
const val MaxPlaylistNameLength = 40

/** The same Figma sheet names a new playlist and renames an existing one. */
enum class PlaylistNameMode {
    Create,
    Rename,
}

data class PlaylistNameState(
    val mode: PlaylistNameMode = PlaylistNameMode.Create,
    val name: String = "",
) {
    /** The spec disables the confirm button while the trimmed name is empty. */
    val canConfirm: Boolean get() = name.isNotBlank()
}
