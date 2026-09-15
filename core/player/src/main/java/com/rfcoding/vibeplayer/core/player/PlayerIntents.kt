package com.rfcoding.vibeplayer.core.player

/** Intents the playback layer sends to the app, which `:core:player` can't reference directly. */
object PlayerIntents {
    /** Sent when the media notification is tapped: the app should show the Player screen. */
    const val ACTION_OPEN_PLAYER = "com.rfcoding.vibeplayer.action.OPEN_PLAYER"
}
