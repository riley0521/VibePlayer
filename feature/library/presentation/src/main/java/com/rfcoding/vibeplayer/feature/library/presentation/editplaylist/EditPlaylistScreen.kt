package com.rfcoding.vibeplayer.feature.library.presentation.editplaylist

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.EditableSongCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeInnerTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.bottomFade
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.R
import com.rfcoding.vibeplayer.feature.library.presentation.songs.PreviewSongs
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

// VibeInnerTopBar already pads itself to Figma's mobile 10dp; tablets add the missing 8dp.
private val TabletTopBarPadding = 8.dp
private val DraggedElevation = 8.dp

@Composable
fun EditPlaylistRoot(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    viewModel: EditPlaylistViewModel = koinViewModel { parametersOf(playlistId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            EditPlaylistEvent.NavigateBack -> onNavigateBack()
            is EditPlaylistEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    EditPlaylistScreen(state = state, onAction = viewModel::onAction)
}

/**
 * The Edit playlist page: back, the title and a Save text button over a list of songs that each carry
 * an X to remove them and a handle to drag them into place. Nothing is written until Save, so leaving
 * with changes asks first.
 */
@Composable
fun EditPlaylistScreen(
    state: EditPlaylistState,
    onAction: (EditPlaylistAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMobile = currentDeviceConfiguration().isMobile
    val sidePadding = if (isMobile) 16.dp else 24.dp

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onAction(EditPlaylistAction.OnMoveSong(from.index, to.index))
    }

    BackHandler { onAction(EditPlaylistAction.OnBackClick) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VibeInnerTopBar(
                title = stringResource(R.string.edit_playlist),
                onBackClick = { onAction(EditPlaylistAction.OnBackClick) },
                modifier = if (isMobile) Modifier else Modifier.padding(horizontal = TabletTopBarPadding),
                actions = {
                    VibeButton(
                        text = stringResource(R.string.action_save),
                        onClick = { onAction(EditPlaylistAction.OnSaveClick) },
                        style = VibeButtonStyle.Text,
                        enabled = state.canSave,
                        isLoading = state.isSaving,
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                // Figma's "Rectangle 5", easing the list into the background.
                .bottomFade(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
        ) {
            items(items = state.songs, key = { it.id }) { song ->
                ReorderableItem(reorderableState, key = song.id) { isDragging ->
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) DraggedElevation else 0.dp,
                        label = "dragElevation",
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shadowElevation = elevation,
                    ) {
                        EditableSongCard(
                            title = song.title,
                            artistName = song.artistName,
                            imageUri = song.imageUri,
                            onRemoveClick = {
                                onAction(EditPlaylistAction.OnRemoveSongClick(song.id))
                            },
                            modifier = Modifier.padding(horizontal = sidePadding),
                            dragHandleModifier = Modifier.draggableHandle(),
                        )
                    }
                }
            }
        }
    }

    if (state.isDiscardSheetVisible) {
        DiscardChangesSheet(onAction = onAction)
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 1024)
@Composable
private fun EditPlaylistScreenPreview() {
    VibePlayerTheme {
        EditPlaylistScreen(
            state = EditPlaylistState(songs = PreviewSongs, isLoading = false, hasChanges = true),
            onAction = {},
        )
    }
}
