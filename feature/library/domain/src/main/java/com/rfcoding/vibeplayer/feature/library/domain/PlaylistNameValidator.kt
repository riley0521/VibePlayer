package com.rfcoding.vibeplayer.feature.library.domain

import com.rfcoding.vibeplayer.core.domain.util.Error
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.feature.library.domain.PlaylistNameValidator.MAX_LENGTH

enum class PlaylistNameError : Error {
    BLANK,
    TOO_LONG,
}

object PlaylistNameValidator {

    const val MAX_LENGTH = 40

    /** Returns the trimmed name, which is what gets stored; surrounding spaces don't count towards [MAX_LENGTH]. */
    fun validate(name: String): Result<String, PlaylistNameError> {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> Result.Error(PlaylistNameError.BLANK)
            trimmed.length > MAX_LENGTH -> Result.Error(PlaylistNameError.TOO_LONG)
            else -> Result.Success(trimmed)
        }
    }
}
