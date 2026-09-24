package com.rfcoding.vibeplayer.feature.downloader.presentation.di

import com.rfcoding.vibeplayer.feature.downloader.presentation.downloader.DownloaderViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val downloaderPresentationModule = module {
    viewModelOf(::DownloaderViewModel)
}
