package com.rfcoding.vibeplayer.core.domain.util

/** The app never touches the network, so local failures are the only data errors. */
sealed interface DataError : Error {
    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN,
    }
}
