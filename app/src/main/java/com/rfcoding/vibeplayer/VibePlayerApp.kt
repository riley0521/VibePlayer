package com.rfcoding.vibeplayer

import android.app.Application
import com.rfcoding.vibeplayer.core.data.di.coreDataModule
import com.rfcoding.vibeplayer.core.database.di.databaseModule
import com.rfcoding.vibeplayer.core.player.di.corePlayerModule
import com.rfcoding.vibeplayer.core.presentation.di.corePresentationModule
import com.rfcoding.vibeplayer.di.appModule
import com.rfcoding.vibeplayer.feature.library.data.di.libraryDataModule
import com.rfcoding.vibeplayer.feature.library.presentation.di.libraryPresentationModule
import com.rfcoding.vibeplayer.feature.permission.presentation.di.permissionPresentationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VibePlayerApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VibePlayerApp)
            modules(
                appModule,
                databaseModule,
                coreDataModule,
                corePlayerModule,
                corePresentationModule,
                permissionPresentationModule,
                libraryDataModule,
                libraryPresentationModule,
            )
        }
    }
}
