package com.rfcoding.vibeplayer.feature.player.presentation.queue

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.player.SleepTimer
import com.rfcoding.vibeplayer.core.presentation.toSongUi
import com.rfcoding.vibeplayer.core.testing.FakeMusicPlayer
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
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class QueueViewModelTest {

    private lateinit var musicPlayer: FakeMusicPlayer

    private val songs = listOf(song("a"), song("b"), song("c"), song("d"), song("e"))

    /** "b" is playing, so "a" has been played and "c", "d", "e" are up next. */
    private val playback = PlaybackState(queue = songs, currentIndex = 1, isPlaying = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        musicPlayer = FakeMusicPlayer()
        musicPlayer.playbackState.value = playback
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = QueueViewModel(musicPlayer)

    private fun QueueViewModel.upcomingIds() = state.value.upcomingSongs.map { it.id }

    @Test
    fun `state lists the current song and only the songs after it`() = runTest {
        val state = createViewModel().state.value

        assertThat(state.currentSong).isEqualTo(songs[1].toSongUi())
        assertThat(state.isPlaying).isTrue()
        assertThat(state.upcomingSongs.map { it.id }).containsExactly("c", "d", "e")
    }

    @Test
    fun `state follows the player's shuffle, repeat and play state`() = runTest {
        val viewModel = createViewModel()

        musicPlayer.playbackState.value = playback.copy(isPlaying = false, isShuffleOn = true, repeatMode = RepeatMode.One)

        val state = viewModel.state.value
        assertThat(state.isPlaying).isFalse()
        assertThat(state.isShuffleOn).isTrue()
        assertThat(state.repeatMode).isEqualTo(RepeatMode.One)
    }

    @Test
    fun `the list follows the player when the song changes`() = runTest {
        val viewModel = createViewModel()

        musicPlayer.playbackState.value = playback.copy(currentIndex = 3)

        assertThat(viewModel.upcomingIds()).containsExactly("e")
    }

    @Test
    fun `nothing is listed while nothing is queued`() = runTest {
        musicPlayer.playbackState.value = PlaybackState()

        val state = createViewModel().state.value

        assertThat(state.currentSong).isEqualTo(null)
        assertThat(state.upcomingSongs).isEmpty()
    }

    @Test
    fun `tapping an upcoming song skips to its place in the queue`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnUpcomingSongClick("d"))

        assertThat(musicPlayer.skippedToIndices).containsExactly(3)
    }

    @Test
    fun `play pause toggles playback`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnPlayPauseClick)

        assertThat(musicPlayer.togglePlayPauseCount).isEqualTo(1)
    }

    @Test
    fun `a drag reorders the list at once and sends one move when released`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnMoveSong(from = 2, to = 1))
        viewModel.onAction(QueueAction.OnMoveSong(from = 1, to = 0))

        assertThat(viewModel.upcomingIds()).containsExactly("e", "c", "d")
        assertThat(musicPlayer.movedQueueItems).isEmpty()

        viewModel.onAction(QueueAction.OnDragStopped)

        // "e" sits at 4 in the whole queue and lands right after the current song, at 2.
        assertThat(musicPlayer.movedQueueItems).containsExactly(4 to 2)
    }

    @Test
    fun `a song dropped where it started sends no move`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnMoveSong(from = 0, to = 1))
        viewModel.onAction(QueueAction.OnMoveSong(from = 1, to = 0))
        viewModel.onAction(QueueAction.OnDragStopped)

        assertThat(musicPlayer.movedQueueItems).isEmpty()
        assertThat(viewModel.upcomingIds()).containsExactly("c", "d", "e")
    }

    @Test
    fun `player updates don't replace the list during a drag`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(QueueAction.OnMoveSong(from = 0, to = 2))

        musicPlayer.playbackState.value = playback.copy(currentIndex = 2)

        assertThat(viewModel.upcomingIds()).containsExactly("d", "e", "c")
    }

    @Test
    fun `the dropped order survives position updates until the player's queue changes`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(QueueAction.OnMoveSong(from = 0, to = 2))
        viewModel.onAction(QueueAction.OnDragStopped)

        musicPlayer.playbackState.value = playback.copy(positionMillis = 1_000)
        assertThat(viewModel.upcomingIds()).containsExactly("d", "e", "c")

        val (a, b, c, d, e) = songs
        musicPlayer.playbackState.value = playback.copy(queue = listOf(a, b, d, e, c))
        assertThat(viewModel.upcomingIds()).containsExactly("d", "e", "c")
    }

    @Test
    fun `swiping a song removes it at once and from the player's queue`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnSongSwiped("d"))

        assertThat(viewModel.upcomingIds()).containsExactly("c", "e")
        assertThat(musicPlayer.removedQueueIndices).containsExactly(3)
    }

    @Test
    fun `swiping a song that isn't upcoming does nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnSongSwiped("a"))

        assertThat(musicPlayer.removedQueueIndices).isEmpty()
        assertThat(viewModel.upcomingIds()).containsExactly("c", "d", "e")
    }

    @Test
    fun `shuffle and repeat go to the player`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnShuffleClick)
        viewModel.onAction(QueueAction.OnRepeatClick)

        assertThat(musicPlayer.toggleShuffleCount).isEqualTo(1)
        assertThat(musicPlayer.repeatModes).containsExactly(RepeatMode.All)
    }

    @Test
    fun `the timer button opens the sleep timer sheet`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(QueueAction.OnTimerClick)

        assertThat(viewModel.state.value.isSleepTimerSheetVisible).isTrue()
    }

    @Test
    fun `picking a sleep timer sets it and closes only the sleep timer sheet`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(QueueAction.OnTimerClick)

        viewModel.onAction(QueueAction.OnSleepTimerSelect(SleepTimerOption.Minutes15))

        assertThat(musicPlayer.sleepTimers).containsExactly(SleepTimer.After(15.minutes))
        assertThat(viewModel.state.value.isSleepTimerSheetVisible).isFalse()
    }

    @Test
    fun `every sleep timer option sends its timer`() = runTest {
        val viewModel = createViewModel()

        SleepTimerOption.entries.forEach { viewModel.onAction(QueueAction.OnSleepTimerSelect(it)) }

        assertThat(musicPlayer.sleepTimers).containsExactly(
            SleepTimer.After(5.minutes),
            SleepTimer.After(10.minutes),
            SleepTimer.After(15.minutes),
            SleepTimer.After(30.minutes),
            SleepTimer.After(45.minutes),
            SleepTimer.After(1.hours),
            SleepTimer.EndOfTrack,
        )
    }

    @Test
    fun `dismissing the sleep timer sheet sets no timer`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(QueueAction.OnTimerClick)

        viewModel.onAction(QueueAction.OnSleepTimerSheetDismiss)

        assertThat(viewModel.state.value.isSleepTimerSheetVisible).isFalse()
        assertThat(musicPlayer.sleepTimers).isEmpty()
    }
}
