package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.presentation.PlaylistUi
import com.rfcoding.vibeplayer.feature.library.presentation.R

/**
 * Figma "Main Page - Playlist Tab": the virtual Favourites card sits above the "My Playlists" section,
 * which either lists the user's playlists or offers the Create playlist button.
 *
 * Unlike [LibrarySongsTab] the horizontal padding lives on each item rather than on the list's
 * `contentPadding`, because the header rows use a narrower end inset than the cards (the trailing icon
 * button's 44dp touch target overhangs) and because the card dividers must stop at the content width.
 */
@Composable
internal fun LibraryPlaylistTab(
    state: LibraryState,
    isMobile: Boolean,
    bottomContentPadding: Dp,
    onAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The tab owns its scroll state; the one LibraryScreen hoists belongs to the Songs tab's FAB.
    val listState = rememberLazyListState()
    val cardPadding = if (isMobile) 16.dp else 24.dp
    val headerPadding = PaddingValues(
        start = cardPadding,
        end = if (isMobile) 12.dp else 20.dp,
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                    onClick = { onAction(LibraryAction.OnCreatePlaylistClick) },
                )
            }
        }
        item(key = "favourites") {
            PlaylistCard(
                title = stringResource(R.string.favourites),
                subtitle = pluralStringResource(
                    R.plurals.playlist_song_count,
                    state.favouriteSongCount,
                    state.favouriteSongCount,
                ),
                artwork = PlaylistArtwork.Favourites,
                onClick = { onAction(LibraryAction.OnFavouritesClick) },
                onMenuClick = { onAction(LibraryAction.OnFavouritesMenuClick) },
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
                    onClick = { onAction(LibraryAction.OnCreatePlaylistClick) },
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
                        R.plurals.playlist_song_count,
                        playlist.songCount,
                        playlist.songCount,
                    ),
                    artwork = playlist.imageUri?.let(PlaylistArtwork::Image) ?: PlaylistArtwork.Default,
                    onClick = { onAction(LibraryAction.OnPlaylistClick(playlist.id)) },
                    onMenuClick = { onAction(LibraryAction.OnPlaylistMenuClick(playlist.id)) },
                    modifier = Modifier.padding(horizontal = cardPadding),
                )
            }
        }
    }
}

/** Favourites is always listed, so it counts towards the total the user sees. */
private val LibraryState.totalPlaylistCount: Int
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
