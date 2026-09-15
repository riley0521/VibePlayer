package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.presentation.SheetPreviewSurface
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheet
import com.rfcoding.vibeplayer.feature.library.presentation.R

/** Figma centers the message in a column this wide, even on a tablet. */
private val MessageMaxWidth = 400.dp

/**
 * Figma "Main Page - Playlist + Action Sheet - Delete". It is drawn as another bottom sheet rather
 * than a dialog, so it reuses the same shell as the action sheet it replaces.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletePlaylistSheet(
    sheet: PlaylistSheet.DeletePlaylist,
    onAction: (PlaylistAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        DeletePlaylistSheetContent(sheet = sheet, onAction = onAction)
    }
}

@Composable
internal fun DeletePlaylistSheetContent(
    sheet: PlaylistSheet.DeletePlaylist,
    onAction: (PlaylistAction) -> Unit,
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
                text = stringResource(R.string.delete_playlist_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.delete_playlist_message, sheet.playlist.name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VibeButton(
                text = stringResource(R.string.cancel),
                onClick = { onAction(PlaylistAction.OnSheetDismiss) },
                style = VibeButtonStyle.Outlined,
                modifier = Modifier.weight(1f),
            )
            VibeButton(
                text = stringResource(R.string.action_delete),
                onClick = { onAction(PlaylistAction.OnConfirmDeletePlaylistClick(sheet.playlist.id)) },
                style = VibeButtonStyle.Destructive,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun DeletePlaylistSheetPreview() {
    SheetPreviewSurface {
        DeletePlaylistSheetContent(
            sheet = PlaylistSheet.DeletePlaylist(playlist = PreviewPlaylists.first()),
            onAction = {},
        )
    }
}
