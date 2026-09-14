# Notification buttons: shuffle toggle + favourite

## Context
The media notification only shows Media3's default previous/play/next. The user wants the notification
to have two extra buttons:
- a **shuffle toggle**;
- an **add/remove favourite** button, like Spotify.

Decisions from the user:
- **One path for shuffle, in the service.** The notification has no ViewModel, so the session owner
  shuffles. The Player screen goes through the same command, so the two can never disagree. This
  replaces the "PlayerViewModel builds the order" approach for the toggle only; the Songs-tab Shuffle
  button still builds its shuffled queue in `SongsViewModel`.
- **Favourite** in the notification, not repeat.
- **Artwork:** no change. `Song.toMediaItem()` already sets `artworkUri` (a `file://` URI from
  `MediaStoreMusicScanner.saveArtwork`). Media3's default bitmap loader turns it into the notification
  picture. Songs without embedded art keep showing no image.

Favourites live in Room (`Song.isFavorite`, and the virtual Favourites playlist is built from it). The
**database stays the source of truth** for favourites; it isn't copied into the session. The service
and the Player screen both write through `SongLocalDataSource.setFavorite` and both observe
`SongLocalDataSource.songs`. That means the notification heart, the Player screen heart and the
Favourites playlist always agree. The Player screen's heart, a no-op until now, becomes live in this
task so it stays in sync with the notification.

## `:core:domain`
- **Move `QueueOrder.kt`** from `feature/player/presentation` to `core/domain/.../player/QueueOrder.kt`.
  Its functions are `PlaybackState.shuffledQueue(random)` and `PlaybackState.originalOrderQueue()`, and
  they become public. Move `QueueOrderTest` to `core/domain/src/test/.../player/` too; it uses a local
  `song()` helper, as `PlaybackStateTest` does.
- **`MusicPlayer`**: replace `reorderQueue(queue, isShuffleOn)` with `suspend fun toggleShuffle()`.
  Nothing else calls `reorderQueue`.

## `:core:player`
- **`PlayerQueue.kt`** (new): `Player` extensions shared by `MusicManager` (a `MediaController`) and the
  service (the `ExoPlayer`):
  - `readQueue()` and `toPlaybackState(queue, sessionExtras)`, moved out of `MusicManager.snapshot`;
  - `reorderQueue(queue)`, moved out of `MusicManager.reorderQueue`: the two `replaceMediaItems` calls
    around the current item.
- **`PlaybackSessionContract.kt`**: add `ToggleShuffleCommand` and `ToggleFavoriteCommand`.
  `SetQueueInfoCommand` stays, because `play()` still resets shuffle and records the original order.
- **`MusicManager.kt`**:
  - `toggleShuffle()` sends `ToggleShuffleCommand`.
  - Remove `reorderQueue`, and use the `PlayerQueue.kt` helpers in `playbackStates()`.
- **`PlaybackService.kt`**:
  - **Dependencies:** inject `SongLocalDataSource` and the app `CoroutineScope` with Koin's
    `by inject()`. Koin is already on the classpath through the koin convention plugin. Add a
    `serviceScope` (`SupervisorJob + Dispatchers.Main.immediate`), cancelled in `onDestroy`.
  - **`onConnect`:** make `ToggleShuffleCommand` and `ToggleFavoriteCommand` available, together with
    `SetQueueInfoCommand`, for controllers from our own package. That includes Media3's
    media-notification controller.
  - **`onCustomCommand`:**
    - `ToggleShuffle`: build a `PlaybackState` from the ExoPlayer and the session extras. Then
      `player.reorderQueue(if on originalOrderQueue() else shuffledQueue())`, followed by
      `session.setSessionExtras(queueInfoExtras(!on, originalOrder))`. This all runs on the main thread
      against the real player, so it is atomic.
    - `ToggleFavorite`: take the current `mediaId` and launch on `applicationScope`, so the write
      finishes even if the service stops. Read the song's current flag from
      `songDataSource.songs.first()` and call `setFavorite(id, !isFavorite)`. A failure is logged, since
      the notification has no place to show it.
  - **Button state:** in `onCreate`, `serviceScope` collects
    `combine(currentMediaIdFlow, songDataSource.songs, shuffleChanges)` and derives
    `(isShuffleOn, isFavorite)`, then applies `distinctUntilChanged()`.
    - `currentMediaIdFlow` is a `callbackFlow` on the player listener.
    - `shuffleChanges` is a `MutableStateFlow` set wherever the service writes the extras.
    - On each change it calls `session.setMediaButtonPreferences(buttons)`, which needs
      `@OptIn(UnstableApi::class)`.
  - **Buttons**, `CommandButton.Builder(icon).setDisplayName(...).setSessionCommand(...)`, with the
    default overflow slot:
    - Shuffle: `ICON_SHUFFLE_ON` or `ICON_SHUFFLE_OFF`.
    - Favourite: `ICON_HEART_FILLED` or `ICON_HEART_UNFILLED`.
    - Media3's `DefaultMediaNotificationProvider.getMediaButtons` keeps previous/play/next and appends
      these two after next.
  - Since `onCustomCommand` for `SetQueueInfo` also changes shuffle, it updates `shuffleChanges`.
- **`res/values/strings.xml`** (new): `shuffle_on`, `shuffle_off`, `add_to_favorites` and
  `remove_from_favorites`, used as the buttons' content descriptions.

## `:feature:player:presentation`
- **Delete `QueueOrder.kt`**; it moves to domain.
- **`PlayerViewModel(musicPlayer, songDataSource)`**:
  - Combine `playbackState` with `songDataSource.songs`, so `isFavorite` comes from the database row of
    the current song.
  - `OnShuffleClick` calls `musicPlayer.toggleShuffle()`.
  - `OnFavoriteClick` calls `songDataSource.setFavorite(id, !state.isFavorite)`.
    `.onFailure { eventChannel.send(PlayerEvent.Error(it.toUiText())) }`.
  - Update the "comes later" comment to list only seek and add to playlist.
- **`PlayerEvent.kt`** (new): `data class Error(val message: UiText)`. `PlayerRoot` shows it as a Toast
  through `ObserveAsEvents`, copying `LibraryRoot` (`LibraryScreen.kt:368`).
- **`PlayerPresentationModule`** stays the same, because `viewModelOf` resolves the new parameter.

## Tests
- `core/domain/.../player/QueueOrderTest.kt` (moved, unchanged cases).
- **`PlayerViewModelTest`**, with a new player-feature `fakes/FakeSongLocalDataSource.kt` copied from
  the library fakes, plus a switchable failure result:
  - `isFavorite` follows the database row of the current song, and changes when the row changes;
  - favourite click calls `setFavorite` with the flipped flag;
  - a failed favourite write sends `PlayerEvent.Error`;
  - favourite click does nothing without a current song;
  - shuffle click calls `toggleShuffle` (replacing the two `reorderQueue` tests).
- Fakes:
  - player `FakeMusicPlayer` counts `toggleShuffle` calls instead of recording `reorderedQueues`;
  - library `FakeMusicPlayer` gets a no-op `toggleShuffle`.
- Not unit-tested: `PlaybackService`, `PlayerQueue.kt` and the notification buttons, which need
  Media3 and Android. The shuffle logic itself stays covered by `QueueOrderTest`.

## After approval
- Update the memory `playback-queue-rule.md`: the shuffle *toggle* is performed by `PlaybackService`
  through `MusicPlayer.toggleShuffle()`, because the notification has no ViewModel. Queues started
  from lists are still built and shuffled by their ViewModels.
- Save this plan as `docs/playback-notification-buttons-plan.md`.

## Verification
1. `./gradlew assembleDebug`
2. `./gradlew testDebugUnitTest`
3. Manual checks by the user, on a device:
   - The notification shows previous/play/next plus shuffle and heart, and the artwork appears for
     songs that have it.
   - Shuffle from the notification changes the icon, and the Player screen's shuffle turns on as well.
   - Heart from the notification fills, and the Player screen's heart and the Favourites playlist
     count follow. The reverse works too, starting from the Player screen.
   - Swipe the app away while playing: both notification buttons still work.
