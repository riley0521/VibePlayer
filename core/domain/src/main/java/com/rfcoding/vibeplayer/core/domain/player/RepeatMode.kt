package com.rfcoding.vibeplayer.core.domain.player

enum class RepeatMode {
    Off,

    /** The queue loops: the last song is followed by the first. */
    All,

    /** The current song loops when it ends; next and previous still move between songs. */
    One,
}

/** The mode a tap on the repeat button switches to: Off, All, One, then Off again. */
fun RepeatMode.next(): RepeatMode = when (this) {
    RepeatMode.Off -> RepeatMode.All
    RepeatMode.All -> RepeatMode.One
    RepeatMode.One -> RepeatMode.Off
}
