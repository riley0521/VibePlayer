# Playback: MusicManager, foreground service, mini player + Player screen

## Context
`MusicPlayer` (`:core:domain`) only has `play(queue)`, and `PlaceholderMusicPlayer` just logs. The mini
player (`LibraryScreen`) and `PlayerScreen` are fully drawn but have no data: `LibraryViewModel`
ignores the playback actions, and `PlayerScreen` has no ViewModel, Root or route.

The goal is real playback: play/pause, next/previous through the queue, a shuffle toggle, and a media
notification. Three surfaces — the mini player, `PlayerScreen` and the notification — must always show
the same state. **The source of truth is the ExoPlayer inside the foreground service's MediaSession.**
That includes the queue order, the shuffle flag and the original order, so the state survives when the
app UI is closed and only the service is running. Everything in the UI is a mirror of that session.

Out of scope (the buttons stay no-ops): seeking, repeat, favourite, add to playlist.

## Architecture
```
PlaybackService (MediaSessionService, :core:player)
  ExoPlayer + MediaSession   ← notification is rendered by Media3 from this session
  session extras: isShuffleOn, originalOrder (song ids)
        ▲  MediaController (bound, same process)
MusicManager : MusicPlayer   (Koin single, :core:player)
  playbackState: StateFlow<PlaybackState>   ← derived only from the controller
        ▲                                ▲
LibraryViewModel → MiniPlayer      PlayerViewModel → PlayerScreen
```
Notification buttons act on the session player → controller listener → StateFlow → both screens.

## 1. `:core:domain` — `player/`
- **`PlaybackState.kt`** (new):
  `queue: List<Song>`, `currentIndex: Int = -1`, `isPlaying`, `positionMillis`, `isShuffleOn`,
  `originalOrder: List<String>` (song ids in the order the queue was started).
  Computed: `currentSong`, `hasPrevious` (`currentIndex > 0`), `hasNext`.
- **`MusicPlayer.kt`** (extend; keep the "callers build the queue" doc):
  ```kotlin
  val playbackState: StateFlow<PlaybackState>
  suspend fun play(queue: List<Song>)          // plays from first song, shuffle off, originalOrder = queue ids
  suspend fun togglePlayPause()
  suspend fun skipToNext()
  suspend fun skipToPrevious()                 // previous item, or restart when on the first
  /** Reorders the queue around the current song without interrupting it. */
  suspend fun reorderQueue(queue: List<Song>, isShuffleOn: Boolean)
  ```

## 2. `:core:player`
- **`build.gradle.kts`**: nothing new is needed. media3-exoplayer and media3-session are already
  there, and Guava's `ListenableFuture` comes with them.
- **`src/main/AndroidManifest.xml`** (new, merged into the app):
  - `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permissions.
  - `<service .PlaybackService exported="true" foregroundServiceType="mediaPlayback">` with the
    `androidx.media3.session.MediaSessionService` intent filter.
  - No `POST_NOTIFICATIONS`, because media-session notifications are exempt from it.
- **`PlaybackSessionContract.kt`**:
  - `SessionCommand` action `SET_QUEUE_INFO`.
  - Bundle keys: `KEY_IS_SHUFFLE_ON`, `KEY_ORIGINAL_ORDER`, plus the MediaItem extras keys.
- **`SongMediaItemMappers.kt`**:
  - `Song.toMediaItem()` sets:
    - `mediaId = id`
    - `MediaMetadata`: title, artist, `artworkUri = imageUri`, `durationMs`, and extras for `fileUri`,
      `imageUri`, `isFavorite`, `createdAt`
    - `RequestMetadata.mediaUri = fileUri`
  - `MediaItem.toSong()` reverses this. The controller only sees metadata, so the queue can always be
    rebuilt from the session.
- **`PlaybackService : MediaSessionService`**:
  - `onCreate`: build an ExoPlayer with music `AudioAttributes` and audio focus handled, and
    `setHandleAudioBecomingNoisy(true)`. Wrap it in a `MediaSession` whose session activity is a
    `PendingIntent` from `packageManager.getLaunchIntentForPackage(packageName)`, since `:core:player`
    can't see `MainActivity`.
  - `MediaSession.Callback`:
    - `onConnect` adds `SET_QUEUE_INFO` to the available commands, but only for controllers from our
      own package.
    - `onCustomCommand(SET_QUEUE_INFO)` calls `session.setSessionExtras(args)`.
    - `onAddMediaItems` restores `setUri(requestMetadata.mediaUri)`, because the URI is stripped when
      items cross from the controller to the session.
  - `onGetSession` returns the session. `onDestroy` releases the player and the session.
    `onTaskRemoved` isn't overridden: Media3 1.11's default already stops the service when nothing is
    playing and keeps it running while playing.
  - The default `MediaNotificationProvider` supplies artwork, title and previous/play/next.
- **`MusicManager : MusicPlayer`** (replaces and deletes `PlaceholderMusicPlayer`). It takes `Context`
  and the app `CoroutineScope`.
  - **Connection**: `awaitController()` connects lazily once, guarded by a `Mutex`, on
    `Dispatchers.Main.immediate`. It is a `suspendCancellableCoroutine` wrapper around
    `MediaController.Builder(...).buildAsync()` using `context.mainExecutor`, and it keeps the
    connection for the life of the process. The builder's `MediaController.Listener.onExtrasChanged`
    pings an internal `MutableSharedFlow<Unit>`.
  - **`playbackState`**: a `flow { emitAll(controller.snapshots()) }` running `flowOn(Main.immediate)`,
    then `stateIn(applicationScope, WhileSubscribed(5_000), PlaybackState())`.
    - `snapshots()` is a `callbackFlow`: a `Player.Listener.onEvents` sends a snapshot, and so does
      the extras ping.
    - While `isPlaying`, a ticker job sends a snapshot every 500ms for the progress bars; it is
      cancelled on pause. `awaitClose` removes the listener.
    - The queue list is rebuilt from `getMediaItemAt(i).toSong()` only when the timeline changes and
      is cached otherwise.
    - `isPlaying` is `playWhenReady && state !in (IDLE, ENDED) && no suppression`, so the icon doesn't
      flicker while buffering.
    - `isShuffleOn` and `originalOrder` are read from `controller.sessionExtras`.
  - **`play`**: `setMediaItems` → `prepare` → `play`, then send `SET_QUEUE_INFO(false, ids)`.
  - **`togglePlayPause`**: pause if playing; otherwise, if `STATE_ENDED`, seek to the default position
    of the current item, then `play`.
  - **`skipToNext`**: `seekToNextMediaItem`.
  - **`skipToPrevious`**: `seekToPreviousMediaItem` if there is a previous item, else `seekTo(0)`.
  - **`reorderQueue`**: find the current song in the new list (index `k`), then:
    - `replaceMediaItems(currentIndex + 1, count, after)`, then
      `replaceMediaItems(0, currentIndex, before)`. Neither range includes the current item, so
      playback isn't interrupted.
    - Send `SET_QUEUE_INFO(isShuffleOn, existing originalOrder)`.
- **`di/CorePlayerModule.kt`**: `singleOf(::MusicManager) { bind<MusicPlayer>() }`.

## 3. `:feature:library:presentation` — mini player
- `LibraryViewModel(musicLibraryRepository, songDataSource, musicPlayer)`:
  - Collect `playbackState` into `nowPlaying = currentSong?.let { NowPlayingUi(it.toSongUi(), isPlaying, positionMillis, canSkipToPrevious = hasPrevious) }`.
  - `OnPlayPauseClick`, `OnSkipToPreviousClick` and `OnSkipNextClick` delegate to the player.
    `OnSeek` stays a no-op because seeking is out of scope.
- `LibraryRoot` gets `onMiniPlayerClick: () -> Unit` and handles `OnMiniPlayerClick` there.
  `libraryGraph(navController, onMiniPlayerClick)` passes it through (`LibraryNavigation.kt`).
- `LibraryPresentationModule` stays the same, because `viewModelOf` picks up the new parameter.

## 4. `:feature:player:presentation` — Player screen
- **`QueueOrder.kt`** holds pure functions, so the shuffling stays in the ViewModel layer, per the
  queue rule:
  - `PlaybackState.shuffledQueue(random: Random = Random)`: `queue.take(currentIndex + 1)` plus the
    upcoming songs shuffled.
  - `PlaybackState.originalOrderQueue()`: the queue sorted by position in `originalOrder`, with
    unknown ids last. The current song lands back at its original place.
- **`PlayerState`**: `song: SongUi? = null`, and the other fields stay. `PlayerScreen` keeps its top
  bar and renders the artwork and transport only when `song != null`, which covers process death with
  an empty session. Update the previews.
- **`PlayerViewModel(musicPlayer)`**: collects `playbackState` into `PlayerState` (song, isPlaying,
  positionMillis, isShuffleOn). Actions:
  - play/pause, next and previous delegate to the player;
  - `OnShuffleClick`: when there is a current song, call
    `reorderQueue(if on originalOrderQueue() else shuffledQueue(), !isShuffleOn)`;
  - seek, repeat, favourite and add-to-playlist do nothing (`Unit`), with a comment saying they are
    out of scope.
- **`PlayerScreen.kt`**: add `PlayerRoot(onNavigateBack, viewModel = koinViewModel())` in the same
  file. `OnBackClick` calls `onNavigateBack`.
- **`PlayerNavigation.kt`**: `@Serializable PlayerGraph` and `PlayerRoute`, plus
  `NavGraphBuilder.playerGraph(onNavigateBack)`, copying `PermissionNavigation.kt`.
- **`di/PlayerPresentationModule.kt`**: `viewModelOf(::PlayerViewModel)`.

## 5. `:app`
- `NavigationRoot`:
  - `libraryGraph(navController, onMiniPlayerClick = { navController.navigate(PlayerGraph) { launchSingleTop = true } })`
  - `playerGraph(onNavigateBack = { navController.navigateUp() })`
- `VibePlayerApp`: add `playerPresentationModule`.

## Behaviour decisions
- Starting a new queue (Songs tab Play, Shuffle or a song tap) turns shuffle off and records the new
  order as the original. The Songs-tab Shuffle is already a shuffled queue, so turning the toggle off
  afterwards leaves that order as it is.
- Shuffle on reorders only the upcoming songs, and already-played songs keep their place. Shuffle off
  restores the whole original order around the current song.
- When the queue ends, the last song stays in the mini player, paused. Play restarts it.

## Tests (JUnit5 + AssertK + Turbine, fakes, `UnconfinedTestDispatcher`)
- `feature/player/presentation/src/test/.../QueueOrderTest.kt`:
  - Shuffle keeps the history and the current song, only permutes the upcoming songs (seeded
    `Random`), and handles having no upcoming songs.
  - The original order restores the positions, puts the current song at its original index, and puts
    unknown ids last.
- `PlayerViewModelTest.kt` with a new `fakes/FakeMusicPlayer.kt` (a `MutableStateFlow<PlaybackState>`
  that records calls):
  - state mapping;
  - play/pause, next and previous delegate to the player;
  - shuffle on and off call `reorderQueue` with the right list and flag;
  - shuffle does nothing when there is no current song.
- `LibraryViewModelTest`: pass the fake player. Check that `nowPlaying` is null when idle, that it maps
  song, isPlaying, position and canSkipToPrevious, and that the mini-player actions delegate.
- Extend the library `fakes/FakeMusicPlayer.kt` with the new members, so `SongsViewModelTest` keeps
  compiling unchanged.
- There are no JVM tests for `MusicManager`, `PlaybackService` or the MediaItem mappers: they need
  Media3 and Android `Bundle`s. The user checks them on a device.

## After approval
- Save a memory: VibePlayer's playback state (queue order, shuffle, and later repeat) lives in the
  service's MediaSession, so it survives with only the foreground service running. The UI and
  `MusicManager` only mirror it.

## Verification
1. `./gradlew assembleDebug`
2. `./gradlew testDebugUnitTest`
3. Manual checks by the user, on a device:
   - Tap a song: the mini player and a notification appear.
   - Play/pause and next from the notification: the mini player and Player screen follow.
   - The mini player opens the Player screen.
   - Turn shuffle on, swipe the app away while it plays, reopen from the notification: shuffle is
     still on and the queue order is unchanged.
