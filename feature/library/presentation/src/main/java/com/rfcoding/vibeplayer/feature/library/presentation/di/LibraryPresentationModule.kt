package com.rfcoding.vibeplayer.feature.library.presentation.di

import com.rfcoding.vibeplayer.feature.library.presentation.addsongs.AddSongsViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.library.LibraryViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.playlist.PlaylistViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail.PlaylistDetailViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.scan.ScanMusicViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.songs.SongsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val libraryPresentationModule = module {
    viewModelOf(::LibraryViewModel)
    viewModelOf(::SongsViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::ScanMusicViewModel)
    // Its playlistId comes from parametersOf in AddSongsRoot.
    viewModelOf(::AddSongsViewModel)
    // Its playlistId comes from parametersOf in PlaylistDetailRoot. It is null for Favourites, which
    // viewModelOf can't resolve, so the parameter is read explicitly.
    viewModel { params -> PlaylistDetailViewModel(params.getOrNull(), get(), get(), get()) }
}
