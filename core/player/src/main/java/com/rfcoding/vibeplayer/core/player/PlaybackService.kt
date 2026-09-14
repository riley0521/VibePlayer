package com.rfcoding.vibeplayer.core.player

import android.app.PendingIntent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.rfcoding.vibeplayer.core.domain.player.originalOrderQueue
import com.rfcoding.vibeplayer.core.domain.player.shuffledQueue
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import com.rfcoding.vibeplayer.core.domain.util.onFailure
import com.rfcoding.vibeplayer.core.player.PlaybackSessionContract.isShuffleOn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Owns the one ExoPlayer and its session: the source of truth for playback. Media3 runs it in the
 * foreground while playing and draws the media notification from the session, so the notification,
 * [MusicManager] and every screen show the same state.
 *
 * Besides previous/play/next, the notification has a shuffle toggle and a favourite button. Shuffle is
 * performed here for the Player screen too, so both always agree. Favourites stay in the database,
 * which the heart icon observes.
 *
 * Media3's default `onTaskRemoved` already stops the service when nothing is playing and keeps it
 * running while music plays.
 */
class PlaybackService : MediaSessionService() {

    private val songDataSource: SongLocalDataSource by inject()
    private val applicationScope: CoroutineScope by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val isShuffleOn = MutableStateFlow(false)

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        val sessionBuilder = MediaSession.Builder(this, player).setCallback(SessionCallback())
        // :core:player can't see MainActivity, so the notification opens the app's launcher activity.
        packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
            sessionBuilder.setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }
        val session = sessionBuilder.build()
        mediaSession = session

        observeNotificationButtons(session)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    /** Redraws the shuffle and heart buttons whenever the song, its favourite flag or shuffle changes. */
    @OptIn(UnstableApi::class)
    private fun observeNotificationButtons(session: MediaSession) {
        combine(session.player.currentMediaIds(), songDataSource.songs, isShuffleOn) { songId, songs, isShuffleOn ->
            val isFavorite = songs.find { it.id == songId }?.isFavorite == true
            isShuffleOn to isFavorite
        }
            .distinctUntilChanged()
            .onEach { (isShuffleOn, isFavorite) ->
                session.setMediaButtonPreferences(notificationButtons(isShuffleOn, isFavorite))
            }
            .launchIn(serviceScope)
    }

    private fun notificationButtons(isShuffleOn: Boolean, isFavorite: Boolean): List<CommandButton> = listOf(
        CommandButton.Builder(if (isShuffleOn) CommandButton.ICON_SHUFFLE_ON else CommandButton.ICON_SHUFFLE_OFF)
            .setDisplayName(getString(if (isShuffleOn) R.string.shuffle_off else R.string.shuffle_on))
            .setSessionCommand(PlaybackSessionContract.ToggleShuffleCommand)
            .build(),
        CommandButton.Builder(if (isFavorite) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED)
            .setDisplayName(getString(if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites))
            .setSessionCommand(PlaybackSessionContract.ToggleFavoriteCommand)
            .build(),
    )

    private fun Player.currentMediaIds(): Flow<String?> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                trySend(player.currentMediaItem?.mediaId)
            }
        }
        addListener(listener)
        send(currentMediaItem?.mediaId)
        awaitClose { removeListener(listener) }
    }.distinctUntilChanged()

    private fun MediaSession.setQueueInfo(extras: Bundle) {
        setSessionExtras(extras)
        isShuffleOn.value = extras.isShuffleOn()
    }

    private fun MediaSession.toggleShuffle() {
        val playback = player.toPlaybackState(player.readQueue(), sessionExtras)
        if (playback.currentSong == null) return
        val queue = if (playback.isShuffleOn) playback.originalOrderQueue() else playback.shuffledQueue()
        player.reorderQueue(queue)
        setQueueInfo(PlaybackSessionContract.queueInfoExtras(!playback.isShuffleOn, playback.originalOrder))
    }

    private fun MediaSession.toggleFavorite() {
        val songId = player.currentMediaItem?.mediaId ?: return
        // The write must finish even if the service stops right after the tap.
        applicationScope.launch {
            val song = songDataSource.songs.first().find { it.id == songId } ?: return@launch
            songDataSource.setFavorite(songId, !song.isFavorite)
                .onFailure { error -> Log.w(TAG, "Couldn't update favourite for $songId: $error") }
        }
    }

    private inner class SessionCallback : MediaSession.Callback {

        private val customCommands = listOf(
            PlaybackSessionContract.SetQueueInfoCommand,
            PlaybackSessionContract.ToggleShuffleCommand,
            PlaybackSessionContract.ToggleFavoriteCommand,
        )

        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val builder = MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)
            // Only this app, MusicManager and the media notification, may send the custom commands.
            if (controller.packageName == packageName) {
                builder.setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .apply { customCommands.forEach(::add) }
                        .build(),
                )
            }
            return builder.build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                PlaybackSessionContract.SetQueueInfoCommand.customAction -> session.setQueueInfo(args)
                PlaybackSessionContract.ToggleShuffleCommand.customAction -> session.toggleShuffle()
                PlaybackSessionContract.ToggleFavoriteCommand.customAction -> session.toggleFavorite()
                else -> return super.onCustomCommand(session, controller, customCommand, args)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> {
            val playableItems = mediaItems.map { item ->
                if (item.localConfiguration != null) {
                    item
                } else {
                    item.buildUpon().setUri(item.requestMetadata.mediaUri).build()
                }
            }
            return Futures.immediateFuture(playableItems.toMutableList())
        }
    }

    private companion object {
        const val TAG = "PlaybackService"
    }
}
