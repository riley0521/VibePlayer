package com.rfcoding.vibeplayer.feature.player.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.presentation.UiText
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import com.rfcoding.vibeplayer.core.testing.FakeMusicPlayer
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
class PlayerViewModelTest {

    private lateinit var musicPlayer: FakeMusicPlayer
    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var playlistDataSource: FakePlaylistLocalDataSource

    private val songs = listOf(song("a"), song("b"), song("c"), song("d"))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        musicPlayer = FakeMusicPlayer()
        songDataSource = FakeSongLocalDataSource()
        songDataSource.songsMutable.value = songs
        playlistDataSource = FakePlaylistLocalDataSource()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = PlayerViewModel(musicPlayer, songDataSource, playlistDataSource)

    private fun playSecondSong() {
        musicPlayer.playbackState.value = PlaybackState(queue = songs, currentIndex = 1)
    }

    @Test
    fun `state has no song while nothing is queued`() = runTest {
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.song).isNull()
    }

    @Test
    fun `state follows the playback session`() = runTest {
        val viewModel = createViewModel()

        musicPlayer.playbackState.value = PlaybackState(
            queue = songs,
            currentIndex = 2,
            isPlaying = true,
            positionMillis = 30_000,
            isShuffleOn = true,
            repeatMode = RepeatMode.One,
        )

        val state = viewModel.state.value
        assertThat(state.song).isEqualTo(songs[2].toSongUi())
        assertThat(state.isPlaying).isTrue()
        assertThat(state.positionMillis).isEqualTo(30_000)
        assertThat(state.isShuffleOn).isTrue()
        assertThat(state.repeatMode).isEqualTo(RepeatMode.One)
    }

    @Test
    fun `transport buttons control the player`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(PlayerAction.OnPlayPauseClick)
        viewModel.onAction(PlayerAction.OnNextClick)
        viewModel.onAction(PlayerAction.OnPreviousClick)
        viewModel.onAction(PlayerAction.OnPreviousClick)

        assertThat(musicPlayer.togglePlayPauseCount).isEqualTo(1)
        assertThat(musicPlayer.skipToNextCount).isEqualTo(1)
        assertThat(musicPlayer.skipToPreviousCount).isEqualTo(2)
    }

    @Test
    fun `shuffle click toggles shuffle in the player`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(PlayerAction.OnShuffleClick)

        assertThat(musicPlayer.toggleShuffleCount).isEqualTo(1)
    }

    @Test
    fun `repeat cycles from Off to All to One and back to Off`() = runTest {
        val viewModel = createViewModel()

        RepeatMode.entries.forEach { current ->
            musicPlayer.playbackState.value = PlaybackState(repeatMode = current)
            viewModel.onAction(PlayerAction.OnRepeatClick)
        }

        assertThat(musicPlayer.repeatModes).containsExactly(RepeatMode.All, RepeatMode.One, RepeatMode.Off)
    }

    @Test
    fun `favourite follows the database row of the current song`() = runTest {
        val viewModel = createViewModel()
        playSecondSong()
        assertThat(viewModel.state.value.isFavorite).isFalse()

        // The queue's copy stays unfavourited; only the database changes, as when the notification toggles it.
        songDataSource.songsMutable.value = songs.map { if (it.id == "b") it.copy(isFavorite = true) else it }

        assertThat(viewModel.state.value.isFavorite).isTrue()
    }

    @Test
    fun `favourite click flips the current song's flag`() = runTest {
        val viewModel = createViewModel()
        playSecondSong()

        viewModel.onAction(PlayerAction.OnFavoriteClick)
        assertThat(viewModel.state.value.isFavorite).isTrue()

        viewModel.onAction(PlayerAction.OnFavoriteClick)
        assertThat(viewModel.state.value.isFavorite).isFalse()
    }

    @Test
    fun `a failed favourite write sends an error`() = runTest {
        songDataSource.setFavoriteError = DataError.Local.DISK_FULL
        val viewModel = createViewModel()
        playSecondSong()

        viewModel.events.test {
            viewModel.onAction(PlayerAction.OnFavoriteClick)

            assertThat(awaitItem()).isInstanceOf<PlayerEvent.Error>()
        }
        assertThat(viewModel.state.value.isFavorite).isFalse()
    }

    @Test
    fun `favourite click does nothing while nothing is queued`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(PlayerAction.OnFavoriteClick)

        assertThat(songDataSource.songsMutable.value.none { it.isFavorite }).isTrue()
    }

    @Test
    fun `seeking moves the song and shows the new position at once`() = runTest {
        val viewModel = createViewModel()
        playSecondSong()

        viewModel.onAction(PlayerAction.OnSeek(0.5f))

        assertThat(musicPlayer.seekPositions).containsExactly(30_000L)
        assertThat(viewModel.state.value.positionMillis).isEqualTo(30_000L)
    }

    @Test
    fun `seeking does nothing while nothing is queued`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(PlayerAction.OnSeek(0.5f))

        assertThat(musicPlayer.seekPositions).isEmpty()
    }

    @Test
    fun `add to playlist opens the sheet for the current song`() = runTest {
        val viewModel = createViewModel()
        playSecondSong()

        viewModel.onAction(PlayerAction.OnAddToPlaylistClick)

        assertThat(viewModel.state.value.activeSheet).isEqualTo(PlayerSheet.AddToPlaylist("b"))
    }

    @Test
    fun `add to playlist does nothing while nothing is queued`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(PlayerAction.OnAddToPlaylistClick)

        assertThat(viewModel.state.value.activeSheet).isNull()
    }

    @Test
    fun `the sheet keeps the song it opened for after the track changes`() = runTest {
        val viewModel = createViewModel()
        playSecondSong()
        viewModel.onAction(PlayerAction.OnAddToPlaylistClick)

        musicPlayer.playbackState.value = PlaybackState(queue = songs, currentIndex = 2)
        viewModel.onAction(PlayerAction.OnCreatePlaylistClick)

        assertThat(viewModel.state.value.activeSheet).isEqualTo(PlayerSheet.CreatePlaylist("b"))
    }

    @Test
    fun `dismissing closes the sheet`() = runTest {
        val viewModel = createViewModel()
        playSecondSong()
        viewModel.onAction(PlayerAction.OnAddToPlaylistClick)

        viewModel.onAction(PlayerAction.OnSheetDismiss)

        assertThat(viewModel.state.value.activeSheet).isNull()
    }

    @Test
    fun `a playlist created from the sheet gets the song and reports its name`() = runTest {
        val viewModel = createViewModel()
        playSecondSong()
        viewModel.onAction(PlayerAction.OnAddToPlaylistClick)
        viewModel.onAction(PlayerAction.OnCreatePlaylistClick)

        viewModel.events.test {
            viewModel.onAction(PlayerAction.OnPlaylistCreated(playlistId = 7, name = "Road trip"))

            val event = awaitItem() as PlayerEvent.AddedToPlaylist
            assertThat((event.playlistName as UiText.DynamicString).value).isEqualTo("Road trip")
        }
        assertThat(playlistDataSource.addedSongIds[7L]).isEqualTo(listOf("b"))
        assertThat(viewModel.state.value.activeSheet).isNull()
    }

    @Test
    fun `a failed add to the created playlist sends an error`() = runTest {
        playlistDataSource.error = DataError.Local.DISK_FULL
        val viewModel = createViewModel()
        playSecondSong()
        viewModel.onAction(PlayerAction.OnAddToPlaylistClick)
        viewModel.onAction(PlayerAction.OnCreatePlaylistClick)

        viewModel.events.test {
            viewModel.onAction(PlayerAction.OnPlaylistCreated(playlistId = 7, name = "Road trip"))

            assertThat(awaitItem()).isInstanceOf<PlayerEvent.Error>()
        }
    }
}
