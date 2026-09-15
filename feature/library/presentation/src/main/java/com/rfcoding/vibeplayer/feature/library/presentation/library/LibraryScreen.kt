package com.rfcoding.vibeplayer.feature.library.presentation.library

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeFab
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeMainTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeRadar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeTabRow
import com.rfcoding.vibeplayer.core.designsystem.components.bottomFade
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.MiniPlayer
import com.rfcoding.vibeplayer.core.presentation.MiniPlayerHeight
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.R
import com.rfcoding.vibeplayer.feature.library.presentation.playlist.PlaylistRoot
import com.rfcoding.vibeplayer.feature.library.presentation.songs.PreviewSongs
import com.rfcoding.vibeplayer.feature.library.presentation.songs.SongsRoot
import com.rfcoding.vibeplayer.feature.library.presentation.songs.SongsScreen
import com.rfcoding.vibeplayer.feature.library.presentation.songs.SongsState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val MobileTopBarPadding = PaddingValues(start = 16.dp, end = 10.dp)
private val TabletTopBarPadding = PaddingValues(start = 24.dp, end = 18.dp)
private val TabletMiniPlayerWidth = 480.dp

@Composable
fun LibraryRoot(
    onScanClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaylistCreated: (playlistId: Long) -> Unit,
    onPlaylistClick: (playlistId: Long?) -> Unit,
    onMiniPlayerClick: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LibraryEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    LibraryScreen(
        state = state,
        onAction = { action ->
            when (action) {
                LibraryAction.OnScanClick -> onScanClick()
                LibraryAction.OnSearchClick -> onSearchClick()
                LibraryAction.OnMiniPlayerClick -> onMiniPlayerClick()
                else -> viewModel.onAction(action)
            }
        },
        listState = listState,
        showScrollToTop = showScrollToTop,
        songsTab = {
            SongsRoot(listState = listState)
        },
        playlistsTab = {
            PlaylistRoot(
                onPlaylistCreated = onPlaylistCreated,
                onPlaylistClick = onPlaylistClick,
            )
        }
    )
}

@Composable
fun LibraryScreen(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    listState: LazyListState,
    showScrollToTop: Boolean,
    modifier: Modifier = Modifier,
    songsTab: @Composable () -> Unit,
    playlistsTab: @Composable () -> Unit
) {
    val isMobile = currentDeviceConfiguration().isMobile
    val coroutineScope = rememberCoroutineScope()
    val hasMiniPlayer = state.nowPlaying != null

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // The mini player runs to the bottom edge and insets itself, so the content keeps no bottom inset.
        contentWindowInsets = WindowInsets(0),
        topBar = {
            VibeMainTopBar(
                contentPadding = if (isMobile) MobileTopBarPadding else TabletTopBarPadding,
                actions = {
                    VibeIconButton(
                        icon = VibeIcons.Scan,
                        contentDescription = stringResource(R.string.scan_music),
                        onClick = { onAction(LibraryAction.OnScanClick) },
                    )
                    if (state.status == LibraryStatus.Loaded) {
                        VibeIconButton(
                            icon = VibeIcons.Search,
                            contentDescription = stringResource(R.string.search_music),
                            onClick = { onAction(LibraryAction.OnSearchClick) },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val centeredModifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
            when (state.status) {
                LibraryStatus.Scanning -> ScanningContent(modifier = centeredModifier)
                LibraryStatus.NoMusicFound -> NoMusicFoundContent(
                    onScanAgainClick = { onAction(LibraryAction.OnScanAgainClick) },
                    modifier = centeredModifier,
                )
                LibraryStatus.Loaded -> LoadedContent(
                    state = state,
                    onAction = onAction,
                    isMobile = isMobile,
                    // Figma's "Rectangle 5". It sits on the list only: the modifier paints over every
                    // child of the node it's applied to, so the FAB and mini player must stay outside.
                    modifier = Modifier.bottomFade(MaterialTheme.colorScheme.background),
                    songsTab = songsTab,
                    playlistsTab = playlistsTab
                )
            }

            if (state.status == LibraryStatus.Loaded) {
                AnimatedVisibility(
                    visible = showScrollToTop && state.selectedTab == LibraryTab.Songs,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(
                            end = 12.dp,
                            bottom = scrollToTopBottomPadding(isMobile, hasMiniPlayer),
                        ),
                ) {
                    VibeFab(
                        icon = VibeIcons.ArrowUp,
                        contentDescription = stringResource(R.string.scroll_to_top),
                        onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                    )
                }

                state.nowPlaying?.let { nowPlaying ->
                    MiniPlayer(
                        song = nowPlaying.song,
                        isPlaying = nowPlaying.isPlaying,
                        positionMillis = nowPlaying.positionMillis,
                        canSkipToPrevious = nowPlaying.canSkipToPrevious,
                        onClick = { onAction(LibraryAction.OnMiniPlayerClick) },
                        onSkipToPreviousClick = { onAction(LibraryAction.OnSkipToPreviousClick) },
                        onPlayPauseClick = { onAction(LibraryAction.OnPlayPauseClick) },
                        onSkipNextClick = { onAction(LibraryAction.OnSkipNextClick) },
                        onSeek = { onAction(LibraryAction.OnSeek(it)) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .widthIn(max = if (isMobile) Dp.Unspecified else TabletMiniPlayerWidth),
                    )
                }
            }
        }
    }
}

/** Figma keeps the FAB 12dp above the mini player, and otherwise 36dp (mobile) / 20dp up. */
private fun scrollToTopBottomPadding(isMobile: Boolean, hasMiniPlayer: Boolean) = when {
    isMobile && hasMiniPlayer -> MiniPlayerHeight + 12.dp
    isMobile -> 36.dp
    else -> 20.dp
}

@Composable
private fun LoadedContent(
    state: LibraryState,
    onAction: (LibraryAction) -> Unit,
    isMobile: Boolean,
    modifier: Modifier = Modifier,
    songsTab: @Composable () -> Unit,
    playlistsTab: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        VibeTabRow(
            tabs = listOf(stringResource(R.string.tab_songs), stringResource(R.string.tab_playlist)),
            selectedTabIndex = state.selectedTab.ordinal,
            onTabClick = { index -> onAction(LibraryAction.OnTabSelect(LibraryTab.entries[index])) },
            stretchTabs = isMobile,
        )
        when (state.selectedTab) {
            LibraryTab.Songs -> songsTab()
            LibraryTab.Playlist -> playlistsTab()
        }
    }
}

@Composable
private fun ScanningContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VibeRadar(isSweeping = true)
        Text(
            text = stringResource(R.string.scanning_device),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NoMusicFoundContent(
    onScanAgainClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.no_music_found),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.no_music_found_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        VibeButton(
            text = stringResource(R.string.scan_again),
            onClick = onScanAgainClick,
        )
    }
}

private val PreviewNowPlaying = NowPlayingUi(song = PreviewSongs.first())

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenScanningPreview() {
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(status = LibraryStatus.Scanning),
            onAction = {},
            listState = rememberLazyListState(),
            showScrollToTop = false,
            songsTab = {},
            playlistsTab = {}
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenNoMusicFoundPreview() {
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(status = LibraryStatus.NoMusicFound),
            onAction = {},
            listState = rememberLazyListState(),
            showScrollToTop = false,
            songsTab = {},
            playlistsTab = {}
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenSongsPreview() {
    val listState = rememberLazyListState()
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(status = LibraryStatus.Loaded),
            onAction = {},
            listState = listState,
            showScrollToTop = false,
            songsTab = { PreviewSongsTab(listState) },
            playlistsTab = {}
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenNoPlaylistPreview() {
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(
                status = LibraryStatus.Loaded,
                selectedTab = LibraryTab.Playlist
            ),
            onAction = {},
            listState = rememberLazyListState(),
            showScrollToTop = false,
            songsTab = {},
            playlistsTab = {}
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenHavePlaylistPreview() {
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(
                status = LibraryStatus.Loaded,
                selectedTab = LibraryTab.Playlist
            ),
            onAction = {},
            listState = rememberLazyListState(),
            showScrollToTop = false,
            songsTab = {},
            playlistsTab = {}
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenMiniPlayerPausedPreview() {
    val listState = rememberLazyListState()
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(
                status = LibraryStatus.Loaded,
                nowPlaying = PreviewNowPlaying,
            ),
            onAction = {},
            listState = listState,
            showScrollToTop = false,
            songsTab = { PreviewSongsTab(listState) },
            playlistsTab = {}
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun LibraryScreenMiniPlayerPlayingPreview() {
    val listState = rememberLazyListState()
    VibePlayerTheme {
        LibraryScreen(
            state = LibraryState(
                status = LibraryStatus.Loaded,
                nowPlaying = NowPlayingUi(
                    song = PreviewSongs[2],
                    isPlaying = true,
                    positionMillis = 127_000,
                    canSkipToPrevious = true,
                ),
            ),
            onAction = {},
            listState = listState,
            showScrollToTop = false,
            songsTab = { PreviewSongsTab(listState) },
            playlistsTab = {}
        )
    }
}

@Composable
private fun PreviewSongsTab(listState: LazyListState) {
    SongsScreen(
        state = SongsState(songs = PreviewSongs),
        listState = listState,
        onAction = {},
    )
}
