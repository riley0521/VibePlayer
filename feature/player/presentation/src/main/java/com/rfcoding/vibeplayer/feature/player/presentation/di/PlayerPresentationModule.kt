package com.rfcoding.vibeplayer.feature.player.presentation.di

import com.rfcoding.vibeplayer.feature.player.presentation.PlayerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val playerPresentationModule = module {
    viewModelOf(::PlayerViewModel)
}
