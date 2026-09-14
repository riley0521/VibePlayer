package com.rfcoding.vibeplayer.core.player.di

import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.player.PlaceholderMusicPlayer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val corePlayerModule = module {
    singleOf(::PlaceholderMusicPlayer) { bind<MusicPlayer>() }
}
