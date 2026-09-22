package com.rfcoding.vibeplayer.feature.player.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.SongArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeInnerTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeSeekBar
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.presentation.DialogSheetScopedViewModel
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.core.presentation.playlistname.PlaylistNameMode
import com.rfcoding.vibeplayer.core.presentation.playlistname.PlaylistNameSheetRoot
import com.rfcoding.vibeplayer.core.presentation.toDurationText
import com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist.AddToPlaylistSheetRoot
import com.rfcoding.vibeplayer.feature.player.presentation.sharecard.ShareCardSheet
import org.koin.androidx.compose.koinViewModel
import com.rfcoding.vibeplayer.core.presentation.R as PresentationR

// VibeInnerTopBar already pads itself to Figma's mobile 10dp; tablets add the missing 8dp.
private val TabletTopBarPadding = 8.dp
private val ArtworkSize = 320.dp
private val TextBlockMaxWidth = 400.dp
private val TransportMaxWidth = 768.dp

@Composable
fun PlayerRoot(
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PlayerEvent.AddedToPlaylist -> {
                val message = context.getString(R.string.added_to_playlist, event.playlistName.asString(context))
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            is PlayerEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
            PlayerEvent.CardSaved -> {
                Toast.makeText(context, R.string.card_saved, Toast.LENGTH_SHORT).show()
            }
        }
    }

    PlayerScreen(
        state = state,
        onAction = { action ->
            when (action) {
                PlayerAction.OnBackClick -> onNavigateBack()
                else -> viewModel.onAction(action)
            }
        },
    )

    val onSheetDismiss = { viewModel.onAction(PlayerAction.OnSheetDismiss) }

    val addToPlaylistSheet = state.activeSheet as? PlayerSheet.AddToPlaylist
    DialogSheetScopedViewModel(visible = addToPlaylistSheet != null) {
        // The scope is cleared a frame after the sheet closes; show nothing in between.
        addToPlaylistSheet?.let {
            AddToPlaylistSheetRoot(
                songId = it.songId,
                onCreatePlaylistClick = { viewModel.onAction(PlayerAction.OnCreatePlaylistClick) },
                onDismiss = onSheetDismiss,
            )
        }
    }

    val createPlaylistSheet = state.activeSheet as? PlayerSheet.CreatePlaylist
    DialogSheetScopedViewModel(visible = createPlaylistSheet != null) {
        createPlaylistSheet?.let {
            PlaylistNameSheetRoot(
                mode = PlaylistNameMode.Create,
                onDismiss = onSheetDismiss,
                onPlaylistCreated = { playlistId, name ->
                    viewModel.onAction(PlayerAction.OnPlaylistCreated(playlistId, name))
                },
            )
        }
    }

    (state.activeSheet as? PlayerSheet.ShareCard)?.let { sheet ->
        ShareCardSheet(
            song = sheet.song,
            isSaving = state.isSavingCard,
            onAction = viewModel::onAction,
            onDismiss = onSheetDismiss,
        )
    }
}

@Composable
fun PlayerScreen(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMobile = currentDeviceConfiguration().isMobile
    val horizontalPadding = if (isMobile) 16.dp else 24.dp

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VibeInnerTopBar(
                title = stringResource(R.string.now_playing),
                onBackClick = { onAction(PlayerAction.OnBackClick) },
                modifier = if (isMobile) Modifier else Modifier.padding(horizontal = TabletTopBarPadding),
                navigationIcon = VibeIcons.ChevronDown,
                actions = {
                    VibeIconButton(
                        icon = VibeIcons.PlaylistLinear,
                        contentDescription = stringResource(R.string.add_to_playlist),
                        onClick = { onAction(PlayerAction.OnAddToPlaylistClick) },
                    )
                    VibeIconButton(
                        icon = if (state.isFavorite) VibeIcons.HeartDuotone else VibeIcons.HeartLinear,
                        contentDescription = stringResource(
                            if (state.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites,
                        ),
                        onClick = { onAction(PlayerAction.OnFavoriteClick) },
                        tint = if (state.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        // Nothing is queued until the session connects; only the top bar shows meanwhile.
        val song = state.song ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ArtworkPager(
                    previous = state.previousSong,
                    current = song,
                    next = state.nextSong,
                    pageSpacing = horizontalPadding,
                    onAction = onAction,
                )
                Column(
                    modifier = Modifier
                        .widthIn(max = TextBlockMaxWidth)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    val artistName = song.artistName
                    if (artistName != null) {
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            TransportControls(
                state = state,
                song = song,
                onAction = onAction,
                modifier = Modifier
                    .widthIn(max = TransportMaxWidth)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .navigationBarsPadding()
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = 16.dp),
            )
        }
    }
}

/**
 * The pager's songs: the current one with its neighbours to either side. A missing neighbour has no
 * page, so the pager can't be swiped past that end of the queue.
 */
private data class ArtworkSlots(val previous: SongUi?, val current: SongUi, val next: SongUi?) {
    val pages: List<SongUi> = listOfNotNull(previous, current, next)
    val currentPage: Int = if (previous != null) 1 else 0
}

/**
 * The artwork, swipeable to the previous or next song. It returns to the current song's page whenever
 * the song changes, so a swipe only asks for the skip; the session decides the rest.
 */
@Composable
private fun ArtworkPager(
    previous: SongUi?,
    current: SongUi,
    next: SongUi?,
    pageSpacing: Dp,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var slots by remember { mutableStateOf(ArtworkSlots(previous, current, next)) }
    val pagerState = rememberPagerState(initialPage = slots.currentPage) { slots.pages.size }
    // Set while this composable scrolls the pager itself, so only the user's swipes ask for a skip.
    var isSyncing by remember { mutableStateOf(false) }
    val currentOnAction by rememberUpdatedState(onAction)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            // The settled page is re-read because this may run after a sync already moved the pager back.
            if (isSyncing || page != pagerState.settledPage) return@collect
            when (page - slots.currentPage) {
                1 -> currentOnAction(PlayerAction.OnArtworkSwipedToNext)
                -1 -> currentOnAction(PlayerAction.OnArtworkSwipedToPrevious)
            }
        }
    }

    LaunchedEffect(previous, current, next) {
        val newSlots = ArtworkSlots(previous, current, next)
        if (newSlots == slots) return@LaunchedEffect
        isSyncing = true
        try {
            // A skip from the buttons, the notification or auto-advance slides like a swipe would.
            // After a swipe the pager already rests on that page, so this does nothing.
            when (current.id) {
                slots.current.id -> Unit
                slots.next?.id -> pagerState.animateScrollToPage(slots.currentPage + 1)
                slots.previous?.id -> pagerState.animateScrollToPage(slots.currentPage - 1)
            }
            // Swapped together with the jump back to the current page, so both land in the same frame.
            slots = newSlots
            pagerState.scrollToPage(newSlots.currentPage)
        } finally {
            isSyncing = false
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(ArtworkSize),
        pageSpacing = pageSpacing,
        userScrollEnabled = slots.pages.size > 1,
    ) { page ->
        val song = slots.pages.getOrNull(page) ?: return@HorizontalPager
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SongArtwork(imageUri = song.imageUri, modifier = Modifier.size(ArtworkSize))
        }
    }
}

@Composable
private fun TransportControls(
    state: PlayerState,
    song: SongUi,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val durationText = song.durationText
    val positionFormat = stringResource(PresentationR.string.seek_position)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VibeSeekBar(
                progress = {
                    if (song.durationMillis > 0) {
                        state.positionMillis.toFloat() / song.durationMillis
                    } else {
                        0f
                    }
                },
                onSeek = { onAction(PlayerAction.OnSeek(it)) },
                label = { fraction ->
                    val position = (song.durationMillis * fraction).toLong().toDurationText()
                    positionFormat.format(position, durationText)
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.positionMillis.toDurationText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Both side groups share the leftover width equally, so Play stays centred even though
            // the right side holds two buttons.
            Row(modifier = Modifier.weight(1f)) {
                VibeIconButton(
                    icon = VibeIcons.Shuffle,
                    contentDescription = stringResource(R.string.shuffle),
                    onClick = { onAction(PlayerAction.OnShuffleClick) },
                    tint = if (state.isShuffleOn) colorScheme.primary else colorScheme.onSurfaceVariant,
                    iconSize = 20.dp,
                    containerColor = Color.Transparent,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VibeIconButton(
                    icon = VibeIcons.SkipPrevious,
                    contentDescription = stringResource(R.string.previous_song),
                    onClick = { onAction(PlayerAction.OnPreviousClick) },
                    containerSize = 44.dp,
                )
                VibeIconButton(
                    icon = if (state.isPlaying) VibeIcons.Pause else VibeIcons.PlayFilled,
                    contentDescription = stringResource(
                        if (state.isPlaying) PresentationR.string.pause else PresentationR.string.play,
                    ),
                    onClick = { onAction(PlayerAction.OnPlayPauseClick) },
                    tint = colorScheme.surface,
                    containerSize = 60.dp,
                    iconSize = 24.dp,
                    containerColor = colorScheme.onSurface,
                )
                VibeIconButton(
                    icon = VibeIcons.SkipNext,
                    contentDescription = stringResource(PresentationR.string.skip_to_next),
                    onClick = { onAction(PlayerAction.OnNextClick) },
                    containerSize = 44.dp,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                VibeIconButton(
                    icon = when (state.repeatMode) {
                        RepeatMode.Off -> VibeIcons.RepeatOff
                        RepeatMode.All -> VibeIcons.Repeat
                        RepeatMode.One -> VibeIcons.RepeatOne
                    },
                    contentDescription = stringResource(R.string.repeat),
                    onClick = { onAction(PlayerAction.OnRepeatClick) },
                    tint = if (state.repeatMode == RepeatMode.Off) {
                        colorScheme.onSurfaceVariant
                    } else {
                        colorScheme.primary
                    },
                    iconSize = 20.dp,
                    containerColor = Color.Transparent,
                )
                VibeIconButton(
                    icon = VibeIcons.Download,
                    contentDescription = stringResource(R.string.download_card),
                    onClick = { onAction(PlayerAction.OnDownloadClick) },
                    tint = colorScheme.onSurfaceVariant,
                    iconSize = 20.dp,
                    containerColor = Color.Transparent,
                )
            }
        }
    }
}

private val PreviewSong = SongUi(
    id = "505.mp3",
    title = "505",
    artistName = "Arctic Monkeys",
    imageUri = null,
    durationMillis = 254_000,
)

private val PreviewPreviousSong = PreviewSong.copy(id = "do-i-wanna-know.mp3", title = "Do I Wanna Know?")
private val PreviewNextSong = PreviewSong.copy(id = "r-u-mine.mp3", title = "R U Mine?")

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun PlayerScreenPausedPreview() {
    VibePlayerTheme {
        PlayerScreen(
            state = PlayerState(
                song = PreviewSong,
                previousSong = PreviewPreviousSong,
                nextSong = PreviewNextSong,
                repeatMode = RepeatMode.Off,
                isShuffleOn = false
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun PlayerScreenPlayingPreview() {
    VibePlayerTheme {
        PlayerScreen(
            state = PlayerState(
                song = PreviewSong,
                previousSong = PreviewPreviousSong,
                nextSong = PreviewNextSong,
                isPlaying = true,
                positionMillis = 127_000,
                isFavorite = true,
                repeatMode = RepeatMode.One,
            ),
            onAction = {},
        )
    }
}
