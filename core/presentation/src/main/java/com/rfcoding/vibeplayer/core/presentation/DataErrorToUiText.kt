package com.rfcoding.vibeplayer.core.presentation

import com.rfcoding.vibeplayer.core.domain.util.DataError

fun DataError.Local.toUiText(): UiText {
    val id = when (this) {
        DataError.Local.DISK_FULL -> R.string.error_disk_full
        DataError.Local.NOT_FOUND -> R.string.error_not_found
        DataError.Local.UNKNOWN -> R.string.error_unknown
    }
    return UiText.StringResource(id)
}
