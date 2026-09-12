package com.rfcoding.vibeplayer.feature.library.presentation.scan

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeFilterChip
import com.rfcoding.vibeplayer.core.designsystem.components.VibeInnerTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeRadar
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.domain.MinDuration
import com.rfcoding.vibeplayer.feature.library.domain.MinSize
import com.rfcoding.vibeplayer.feature.library.presentation.R
import org.koin.androidx.compose.koinViewModel

// VibeInnerTopBar already pads itself to Figma's mobile 10dp; tablets add the missing 8dp.
private val TabletTopBarPadding = 8.dp
private val ContentMaxWidth = 400.dp

@Composable
fun ScanMusicRoot(
    onNavigateBack: () -> Unit,
    viewModel: ScanMusicViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ScanMusicEvent.NavigateBack -> onNavigateBack()
            is ScanMusicEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    ScanMusicScreen(
        state = state,
        onAction = { action ->
            when (action) {
                ScanMusicAction.OnBackClick -> onNavigateBack()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun ScanMusicScreen(
    state: ScanMusicState,
    onAction: (ScanMusicAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMobile = currentDeviceConfiguration().isMobile
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VibeInnerTopBar(
                title = stringResource(R.string.scan_music),
                onBackClick = { onAction(ScanMusicAction.OnBackClick) },
                modifier = if (isMobile) Modifier else Modifier.padding(horizontal = TabletTopBarPadding),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VibeRadar(isSweeping = state.isScanning)
            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FilterGroup(title = stringResource(R.string.ignore_duration_less_than)) {
                    MinDuration.entries.forEach { minDuration ->
                        VibeFilterChip(
                            label = stringResource(minDuration.labelRes),
                            selected = minDuration == state.minDuration,
                            onClick = { onAction(ScanMusicAction.OnMinDurationSelect(minDuration)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                FilterGroup(title = stringResource(R.string.ignore_size_less_than)) {
                    MinSize.entries.forEach { minSize ->
                        VibeFilterChip(
                            label = stringResource(minSize.labelRes),
                            selected = minSize == state.minSize,
                            onClick = { onAction(ScanMusicAction.OnMinSizeSelect(minSize)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            VibeButton(
                text = stringResource(if (state.isScanning) R.string.scanning else R.string.scan),
                onClick = { onAction(ScanMusicAction.OnScanClick) },
                enabled = !state.isScanning,
                isLoading = state.isScanning,
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    modifier: Modifier = Modifier,
    chips: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLargeMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = chips,
        )
    }
}

private val MinDuration.labelRes: Int
    get() = when (this) {
        MinDuration.ThirtySeconds -> R.string.duration_30s
        MinDuration.SixtySeconds -> R.string.duration_60s
    }

private val MinSize.labelRes: Int
    get() = when (this) {
        MinSize.OneHundredKb -> R.string.size_100kb
        MinSize.FiveHundredKb -> R.string.size_500kb
    }

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun ScanMusicScreenPreview() {
    VibePlayerTheme {
        ScanMusicScreen(state = ScanMusicState(), onAction = {})
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun ScanMusicScreenLoadingPreview() {
    VibePlayerTheme {
        ScanMusicScreen(state = ScanMusicState(isScanning = true), onAction = {})
    }
}
