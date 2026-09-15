package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.single
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.FakeMusicPlayer
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.FakePlaylistLocalDataSource
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.FakeSongLocalDataSource
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.playlist
import com.rfcoding.vibeplayer.feature.library.presentation.fakes.song
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
class PlaylistDetailViewModelTest {

    private lateinit var playlistDataSource: FakePlaylistLocalDataSource
    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var musicPlayer: FakeMusicPlayer

    private val songs = listOf(song("a"), song("b"), song("c"), song("d"))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        playlistDataSource = FakePlaylistLocalDataSource()
        songDataSource = FakeSongLocalDataSource()
        musicPlayer = FakeMusicPlayer()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(playlistId: Long? = 1) =
        PlaylistDetailViewModel(playlistId, playlistDataSource, songDataSource, musicPlayer)

    @Test
    fun `state shows the playlist's name, cover and songs`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, name = "Chill", songs = songs, coverUri = "content://cover"))

        val state = createViewModel().state.value

        assertThat(state.isFavourites).isFalse()
        assertThat(state.canAddSongs).isTrue()
        assertThat(state.name).isEqualTo("Chill")
        assertThat(state.imageUri).isEqualTo("content://cover")
        assertThat(state.songs).containsExactly(*songs.map { it.toSongUi() }.toTypedArray())
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `a playlist without a cover shows its first song's artwork`() = runTest {
        val withArt = song("b").copy(imageUri = "content://art")
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = listOf(song("a"), withArt)))

        assertThat(createViewModel().state.value.imageUri).isEqualTo("content://art")
    }

    @Test
    fun `state is loading until the playlist arrives`() = runTest {
        assertThat(createViewModel().state.value.isLoading).isTrue()
    }

    @Test
    fun `an empty playlist has no songs once loaded`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1))

        val state = createViewModel().state.value

        assertThat(state.songs).isEmpty()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `favourites shows the favourite songs and follows their changes`() = runTest {
        songDataSource.songsMutable.value = listOf(song("a", isFavorite = true), song("b"))
        val viewModel = createViewModel(playlistId = null)

        assertThat(viewModel.state.value.isFavourites).isTrue()
        assertThat(viewModel.state.value.canAddSongs).isFalse()
        assertThat(viewModel.state.value.songs).containsExactly(song("a", isFavorite = true).toSongUi())

        songDataSource.setFavorite("b", isFavorite = true)

        assertThat(viewModel.state.value.songs.map { it.id }).containsExactly("a", "b")
    }

    @Test
    fun `a deleted playlist navigates back`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1))
        val viewModel = createViewModel()

        viewModel.events.test {
            playlistDataSource.playlists.value = emptyList()

            assertThat(awaitItem()).isEqualTo(PlaylistDetailEvent.NavigateBack)
        }
    }

    @Test
    fun `song click queues the tapped song and the ones after it`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnSongClick("c"))

        assertThat(musicPlayer.playedQueues).single().containsExactly(song("c"), song("d"))
    }

    @Test
    fun `play queues the whole playlist in order`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnPlayClick)

        assertThat(musicPlayer.playedQueues).single().containsExactly(*songs.toTypedArray())
    }

    @Test
    fun `shuffle queues every song of the playlist`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnShuffleClick)

        assertThat(musicPlayer.playedQueues).single().containsExactlyInAnyOrder(*songs.toTypedArray())
    }

    @Test
    fun `an empty playlist plays nothing`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnPlayClick)
        viewModel.onAction(PlaylistDetailAction.OnShuffleClick)

        assertThat(musicPlayer.playedQueues).isEmpty()
    }
}
