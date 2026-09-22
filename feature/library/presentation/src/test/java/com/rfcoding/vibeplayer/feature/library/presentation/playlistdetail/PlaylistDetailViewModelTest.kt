package com.rfcoding.vibeplayer.feature.library.presentation.playlistdetail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.single
import com.rfcoding.vibeplayer.core.domain.util.DataError
import com.rfcoding.vibeplayer.core.presentation.toSongUi
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
        assertThat(musicPlayer.playedAsPlaylist).containsExactly(true)
    }

    @Test
    fun `play queues the whole playlist in order`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnPlayClick)

        assertThat(musicPlayer.playedQueues).single().containsExactly(*songs.toTypedArray())
        assertThat(musicPlayer.playedAsPlaylist).containsExactly(true)
    }

    @Test
    fun `shuffle queues every song of the playlist`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnShuffleClick)

        assertThat(musicPlayer.playedQueues).single().containsExactlyInAnyOrder(*songs.toTypedArray())
        assertThat(musicPlayer.playedAsPlaylist).containsExactly(true)
    }

    @Test
    fun `edit enters delete mode and exiting clears the selection`() {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()
        assertThat(viewModel.state.value.canEditSongs).isTrue()

        viewModel.onAction(PlaylistDetailAction.OnEditClick)
        viewModel.onAction(PlaylistDetailAction.OnSongSelectedChange("a", selected = true))
        assertThat(viewModel.state.value.isDeleteMode).isTrue()
        assertThat(viewModel.state.value.selectedSongIds).containsOnly("a")

        viewModel.onAction(PlaylistDetailAction.OnExitDeleteModeClick)
        assertThat(viewModel.state.value.isDeleteMode).isFalse()
        assertThat(viewModel.state.value.selectedSongIds).isEmpty()
    }

    @Test
    fun `an empty playlist can't enter delete mode`() {
        playlistDataSource.playlists.value = listOf(playlist(id = 1))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnEditClick)

        assertThat(viewModel.state.value.canEditSongs).isFalse()
        assertThat(viewModel.state.value.isDeleteMode).isFalse()
    }

    @Test
    fun `songs can't be selected outside delete mode`() {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()

        viewModel.onAction(PlaylistDetailAction.OnSongSelectedChange("a", selected = true))

        assertThat(viewModel.state.value.selectedSongIds).isEmpty()
    }

    @Test
    fun `select all ticks every song and unticking one clears select all`() {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistDetailAction.OnEditClick)

        viewModel.onAction(PlaylistDetailAction.OnSelectAllChange(selected = true))
        assertThat(viewModel.state.value.selectedSongIds).containsOnly("a", "b", "c", "d")
        assertThat(viewModel.state.value.isAllSelected).isTrue()

        viewModel.onAction(PlaylistDetailAction.OnSongSelectedChange("b", selected = false))
        assertThat(viewModel.state.value.isAllSelected).isFalse()

        viewModel.onAction(PlaylistDetailAction.OnSelectAllChange(selected = false))
        assertThat(viewModel.state.value.selectedSongIds).isEmpty()
    }

    @Test
    fun `delete opens the confirmation sheet only with a selection, and dismissing closes it`() {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistDetailAction.OnEditClick)

        viewModel.onAction(PlaylistDetailAction.OnDeleteSelectedClick)
        assertThat(viewModel.state.value.isRemoveSheetVisible).isFalse()

        viewModel.onAction(PlaylistDetailAction.OnSongSelectedChange("a", selected = true))
        viewModel.onAction(PlaylistDetailAction.OnDeleteSelectedClick)
        assertThat(viewModel.state.value.isRemoveSheetVisible).isTrue()

        viewModel.onAction(PlaylistDetailAction.OnDismissRemoveSheet)
        assertThat(viewModel.state.value.isRemoveSheetVisible).isFalse()
        assertThat(viewModel.state.value.selectedSongIds).containsOnly("a")
    }

    @Test
    fun `confirming removes the selected songs from the playlist and leaves delete mode`() {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()
        viewModel.selectAndConfirm("b", "d")

        val state = viewModel.state.value
        assertThat(state.songs.map { it.id }).containsExactly("a", "c")
        assertThat(state.isDeleteMode).isFalse()
        assertThat(state.selectedSongIds).isEmpty()
        assertThat(state.isRemoveSheetVisible).isFalse()
        assertThat(state.isRemoving).isFalse()
    }

    @Test
    fun `confirming on favourites unfavourites the selected songs`() {
        songDataSource.songsMutable.value = listOf(
            song("a", isFavorite = true),
            song("b", isFavorite = true),
            song("c", isFavorite = true),
        )
        val viewModel = createViewModel(playlistId = null)
        viewModel.selectAndConfirm("a", "c")

        assertThat(viewModel.state.value.songs.map { it.id }).containsExactly("b")
        assertThat(songDataSource.songsMutable.value.filter { it.isFavorite }.map { it.id }).containsExactly("b")
        assertThat(viewModel.state.value.isDeleteMode).isFalse()
    }

    @Test
    fun `a failed removal shows an error and keeps the selection`() = runTest {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        playlistDataSource.error = DataError.Local.UNKNOWN
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.selectAndConfirm("a")

            assertThat(awaitItem()).isInstanceOf<PlaylistDetailEvent.Error>()
        }
        val state = viewModel.state.value
        assertThat(state.songs).hasSize(4)
        assertThat(state.isDeleteMode).isTrue()
        assertThat(state.selectedSongIds).containsOnly("a")
        assertThat(state.isRemoveSheetVisible).isFalse()
        assertThat(state.isRemoving).isFalse()
    }

    @Test
    fun `songs that disappear lose their tick, and an emptied playlist leaves delete mode`() {
        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = songs))
        val viewModel = createViewModel()
        viewModel.onAction(PlaylistDetailAction.OnEditClick)
        viewModel.onAction(PlaylistDetailAction.OnSelectAllChange(selected = true))

        playlistDataSource.playlists.value = listOf(playlist(id = 1, songs = listOf(song("a"), song("b"))))
        assertThat(viewModel.state.value.selectedSongIds).containsOnly("a", "b")
        assertThat(viewModel.state.value.isDeleteMode).isTrue()

        playlistDataSource.playlists.value = listOf(playlist(id = 1))
        assertThat(viewModel.state.value.isDeleteMode).isFalse()
        assertThat(viewModel.state.value.selectedSongIds).isEmpty()
    }

    private fun PlaylistDetailViewModel.selectAndConfirm(vararg songIds: String) {
        onAction(PlaylistDetailAction.OnEditClick)
        songIds.forEach { onAction(PlaylistDetailAction.OnSongSelectedChange(it, selected = true)) }
        onAction(PlaylistDetailAction.OnDeleteSelectedClick)
        onAction(PlaylistDetailAction.OnConfirmRemoveClick)
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
