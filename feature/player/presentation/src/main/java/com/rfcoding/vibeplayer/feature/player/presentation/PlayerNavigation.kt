package com.rfcoding.vibeplayer.feature.player.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object PlayerGraph

@Serializable
data object PlayerRoute

fun NavGraphBuilder.playerGraph(onNavigateBack: () -> Unit) {
    navigation<PlayerGraph>(startDestination = PlayerRoute) {
        composable<PlayerRoute> {
            PlayerRoot(onNavigateBack = onNavigateBack)
        }
    }
}
