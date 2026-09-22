package com.rfcoding.vibeplayer.feature.player.presentation.queue

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.NowPlayingSongCard
import com.rfcoding.vibeplayer.core.designsystem.components.ReorderableSongCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeListItemRow
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.presentation.SheetPreviewSurface
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheet
import com.rfcoding.vibeplayer.feature.player.presentation.R
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val DraggedElevation = 8.dp

/**
 * Hosts [QueueViewModel]; call it inside `DialogSheetScopedViewModel` so each opening of the sheet
 * starts from the player's queue.
 */
@Composable
fun QueueSheetRoot(
    onDismiss: () -> Unit,
    viewModel: QueueViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QueueSheet(state = state, onAction = viewModel::onAction, onDismiss = onDismiss)
}

/**
 * The current song with its play/pause button, then the upcoming songs, which can be dragged by their
 * handle or swiped away, and a fixed row of Shuffle, Repeat and Timer. Timer opens the Sleep timer
 * sheet over this one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    state: QueueState,
    onAction: (QueueAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeBottomSheet(
        onDismiss = onDismiss,
        // Half open, the sheet would hide the controls row below the screen's edge.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        QueueSheetContent(state = state, onAction = onAction)
    }
    if (state.isSleepTimerSheetVisible) {
        SleepTimerSheet(
            onOptionClick = { onAction(QueueAction.OnSleepTimerSelect(it)) },
            onDismiss = { onAction(QueueAction.OnSleepTimerSheetDismiss) },
        )
    }
}

@Composable
internal fun QueueSheetContent(
    state: QueueState,
    onAction: (QueueAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // The now-playing row sits above the songs, so the list's indices are looked up by key.
        val songs = state.upcomingSongs
        val fromIndex = songs.indexOfFirst { it.id == from.key }
        val toIndex = songs.indexOfFirst { it.id == to.key }
        if (fromIndex != -1 && toIndex != -1) onAction(QueueAction.OnMoveSong(fromIndex, toIndex))
    }

    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)) {
        SheetTitle(
            text = stringResource(R.string.queue),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f, fill = false),
            // The sheet keeps 16dp side padding on tablets too, like the playlist action sheet.
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            state.currentSong?.let { song ->
                item(key = NowPlayingKey) {
                    NowPlayingSongCard(
                        title = song.title,
                        artistName = song.artistName,
                        imageUri = song.imageUri,
                        isPlaying = state.isPlaying,
                        onPlayPauseClick = { onAction(QueueAction.OnPlayPauseClick) },
                    )
                }
            }
            if (state.upcomingSongs.isEmpty()) {
                item(key = EmptyKey) {
                    Text(
                        text = stringResource(R.string.no_upcoming_songs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    )
                }
            }
            items(items = state.upcomingSongs, key = { it.id }) { song ->
                ReorderableItem(reorderableState, key = song.id) { isDragging ->
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) DraggedElevation else 0.dp,
                        label = "dragElevation",
                    )
                    SwipeToRemove(onRemove = { onAction(QueueAction.OnSongSwiped(song.id)) }) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shadowElevation = elevation,
                        ) {
                            ReorderableSongCard(
                                title = song.title,
                                artistName = song.artistName,
                                imageUri = song.imageUri,
                                onClick = { onAction(QueueAction.OnUpcomingSongClick(song.id)) },
                                dragHandleModifier = Modifier.draggableHandle(
                                    onDragStopped = { onAction(QueueAction.OnDragStopped) },
                                ),
                            )
                        }
                    }
                }
            }
        }
        QueueControls(state = state, onAction = onAction)
    }
}

/** Swiping the row towards the start removes it; the destructive color and bin show beneath. */
@Composable
private fun SwipeToRemove(
    onRemove: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = { value -> if (value == SwipeToDismissBoxValue.EndToStart) onRemove() },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.extendedColors.buttonDestructive)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = VibeIcons.Bin,
                    contentDescription = stringResource(R.string.remove_from_queue),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) {
        content()
    }
}

/** The Player screen's bare shuffle and repeat buttons, with the Timer beside them. */
@Composable
private fun QueueControls(
    state: QueueState,
    onAction: (QueueAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VibeIconButton(
            icon = VibeIcons.Shuffle,
            contentDescription = stringResource(R.string.shuffle),
            onClick = { onAction(QueueAction.OnShuffleClick) },
            tint = if (state.isShuffleOn) colorScheme.primary else colorScheme.onSurfaceVariant,
            iconSize = 20.dp,
            containerColor = Color.Transparent,
        )
        VibeIconButton(
            icon = when (state.repeatMode) {
                RepeatMode.Off -> VibeIcons.RepeatOff
                RepeatMode.All -> VibeIcons.Repeat
                RepeatMode.One -> VibeIcons.RepeatOne
            },
            contentDescription = stringResource(R.string.repeat),
            onClick = { onAction(QueueAction.OnRepeatClick) },
            tint = if (state.repeatMode == RepeatMode.Off) colorScheme.onSurfaceVariant else colorScheme.primary,
            iconSize = 20.dp,
            containerColor = Color.Transparent,
        )
        VibeIconButton(
            icon = VibeIcons.Timer,
            contentDescription = stringResource(R.string.sleep_timer),
            onClick = { onAction(QueueAction.OnTimerClick) },
            tint = colorScheme.onSurfaceVariant,
            iconSize = 20.dp,
            containerColor = Color.Transparent,
        )
    }
}

/** Opens over the Queue sheet; picking an option replaces any timer already running. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(
    onOptionClick: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit,
) {
    VibeBottomSheet(
        onDismiss = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        SleepTimerSheetContent(onOptionClick = onOptionClick)
    }
}

@Composable
internal fun SleepTimerSheetContent(
    onOptionClick: (SleepTimerOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        SheetTitle(text = stringResource(R.string.sleep_timer), modifier = Modifier.padding(bottom = 8.dp))
        SleepTimerOption.entries.forEach { option ->
            VibeListItemRow(onClick = { onOptionClick(option) }) {
                Text(
                    text = option.label(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun SleepTimerOption.label(): String = when (this) {
    SleepTimerOption.Minutes5 -> pluralStringResource(R.plurals.sleep_timer_minutes, 5, 5)
    SleepTimerOption.Minutes10 -> pluralStringResource(R.plurals.sleep_timer_minutes, 10, 10)
    SleepTimerOption.Minutes15 -> pluralStringResource(R.plurals.sleep_timer_minutes, 15, 15)
    SleepTimerOption.Minutes30 -> pluralStringResource(R.plurals.sleep_timer_minutes, 30, 30)
    SleepTimerOption.Minutes45 -> pluralStringResource(R.plurals.sleep_timer_minutes, 45, 45)
    SleepTimerOption.Hour1 -> stringResource(R.string.sleep_timer_hour)
    SleepTimerOption.EndOfTrack -> stringResource(R.string.sleep_timer_end_of_track)
}

private const val NowPlayingKey = "now_playing"
private const val EmptyKey = "empty"

private val PreviewSongs = listOf(
    SongUi(id = "1", title = "Midnight Drive", artistName = "The Night Owls", imageUri = null, durationMillis = 225_000),
    SongUi(id = "2", title = "Neon Rain", artistName = "Synthwave Co.", imageUri = null, durationMillis = 198_000),
    SongUi(id = "3", title = "Slow Burn", artistName = null, imageUri = null, durationMillis = 240_000),
)

// ModalBottomSheet renders in its own window and doesn't show up in previews, so the content is
// previewed directly: full width for mobile, and at the 480dp the tablet sheet is capped to.
@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun QueueSheetPreview() {
    SheetPreviewSurface {
        QueueSheetContent(
            state = QueueState(
                currentSong = PreviewSongs.first(),
                isPlaying = true,
                upcomingSongs = PreviewSongs.drop(1),
                isShuffleOn = true,
                repeatMode = RepeatMode.All,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Nothing up next", widthDp = 412)
@Composable
private fun QueueSheetEmptyPreview() {
    SheetPreviewSurface {
        QueueSheetContent(state = QueueState(currentSong = PreviewSongs.first()), onAction = {})
    }
}

@Preview(name = "Sleep timer", widthDp = 412)
@Composable
private fun SleepTimerSheetPreview() {
    SheetPreviewSurface {
        SleepTimerSheetContent(onOptionClick = {})
    }
}
