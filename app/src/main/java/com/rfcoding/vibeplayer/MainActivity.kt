package com.rfcoding.vibeplayer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.player.PlayerIntents
import com.rfcoding.vibeplayer.feature.library.presentation.LibraryGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.PermissionGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.hasMusicPermission
import com.rfcoding.vibeplayer.navigation.NavigationRoot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MainActivity : ComponentActivity() {

    /** Notification taps waiting for the NavHost; conflated because opening the Player twice is once. */
    private val openPlayerRequests = Channel<Unit>(Channel.CONFLATED)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // A restored activity already has its back stack, so only a fresh launch acts on the intent.
        if (savedInstanceState == null) handleIntent(intent)
        // The app is always dark, so the system bar icons stay light.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            VibePlayerTheme {
                // Remembered so granting the permission navigates instead of rebuilding the graph.
                val startDestination = remember {
                    if (hasMusicPermission()) LibraryGraph else PermissionGraph
                }
                NavigationRoot(
                    startDestination = startDestination,
                    openPlayerRequests = remember { openPlayerRequests.receiveAsFlow() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == PlayerIntents.ACTION_OPEN_PLAYER) {
            openPlayerRequests.trySend(Unit)
        }
    }
}
