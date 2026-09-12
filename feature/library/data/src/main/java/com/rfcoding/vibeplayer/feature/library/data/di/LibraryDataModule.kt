package com.rfcoding.vibeplayer.feature.library.data.di

import com.rfcoding.vibeplayer.feature.library.data.MediaStoreMusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.data.scanner.MediaStoreMusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.MusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val libraryDataModule = module {
    single<MusicScanner> { MediaStoreMusicScanner(androidContext()) }
    single<MusicLibraryRepository> { MediaStoreMusicLibraryRepository(get(), get()) }
}
