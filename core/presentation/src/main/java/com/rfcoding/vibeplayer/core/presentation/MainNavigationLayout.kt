package com.rfcoding.vibeplayer.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Places the app's top-level [navigation] for a tab screen: at the start on tablet (a rail), or
 * handed to [content] as `bottomBar` for its Scaffold on mobile.
 */
@Composable
fun MainNavigationLayout(
    navigation: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (bottomBar: @Composable () -> Unit) -> Unit,
) {
    val isMobile = currentDeviceConfiguration().isMobile
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (!isMobile) navigation()
        Box(modifier = Modifier.weight(1f)) {
            content(if (isMobile) navigation else NoBottomBar)
        }
    }
}

private val NoBottomBar: @Composable () -> Unit = {}
