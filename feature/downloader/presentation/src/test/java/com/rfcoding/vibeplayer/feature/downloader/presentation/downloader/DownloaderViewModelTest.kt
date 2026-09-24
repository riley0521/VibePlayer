package com.rfcoding.vibeplayer.feature.downloader.presentation.downloader

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.rfcoding.vibeplayer.core.designsystem.components.SongDownloadState
import com.rfcoding.vibeplayer.core.domain.song.Song
import com.rfcoding.vibeplayer.core.domain.util.Result
import com.rfcoding.vibeplayer.core.presentation.UiText
import com.rfcoding.vibeplayer.core.testing.FakeMusicPlayer
import com.rfcoding.vibeplayer.core.testing.FakeSongLocalDataSource
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadError
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadStatus
import com.rfcoding.vibeplayer.feature.downloader.domain.RemoteTrack
import com.rfcoding.vibeplayer.feature.downloader.presentation.R
import com.rfcoding.vibeplayer.feature.downloader.presentation.fakes.FakeDownloadQueue
import com.rfcoding.vibeplayer.feature.downloader.presentation.fakes.FakeMediaLinkResolver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val VIDEO_LINK = "https://youtu.be/v1"
private const val PLAYLIST_LINK = "https://www.youtube.com/playlist?list=PL1"

@OptIn(ExperimentalCoroutinesApi::class)
class DownloaderViewModelTest {

    private lateinit var resolver: FakeMediaLinkResolver
    private lateinit var queue: FakeDownloadQueue
    private lateinit var songDataSource: FakeSongLocalDataSource
    private lateinit var savedStateHandle: SavedStateHandle

    private val playlist = listOf(
        track("owned", title = "Owned", artist = "Band"),
        track("v2", title = "Second", artist = "Band"),
        track("v3", title = "Third", artist = null),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        resolver = FakeMediaLinkResolver()
        queue = FakeDownloadQueue()
        songDataSource = FakeSongLocalDataSource().apply {
            songsMutable.value = listOf(librarySong(title = "owned", artist = "BAND"))
        }
        savedStateHandle = SavedStateHandle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DownloaderViewModel(
        savedStateHandle = savedStateHandle,
        linkResolver = resolver,
        downloadQueue = queue,
        songDataSource = songDataSource,
        musicPlayer = FakeMusicPlayer(),
    )

    private fun DownloaderViewModel.find(url: String) {
        onAction(DownloaderAction.OnUrlChange(url))
        onAction(DownloaderAction.OnFindClick)
    }

    private fun DownloaderViewModel.downloadStates() =
        state.value.tracks.associate { it.videoId to it.downloadState }

    @Test
    fun `finding a link shows a loader, then the songs marked against the library`() {
        val gate = CompletableDeferred<Result<List<RemoteTrack>, DownloadError>>()
        resolver.gate = gate
        val viewModel = createViewModel()

        viewModel.find(PLAYLIST_LINK)
        assertThat(viewModel.state.value.isResolving).isTrue()

        gate.complete(Result.Success(playlist))

        assertThat(viewModel.state.value.isResolving).isFalse()
        assertThat(resolver.resolvedUrls).containsExactly(PLAYLIST_LINK)
        assertThat(viewModel.downloadStates()).isEqualTo(
            mapOf(
                "owned" to SongDownloadState.Downloaded,
                "v2" to SongDownloadState.NotDownloaded,
                "v3" to SongDownloadState.NotDownloaded,
            ),
        )
    }

    @Test
    fun `a link that isn't YouTube is rejected without a lookup`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.find("https://vimeo.com/123")

            assertThat(awaitItem()).isInstanceOf<DownloaderEvent.Error>()
                .prop(DownloaderEvent.Error::message)
                .isInstanceOf<UiText.StringResource>()
                .prop(UiText.StringResource::id)
                .isEqualTo(R.string.error_invalid_link)
        }
        assertThat(resolver.resolvedUrls).isEmpty()
    }

    @Test
    fun `a failed lookup reports its error`() = runTest {
        resolver.result = Result.Error(DownloadError.NO_INTERNET)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.find(VIDEO_LINK)

            assertThat(awaitItem()).isInstanceOf<DownloaderEvent.Error>()
                .prop(DownloaderEvent.Error::message)
                .isInstanceOf<UiText.StringResource>()
                .prop(UiText.StringResource::id)
                .isEqualTo(R.string.error_no_internet)
        }
        assertThat(viewModel.state.value.tracks).isEmpty()
    }

    @Test
    fun `pasting replaces the link and looks it up`() {
        resolver.result = Result.Success(playlist.take(1))
        val viewModel = createViewModel()

        viewModel.onAction(DownloaderAction.OnPaste("  $VIDEO_LINK \n"))

        assertThat(viewModel.state.value.url).isEqualTo(VIDEO_LINK)
        assertThat(resolver.resolvedUrls).containsExactly(VIDEO_LINK)
        assertThat(savedStateHandle.get<String>("url")).isEqualTo(VIDEO_LINK)
    }

    @Test
    fun `a link restored after process death is looked up again`() {
        savedStateHandle["url"] = PLAYLIST_LINK
        resolver.result = Result.Success(playlist)

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.url).isEqualTo(PLAYLIST_LINK)
        assertThat(viewModel.state.value.tracks.map { it.videoId }).containsExactly("owned", "v2", "v3")
    }

    @Test
    fun `downloading a song queues it and the card follows its progress until it's in the library`() {
        resolver.result = Result.Success(playlist)
        val viewModel = createViewModel()
        viewModel.find(PLAYLIST_LINK)

        viewModel.onAction(DownloaderAction.OnDownloadClick("v2"))
        assertThat(queue.enqueuedBatches.single().map { it.videoId }).containsExactly("v2")
        assertThat(viewModel.downloadStates()["v2"]).isEqualTo(SongDownloadState.Queued)

        queue.statusesMutable.value = mapOf("v2" to DownloadStatus.Downloading(0.4f))
        assertThat(viewModel.downloadStates()["v2"]).isEqualTo(SongDownloadState.Downloading(0.4f))

        songDataSource.songsMutable.value += librarySong(title = "Second", artist = "Band")
        queue.statusesMutable.value = emptyMap()
        assertThat(viewModel.downloadStates()["v2"]).isEqualTo(SongDownloadState.Downloaded)
    }

    @Test
    fun `a song already in the library isn't downloaded again`() {
        resolver.result = Result.Success(playlist)
        val viewModel = createViewModel()
        viewModel.find(PLAYLIST_LINK)

        viewModel.onAction(DownloaderAction.OnDownloadClick("owned"))

        assertThat(queue.enqueuedBatches).isEmpty()
    }

    @Test
    fun `download all queues only the songs still missing and then hides`() {
        resolver.result = Result.Success(playlist)
        val viewModel = createViewModel()
        viewModel.find(PLAYLIST_LINK)
        assertThat(viewModel.state.value.canDownloadAll).isTrue()

        viewModel.onAction(DownloaderAction.OnDownloadAllClick)

        assertThat(queue.enqueuedBatches.single().map { it.videoId }).containsExactly("v2", "v3")
        assertThat(viewModel.state.value.canDownloadAll).isFalse()
    }

    @Test
    fun `download all isn't offered for a single song`() {
        resolver.result = Result.Success(playlist.drop(1).take(1))
        val viewModel = createViewModel()

        viewModel.find(VIDEO_LINK)

        assertThat(viewModel.state.value.tracks.map { it.videoId }).containsExactly("v2")
        assertThat(viewModel.state.value.canDownloadAll).isFalse()
    }

    @Test
    fun `a failed download is reported once, by title, and can be tried again`() = runTest {
        resolver.result = Result.Success(playlist)
        val viewModel = createViewModel()
        viewModel.find(PLAYLIST_LINK)
        viewModel.onAction(DownloaderAction.OnDownloadClick("v2"))

        viewModel.events.test {
            queue.statusesMutable.value = mapOf("v2" to DownloadStatus.Failed(DownloadError.UNAVAILABLE))
            // The same failure again (e.g. another song's progress changed) isn't a new one.
            queue.statusesMutable.value = mapOf(
                "v2" to DownloadStatus.Failed(DownloadError.UNAVAILABLE),
                "v3" to DownloadStatus.Queued,
            )

            val event = awaitItem() as DownloaderEvent.DownloadsFailed
            assertThat(event.count).isEqualTo(1)
            assertThat(event.title).isEqualTo("Second")
            expectNoEvents()
        }
        assertThat(viewModel.downloadStates()["v2"]).isEqualTo(SongDownloadState.NotDownloaded)

        viewModel.onAction(DownloaderAction.OnDownloadClick("v2"))
        assertThat(queue.enqueuedBatches.map { batch -> batch.map { it.videoId } })
            .containsExactly(listOf("v2"), listOf("v2"))
    }

    @Test
    fun `failures from before the screen opened aren't reported`() = runTest {
        queue.statusesMutable.value = mapOf("v2" to DownloadStatus.Failed(DownloadError.NO_INTERNET))
        resolver.result = Result.Success(playlist)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.find(PLAYLIST_LINK)
            expectNoEvents()
        }
    }

    private fun track(videoId: String, title: String, artist: String?) = RemoteTrack(
        videoId = videoId,
        url = "https://www.youtube.com/watch?v=$videoId",
        title = title,
        artistName = artist,
        thumbnailUrl = null,
        durationMillis = null,
    )

    private fun librarySong(title: String, artist: String?) = Song(
        id = title,
        title = title,
        artistName = artist,
        fileUri = "content://$title",
        imageUri = null,
        durationMillis = 60_000,
        isFavorite = false,
        createdAt = 0,
    )
}
