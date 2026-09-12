package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.SongCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.feature.library.presentation.R

/**
 * Figma "Main Page - Songs Tab": the Shuffle/Play header scrolls away with the list, while the
 * top bar and the tabs above stay pinned.
 */
@Composable
internal fun LibrarySongsTab(
    songs: List<SongUi>,
    listState: LazyListState,
    isMobile: Boolean,
    bottomContentPadding: Dp,
    onAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = if (isMobile) 16.dp else 24.dp,
            end = if (isMobile) 16.dp else 24.dp,
            bottom = bottomContentPadding,
        ),
    ) {
        item(key = "header") {
            SongsHeader(
                songCount = songs.size,
                isMobile = isMobile,
                onShuffleClick = { onAction(LibraryAction.OnShuffleClick) },
                onPlayClick = { onAction(LibraryAction.OnPlayClick) },
            )
        }
        items(items = songs, key = { it.id }) { song ->
            SongCard(
                title = song.title,
                artistName = song.artistName,
                duration = song.durationText,
                imageUri = song.imageUri,
                onClick = { onAction(LibraryAction.OnSongClick(song.id)) },
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

@Composable
private fun ShuffleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    VibeButton(
        text = stringResource(R.string.shuffle),
        onClick = onClick,
        style = VibeButtonStyle.Outlined,
        leadingIcon = VibeIcons.Shuffle,
        modifier = modifier,
    )
}

@Composable
private fun PlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    VibeButton(
        text = stringResource(R.string.play),
        onClick = onClick,
        style = VibeButtonStyle.Outlined,
        leadingIcon = VibeIcons.PlayLinear,
        modifier = modifier,
    )
}

@Composable
private fun SongCountText(songCount: Int, modifier: Modifier = Modifier) {
    Text(
        text = pluralStringResource(R.plurals.songs_count, songCount, songCount),
        style = MaterialTheme.typography.bodyLargeMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
