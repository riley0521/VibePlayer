package com.rfcoding.vibeplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rfcoding.vibeplayer.feature.library.presentation.LibraryGraph
import com.rfcoding.vibeplayer.feature.library.presentation.libraryGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.PermissionGraph
import com.rfcoding.vibeplayer.feature.permission.presentation.permissionGraph

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
        libraryGraph(navController)
    }
}
