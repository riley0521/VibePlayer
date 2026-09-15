# VibePlayer

An Android music player that runs entirely offline (Kotlin, Jetpack Compose, Material 3). It scans the device's `Music/` folder, stores songs and playlists in Room, and plays audio through Media3. The app never uses the network. `minSdk 28`, `compileSdk`/`targetSdk 37`, package `com.rfcoding.vibeplayer`.

> **Current state:** every module under "Target module layout" exists and is wired into `:app`, but most contain no code yet. The UI, however, is done: all screens and all design-system components are already implemented, so there is no need to open Figma unless the user asks for it. Build config lives in `:build-logic` convention plugins, applied as `alias(libs.plugins.vibeplayer.<name>)`:
> `android.application`, `android.library`, `android.feature` (library + Compose + Koin + serialization, plus `:core:domain`, `:core:presentation`, `:core:design-system`, navigation and lifecycle-compose), `domain.module` (pure Kotlin; its `testDebugUnitTest` task aliases `test`), `compose`, `koin`, `room` and `kotlinx.serialization`.
> Apply these plugins instead of writing Android config by hand, and add every new dependency to `gradle/libs.versions.toml`.

## Where to find things

| Resource | Location | When to load it |
|---|---|---|
| Requirements (the source of truth for behavior) | `specs/vibe-player-requirements.md` | Before any feature work, read the section for the screen you're changing. It is deliberately **not** auto-imported. |
| Figma designs | Links in the spec's **Figma links** section. Each screen names its Figma layers, and mobile and tablet layers share the same names. | **Only when the user explicitly asks.** Every screen and every reusable component has already been built, so the existing composables are the source of truth for layout, spacing, colors and type — copy a comparable one instead of fetching a design. When the user does ask, fetch through the project-scoped `figma@claude-plugins-official` plugin; if the Figma tools aren't loaded, ask the user to authenticate with `/mcp`, and never guess a design. |
| App font (Host Grotesk, variable) | `specs/fonts/Host_Grotesk/HostGrotesk-VariableFont_wght.ttf` | When setting up the design system: copy it into `res/font` of `:core:design-system`. |
| Project skills | `.claude/skills/<skill>/SKILL.md` | See "Skills" below. |

## Commands

```bash
./gradlew assembleDebug                                  # must compile
./gradlew testDebugUnitTest                              # all JVM unit tests
./gradlew :core:data:testDebugUnitTest                   # one module
./gradlew :core:domain:test --tests "*PlaylistNameValidatorTest"
```

Don't launch an emulator or device. The user checks the UI manually.

## Target module layout

```
:app                               VibePlayerApp (startKoin, applicationScope), MainActivity, NavHost, splash
:build-logic                       convention plugins: android-application, android-library, android-feature,
                                   domain-module, compose, koin, room, kotlinx-serialization
:core:domain                       Result/DataError, Song, Playlist, SongRepository, PlaylistRepository, MusicPlayer,
                                   PlaylistNameValidator
:core:data                         implementations of the core repositories/data sources, entity<->domain mappers
:core:database                     VibePlayerDatabase, SongEntity, PlaylistEntity, PlaylistSongCrossRef, DAOs
:core:player                       Media3 ExoPlayer + MediaSessionService, MediaController-backed MusicPlayer
:core:presentation                 UiText, ObserveAsEvents, DataError.toUiText(), MiniPlayer, DeviceConfiguration,
                                   create/rename playlist sheet (shared by library and player)
:core:design-system                theme, colors, Host Grotesk typography, icons, reusable components
:core:testing                      pure Kotlin; shared fakes of :core:domain interfaces (FakeMusicPlayer,
                                   FakeSongLocalDataSource, FakePlaylistLocalDataSource) and song()/playlist()
                                   builders. Add it with testImplementation; never copy a fake into a module.
:feature:permission:presentation   permission screen
:feature:library:{domain,data,presentation}
                                   main screen (songs + playlist tabs), playlist page, search, scan music,
                                   add-songs screen; MediaStore scanner, scan filters
:feature:player:presentation       full player screen, add-to-playlist, favorite toggle
```

- Both `library` and `player` use songs and playlists, so their models and interfaces live in `:core:domain`, with implementations in `:core:data`. Logic only the library needs stays in `:feature:library:*`.
- Features never depend on each other. Navigation between features goes through callbacks wired up in `:app`.

## Domain rules (already decided; don't reopen them)

- **Offline only.** No Ktor, no tokens, no `HttpClient`/`safeCall`: skip those parts of the data-layer and error-handling skills. `DataError.Local` is the only data error.
- **Permission**: `READ_MEDIA_AUDIO` on API 33+, `READ_EXTERNAL_STORAGE` on API 32 and below. Show the permission screen until it's granted.
- **Scanning**: only `.mp3` files **directly inside** the device's `Music/` folder count; subfolders are ignored. Read metadata with `MediaMetadataRetriever`. Store `fileUri` as a string, never the file contents.
- **Song ID = file name** (e.g. `song.mp3`). Rescans upsert by this ID.
- **Rescan = upsert + prune**, in one DAO `@Transaction`:
  - upsert every file that passes the filters, keeping `isFavorite` and playlist links;
  - delete rows whose file is gone or no longer passes the filters.

  The filters come from the Scan music screen: minimum duration 30s or 60s, minimum size 100KB or 500KB.
- **Song**: `id`, `name`, `artistName?`, `fileUri`, `imageUri?`, `durationMillis`, `isFavorite`, `createdAt`.
- **Playlist**: `name`, `createdAt`. Linked to songs many-to-many through `PlaylistSongCrossRef`, and sorted newest first.
- **Favourites** is a *virtual* playlist built from `isFavorite = true`. It is not a database row, is always listed first and can't be deleted.
- **Playlist name**: trimmed, 1–40 characters. The Create button is disabled while the name is blank.
- **Playback**: Media3 `ExoPlayer` runs inside a `MediaSessionService` in `:core:player`, which gives background playback and a media notification. The UI talks only to the `MusicPlayer` interface in `:core:domain`, which exposes a `StateFlow` and is backed by a `MediaController`.
- **Songs tab**: Shuffle plays a random song, Play starts the first song, and the scroll-to-top FAB appears once the user scrolls.
- **Search**: an empty query shows all songs.
- **Splash**: use the Splash Screen API with the brand background and logo.
- **Adaptive layouts**: mobile and tablet layouts switch on `DeviceConfiguration`.

## Skills: how they're applied here

Apply these proactively, and use any other available skill when it fits (e.g. `simplify`, `code-review`).

| Skill | Use when | Applied in VibePlayer as |
|---|---|---|
| `android-module-structure` | Adding a module or Gradle config | The layout above; `:build-logic` convention plugins; every version in `libs.versions.toml` |
| `android-presentation-mvi` | Any screen or ViewModel | State/Action/Event, Root + Screen in one file, `UiText`; `DialogSheetScopedViewModel` for the create-playlist sheet; `SavedStateHandle` for the playlist name and search query |
| `android-compose-ui` | Any composable | `DeviceConfiguration` for mobile/tablet; `key = song.id` in lists; scroll-to-top FAB via `derivedStateOf`; seek-bar and animation values read in `graphicsLayer` or lambdas |
| `android-data-layer` | Repositories, data sources, entities, mappers | Name a class `*Repository` only when it combines MediaStore and Room, otherwise `*DataSource`; names say what the class wraps (`RoomPlaylistDataSource`), never `Impl` |
| `android-error-handling` | Any operation that can fail | `Result`/`EmptyResult`/`DataError.Local` in `:core:domain`; feature errors such as `PlaylistNameError`; `toUiText()` for every error the user sees |
| `room-convention` | Any Room work | Multi-query `@Transaction` functions live in the DAO (rescan upsert + prune, add songs to a playlist); never inject the DB into repositories; wrap writes in `safeDatabaseUpdate`; no try/catch around reads |
| `coroutines-convention` | Coroutines and Flow | `applicationScope` in `VibePlayerApp`, provided by Koin; `callbackFlow` for player listeners; a suspend wrapper for the `MediaController` future; `ensureActive()` after every `MediaMetadataRetriever` call |
| `android-di-koin` | Wiring dependencies | One module per layer (`libraryDataModule`, …) assembled in `:app`; `koinViewModel()` only in Root composables |
| `android-navigation` | Routes and graphs | `@Serializable` routes; `PermissionGraph`, `LibraryGraph`, `PlayerGraph`; cross-feature callbacks in `:app` (e.g. mini player → player) |
| `android-testing` | Any change with logic | JUnit5 (`useJUnitPlatform()` set in the convention plugin), AssertK, Turbine, fakes rather than mocks, `UnconfinedTestDispatcher` |

## Using subagents

Use Sonnet subagents for tasks that require broad exploration or investigation. Run **at most 2** subagents at a time.

Spawn a Sonnet subagent when:
- Exploring core modules (e.g. core:data, core:designsystem, core:domain, core:presentation). I notice you keep checking it since most of the classes that a feature use stays here.
- Investigating how an interface repository was implemented, check utility functions, what reusable composables are already available.
- Using the Figma MCP to inspect designs, screens, components, or design specifications.

Prefer subagents for exploration and information gathering so the main agent's context remains focused on writing solid plan and implementation task.

Do not spawn a subagent for simple, localized changes where the relevant files are already known.

## Definition of done

A task is finished only when:

1. `./gradlew assembleDebug` succeeds.
2. `./gradlew testDebugUnitTest` passes.
3. New logic has unit tests wherever they add value:
   - repositories and data sources, with fake DAOs or a fake scanner;
   - input validators, e.g. the playlist name;
   - pure functions with non-trivial logic: scan filters, the "directly inside `Music/`" path rule, the upsert/prune diff, search filtering, duration formatting, shuffle and queue logic;
   - ViewModels with real logic.

   Skip tests for trivial mappers and purely visual UI.
