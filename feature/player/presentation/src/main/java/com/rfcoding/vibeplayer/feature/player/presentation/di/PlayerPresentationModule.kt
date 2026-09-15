package com.rfcoding.vibeplayer.feature.player.presentation.di

import com.rfcoding.vibeplayer.feature.player.presentation.PlayerViewModel
import com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist.AddToPlaylistViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val playerPresentationModule = module {
    viewModelOf(::PlayerViewModel)
    // Its songId comes from parametersOf in AddToPlaylistSheetRoot.
    viewModelOf(::AddToPlaylistViewModel)
}
