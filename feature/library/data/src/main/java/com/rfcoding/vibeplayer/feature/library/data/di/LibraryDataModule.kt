package com.rfcoding.vibeplayer.feature.library.data.di

import com.rfcoding.vibeplayer.feature.library.data.MediaStoreMusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.data.scanner.MediaStoreMusicScanner
import com.rfcoding.vibeplayer.feature.library.domain.MusicLibraryRepository
import com.rfcoding.vibeplayer.feature.library.domain.MusicScanner
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val libraryDataModule = module {
    singleOf(::MediaStoreMusicScanner) { bind<MusicScanner>() }
    single<MusicLibraryRepository> { MediaStoreMusicLibraryRepository(get(), get()) }
}
