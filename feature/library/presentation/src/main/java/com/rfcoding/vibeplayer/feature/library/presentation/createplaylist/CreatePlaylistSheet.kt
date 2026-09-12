package com.rfcoding.vibeplayer.feature.library.presentation.createplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeTextField
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.R

/** Figma keeps the tablet sheet a centered 480dp card instead of letting it span the screen. */
private val TabletSheetWidth = 480.dp

/**
 * Figma "Create Playlist bottom sheet". The Create button follows [CreatePlaylistState.canCreate]:
 * disabled it is dim and unfilled, enabled it is the purple pill with a glow — both already baked
 * into [VibeButton].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistSheet(
    state: CreatePlaylistState,
    onAction: (CreatePlaylistAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMobile = currentDeviceConfiguration().isMobile
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
        // Unspecified lets the sheet fill a phone; Material's default would cap it at 640dp.
        sheetMaxWidth = if (isMobile) Dp.Unspecified else TabletSheetWidth,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        scrimColor = MaterialTheme.colorScheme.scrim,
        // Figma draws no drag handle.
        dragHandle = null,
    ) {
        CreatePlaylistSheetContent(state = state, onAction = onAction)
    }
}

@Composable
internal fun CreatePlaylistSheetContent(
    state: CreatePlaylistState,
    onAction: (CreatePlaylistAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.create_new_playlist),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        VibeTextField(
            value = state.name,
            onValueChange = { onAction(CreatePlaylistAction.OnNameChange(it)) },
            placeholder = stringResource(R.string.playlist_name_placeholder),
            maxLength = MaxPlaylistNameLength,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VibeButton(
                text = stringResource(R.string.cancel),
                onClick = { onAction(CreatePlaylistAction.OnCancelClick) },
                style = VibeButtonStyle.Outlined,
                modifier = Modifier.weight(1f),
            )
            VibeButton(
                text = stringResource(R.string.create),
                onClick = { onAction(CreatePlaylistAction.OnCreateClick) },
                enabled = state.canCreate,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Stands in for the sheet window the previews can't render, so the surface and corners still show. */
@Composable
private fun SheetPreviewSurface(content: @Composable () -> Unit) {
    VibePlayerTheme {
        Box(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            ),
        ) {
            content()
        }
    }
}

// ModalBottomSheet renders in its own window and doesn't show up in previews, so the content is
// previewed directly: full width for mobile, and at the 480dp the tablet sheet is capped to.
@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun CreatePlaylistSheetInitialPreview() {
    SheetPreviewSurface {
        CreatePlaylistSheetContent(state = CreatePlaylistState(), onAction = {})
    }
}

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun CreatePlaylistSheetValidInputPreview() {
    SheetPreviewSurface {
        CreatePlaylistSheetContent(
            state = CreatePlaylistState(name = "Hypin' myself up for cleaning"),
            onAction = {},
        )
    }
}
