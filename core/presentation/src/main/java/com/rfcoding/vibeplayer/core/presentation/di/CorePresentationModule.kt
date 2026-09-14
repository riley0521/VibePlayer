package com.rfcoding.vibeplayer.core.presentation.di

import com.rfcoding.vibeplayer.core.presentation.ScopedStoreRegistryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val corePresentationModule = module {
    viewModelOf(::ScopedStoreRegistryViewModel)
}
