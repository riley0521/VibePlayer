package com.rfcoding.vibeplayer.di

import com.rfcoding.vibeplayer.VibePlayerApp
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<CoroutineScope> { (androidContext() as VibePlayerApp).applicationScope }
}
