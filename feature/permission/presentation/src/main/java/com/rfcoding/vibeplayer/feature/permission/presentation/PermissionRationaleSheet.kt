package com.rfcoding.vibeplayer.feature.permission.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheet
import com.rfcoding.vibeplayer.feature.permission.presentation.components.SheetPreviewSurface

/** The delete-playlist sheet centers its message in a column this wide; this one matches it. */
private val MessageMaxWidth = 400.dp

/**
 * Explains that music access is not optional, shown once the user denies. Both
 * [PermissionRationale] variants draw the same sheet; only the button changes, because after a
 * second denial asking again is a no-op and the user has to go through system Settings.
 */
@Composable
fun PermissionRationaleSheet(
    rationale: PermissionRationale,
    onAction: (PermissionAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        PermissionRationaleSheetContent(rationale = rationale, onAction = onAction)
    }
}

@Composable
internal fun PermissionRationaleSheetContent(
    rationale: PermissionRationale,
    onAction: (PermissionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = MessageMaxWidth)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.permission_required_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.permission_required_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        val buttonText = when (rationale) {
            PermissionRationale.Retry -> R.string.allow_access
            PermissionRationale.Settings -> R.string.open_settings
        }
        val buttonAction = when (rationale) {
            PermissionRationale.Retry -> PermissionAction.OnAllowAccessClick
            PermissionRationale.Settings -> PermissionAction.OnOpenSettingsClick
        }
        VibeButton(
            text = stringResource(buttonText),
            onClick = { onAction(buttonAction) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "Retry - Mobile", widthDp = 412)
@Preview(name = "Retry - Tablet sheet", widthDp = 480)
@Composable
private fun PermissionRationaleSheetRetryPreview() {
    SheetPreviewSurface {
        PermissionRationaleSheetContent(rationale = PermissionRationale.Retry, onAction = {})
    }
}

@Preview(name = "Settings - Mobile", widthDp = 412)
@Preview(name = "Settings - Tablet sheet", widthDp = 480)
@Composable
private fun PermissionRationaleSheetSettingsPreview() {
    SheetPreviewSurface {
        PermissionRationaleSheetContent(rationale = PermissionRationale.Settings, onAction = {})
    }
}
