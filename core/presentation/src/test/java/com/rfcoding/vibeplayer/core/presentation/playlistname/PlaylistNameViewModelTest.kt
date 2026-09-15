package com.rfcoding.vibeplayer.core.presentation.playlistname

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.presentation.fakes.FakePlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.presentation.fakes.playlist
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
class PlaylistNameViewModelTest {

    private lateinit var playlistDataSource: FakePlaylistLocalDataSource
    private lateinit var savedStateHandle: SavedStateHandle

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        playlistDataSource = FakePlaylistLocalDataSource()
        savedStateHandle = SavedStateHandle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(mode: PlaylistNameMode = PlaylistNameMode.Create) =
        PlaylistNameViewModel(mode, savedStateHandle, playlistDataSource)

    @Test
    fun `create starts with an empty name`() {
        assertThat(createViewModel().state.value.name).isEqualTo("")
    }

    @Test
    fun `rename starts with the playlist's current name`() {
        val viewModel = createViewModel(PlaylistNameMode.Rename(playlistId = 1, currentName = "Chill"))

        assertThat(viewModel.state.value.name).isEqualTo("Chill")
    }

    @Test
    fun `a name typed before process death is restored`() {
        savedStateHandle["name"] = "Half typed"

        val viewModel = createViewModel(PlaylistNameMode.Rename(playlistId = 1, currentName = "Chill"))

        assertThat(viewModel.state.value.name).isEqualTo("Half typed")
    }

    @Test
    fun `typing updates the state and the saved state`() {
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistNameAction.OnNameChange("Road trip"))

        assertThat(viewModel.state.value.name).isEqualTo("Road trip")
        assertThat(savedStateHandle.get<String>("name")).isEqualTo("Road trip")
    }

    @Test
    fun `create saves the trimmed name and reports the new id and name`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistNameAction.OnNameChange("  Road trip  "))

        viewModel.events.test {
            viewModel.onAction(PlaylistNameAction.OnConfirmClick)

            assertThat(awaitItem()).isEqualTo(PlaylistNameEvent.PlaylistCreated(playlistId = 1, name = "Road trip"))
        }
        assertThat(playlistDataSource.playlists.value.map { it.name }).containsExactly("Road trip")
    }

    @Test
    fun `rename saves the trimmed name and reports it`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 7, name = "Chill"))
        val viewModel = createViewModel(PlaylistNameMode.Rename(playlistId = 7, currentName = "Chill"))
        viewModel.onAction(PlaylistNameAction.OnNameChange(" Sunday Chill "))

        viewModel.events.test {
            viewModel.onAction(PlaylistNameAction.OnConfirmClick)

            assertThat(awaitItem()).isEqualTo(PlaylistNameEvent.PlaylistRenamed)
        }
        assertThat(playlistDataSource.playlists.value.single().name).isEqualTo("Sunday Chill")
    }

    @Test
    fun `a failed save sends an error and lets the user try again`() = runTest {
        playlistDataSource.error = DataError.Local.DISK_FULL
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistNameAction.OnNameChange("Road trip"))

        viewModel.events.test {
            viewModel.onAction(PlaylistNameAction.OnConfirmClick)

            assertThat(awaitItem()).isInstanceOf<PlaylistNameEvent.Error>()
        }
        assertThat(viewModel.state.value.canConfirm).isTrue()
    }

    @Test
    fun `a blank name sends an error and saves nothing`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistNameAction.OnNameChange("   "))

        viewModel.events.test {
            viewModel.onAction(PlaylistNameAction.OnConfirmClick)

            assertThat(awaitItem()).isInstanceOf<PlaylistNameEvent.Error>()
        }
        assertThat(playlistDataSource.playlists.value).isEmpty()
    }
}
