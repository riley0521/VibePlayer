package com.rfcoding.vibeplayer.core.player.di

import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.player.MusicManager
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val corePlayerModule = module {
    // One manager, so every screen mirrors the same session connection.
    singleOf(::MusicManager) { bind<MusicPlayer>() }
}
