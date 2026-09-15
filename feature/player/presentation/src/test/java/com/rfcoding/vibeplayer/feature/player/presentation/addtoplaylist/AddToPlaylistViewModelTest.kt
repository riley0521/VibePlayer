package com.rfcoding.vibeplayer.feature.player.presentation.addtoplaylist

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.presentation.UiText
import com.rfcoding.vibeplayer.core.presentation.toPlaylistUi
import com.rfcoding.vibeplayer.core.testing.FakePlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.testing.FakeSongLocalDataSource
import com.rfcoding.vibeplayer.core.testing.playlist
import com.rfcoding.vibeplayer.core.testing.song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.rfcoding.vibeplayer.core.presentation.R as PresentationR

@OptIn(ExperimentalCoroutinesApi::class)
class AddToPlaylistViewModelTest {

    private lateinit var playlistDataSource: FakePlaylistLocalDataSource
    private lateinit var songDataSource: FakeSongLocalDataSource

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        playlistDataSource = FakePlaylistLocalDataSource()
        songDataSource = FakeSongLocalDataSource()
        songDataSource.songsMutable.value = listOf(song("a"), song("b", isFavorite = true), song("c", isFavorite = true))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(songId: String = "a") =
        AddToPlaylistViewModel(songId, playlistDataSource, songDataSource)

    @Test
    fun `state lists the playlists and counts the favourites`() = runTest {
        val playlists = listOf(playlist(id = 2, name = "Friday Chill"), playlist(id = 1, name = "Workout"))
        playlistDataSource.playlists.value = playlists

        val state = createViewModel().state.value

        assertThat(state.favouriteSongCount).isEqualTo(2)
        assertThat(state.playlists).containsExactly(*playlists.map { it.toPlaylistUi() }.toTypedArray())
    }

    @Test
    fun `playlist click adds the song and reports the playlist's name`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 2, name = "Friday Chill"))
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AddToPlaylistAction.OnPlaylistClick(playlistId = 2))

            val event = awaitItem() as AddToPlaylistEvent.AddedToPlaylist
            assertThat((event.playlistName as UiText.DynamicString).value).isEqualTo("Friday Chill")
        }
        assertThat(playlistDataSource.addedSongIds[2L]).isEqualTo(listOf("a"))
    }

    @Test
    fun `favourites click marks the song as a favourite`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AddToPlaylistAction.OnFavouritesClick)

            val event = awaitItem() as AddToPlaylistEvent.AddedToPlaylist
            assertThat((event.playlistName as UiText.StringResource).id).isEqualTo(PresentationR.string.favourites)
        }
        assertThat(songDataSource.songsMutable.value.first { it.id == "a" }.isFavorite).isTrue()
        assertThat(viewModel.state.value.favouriteSongCount).isEqualTo(3)
    }

    @Test
    fun `a failed playlist write sends an error`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 2))
        playlistDataSource.error = DataError.Local.DISK_FULL
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AddToPlaylistAction.OnPlaylistClick(playlistId = 2))

            assertThat(awaitItem()).isInstanceOf<AddToPlaylistEvent.Error>()
        }
    }

    @Test
    fun `a failed favourite write sends an error`() = runTest {
        songDataSource.setFavoriteError = DataError.Local.UNKNOWN
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AddToPlaylistAction.OnFavouritesClick)

            assertThat(awaitItem()).isInstanceOf<AddToPlaylistEvent.Error>()
        }
    }

    @Test
    fun `clicking a playlist that no longer exists does nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AddToPlaylistAction.OnPlaylistClick(playlistId = 9))

        assertThat(playlistDataSource.addedSongIds[9L]).isNull()
    }
}
