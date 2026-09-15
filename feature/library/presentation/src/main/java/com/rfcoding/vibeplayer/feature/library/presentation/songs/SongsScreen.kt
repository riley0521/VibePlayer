package com.rfcoding.vibeplayer.feature.library.presentation.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.SongCard
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.MiniPlayerHeight
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.components.PlayButton
import com.rfcoding.vibeplayer.feature.library.presentation.components.ShuffleButton
import com.rfcoding.vibeplayer.feature.library.presentation.components.SongCountText
import org.koin.androidx.compose.koinViewModel

/**
 * [listState] comes from LibraryRoot, which drives the scroll-to-top FAB with it and keeps the scroll
 * position while the user is on the Playlist tab.
 */
@Composable
internal fun SongsRoot(
    listState: LazyListState,
    viewModel: SongsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SongsScreen(
        state = state,
        listState = listState,
        onAction = viewModel::onAction
    )
}

/**
 * Figma "Main Page - Songs Tab": the Shuffle/Play header scrolls away with the list, while the
 * top bar and the tabs above stay pinned.
 */
@Composable
internal fun SongsScreen(
    state: SongsState,
    listState: LazyListState,
    onAction: (SongsAction) -> Unit
) {
    val isMobile = currentDeviceConfiguration().isMobile
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + MiniPlayerHeight

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = if (isMobile) 16.dp else 24.dp,
            end = if (isMobile) 16.dp else 24.dp,
            bottom = bottomContentPadding,
        ),
    ) {
        item(key = "header") {
            SongsHeader(
                songCount = state.songs.size,
                isMobile = isMobile,
                onShuffleClick = { onAction(SongsAction.OnShuffleClick) },
                onPlayClick = { onAction(SongsAction.OnPlayClick) },
            )
        }
        items(items = state.songs, key = { it.id }) { song ->
            SongCard(
                title = song.title,
                artistName = song.artistName,
                duration = song.durationText,
                imageUri = song.imageUri,
                onClick = { onAction(SongsAction.OnSongClick(song.id)) },
            )
        }
    }
}

@Composable
private fun SongsHeader(
    songCount: Int,
    isMobile: Boolean,
    onShuffleClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val headerModifier = modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 8.dp)

    if (isMobile) {
        // Mobile stacks: the two buttons share the row and the count wraps onto its own line.
        Column(
            modifier = headerModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShuffleButton(onClick = onShuffleClick, modifier = Modifier.weight(1f))
                PlayButton(onClick = onPlayClick, modifier = Modifier.weight(1f))
            }
            SongCountText(songCount = songCount)
        }
    } else {
        Row(
            modifier = headerModifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShuffleButton(onClick = onShuffleClick)
            PlayButton(onClick = onPlayClick)
            Spacer(modifier = Modifier.weight(1f))
            SongCountText(songCount = songCount)
        }
    }
}

internal val PreviewSongs = listOf(
    SongUi("505.mp3", "505", "Arctic Monkeys", null, 254_000),
    SongUi("do-i-wanna-know.mp3", "Do I Wanna Know?", "Arctic Monkeys", null, 272_000),
    SongUi("mr-brightside.mp3", "Mr. Brightside", "The Killers", null, 222_000),
    SongUi("somebody-told-me.mp3", "Somebody Told Me", "The Killers", null, 199_000),
    SongUi("take-me-out.mp3", "Take Me Out", "Franz Ferdinand", null, 237_000),
    SongUi("last-nite.mp3", "Last Nite", "The Strokes", null, 192_000),
    SongUi("house-of-the-rising-sun.mp3", "House of the Rising Sun", null, null, 269_000),
    SongUi("house-of-the-rising-sun1.mp3", "House of the Rising Sun", null, null, 269_000),
    SongUi("house-of-the-rising-sun2.mp3", "House of the Rising Sun", null, null, 269_000),
    SongUi("house-of-the-rising-sun3.mp3", "House of the Rising Sun", null, null, 269_000),
    SongUi("house-of-the-rising-sun4.mp3", "House of the Rising Sun", null, null, 269_000),
)

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun SongsScreenPreview() {
    VibePlayerTheme {
        SongsScreen(
            state = SongsState(songs = PreviewSongs),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}
