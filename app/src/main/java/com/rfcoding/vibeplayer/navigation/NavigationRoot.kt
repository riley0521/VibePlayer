package com.rfcoding.vibeplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rfcoding.vibeplayer.feature.library.presentation.library.LibraryScreen
import com.rfcoding.vibeplayer.feature.library.presentation.library.LibraryState
import com.rfcoding.vibeplayer.feature.permission.presentation.PermissionGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.permissionGraph
import kotlinx.serialization.Serializable

/**
 * TODO: replace with the library feature's own graph once `:feature:library:presentation` has a
 *  ViewModel. Until then this is a placeholder destination so the permission flow has somewhere
 *  to land, and it lives here rather than in the feature module to avoid committing to a nav API
 *  that feature hasn't been built against yet.
 */
@Serializable
data object LibraryGraph

@Composable
fun NavigationRoot(
    startDestination: Any,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        permissionGraph(
            onPermissionGranted = {
                navController.navigate(LibraryGraph) {
                    // The permission screen must never be reachable again once access is granted.
                    popUpTo<PermissionGraph> { inclusive = true }
                }
            },
        )
        composable<LibraryGraph> {
            // TODO: LibraryRoot. The screen is still stateless, so it shows its default Scanning state.
            LibraryScreen(state = LibraryState(), onAction = {})
        }
    }
}
