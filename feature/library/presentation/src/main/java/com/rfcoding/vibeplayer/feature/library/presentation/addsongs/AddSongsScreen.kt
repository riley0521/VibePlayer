package com.rfcoding.vibeplayer.feature.library.presentation.addsongs

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.SelectableSongCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeInnerTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeSearchField
import com.rfcoding.vibeplayer.core.designsystem.components.bottomFade
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.feature.library.presentation.R
import com.rfcoding.vibeplayer.feature.library.presentation.components.BottomActionButton
import com.rfcoding.vibeplayer.feature.library.presentation.components.SelectAllRow
import com.rfcoding.vibeplayer.feature.library.presentation.components.TabletBottomActionButtonWidth
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

// VibeInnerTopBar already pads itself to Figma's mobile 10dp; tablets add the missing 8dp.
private val TabletTopBarPadding = 8.dp

@Composable
fun AddSongsRoot(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddSongsViewModel = koinViewModel { parametersOf(playlistId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AddSongsEvent.SongsAdded -> onNavigateBack()
            is AddSongsEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG).show()
            }
        }
    }

    AddSongsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                AddSongsAction.OnBackClick -> onNavigateBack()
                else -> viewModel.onAction(action)
            }
        },
    )
}

/**
 * Figma "Add songs to playlist page". The top bar's title doubles as the selection count, and the OK
 * button only appears once something is ticked. Unlike the body, the search row keeps its 16dp side
 * padding on tablets.
 */
@Composable
fun AddSongsScreen(
    state: AddSongsState,
    onAction: (AddSongsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMobile = currentDeviceConfiguration().isMobile

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                VibeInnerTopBar(
                    title = if (state.hasSelection) {
                        stringResource(R.string.selected_count, state.selectedCount)
                    } else {
                        stringResource(R.string.add_songs)
                    },
                    onBackClick = { onAction(AddSongsAction.OnBackClick) },
                    modifier = if (isMobile) {
                        Modifier
                    } else {
                        Modifier.padding(horizontal = TabletTopBarPadding)
                    },
                )
                VibeSearchField(
                    query = state.query,
                    onQueryChange = { onAction(AddSongsAction.OnQueryChange(it)) },
                    placeholder = stringResource(R.string.search),
                    onClearClick = { onAction(AddSongsAction.OnClearQueryClick) },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
        },
        bottomBar = {
            if (state.hasSelection) {
                BottomActionButton(
                    text = stringResource(R.string.ok),
                    onClick = { onAction(AddSongsAction.OnOkClick) },
                    maxWidth = if (isMobile) Dp.Unspecified else TabletBottomActionButtonWidth,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Figma's "Rectangle 5", easing the list into the background.
                .bottomFade(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = if (isMobile) 16.dp else 24.dp),
        ) {
            item(key = "selectAll") {
                SelectAllRow(
                    selected = state.isAllSelected,
                    onSelectedChange = { onAction(AddSongsAction.OnSelectAllChange(it)) },
                )
            }
            items(items = state.songs, key = { it.id }) { song ->
                SelectableSongCard(
                    title = song.title,
                    artistName = song.artistName,
                    duration = song.durationText,
                    imageUri = song.imageUri,
                    selected = song.id in state.selectedSongIds,
                    onSelectedChange = { onAction(AddSongsAction.OnSongSelectedChange(song.id, it)) },
                )
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
    SongUi("take-me-out.mp3", "Take Me Out", "Franz Ferdinand", null, 237_000),
    SongUi("house-of-the-rising-sun.mp3", "House of the Rising Sun", null, null, 269_000),
)

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun AddSongsScreenNoneSelectedPreview() {
    VibePlayerTheme {
        AddSongsScreen(state = AddSongsState(songs = PreviewSongs), onAction = {})
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun AddSongsScreenSomeSelectedPreview() {
    VibePlayerTheme {
        AddSongsScreen(
            state = AddSongsState(
                songs = PreviewSongs,
                selectedSongIds = PreviewSongs.take(4).map { it.id }.toSet(),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun AddSongsScreenSearchResultPreview() {
    VibePlayerTheme {
        AddSongsScreen(
            state = AddSongsState(query = "Les", songs = PreviewSongs.take(3)),
            onAction = {},
        )
    }
}
