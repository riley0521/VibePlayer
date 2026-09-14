package com.rfcoding.vibeplayer.feature.library.presentation.di

import com.rfcoding.vibeplayer.feature.library.presentation.library.LibraryViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.playlist.PlaylistViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.scan.ScanMusicViewModel
import com.rfcoding.vibeplayer.feature.library.presentation.songs.SongsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val libraryPresentationModule = module {
    viewModelOf(::LibraryViewModel)
    viewModelOf(::SongsViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::ScanMusicViewModel)
}
