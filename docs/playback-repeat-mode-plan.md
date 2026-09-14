# Repeat mode for the Player screen

## Context
The previous task shipped playback: `MusicManager` mirrors the MediaSession in `PlaybackService`, and
the mini player, the Player screen and the notification all stay in sync. The Player screen's repeat
button is already drawn: its icon cycles through `RepeatMode.Off/All/One` in `PlayerScreen.kt:229`.
But `PlayerAction.OnRepeatClick` is a no-op, and `PlayerState.repeatMode` is never set.

The goal is to make repeat work. Like shuffle, its state lives in the session, so it survives when
only the foreground service is running. Unlike shuffle it needs no session extras, because ExoPlayer
stores the repeat mode itself. The controller mirrors it, and a change fires the listener that already
feeds `playbackState`.

## Behaviour
- Tapping the button cycles **Off → All → One → Off**, matching the existing icons.
- **All**: the queue loops. Next on the last song goes to the first, and previous on the first song
  goes to the last. In that case the mini player shows its previous button on the first song.
- **One**: the current song loops when it ends. Next and previous still move between songs, because
  Media3 treats One as Off for manual navigation (`MediaControllerImplBase`
  `getRepeatModeForNavigation`).
- The repeat mode carries over when a new queue is started from the Songs tab. That is ExoPlayer's
  behaviour, and it differs from shuffle, which resets because the caller already built the order.
- No repeat button in the notification, because Media3's default layout has none. The mini player has
  no repeat UI.

## Changes

### `:core:domain` — `player/`
- **`RepeatMode.kt`** (new): `enum class RepeatMode { Off, All, One }`. This moves the enum out of
  `feature/player/presentation/PlayerState.kt` so the player and the screen share one type.
- **`PlaybackState.kt`**:
  - Add `repeatMode: RepeatMode = RepeatMode.Off`.
  - `hasPrevious`: `currentIndex > 0 || (repeatMode == All && queue.isNotEmpty())`.
  - `hasNext`: `currentIndex in 0 until queue.lastIndex || (repeatMode == All && queue.isNotEmpty())`.
- **`MusicPlayer.kt`**: add `suspend fun setRepeatMode(repeatMode: RepeatMode)`.

### `:core:player` — `MusicManager.kt`
- `setRepeatMode`: `withController { it.repeatMode = repeatMode.toMedia3() }`.
- `snapshot()`: set `repeatMode = repeatMode.toRepeatMode()`. The existing `onEvents` listener already
  sends a snapshot on `EVENT_REPEAT_MODE_CHANGED`.
- Private mappers `RepeatMode.toMedia3()` and `Int.toRepeatMode()` (`Player.REPEAT_MODE_*`).
- `skipToNext` and `skipToPrevious` stay as they are: `hasPreviousMediaItem` and `seekToNextMediaItem`
  already follow the repeat mode.

### `:feature:player:presentation`
- **`PlayerState.kt`**: remove the local `RepeatMode` enum and import the domain one.
- **`PlayerScreen.kt`**: import the domain `RepeatMode`; the icon and tint logic stay unchanged.
- **`PlayerViewModel.kt`**:
  - Map `repeatMode = playback.repeatMode`.
  - `OnRepeatClick` → `musicPlayer.setRepeatMode(playback.repeatMode.next())`.
  - A private `RepeatMode.next()` does Off → All → One → Off.
  - Update the "comes later" comment so it no longer lists repeat.

### Fakes
- `feature/player/presentation/src/test/.../fakes/FakeMusicPlayer.kt`: record `repeatModes` in a list.
- `feature/library/presentation/src/test/.../fakes/FakeMusicPlayer.kt`: a no-op `setRepeatMode`.

## Tests
- **`core/domain/src/test/.../player/PlaybackStateTest.kt`** (new; the domain plugin already sets up
  JUnit5 and aliases `testDebugUnitTest`):
  - `hasPrevious` and `hasNext` at the first, middle and last song with Off.
  - All wraps at both ends.
  - One behaves like Off.
  - An empty queue has neither.
- **`PlayerViewModelTest`**:
  - State maps `repeatMode`.
  - Repeat click sets All from Off, One from All, and Off from One.
- **`LibraryViewModelTest`**: the first song can skip to previous when repeat is All.

## Docs
- Save this plan as `docs/playback-repeat-mode-plan.md`, next to the other plans.

## Verification
1. `./gradlew assembleDebug`
2. `./gradlew testDebugUnitTest`
3. Manual checks by the user, on a device:
   - Cycle repeat on the Player screen; the icon follows.
   - With All, next on the last song plays the first.
   - With One, the song restarts when it ends.
   - Close the Player screen and reopen it, or swipe the app away while playing and reopen it from the
     notification: the repeat mode is kept.
