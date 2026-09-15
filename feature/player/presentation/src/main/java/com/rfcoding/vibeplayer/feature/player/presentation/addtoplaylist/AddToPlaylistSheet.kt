package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistCard
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.PlaylistUi
import com.rfcoding.vibeplayer.core.presentation.SheetPreviewSurface
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheet
import com.rfcoding.vibeplayer.feature.player.presentation.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.rfcoding.vibeplayer.core.presentation.R as PresentationR

/**
 * Hosts [AddToPlaylistViewModel]; call it inside `DialogSheetScopedViewModel` so each opening of the
 * sheet gets a fresh ViewModel for [songId]. Once the song is added it shows a toast and closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheetRoot(
    songId: String,
    onCreatePlaylistClick: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: AddToPlaylistViewModel = koinViewModel { parametersOf(songId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AddToPlaylistEvent.AddedToPlaylist -> {
                val message = context.getString(R.string.added_to_playlist, event.playlistName.asString(context))
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            }
            is AddToPlaylistEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    AddToPlaylistSheet(
        state = state,
        onAction = { action ->
            when (action) {
                AddToPlaylistAction.OnCreatePlaylistClick -> onCreatePlaylistClick()
                else -> viewModel.onAction(action)
            }
        },
        onDismiss = onDismiss,
        sheetState = sheetState,
    )
}

/**
 * Figma "Now Playing - Add to Playlist": no title and no handle, just the Create Playlist row, the
 * virtual Favourites and the user's playlists as playlist cards without their options button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    state: AddToPlaylistState,
    onAction: (AddToPlaylistAction) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
) {
    VibeBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        AddToPlaylistSheetContent(state = state, onAction = onAction)
    }
}

@Composable
internal fun AddToPlaylistSheetContent(
    state: AddToPlaylistState,
    onAction: (AddToPlaylistAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        // The sheet keeps 16dp side padding on tablets too, like the playlist action sheet.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 24.dp),
    ) {
        item(key = "create") {
            PlaylistCard(
                title = stringResource(R.string.create_playlist),
                subtitle = null,
                artwork = PlaylistArtwork.Create,
                onClick = { onAction(AddToPlaylistAction.OnCreatePlaylistClick) },
            )
        }
        item(key = "favourites") {
            PlaylistCard(
                title = stringResource(PresentationR.string.favourites),
                subtitle = pluralStringResource(
                    PresentationR.plurals.playlist_song_count,
                    state.favouriteSongCount,
                    state.favouriteSongCount,
                ),
                artwork = PlaylistArtwork.Favourites,
                onClick = { onAction(AddToPlaylistAction.OnFavouritesClick) },
            )
        }
        items(items = state.playlists, key = { it.id }) { playlist ->
            PlaylistCard(
                title = playlist.name,
                subtitle = pluralStringResource(
                    PresentationR.plurals.playlist_song_count,
                    playlist.songCount,
                    playlist.songCount,
                ),
                artwork = playlist.imageUri?.let(PlaylistArtwork::Image) ?: PlaylistArtwork.Default,
                onClick = { onAction(AddToPlaylistAction.OnPlaylistClick(playlist.id)) },
            )
        }
    }
}

// ModalBottomSheet renders in its own window and doesn't show up in previews, so the content is
// previewed directly: full width for mobile, and at the 480dp the tablet sheet is capped to.
@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun AddToPlaylistSheetPreview() {
    SheetPreviewSurface {
        AddToPlaylistSheetContent(
            state = AddToPlaylistState(
                favouriteSongCount = 2,
                playlists = listOf(
                    PlaylistUi(id = 1, name = "Friday Chill", songCount = 2, imageUri = null),
                    PlaylistUi(id = 2, name = "Hypin' myself up for cleaning", songCount = 2, imageUri = null),
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "No playlists", widthDp = 412)
@Composable
private fun AddToPlaylistSheetNoPlaylistsPreview() {
    SheetPreviewSurface {
        AddToPlaylistSheetContent(state = AddToPlaylistState(), onAction = {})
    }
}
