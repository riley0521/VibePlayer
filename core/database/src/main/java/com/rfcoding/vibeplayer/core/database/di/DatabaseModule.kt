package com.rfcoding.vibeplayer.core.database.di

import androidx.room.Room
import com.rfcoding.vibeplayer.core.database.VibePlayerDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            VibePlayerDatabase::class.java,
            VibePlayerDatabase.NAME,
        ).build()
    }
    single { get<VibePlayerDatabase>().songDao }
    single { get<VibePlayerDatabase>().playlistDao }
}
