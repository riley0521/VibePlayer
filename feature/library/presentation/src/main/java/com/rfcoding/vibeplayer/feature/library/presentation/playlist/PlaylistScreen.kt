package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.presentation.DialogSheetScopedViewModel
import com.rfcoding.vibeplayer.core.presentation.MiniPlayerHeight
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.PlaylistUi
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.core.presentation.playlistname.PlaylistNameSheetRoot
import com.rfcoding.vibeplayer.feature.library.presentation.R
import org.koin.androidx.compose.koinViewModel
import com.rfcoding.vibeplayer.core.presentation.R as PresentationR

/**
 * @param onPlaylistCreated called with the new playlist's id once the create sheet has saved it.
 * @param onPlaylistClick opens the Playlist Page; a null id is the virtual Favourites.
 */
@Composable
internal fun PlaylistRoot(
    onPlaylistCreated: (playlistId: Long) -> Unit,
    onPlaylistClick: (playlistId: Long?) -> Unit,
    viewModel: PlaylistViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Without a persistable grant the picked image stops loading after the app restarts.
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some providers don't offer persistable grants; the cover still shows for this session.
            }
        }
        viewModel.onAction(PlaylistAction.OnCoverPicked(uri?.toString()))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PlaylistEvent.LaunchCoverPicker -> coverPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            is PlaylistEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    PlaylistScreen(
        state = state,
        onAction = { action ->
            when (action) {
                PlaylistAction.OnFavouritesClick -> onPlaylistClick(null)
                is PlaylistAction.OnPlaylistClick -> onPlaylistClick(action.playlistId)
                else -> viewModel.onAction(action)
            }
        }
    )

    val onSheetDismiss = { viewModel.onAction(PlaylistAction.OnSheetDismiss) }
    when (val sheet = state.activeSheet) {
        null, is PlaylistSheet.PlaylistName -> Unit
        is PlaylistSheet.PlaylistActions -> PlaylistActionSheet(
            sheet = sheet,
            onAction = viewModel::onAction,
            onDismiss = onSheetDismiss,
        )
        is PlaylistSheet.DeletePlaylist -> DeletePlaylistSheet(
            sheet = sheet,
            onAction = viewModel::onAction,
            onDismiss = onSheetDismiss,
        )
    }

    val nameSheet = state.activeSheet as? PlaylistSheet.PlaylistName
    DialogSheetScopedViewModel(visible = nameSheet != null) {
        // The scope is cleared a frame after the sheet closes; show nothing in between.
        nameSheet?.let {
            PlaylistNameSheetRoot(
                mode = it.mode,
                onDismiss = onSheetDismiss,
                onPlaylistCreated = { playlistId, _ ->
                    onSheetDismiss()
                    onPlaylistCreated(playlistId)
                },
            )
        }
    }
}

/**
 * Figma "Main Page - Playlist Tab": the virtual Favourites card sits above the "My Playlists" section,
 * which either lists the user's playlists or offers the Create playlist button.
 *
 * Unlike [com.rfcoding.vibeplayer.feature.library.presentation.songs.SongsScreen] the horizontal padding lives on each item rather than on the list's
 * `contentPadding`, because the header rows use a narrower end inset than the cards (the trailing icon
 * button's 44dp touch target overhangs) and because the card dividers must stop at the content width.
 */
@Composable
internal fun PlaylistScreen(
    state: PlaylistState,
    onAction: (PlaylistAction) -> Unit
) {
    // The tab owns its scroll state; the one LibraryRoot hoists belongs to the Songs tab's FAB.
    val listState = rememberLazyListState()
    val isMobile = currentDeviceConfiguration().isMobile
    val cardPadding = if (isMobile) 16.dp else 24.dp
    val headerPadding = PaddingValues(
        start = cardPadding,
        end = if (isMobile) 12.dp else 20.dp,
    )
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + MiniPlayerHeight

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = bottomContentPadding),
    ) {
        item(key = "total") {
            SectionHeader(
                title = pluralStringResource(
                    R.plurals.playlist_count,
                    state.totalPlaylistCount,
                    state.totalPlaylistCount,
                ),
                modifier = Modifier.padding(headerPadding).padding(top = 12.dp, bottom = 4.dp),
            ) {
                VibeIconButton(
                    icon = VibeIcons.Plus,
                    contentDescription = stringResource(R.string.create_playlist),
                    onClick = { onAction(PlaylistAction.OnCreatePlaylistClick) },
                )
            }
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
                onClick = { onAction(PlaylistAction.OnFavouritesClick) },
                onMenuClick = { onAction(PlaylistAction.OnFavouritesMenuClick) },
                modifier = Modifier.padding(horizontal = cardPadding),
            )
        }
        item(key = "myPlaylists") {
            SectionHeader(
                title = stringResource(R.string.my_playlists, state.playlists.size),
                modifier = Modifier.padding(headerPadding).padding(top = 16.dp, bottom = 8.dp),
            )
        }
        if (state.playlists.isEmpty()) {
            item(key = "create") {
                VibeButton(
                    text = stringResource(R.string.create_playlist),
                    onClick = { onAction(PlaylistAction.OnCreatePlaylistClick) },
                    style = VibeButtonStyle.Outlined,
                    leadingIcon = VibeIcons.Plus,
                    modifier = Modifier
                        .padding(horizontal = cardPadding)
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                )
            }
        } else {
            items(items = state.playlists, key = { it.id }) { playlist ->
                PlaylistCard(
                    title = playlist.name,
                    subtitle = pluralStringResource(
                        PresentationR.plurals.playlist_song_count,
                        playlist.songCount,
                        playlist.songCount,
                    ),
                    artwork = playlist.imageUri?.let(PlaylistArtwork::Image) ?: PlaylistArtwork.Default,
                    onClick = { onAction(PlaylistAction.OnPlaylistClick(playlist.id)) },
                    onMenuClick = { onAction(PlaylistAction.OnPlaylistMenuClick(playlist.id)) },
                    modifier = Modifier.padding(horizontal = cardPadding),
                )
            }
        }
    }
}

/** Favourites is always listed, so it counts towards the total the user sees. */
private val PlaylistState.totalPlaylistCount: Int
    get() = playlists.size + 1

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLargeMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        trailing?.invoke()
    }
}

internal val PreviewPlaylists = listOf(
    PlaylistUi(id = 1, name = "Friday Chill", songCount = 2, imageUri = null),
    PlaylistUi(id = 2, name = "Hypin' myself up for cleaning", songCount = 2, imageUri = null),
)
