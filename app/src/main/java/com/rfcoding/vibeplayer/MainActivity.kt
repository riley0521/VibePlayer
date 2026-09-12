package com.rfcoding.vibeplayer

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
import com.rfcoding.vibeplayer.feature.permission.presentation.PermissionGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.hasMusicPermission
import com.rfcoding.vibeplayer.navigation.LibraryGraph
import com.rfcoding.vibeplayer.navigation.NavigationRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
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
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
