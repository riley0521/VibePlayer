package com.rfcoding.vibeplayer.feature.library.presentation.fakes

import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.song.Song

class FakeMusicPlayer : MusicPlayer {
    val playedQueues = mutableListOf<List<Song>>()

    override suspend fun play(queue: List<Song>) {
        playedQueues += queue
    }
}
