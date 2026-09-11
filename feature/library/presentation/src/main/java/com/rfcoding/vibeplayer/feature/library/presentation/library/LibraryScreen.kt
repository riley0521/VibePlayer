package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeMainTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeRadar
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.R

private val MobileTopBarPadding = PaddingValues(start = 16.dp, end = 10.dp)
private val TabletTopBarPadding = PaddingValues(start = 24.dp, end = 18.dp)

@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val deviceConfiguration = currentDeviceConfiguration()
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VibeMainTopBar(
                contentPadding = if (deviceConfiguration.isMobile) MobileTopBarPadding else TabletTopBarPadding,
                actions = {
                    VibeIconButton(
                        icon = VibeIcons.Scan,
                        contentDescription = stringResource(R.string.scan_music),
                        onClick = { onAction(LibraryAction.OnScanClick) },
                    )
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
        when (state.status) {
            LibraryStatus.Scanning -> ScanningContent(modifier = contentModifier)
            LibraryStatus.NoMusicFound -> NoMusicFoundContent(
                onScanAgainClick = { onAction(LibraryAction.OnScanAgainClick) },
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun ScanningContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VibeRadar(isSweeping = true)
        Text(
            text = stringResource(R.string.scanning_device),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NoMusicFoundContent(
    onScanAgainClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.no_music_found),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.no_music_found_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        VibeButton(
            text = stringResource(R.string.scan_again),
            onClick = onScanAgainClick,
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenScanningPreview() {
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(status = LibraryStatus.Scanning),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenNoMusicFoundPreview() {
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(status = LibraryStatus.NoMusicFound),
            onAction = {},
        )
    }
}
