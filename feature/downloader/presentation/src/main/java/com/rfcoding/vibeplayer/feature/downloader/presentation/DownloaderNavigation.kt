package com.rfcoding.vibeplayer.feature.downloader.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.rfcoding.vibeplayer.feature.downloader.presentation.downloader.DownloaderRoot
import kotlinx.serialization.Serializable

@Serializable
data object DownloaderGraph

@Serializable
data object DownloaderRoute

/**
 * The Downloader tab. Switching tabs and opening the player cross into other features, so `:app`
 * supplies [navigation] (the Library/Downloader switch) and [onOpenPlayer].
 */
fun NavGraphBuilder.downloaderGraph(
    onOpenPlayer: () -> Unit,
    navigation: @Composable () -> Unit,
) {
    navigation<DownloaderGraph>(startDestination = DownloaderRoute) {
        composable<DownloaderRoute> {
            DownloaderRoot(
                onMiniPlayerClick = onOpenPlayer,
                navigation = navigation,
            )
        }
    }
}
