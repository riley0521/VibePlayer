package com.rfcoding.vibeplayer.feature.library.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.SongCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeSearchTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.bottomFade
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.R

private val MobileTopBarPadding = PaddingValues(start = 16.dp, end = 8.dp)
private val TabletTopBarPadding = PaddingValues(start = 24.dp, end = 16.dp)

/** Figma centers the no-result message in a column this wide, even on a tablet. */
private val EmptyResultMaxWidth = 412.dp

/**
 * Figma "Main Page + Search". Unlike the mockup, an empty query lists every song rather than nothing,
 * because the requirements call for the default state to show all music.
 */
@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMobile = currentDeviceConfiguration().isMobile

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VibeSearchTopBar(
                query = state.query,
                onQueryChange = { onAction(SearchAction.OnQueryChange(it)) },
                onClearClick = { onAction(SearchAction.OnClearClick) },
                onCancelClick = { onAction(SearchAction.OnCancelClick) },
                placeholder = stringResource(R.string.search),
                cancelText = stringResource(R.string.cancel),
                contentPadding = if (isMobile) MobileTopBarPadding else TabletTopBarPadding,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.isEmptyResult) {
                Text(
                    text = stringResource(R.string.no_results_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = EmptyResultMaxWidth)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else {
                LazyColumn(
                    // Figma's "Rectangle 5", on the results list only.
                    modifier = Modifier
                        .fillMaxSize()
                        .bottomFade(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(horizontal = if (isMobile) 16.dp else 24.dp),
                ) {
                    items(items = state.songs, key = { it.id }) { song ->
                        SongCard(
                            title = song.title,
                            artistName = song.artistName,
                            duration = song.durationText,
                            imageUri = song.imageUri,
                            onClick = { onAction(SearchAction.OnSongClick(song.id)) },
                        )
                    }
                }
            }
        }
    }
}

private val PreviewSongs = listOf(
    SongUi("les-passants.mp3", "Les passants", "Zaz", null, 213_000),
    SongUi("the-less-i-know-the-better.mp3", "The Less I Know The Better", "Tame Impala", null, 317_000),
    SongUi("less-than-zero.mp3", "Less Than Zero", "The Weeknd", null, 211_000),
    SongUi("505.mp3", "505", "Arctic Monkeys", null, 254_000),
    SongUi("mr-brightside.mp3", "Mr. Brightside", "The Killers", null, 222_000),
    SongUi("last-nite.mp3", "Last Nite", "The Strokes", null, 192_000),
)

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun SearchScreenDefaultPreview() {
    VibePlayerTheme {
        SearchScreen(state = SearchState(songs = PreviewSongs), onAction = {})
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun SearchScreenWithResultPreview() {
    VibePlayerTheme {
        SearchScreen(
            state = SearchState(query = "Les", songs = PreviewSongs.take(3)),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun SearchScreenNoResultPreview() {
    VibePlayerTheme {
        SearchScreen(state = SearchState(query = "knw"), onAction = {})
    }
}
