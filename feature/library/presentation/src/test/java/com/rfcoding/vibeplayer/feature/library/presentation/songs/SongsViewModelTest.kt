package com.rfcoding.vibeplayer.feature.library.presentation.songs

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.single
import com.rfcoding.vibeplayer.core.presentation.toSongUi
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
class SongsViewModelTest {

    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var musicPlayer: FakeMusicPlayer

    private val songs = listOf(song("a"), song("b"), song("c"), song("d"))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        songDataSource = FakeSongLocalDataSource()
        musicPlayer = FakeMusicPlayer()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SongsViewModel(songDataSource, musicPlayer)

    @Test
    fun `state shows the stored songs and follows their changes`() = runTest {
        songDataSource.songsMutable.value = songs
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.songs).containsExactly(*songs.map { it.toSongUi() }.toTypedArray())

        songDataSource.songsMutable.value = listOf(song("z"))

        assertThat(viewModel.state.value.songs).containsExactly(song("z").toSongUi())
    }

    @Test
    fun `play queues every song in list order`() = runTest {
        songDataSource.songsMutable.value = songs
        val viewModel = createViewModel()

        viewModel.onAction(SongsAction.OnPlayClick)

        assertThat(musicPlayer.playedQueues).single().containsExactly(*songs.toTypedArray())
    }

    @Test
    fun `tapping a song queues it and the songs after it, but not the ones before`() = runTest {
        songDataSource.songsMutable.value = songs
        val viewModel = createViewModel()

        viewModel.onAction(SongsAction.OnSongClick("c"))

        assertThat(musicPlayer.playedQueues).single().containsExactly(song("c"), song("d"))
    }

    @Test
    fun `tapping an unknown song plays nothing`() = runTest {
        songDataSource.songsMutable.value = songs
        val viewModel = createViewModel()

        viewModel.onAction(SongsAction.OnSongClick("missing"))

        assertThat(musicPlayer.playedQueues).isEmpty()
    }

    @Test
    fun `shuffle queues every song in any order`() = runTest {
        songDataSource.songsMutable.value = songs
        val viewModel = createViewModel()

        viewModel.onAction(SongsAction.OnShuffleClick)

        assertThat(musicPlayer.playedQueues).single().containsExactlyInAnyOrder(*songs.toTypedArray())
    }

    @Test
    fun `play and shuffle with no songs play nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(SongsAction.OnPlayClick)
        viewModel.onAction(SongsAction.OnShuffleClick)

        assertThat(musicPlayer.playedQueues).isEmpty()
    }
}
