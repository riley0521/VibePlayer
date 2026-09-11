package com.rfcoding.vibeplayer

import android.app.Application
import com.rfcoding.vibeplayer.di.appModule
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
            )
        }
    }
}
