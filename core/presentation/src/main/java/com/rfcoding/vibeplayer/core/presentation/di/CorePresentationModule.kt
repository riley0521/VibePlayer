package com.rfcoding.vibeplayer.core.presentation.di

import com.rfcoding.vibeplayer.core.presentation.ScopedStoreRegistryViewModel
import com.rfcoding.vibeplayer.core.presentation.playlistname.PlaylistNameViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val corePresentationModule = module {
    viewModelOf(::ScopedStoreRegistryViewModel)
    // Shared by the library and the player. Its PlaylistNameMode comes from parametersOf in PlaylistNameSheetRoot.
    viewModelOf(::PlaylistNameViewModel)
}
