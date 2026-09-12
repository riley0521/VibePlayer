package com.rfcoding.vibeplayer.feature.library.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.rfcoding.vibeplayer.feature.library.presentation.library.LibraryRoot
import com.rfcoding.vibeplayer.feature.library.presentation.scan.ScanMusicRoot
import kotlinx.serialization.Serializable

@Serializable
data object LibraryGraph

@Serializable
data object LibraryRoute

@Serializable
data object ScanMusicRoute

/** Library → Scan music stays inside this feature, so the graph navigates on its own. */
fun NavGraphBuilder.libraryGraph(navController: NavController) {
    navigation<LibraryGraph>(startDestination = LibraryRoute) {
        composable<LibraryRoute> {
            LibraryRoot(
                onScanClick = { navController.navigate(ScanMusicRoute) },
            )
        }
        composable<ScanMusicRoute> {
            ScanMusicRoot(
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}
