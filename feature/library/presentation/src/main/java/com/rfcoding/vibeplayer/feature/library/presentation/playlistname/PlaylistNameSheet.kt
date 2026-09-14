package com.rfcoding.vibeplayer.feature.library.presentation.playlistname

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeTextField
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheet
import com.rfcoding.vibeplayer.feature.library.domain.PlaylistNameValidator
import com.rfcoding.vibeplayer.feature.library.presentation.R
import com.rfcoding.vibeplayer.feature.library.presentation.components.SheetPreviewSurface
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Hosts [PlaylistNameViewModel]; call it inside `DialogSheetScopedViewModel` so each opening of the
 * sheet gets a fresh ViewModel for [mode].
 */
@Composable
fun PlaylistNameSheetRoot(
    mode: PlaylistNameMode,
    onDismiss: () -> Unit,
    onPlaylistCreated: (playlistId: Long) -> Unit,
    viewModel: PlaylistNameViewModel = koinViewModel { parametersOf(mode) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PlaylistNameEvent.PlaylistCreated -> onPlaylistCreated(event.playlistId)
            PlaylistNameEvent.PlaylistRenamed -> onDismiss()
            is PlaylistNameEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    PlaylistNameSheet(
        state = state,
        onAction = { action ->
            when (action) {
                PlaylistNameAction.OnCancelClick -> onDismiss()
                else -> viewModel.onAction(action)
            }
        },
        onDismiss = onDismiss,
    )
}

/**
 * Figma "Create Playlist bottom sheet", which also serves the action sheet's Rename row: only the
 * title and the confirm label change. The confirm button follows [PlaylistNameState.canConfirm] —
 * disabled it is dim and unfilled, enabled it is the purple pill with a glow, both baked into
 * [VibeButton].
 */
@Composable
fun PlaylistNameSheet(
    state: PlaylistNameState,
    onAction: (PlaylistNameAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        PlaylistNameSheetContent(state = state, onAction = onAction)
    }
}

@Composable
internal fun PlaylistNameSheetContent(
    state: PlaylistNameState,
    onAction: (PlaylistNameAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleRes = when (state.mode) {
        PlaylistNameMode.Create -> R.string.create_new_playlist
        is PlaylistNameMode.Rename -> R.string.rename_playlist
    }
    val confirmRes = when (state.mode) {
        PlaylistNameMode.Create -> R.string.create
        is PlaylistNameMode.Rename -> R.string.rename
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        VibeTextField(
            value = state.name,
            onValueChange = { onAction(PlaylistNameAction.OnNameChange(it)) },
            placeholder = stringResource(R.string.playlist_name_placeholder),
            maxLength = PlaylistNameValidator.MAX_LENGTH,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VibeButton(
                text = stringResource(R.string.cancel),
                onClick = { onAction(PlaylistNameAction.OnCancelClick) },
                style = VibeButtonStyle.Outlined,
                modifier = Modifier.weight(1f),
            )
            VibeButton(
                text = stringResource(confirmRes),
                onClick = { onAction(PlaylistNameAction.OnConfirmClick) },
                enabled = state.canConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ModalBottomSheet renders in its own window and doesn't show up in previews, so the content is
// previewed directly: full width for mobile, and at the 480dp the tablet sheet is capped to.
@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun PlaylistNameSheetInitialPreview() {
    SheetPreviewSurface {
        PlaylistNameSheetContent(state = PlaylistNameState(), onAction = {})
    }
}

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun PlaylistNameSheetValidInputPreview() {
    SheetPreviewSurface {
        PlaylistNameSheetContent(
            state = PlaylistNameState(name = "Hypin' myself up for cleaning"),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun PlaylistNameSheetRenamePreview() {
    SheetPreviewSurface {
        PlaylistNameSheetContent(
            state = PlaylistNameState(
                mode = PlaylistNameMode.Rename(playlistId = 1, currentName = "Friday Chill"),
                name = "Friday Chill",
            ),
            onAction = {},
        )
    }
}
