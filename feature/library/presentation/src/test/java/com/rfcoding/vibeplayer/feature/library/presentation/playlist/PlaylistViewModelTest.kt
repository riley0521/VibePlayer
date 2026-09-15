package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.single
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.presentation.playlistname.PlaylistNameMode
import com.rfcoding.vibeplayer.core.presentation.toPlaylistUi
import com.rfcoding.vibeplayer.core.testing.FakeMusicPlayer
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

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var playlistDataSource: FakePlaylistLocalDataSource
    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var musicPlayer: FakeMusicPlayer

    private val chill = playlist(id = 2, name = "Chill", songs = listOf(song("b"), song("c")))
    private val empty = playlist(id = 1, name = "Empty")

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        savedStateHandle = SavedStateHandle()
        playlistDataSource = FakePlaylistLocalDataSource()
        songDataSource = FakeSongLocalDataSource()
        musicPlayer = FakeMusicPlayer()

        playlistDataSource.playlists.value = listOf(chill, empty)
        songDataSource.songsMutable.value = listOf(song("a", isFavorite = true), song("b"), song("c", isFavorite = true))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = PlaylistViewModel(savedStateHandle, playlistDataSource, songDataSource, musicPlayer)

    @Test
    fun `state shows the playlists and the favourite count, and follows changes`() {
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.playlists).containsExactly(chill.toPlaylistUi(), empty.toPlaylistUi())
        assertThat(viewModel.state.value.favouriteSongCount).isEqualTo(2)

        playlistDataSource.playlists.value = listOf(empty)
        songDataSource.songsMutable.value = emptyList()

        assertThat(viewModel.state.value.playlists).containsExactly(empty.toPlaylistUi())
        assertThat(viewModel.state.value.favouriteSongCount).isEqualTo(0)
    }

    @Test
    fun `create opens the name sheet in create mode`() {
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistAction.OnCreatePlaylistClick)

        assertThat(viewModel.state.value.activeSheet).isEqualTo(PlaylistSheet.PlaylistName(PlaylistNameMode.Create))
    }

    @Test
    fun `the favourites menu opens the action sheet for favourites`() {
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistAction.OnFavouritesMenuClick)

        assertThat(viewModel.state.value.activeSheet)
            .isEqualTo(PlaylistSheet.PlaylistActions(playlist = null, favouriteSongCount = 2))
    }

    @Test
    fun `a playlist menu opens the action sheet for that playlist`() {
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistAction.OnPlaylistMenuClick(chill.id))

        assertThat(viewModel.state.value.activeSheet)
            .isEqualTo(PlaylistSheet.PlaylistActions(playlist = chill.toPlaylistUi()))
    }

    @Test
    fun `rename opens the name sheet with the playlist's current name`() {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistAction.OnPlaylistMenuClick(chill.id))

        viewModel.onAction(PlaylistAction.OnRenamePlaylistClick(chill.id))

        assertThat(viewModel.state.value.activeSheet)
            .isEqualTo(PlaylistSheet.PlaylistName(PlaylistNameMode.Rename(chill.id, "Chill")))
    }

    @Test
    fun `delete asks for confirmation first`() {
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistAction.OnDeletePlaylistClick(chill.id))

        assertThat(viewModel.state.value.activeSheet).isEqualTo(PlaylistSheet.DeletePlaylist(chill.toPlaylistUi()))
        assertThat(playlistDataSource.playlists.value).containsExactly(chill, empty)
    }

    @Test
    fun `dismissing closes the sheet`() {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistAction.OnFavouritesMenuClick)

        viewModel.onAction(PlaylistAction.OnSheetDismiss)

        assertThat(viewModel.state.value.activeSheet).isNull()
    }

    @Test
    fun `playing favourites queues the favourite songs and closes the sheet`() {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistAction.OnFavouritesMenuClick)

        viewModel.onAction(PlaylistAction.OnPlayFavouritesClick)

        assertThat(musicPlayer.playedQueues).single().containsExactly(song("a", isFavorite = true), song("c", isFavorite = true))
        assertThat(viewModel.state.value.activeSheet).isNull()
    }

    @Test
    fun `playing a playlist queues its songs and closes the sheet`() {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistAction.OnPlaylistMenuClick(chill.id))

        viewModel.onAction(PlaylistAction.OnPlayPlaylistClick(chill.id))

        assertThat(musicPlayer.playedQueues).single().containsExactly(song("b"), song("c"))
        assertThat(viewModel.state.value.activeSheet).isNull()
    }

    @Test
    fun `playing an empty playlist plays nothing`() {
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistAction.OnPlayPlaylistClick(empty.id))

        assertThat(musicPlayer.playedQueues).isEmpty()
    }

    @Test
    fun `confirming a delete deletes the playlist and closes the sheet`() {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistAction.OnDeletePlaylistClick(chill.id))

        viewModel.onAction(PlaylistAction.OnConfirmDeletePlaylistClick(chill.id))

        assertThat(playlistDataSource.playlists.value).containsExactly(empty)
        assertThat(viewModel.state.value.activeSheet).isNull()
    }

    @Test
    fun `a failed delete sends an error`() = runTest {
        playlistDataSource.error = DataError.Local.UNKNOWN
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(PlaylistAction.OnConfirmDeletePlaylistClick(chill.id))

            assertThat(awaitItem()).isInstanceOf<PlaylistEvent.Error>()
        }
    }

    @Test
    fun `change cover closes the sheet, opens the picker and saves the picked image`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistAction.OnPlaylistMenuClick(chill.id))

        viewModel.events.test {
            viewModel.onAction(PlaylistAction.OnChangePlaylistCoverClick(chill.id))

            assertThat(awaitItem()).isEqualTo(PlaylistEvent.LaunchCoverPicker)
        }
        assertThat(viewModel.state.value.activeSheet).isNull()

        viewModel.onAction(PlaylistAction.OnCoverPicked("content://cover"))

        assertThat(playlistDataSource.playlists.value.first { it.id == chill.id }.coverUri).isEqualTo("content://cover")
        assertThat(viewModel.state.value.playlists.first { it.id == chill.id }.imageUri).isEqualTo("content://cover")
    }

    @Test
    fun `closing the picker without an image saves nothing`() = runTest {
        val viewModel = createViewModel()
        viewModel.events.test {
            viewModel.onAction(PlaylistAction.OnChangePlaylistCoverClick(chill.id))
            awaitItem()
        }

        viewModel.onAction(PlaylistAction.OnCoverPicked(null))

        assertThat(playlistDataSource.playlists.value).containsExactly(chill, empty)
    }

    @Test
    fun `a picked cover still lands on its playlist after process death`() {
        savedStateHandle["coverPlaylistId"] = chill.id
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistAction.OnCoverPicked("content://cover"))

        assertThat(playlistDataSource.playlists.value.first { it.id == chill.id }.coverUri).isEqualTo("content://cover")
    }
}
