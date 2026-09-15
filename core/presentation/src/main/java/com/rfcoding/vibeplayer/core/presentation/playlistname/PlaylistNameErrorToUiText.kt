package com.rfcoding.vibeplayer.core.presentation.playlistname

import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistNameError
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistNameValidator
import com.rfcoding.vibeplayer.core.presentation.R
import com.rfcoding.vibeplayer.core.presentation.UiText

fun PlaylistNameError.toUiText(): UiText = when (this) {
    PlaylistNameError.BLANK -> UiText.StringResource(R.string.error_playlist_name_blank)
    PlaylistNameError.TOO_LONG -> UiText.StringResource(
        R.string.error_playlist_name_too_long,
        arrayOf(PlaylistNameValidator.MAX_LENGTH),
    )
}
