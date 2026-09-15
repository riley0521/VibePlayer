package com.rfcoding.vibeplayer.feature.library.presentation.search

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.single
import com.rfcoding.vibeplayer.core.testing.FakeMusicPlayer
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
class SearchViewModelTest {

    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var musicPlayer: FakeMusicPlayer
    private lateinit var savedStateHandle: SavedStateHandle

    // Titles are "Song a", "Song ab", …, so "song a" matches the first three and "song b" only one.
    private val library = listOf(song("a"), song("ab"), song("ac"), song("b"))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        songDataSource = FakeSongLocalDataSource().apply { songsMutable.value = library }
        musicPlayer = FakeMusicPlayer()
        savedStateHandle = SavedStateHandle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SearchViewModel(savedStateHandle, songDataSource, musicPlayer)

    private fun SearchViewModel.visibleIds() = state.value.songs.map { it.id }

    @Test
    fun `an empty query shows every song`() {
        val viewModel = createViewModel()

        assertThat(viewModel.visibleIds()).containsExactly("a", "ab", "ac", "b")
        assertThat(viewModel.state.value.isEmptyResult).isFalse()
    }

    @Test
    fun `typing filters the songs and clearing shows them all again`() {
        val viewModel = createViewModel()

        viewModel.onAction(SearchAction.OnQueryChange("song a"))
        assertThat(viewModel.visibleIds()).containsExactly("a", "ab", "ac")
        assertThat(viewModel.state.value.query).isEqualTo("song a")

        viewModel.onAction(SearchAction.OnClearClick)
        assertThat(viewModel.visibleIds()).containsExactly("a", "ab", "ac", "b")
        assertThat(viewModel.state.value.query).isEmpty()
    }

    @Test
    fun `results follow library changes`() {
        val viewModel = createViewModel()
        viewModel.onAction(SearchAction.OnQueryChange("song b"))

        songDataSource.songsMutable.value = library + song("bb")

        assertThat(viewModel.visibleIds()).containsExactly("b", "bb")
    }

    @Test
    fun `a query with no match is an empty result`() {
        val viewModel = createViewModel()

        viewModel.onAction(SearchAction.OnQueryChange("zzz"))

        assertThat(viewModel.visibleIds()).isEmpty()
        assertThat(viewModel.state.value.isEmptyResult).isTrue()
    }

    @Test
    fun `the query is restored from the saved state`() {
        createViewModel().onAction(SearchAction.OnQueryChange("song b"))

        val restored = createViewModel()

        assertThat(restored.state.value.query).isEqualTo("song b")
        assertThat(restored.visibleIds()).containsExactly("b")
    }

    @Test
    fun `song click queues the tapped result and the results after it, then opens the player`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(SearchAction.OnQueryChange("song a"))

        viewModel.events.test {
            viewModel.onAction(SearchAction.OnSongClick("ab"))

            assertThat(awaitItem()).isEqualTo(SearchEvent.NavigateToPlayer)
        }
        assertThat(musicPlayer.playedQueues).single().containsExactly(song("ab"), song("ac"))
    }

    @Test
    fun `clicking a song that is not in the results plays nothing`() {
        val viewModel = createViewModel()
        viewModel.onAction(SearchAction.OnQueryChange("song b"))

        viewModel.onAction(SearchAction.OnSongClick("a"))

        assertThat(musicPlayer.playedQueues).isEmpty()
    }
}
