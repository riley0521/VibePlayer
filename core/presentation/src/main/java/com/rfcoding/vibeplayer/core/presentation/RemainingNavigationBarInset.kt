package com.rfcoding.vibeplayer.core.presentation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * The navigation-bar inset that an ancestor hasn't consumed yet. Under the mobile bottom bar,
 * which already pads for the system navigation bar, it is zero, so a list's `contentPadding` doesn't
 * leave a second gap. Apply [modifier] to the list whose padding reads [bottom].
 */
@Stable
class RemainingNavigationBarInset {
    private var consumed by mutableStateOf(WindowInsets(0))

    @OptIn(ExperimentalLayoutApi::class)
    val modifier: Modifier = Modifier.onConsumedWindowInsetsChanged { consumed = it }

    @Composable
    fun bottom(): Dp = WindowInsets.navigationBars.exclude(consumed).asPaddingValues().calculateBottomPadding()
}

@Composable
fun rememberRemainingNavigationBarInset(): RemainingNavigationBarInset = remember { RemainingNavigationBarInset() }
