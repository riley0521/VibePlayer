package com.rfcoding.vibeplayer.core.player

import android.util.Log
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.song.Song

/**
 * Stands in for the Media3 `MediaController`-backed player until it exists: it logs the queue it is
 * given so the screens can already hand songs over, but plays nothing.
 */
class PlaceholderMusicPlayer : MusicPlayer {

    override suspend fun play(queue: List<Song>) {
        Log.d(TAG, "play: ${queue.map { it.id }}")
    }

    private companion object {
        const val TAG = "PlaceholderMusicPlayer"
    }
}
