package com.rfcoding.vibeplayer.feature.library.presentation.addsongs

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.testing.FakePlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.testing.FakeSongLocalDataSource
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
class AddSongsViewModelTest {

    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var playlistDataSource: FakePlaylistLocalDataSource
    private lateinit var savedStateHandle: SavedStateHandle

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // The fake titles are "Song a", "Song b", …, so a query of the id narrows to one song.
        songDataSource = FakeSongLocalDataSource().apply {
            songsMutable.value = listOf(song("a"), song("b"), song("c"))
        }
        playlistDataSource = FakePlaylistLocalDataSource()
        savedStateHandle = SavedStateHandle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AddSongsViewModel(
        playlistId = PLAYLIST_ID,
        savedStateHandle = savedStateHandle,
        songDataSource = songDataSource,
        playlistDataSource = playlistDataSource,
    )

    private fun AddSongsViewModel.visibleIds() = state.value.songs.map { it.id }

    @Test
    fun `an empty query shows every song`() {
        assertThat(createViewModel().visibleIds()).containsExactly("a", "b", "c")
    }

    @Test
    fun `typing a query filters the songs and clearing it shows them all again`() {
        val viewModel = createViewModel()

        viewModel.onAction(AddSongsAction.OnQueryChange("song b"))
        assertThat(viewModel.visibleIds()).containsExactly("b")
        assertThat(viewModel.state.value.query).isEqualTo("song b")

        viewModel.onAction(AddSongsAction.OnClearQueryClick)
        assertThat(viewModel.visibleIds()).containsExactly("a", "b", "c")
        assertThat(viewModel.state.value.query).isEqualTo("")
    }

    @Test
    fun `ticking and unticking a song updates the selection`() {
        val viewModel = createViewModel()

        viewModel.onAction(AddSongsAction.OnSongSelectedChange("a", selected = true))
        viewModel.onAction(AddSongsAction.OnSongSelectedChange("c", selected = true))
        viewModel.onAction(AddSongsAction.OnSongSelectedChange("a", selected = false))

        assertThat(viewModel.state.value.selectedSongIds).containsOnly("c")
    }

    @Test
    fun `select all ticks only the visible songs and keeps hidden ticks`() {
        val viewModel = createViewModel()
        viewModel.onAction(AddSongsAction.OnSongSelectedChange("a", selected = true))
        viewModel.onAction(AddSongsAction.OnQueryChange("song b"))

        viewModel.onAction(AddSongsAction.OnSelectAllChange(selected = true))

        assertThat(viewModel.state.value.selectedSongIds).containsOnly("a", "b")
    }

    @Test
    fun `unchecking select all unticks only the visible songs`() {
        val viewModel = createViewModel()
        viewModel.onAction(AddSongsAction.OnSelectAllChange(selected = true))
        viewModel.onAction(AddSongsAction.OnQueryChange("song b"))

        viewModel.onAction(AddSongsAction.OnSelectAllChange(selected = false))

        assertThat(viewModel.state.value.selectedSongIds).containsOnly("a", "c")
    }

    @Test
    fun `the query and the selection are restored after process death`() {
        savedStateHandle["query"] = "song c"
        savedStateHandle["selectedSongIds"] = arrayListOf("a", "c")

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.query).isEqualTo("song c")
        assertThat(viewModel.visibleIds()).containsExactly("c")
        assertThat(viewModel.state.value.selectedSongIds).containsOnly("a", "c")
    }

    @Test
    fun `changes are written to the saved state`() {
        val viewModel = createViewModel()

        viewModel.onAction(AddSongsAction.OnQueryChange("song"))
        viewModel.onAction(AddSongsAction.OnSongSelectedChange("b", selected = true))

        assertThat(savedStateHandle.get<String>("query")).isEqualTo("song")
        assertThat(savedStateHandle.get<ArrayList<String>>("selectedSongIds")!!).containsExactly("b")
    }

    @Test
    fun `OK adds the selected songs in library order and reports success`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(AddSongsAction.OnSongSelectedChange("c", selected = true))
        viewModel.onAction(AddSongsAction.OnSongSelectedChange("a", selected = true))

        viewModel.events.test {
            viewModel.onAction(AddSongsAction.OnOkClick)

            assertThat(awaitItem()).isEqualTo(AddSongsEvent.SongsAdded)
        }
        assertThat(playlistDataSource.addedSongIds[PLAYLIST_ID]!!).containsExactly("a", "c")
    }

    @Test
    fun `OK skips selected songs that no longer exist`() = runTest {
        savedStateHandle["selectedSongIds"] = arrayListOf("a", "gone")
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AddSongsAction.OnOkClick)

            assertThat(awaitItem()).isEqualTo(AddSongsEvent.SongsAdded)
        }
        assertThat(playlistDataSource.addedSongIds[PLAYLIST_ID]!!).containsExactly("a")
    }

    @Test
    fun `a failed save sends an error and lets the user try again`() = runTest {
        playlistDataSource.error = DataError.Local.DISK_FULL
        val viewModel = createViewModel()
        viewModel.onAction(AddSongsAction.OnSongSelectedChange("a", selected = true))

        viewModel.events.test {
            viewModel.onAction(AddSongsAction.OnOkClick)

            assertThat(awaitItem()).isInstanceOf<AddSongsEvent.Error>()
        }
        assertThat(viewModel.state.value.isSaving).isFalse()
        assertThat(playlistDataSource.addedSongIds).isEmpty()
    }

    @Test
    fun `OK does nothing without a selection`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AddSongsAction.OnOkClick)

            expectNoEvents()
        }
        assertThat(playlistDataSource.addedSongIds).isEmpty()
    }

    private companion object {
        const val PLAYLIST_ID = 7L
    }
}
