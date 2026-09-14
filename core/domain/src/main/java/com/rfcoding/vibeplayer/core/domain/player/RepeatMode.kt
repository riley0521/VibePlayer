package com.rfcoding.vibeplayer.core.domain.player

enum class RepeatMode {
    Off,

    /** The queue loops: the last song is followed by the first. */
    All,

    /** The current song loops when it ends; next and previous still move between songs. */
    One,
}
