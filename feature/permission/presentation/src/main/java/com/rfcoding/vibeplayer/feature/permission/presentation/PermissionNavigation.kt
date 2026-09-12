package com.rfcoding.vibeplayer.feature.permission.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

/** Wraps the feature's one screen so `:app` can pop the whole graph once access is granted. */
@Serializable
data object PermissionGraph

@Serializable
data object PermissionRoute

fun NavGraphBuilder.permissionGraph(onPermissionGranted: () -> Unit) {
    navigation<PermissionGraph>(startDestination = PermissionRoute) {
        composable<PermissionRoute> {
            PermissionRoot(onPermissionGranted = onPermissionGranted)
        }
    }
}
