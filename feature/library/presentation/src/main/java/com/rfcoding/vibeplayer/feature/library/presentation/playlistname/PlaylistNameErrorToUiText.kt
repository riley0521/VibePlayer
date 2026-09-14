package com.rfcoding.vibeplayer.feature.library.presentation.playlistname

import com.rfcoding.vibeplayer.core.presentation.UiText
import com.rfcoding.vibeplayer.feature.library.domain.PlaylistNameError
import com.rfcoding.vibeplayer.feature.library.domain.PlaylistNameValidator
import com.rfcoding.vibeplayer.feature.library.presentation.R

fun PlaylistNameError.toUiText(): UiText = when (this) {
    PlaylistNameError.BLANK -> UiText.StringResource(R.string.error_playlist_name_blank)
    PlaylistNameError.TOO_LONG -> UiText.StringResource(
        R.string.error_playlist_name_too_long,
        arrayOf(PlaylistNameValidator.MAX_LENGTH),
    )
}
