package com.rfcoding.vibeplayer.feature.library.presentation.editplaylist

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.testing.FakePlaylistLocalDataSource
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
class EditPlaylistViewModelTest {

    private lateinit var playlistDataSource: FakePlaylistLocalDataSource
    private lateinit var savedStateHandle: SavedStateHandle

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        playlistDataSource = FakePlaylistLocalDataSource().apply {
            playlists.value = listOf(
                playlist(PLAYLIST_ID, songs = listOf(song("a"), song("b"), song("c"))),
            )
        }
        savedStateHandle = SavedStateHandle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = EditPlaylistViewModel(
        playlistId = PLAYLIST_ID,
        savedStateHandle = savedStateHandle,
        playlistDataSource = playlistDataSource,
    )

    private fun EditPlaylistViewModel.songIds() = state.value.songs.map { it.id }

    @Test
    fun `the screen opens on the playlist's stored order with nothing to save`() {
        val viewModel = createViewModel()

        assertThat(viewModel.songIds()).containsExactly("a", "b", "c")
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.hasChanges).isFalse()
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `dragging a song to a new place reorders the list`() {
        val viewModel = createViewModel()

        viewModel.onAction(EditPlaylistAction.OnMoveSong(from = 2, to = 0))

        assertThat(viewModel.songIds()).containsExactly("c", "a", "b")
        assertThat(viewModel.state.value.canSave).isTrue()
    }

    @Test
    fun `moving a song back where it started leaves nothing to save`() {
        val viewModel = createViewModel()

        viewModel.onAction(EditPlaylistAction.OnMoveSong(from = 0, to = 2))
        viewModel.onAction(EditPlaylistAction.OnMoveSong(from = 2, to = 0))

        assertThat(viewModel.songIds()).containsExactly("a", "b", "c")
        assertThat(viewModel.state.value.hasChanges).isFalse()
    }

    @Test
    fun `a move outside the list is ignored`() {
        val viewModel = createViewModel()

        viewModel.onAction(EditPlaylistAction.OnMoveSong(from = 0, to = 9))

        assertThat(viewModel.songIds()).containsExactly("a", "b", "c")
        assertThat(viewModel.state.value.hasChanges).isFalse()
    }

    @Test
    fun `the X drops a song from the list without touching the playlist`() {
        val viewModel = createViewModel()

        viewModel.onAction(EditPlaylistAction.OnRemoveSongClick("b"))

        assertThat(viewModel.songIds()).containsExactly("a", "c")
        assertThat(viewModel.state.value.hasChanges).isTrue()
        assertThat(playlistDataSource.playlists.value.first().songs.map { it.id })
            .containsExactly("a", "b", "c")
    }

    @Test
    fun `Save writes the working order and leaves the screen`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(EditPlaylistAction.OnRemoveSongClick("a"))
        viewModel.onAction(EditPlaylistAction.OnMoveSong(from = 1, to = 0))

        viewModel.events.test {
            viewModel.onAction(EditPlaylistAction.OnSaveClick)

            assertThat(awaitItem()).isEqualTo(EditPlaylistEvent.NavigateBack)
            assertThat(playlistDataSource.setSongIds[PLAYLIST_ID]).isNotNull().containsExactly("c", "b")
            assertThat(viewModel.state.value.isSaving).isFalse()
        }
    }

    @Test
    fun `a failed save keeps the screen open and reports the error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(EditPlaylistAction.OnRemoveSongClick("a"))
        playlistDataSource.error = DataError.Local.DISK_FULL

        viewModel.events.test {
            viewModel.onAction(EditPlaylistAction.OnSaveClick)

            assertThat(awaitItem()).isInstanceOf<EditPlaylistEvent.Error>()
            assertThat(viewModel.state.value.isSaving).isFalse()
            assertThat(viewModel.state.value.canSave).isTrue()
        }
    }

    @Test
    fun `Save does nothing while there is nothing to save`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(EditPlaylistAction.OnSaveClick)

        assertThat(playlistDataSource.setSongIds[PLAYLIST_ID]).isNull()
    }

    @Test
    fun `going back with unsaved edits asks before leaving`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(EditPlaylistAction.OnRemoveSongClick("a"))

        viewModel.events.test {
            viewModel.onAction(EditPlaylistAction.OnBackClick)
            assertThat(viewModel.state.value.isDiscardSheetVisible).isTrue()
            expectNoEvents()

            viewModel.onAction(EditPlaylistAction.OnConfirmDiscardClick)
            assertThat(awaitItem()).isEqualTo(EditPlaylistEvent.NavigateBack)
            assertThat(viewModel.state.value.isDiscardSheetVisible).isFalse()
        }
    }

    @Test
    fun `going back with no edits leaves straight away`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(EditPlaylistAction.OnBackClick)

            assertThat(awaitItem()).isEqualTo(EditPlaylistEvent.NavigateBack)
            assertThat(viewModel.state.value.isDiscardSheetVisible).isFalse()
        }
    }

    @Test
    fun `keeping editing closes the sheet and stays on the screen`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(EditPlaylistAction.OnRemoveSongClick("a"))
        viewModel.onAction(EditPlaylistAction.OnBackClick)

        viewModel.events.test {
            viewModel.onAction(EditPlaylistAction.OnDismissDiscardSheet)

            assertThat(viewModel.state.value.isDiscardSheetVisible).isFalse()
            assertThat(viewModel.songIds()).containsExactly("b", "c")
            expectNoEvents()
        }
    }

    @Test
    fun `a rescan underneath the screen does not throw away the edit in progress`() {
        val viewModel = createViewModel()
        viewModel.onAction(EditPlaylistAction.OnMoveSong(from = 2, to = 0))

        playlistDataSource.playlists.value = listOf(
            playlist(PLAYLIST_ID, songs = listOf(song("a"), song("b"), song("c"), song("d"))),
        )

        assertThat(viewModel.songIds()).containsExactly("c", "a", "b")
    }

    @Test
    fun `a playlist deleted elsewhere closes the screen`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            playlistDataSource.playlists.value = emptyList()

            assertThat(awaitItem()).isEqualTo(EditPlaylistEvent.NavigateBack)
        }
    }

    @Test
    fun `an order saved before process death is restored, minus songs that have gone`() {
        savedStateHandle["songIds"] = arrayListOf("c", "a", "gone")

        val viewModel = createViewModel()

        assertThat(viewModel.songIds()).containsExactly("c", "a")
        assertThat(viewModel.state.value.hasChanges).isTrue()
    }

    private companion object {
        const val PLAYLIST_ID = 1L
    }
}
