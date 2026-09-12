package com.rfcoding.vibeplayer.feature.permission.presentation

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeLogo
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import org.koin.androidx.compose.koinViewModel
import com.rfcoding.vibeplayer.core.designsystem.R as DesignSystemR

@Composable
fun PermissionRoot(
    onPermissionGranted: () -> Unit,
    viewModel: PermissionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onPermissionResult(
            isGranted = isGranted,
            // Reading this after the result is what separates a first denial from a permanent one.
            canAskAgain = activity?.canAskForMusicPermissionAgain() == true,
        )
    }

    // Granting in system Settings sends nothing back to the launcher, so re-read it on every resume.
    LifecycleResumeEffect(Unit) {
        viewModel.onPermissionChecked(context.hasMusicPermission())
        onPauseOrDispose { }
    }

    LaunchedEffect(state.isGranted) {
        if (state.isGranted) onPermissionGranted()
    }

    PermissionScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
            when (action) {
                PermissionAction.OnAllowAccessClick -> requestPermission.launch(MusicPermission.NAME)
                PermissionAction.OnOpenSettingsClick -> context.openAppSettings()
                PermissionAction.OnRationaleDismiss -> Unit
            }
        },
    )
}

@Composable
fun PermissionScreen(
    state: PermissionState,
    onAction: (PermissionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VibeLogo(size = 56.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(DesignSystemR.string.vibe_player),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.permission_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        VibeButton(
            text = stringResource(R.string.allow_access),
            onClick = { onAction(PermissionAction.OnAllowAccessClick) },
        )
    }

    state.rationale?.let { rationale ->
        PermissionRationaleSheet(
            rationale = rationale,
            onAction = onAction,
            onDismiss = { onAction(PermissionAction.OnRationaleDismiss) },
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun PermissionScreenPreview() {
    VibePlayerTheme {
        PermissionScreen(state = PermissionState(), onAction = {})
    }
}
