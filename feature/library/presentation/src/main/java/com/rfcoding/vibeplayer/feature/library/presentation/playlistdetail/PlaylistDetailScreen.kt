package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistArtworkImage
import com.rfcoding.vibeplayer.core.designsystem.components.SongCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeInnerTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.bottomFade
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.R
import com.rfcoding.vibeplayer.feature.library.presentation.components.PlayButton
import com.rfcoding.vibeplayer.feature.library.presentation.components.ShuffleButton
import com.rfcoding.vibeplayer.feature.library.presentation.components.SongCountText
import com.rfcoding.vibeplayer.feature.library.presentation.songs.PreviewSongs
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.rfcoding.vibeplayer.core.presentation.R as PresentationR

// VibeInnerTopBar already pads itself to Figma's mobile 10dp; tablets add the missing 8dp.
private val TabletTopBarPadding = 8.dp
private val HeroHeight = 320.dp
private val ArtworkSize = 200.dp

/**
 * @param playlistId the playlist to show, or null for the virtual Favourites.
 * @param onAddSongsClick only called for a real playlist; Favourites offers no Add Songs button.
 */
@Composable
fun PlaylistDetailRoot(
    playlistId: Long?,
    onNavigateBack: () -> Unit,
    onAddSongsClick: (playlistId: Long) -> Unit,
    viewModel: PlaylistDetailViewModel = koinViewModel { parametersOf(playlistId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PlaylistDetailEvent.NavigateBack -> onNavigateBack()
        }
    }

    PlaylistDetailScreen(
        state = state,
        onAction = { action ->
            when (action) {
                PlaylistDetailAction.OnBackClick -> onNavigateBack()
                PlaylistDetailAction.OnAddSongsClick -> playlistId?.let(onAddSongsClick)
                else -> viewModel.onAction(action)
            }
        },
    )
}

/**
 * Figma "Playlist Page" / "Playlist Page - Empty". The circular cover and the title scroll away with
 * the songs. On mobile the song count gets its own row under Shuffle/Play; tablets put both in one row.
 *
 * Like `PlaylistScreen`, the horizontal padding lives on each item, because the count row's end inset is
 * narrower (the add button's 44dp touch target overhangs).
 */
@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailState,
    onAction: (PlaylistDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMobile = currentDeviceConfiguration().isMobile
    val sidePadding = if (isMobile) 16.dp else 24.dp

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VibeInnerTopBar(
                title = "",
                onBackClick = { onAction(PlaylistDetailAction.OnBackClick) },
                modifier = if (isMobile) Modifier else Modifier.padding(horizontal = TabletTopBarPadding),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                // Figma's "Rectangle 5", easing the list into the background.
                .bottomFade(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
        ) {
            item(key = "hero") {
                PlaylistHero(
                    name = if (state.isFavourites) stringResource(PresentationR.string.favourites) else state.name,
                    artwork = when {
                        state.isFavourites -> PlaylistArtwork.Favourites
                        state.imageUri != null -> PlaylistArtwork.Image(state.imageUri)
                        else -> PlaylistArtwork.Default
                    },
                    modifier = Modifier.padding(horizontal = sidePadding),
                )
            }
            when {
                state.isLoading -> Unit
                state.songs.isEmpty() -> item(key = "empty") {
                    EmptyPlaylistContent(
                        canAddSongs = state.canAddSongs,
                        onAddSongsClick = { onAction(PlaylistDetailAction.OnAddSongsClick) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                else -> {
                    item(key = "header") {
                        PlaylistDetailHeader(
                            songCount = state.songs.size,
                            isMobile = isMobile,
                            canAddSongs = state.canAddSongs,
                            onAction = onAction,
                        )
                    }
                    items(items = state.songs, key = { it.id }) { song ->
                        SongCard(
                            title = song.title,
                            artistName = song.artistName,
                            duration = song.durationText,
                            imageUri = song.imageUri,
                            onClick = { onAction(PlaylistDetailAction.OnSongClick(song.id)) },
                            modifier = Modifier.padding(horizontal = sidePadding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistHero(
    name: String,
    artwork: PlaylistArtwork,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HeroHeight),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlaylistArtworkImage(
            artwork = artwork,
            modifier = Modifier
                .size(ArtworkSize)
                .clip(CircleShape),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun PlaylistDetailHeader(
    songCount: Int,
    isMobile: Boolean,
    canAddSongs: Boolean,
    onAction: (PlaylistDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onShuffleClick = { onAction(PlaylistDetailAction.OnShuffleClick) }
    val onPlayClick = { onAction(PlaylistDetailAction.OnPlayClick) }

    if (isMobile) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShuffleButton(onClick = onShuffleClick, modifier = Modifier.weight(1f))
                PlayButton(onClick = onPlayClick, modifier = Modifier.weight(1f))
            }
            SongCountRow(
                songCount = songCount,
                canAddSongs = canAddSongs,
                onAddSongsClick = { onAction(PlaylistDetailAction.OnAddSongsClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
            )
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShuffleButton(onClick = onShuffleClick)
            PlayButton(onClick = onPlayClick)
            Spacer(modifier = Modifier.weight(1f))
            SongCountRow(
                songCount = songCount,
                canAddSongs = canAddSongs,
                onAddSongsClick = { onAction(PlaylistDetailAction.OnAddSongsClick) },
            )
        }
    }
}

@Composable
private fun SongCountRow(
    songCount: Int,
    canAddSongs: Boolean,
    onAddSongsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        // Keeps the add button's height when Favourites leaves it out.
        modifier = modifier.heightIn(min = 44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongCountText(songCount = songCount)
        if (canAddSongs) {
            VibeIconButton(
                icon = VibeIcons.Plus,
                contentDescription = stringResource(R.string.add_songs),
                onClick = onAddSongsClick,
            )
        }
    }
}

@Composable
private fun EmptyPlaylistContent(
    canAddSongs: Boolean,
    onAddSongsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.no_songs_found),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (canAddSongs) {
            VibeButton(
                text = stringResource(R.string.add_songs),
                onClick = onAddSongsClick,
                style = VibeButtonStyle.Outlined,
                leadingIcon = VibeIcons.Plus,
            )
        }
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 1024)
@Composable
private fun PlaylistDetailScreenPreview() {
    VibePlayerTheme {
        PlaylistDetailScreen(
            state = PlaylistDetailState(name = "My Playlist", songs = PreviewSongs, isLoading = false),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 1024)
@Composable
private fun PlaylistDetailScreenEmptyPreview() {
    VibePlayerTheme {
        PlaylistDetailScreen(
            state = PlaylistDetailState(name = "My Playlist", isLoading = false),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 1024)
@Composable
private fun PlaylistDetailScreenFavouritesPreview() {
    VibePlayerTheme {
        PlaylistDetailScreen(
            state = PlaylistDetailState(isFavourites = true, songs = PreviewSongs.take(3), isLoading = false),
            onAction = {},
        )
    }
}
