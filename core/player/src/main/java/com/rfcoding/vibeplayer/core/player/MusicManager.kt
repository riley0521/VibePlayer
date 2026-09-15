package com.rfcoding.vibeplayer.core.player

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.rfcoding.vibeplayer.core.domain.player.MusicPlayer
import com.rfcoding.vibeplayer.core.domain.player.PlaybackState
import com.rfcoding.vibeplayer.core.domain.player.RepeatMode
import com.rfcoding.vibeplayer.core.domain.song.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds

/**
 * The music manager shared by the mini player, the Player screen and the notification. It holds no
 * playback state of its own: [playbackState] mirrors the session in [PlaybackService] through a
 * [MediaController], and every command goes to that session.
 */
class MusicManager(
    private val context: Context,
    applicationScope: CoroutineScope,
) : MusicPlayer {

    private val controllerMutex = Mutex()
    private var controller: MediaController? = null

    /** Pinged when the session extras (shuffle flag, original order) change. */
    private val sessionExtrasChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override val playbackState: StateFlow<PlaybackState> = flow { emitAll(awaitController().playbackStates()) }
        // MediaController may only be touched on the thread it was built on.
        .flowOn(Dispatchers.Main.immediate)
        .stateIn(applicationScope, SharingStarted.WhileSubscribed(5_000), PlaybackState())

    override suspend fun play(queue: List<Song>) = withController { controller ->
        if (queue.isEmpty()) return@withController
        controller.setMediaItems(queue.map { it.toMediaItem() })
        controller.prepare()
        controller.play()
        sendQueueInfo(controller, originalOrder = queue.map { it.id })
    }

    override suspend fun togglePlayPause() = withController { controller ->
        if (controller.playWhenReady && controller.playbackState != Player.STATE_ENDED) {
            controller.pause()
            return@withController
        }
        when (controller.playbackState) {
            Player.STATE_ENDED -> controller.seekToDefaultPosition(controller.currentMediaItemIndex)
            // A failed load leaves the player idle; preparing again retries the current song.
            Player.STATE_IDLE -> controller.prepare()
        }
        controller.play()
    }

    override suspend fun skipToNext() = withController { controller ->
        controller.seekToNextMediaItem()
    }

    override suspend fun skipToPrevious() = withController { controller ->
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0)
        }
    }

    override suspend fun seekTo(positionMillis: Long) = withController { controller ->
        controller.seekTo(positionMillis)
    }

    override suspend fun setRepeatMode(repeatMode: RepeatMode) = withController { controller ->
        controller.repeatMode = repeatMode.toMedia3()
    }

    override suspend fun toggleShuffle() = withController { controller ->
        controller.sendCustomCommand(PlaybackSessionContract.ToggleShuffleCommand, Bundle.EMPTY)
        Unit
    }

    private fun sendQueueInfo(controller: MediaController, originalOrder: List<String>) {
        controller.sendCustomCommand(
            PlaybackSessionContract.SetQueueInfoCommand,
            PlaybackSessionContract.queueInfoExtras(false, originalOrder),
        )
    }

    private suspend fun <T> withController(block: (MediaController) -> T): T =
        withContext(Dispatchers.Main.immediate) { block(awaitController()) }

    private suspend fun awaitController(): MediaController = withContext(Dispatchers.Main.immediate) {
        controllerMutex.withLock {
            controller?.takeIf { it.isConnected } ?: connect().also { controller = it }
        }
    }

    private suspend fun connect(): MediaController {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        return MediaController.Builder(context, token)
            .setListener(
                object : MediaController.Listener {
                    override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
                        sessionExtrasChanged.tryEmit(Unit)
                    }
                },
            )
            .buildAsync()
            .await()
    }

    private suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addListener(
            {
                try {
                    continuation.resume(get())
                } catch (e: ExecutionException) {
                    continuation.resumeWithException(e.cause ?: e)
                }
            },
            context.mainExecutor,
        )
        continuation.invokeOnCancellation { cancel(false) }
    }

    private fun MediaController.playbackStates(): Flow<PlaybackState> = callbackFlow {
        val controller = this@playbackStates
        var queue = controller.readQueue()

        fun sendSnapshot() {
            trySend(controller.toPlaybackState(queue, controller.sessionExtras))
        }

        // Media3 has no position callback, so the progress bars are fed by polling while playing.
        var ticker = if (controller.isPlaying) launch { tickWhilePlaying(::sendSnapshot) } else null

        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    queue = controller.readQueue()
                }
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                    ticker?.cancel()
                    ticker = if (player.isPlaying) launch { tickWhilePlaying(::sendSnapshot) } else null
                }
                sendSnapshot()
            }
        }
        controller.addListener(listener)
        val extrasJob = launch { sessionExtrasChanged.collect { sendSnapshot() } }
        sendSnapshot()

        awaitClose {
            controller.removeListener(listener)
            extrasJob.cancel()
            ticker?.cancel()
        }
    }

    private suspend fun CoroutineScope.tickWhilePlaying(onTick: () -> Unit) {
        while (isActive) {
            delay(PositionPollInterval)
            onTick()
        }
    }

    private companion object {
        val PositionPollInterval = 500.milliseconds
    }
}
