# Plan: `:feature:downloader` — download YouTube audio into `Music/`

## Context
`specs/music-downloader-feature.md` asks for a second top-level destination, **Downloader**, next to
**Library**. The user pastes a YouTube link to a video or a playlist and sees the results as song cards
showing the thumbnail, title and artist. Each card has either a download button or a check mark if the
song is already in the library. Playlists also get a "Download all" FAB. Files are MP3s with an
embedded thumbnail and metadata, named `[title].mp3`, and are saved to the same `Music/` folder the
scanner reads.

Decisions from the interview:
- **Engine:** youtubedl-android (yt-dlp + Python + FFmpeg), the same stack ytdlnis uses.
- **Network:** the network is allowed only in `:feature:downloader`. Everything else stays offline.
  Update CLAUDE.md and the requirements spec to say so.
- **Background:** each download is a foreground WorkManager job with a progress notification, so it
  survives leaving the tab or backgrounding the app.
- **Adding to the library:** after a download, only that file is read and upserted, and the scan
  filters are ignored. A later rescan applies the filters as usual.
- **Design:** no Figma. The screen is built from existing components.
- **Metadata:** use YouTube Music's `track`/`artist` fields when they exist. Otherwise use the video
  title and the channel name with a trailing ` - Topic` removed.
- **Artwork:** the thumbnail is center-cropped to a square before it is embedded.
- **Navigation:** a bottom bar on mobile and a navigation rail on tablet, switched on `DeviceConfiguration`.

Decisions I made (flag any you want changed):
- The mini player also shows on the Downloader tab.
- Back from Downloader returns to Library.
- Opening the Player from either tab returns to that tab.
- Downloads run one at a time.
- If `Music/[title].mp3` already exists (same title, different artist), the new file is named
  `[title] - [artist].mp3` (the user's choice). If that name is also taken, or the artist is
  unknown, fall back to `[title] (1).mp3`.
- ABI filters are `arm64-v8a` and `x86_64`, which covers phones and emulators and keeps the APK smaller.
- The yt-dlp binary is updated at most once a day, when the Downloader opens.

## Step 0
Save this plan as `docs/music-downloader-plan.md`, next to the existing `docs/*-plan.md` files.
Also add the file-name collision rule to `specs/music-downloader-feature.md`.

## Modules & build
- `settings.gradle.kts`: include `:feature:downloader:domain`, `:data` and `:presentation`, following the library pattern. Wire all three into `app/build.gradle.kts`.
- `gradle/libs.versions.toml`: add these entries, checking each version at implementation time:
  - `youtubedl-android` `library` and `ffmpeg` (`io.github.junkfood02.youtubedl-android`);
  - `androidx-work-runtime-ktx`;
  - `koin-androidx-workmanager`;
  - `coil-network-okhttp` (Coil 3 can't load `https` thumbnails without it).
- Convention plugins:
  - domain uses `vibeplayer.domain.module`;
  - data uses `vibeplayer.android.library` + `koin` + `kotlinx.serialization` and depends on `core.domain`, `core.data` and downloader domain;
  - presentation uses `vibeplayer.android.feature` + `coil-network-okhttp`.
- `app/build.gradle.kts`:
  - `ndk { abiFilters += listOf("arm64-v8a", "x86_64") }`;
  - `packaging { jniLibs.useLegacyPackaging = true }`;
  - R8 keep rules for `com.yausername.**` and its Jackson dependency, because release has `optimization.enable = true`.
- `app/src/main/AndroidManifest.xml`:
  - `android:extractNativeLibs="true"`, which youtubedl-android needs;
  - remove WorkManager's default `androidx.startup` initializer, because Koin supplies the `WorkerFactory`.
- `feature/downloader/data/src/main/AndroidManifest.xml`:
  - permissions: `INTERNET`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`, `POST_NOTIFICATIONS`, and `WRITE_EXTERNAL_STORAGE` with `maxSdkVersion=28`;
  - a `SystemForegroundService` merge entry with `foregroundServiceType="dataSync"`.
- `VibePlayerApp`: add `workManagerFactory()` and the new Koin modules
  (`downloaderDataModule`, `downloaderPresentationModule`).

## Shared refactors (reuse, not duplication)
1. **Per-file metadata reader → `:core:data`.** Move the body of
   `MediaStoreMusicScanner.readSong`/`saveArtwork`
   (`feature/library/data/.../scanner/MediaStoreMusicScanner.kt`) into a concrete
   `core/data/.../song/MusicFileReader.kt`. It reads the title, artist and duration, calls
   `ensureActive()` after the `MediaMetadataRetriever` call, and writes artwork to
   `filesDir/artwork/<mediaId>`. The scanner keeps the duration filter and the `(title, artist)` dedup
   on top. The downloader uses the same reader, so a downloaded song gets exactly the row a rescan
   would produce. The upsert merges on `(title, artistName)` (`SongSyncDiff.kt`), so rescans won't
   create duplicates or lose favourites.
2. **Mini-player model → `:core:presentation`.** Move `NowPlayingUi` (`LibraryState.kt`) and
   `PlaybackState.toNowPlayingUi()` (`LibraryViewModel.kt`) next to `MiniPlayer.kt`, so Library and
   Downloader share them.
3. **Navigation components → `:core:design-system`.** Add `VibeNavigationBar` and
   `VibeNavigationRail` (items: label + icon, selected index, `onItemClick`), styled after
   `VibeTabRow.kt`, with Mobile/Tablet previews. Library needs an icon; add a vector if none fits.

## `:feature:downloader:domain` (pure Kotlin, fully unit-tested)
- **Models:**
  - `RemoteTrack(videoId, title, artistName?, thumbnailUrl?, durationMillis?)`.
  - `DownloadStatus`: `Queued`, `Downloading(progress: Float)`, `Failed(DownloadError)`.
  - `DownloadError : Error`. Values: `NO_INTERNET`, `INVALID_LINK`, `UNAVAILABLE`,
    `EXTRACTOR_FAILED`, `DISK_FULL`, `STORAGE_PERMISSION_DENIED`, `UNKNOWN`. This is a feature error,
    so `DataError` stays `Local`-only.
- **Interfaces:**
  - `MediaLinkResolver.resolve(url): Result<List<RemoteTrack>, DownloadError>`;
  - `DownloadQueue`: `enqueue(tracks)`, `statuses: Flow<Map<String, DownloadStatus>>` keyed by `videoId`, `cancel(videoId)`.
- **Pure functions:**
  - `isYoutubeLink(url)`: accepts `watch?v=`, `youtu.be/`, `shorts/`, `music.youtube.com` and `playlist?list=`.
  - `resolveTrackMetadata(track, artist, title, channel)`: uses the Music fields first, then falls back to title/channel and strips ` - Topic`.
  - `isInLibrary(track, songs)`: trimmed, case-insensitive `(title, artist)` match, with a null artist treated as `""`.
  - `tracksToDownload(tracks, songs, statuses)`: skips songs already in the library, queued or downloading.
  - `sanitizeFileName(text)`: strips `/\:*?"<>|` and control characters, trims, caps the length and falls back to the video id.
  - `mp3FileName(title, artist, isTaken: (String) -> Boolean)`: returns `[title].mp3`; if that's taken, `[title] - [artist].mp3`; if that's also taken or the artist is null, `[title] (n).mp3`.
  - `ytDlpLiteral(text)`: escapes `%` → `%%` and `:` → `\:` for `--parse-metadata`.
  - `classifyYtDlpError(stderr)`: maps yt-dlp error text to a `DownloadError`.

## `:feature:downloader:data`
- `YoutubeDlEngine`: a single instance that lazily runs `YoutubeDL.init` + `FFmpeg.init` on
  `Dispatchers.IO` under a `Mutex`. It also runs `updateYoutubeDL(STABLE)` at most once every 24 hours
  and stores the timestamp in SharedPreferences.
- `YoutubeDlLinkResolver`:
  - runs `yt-dlp -J --flat-playlist --no-warnings <url>`;
  - parses the output into `@Serializable` DTOs (a single video or a playlist's `entries`);
  - maps them to `RemoteTrack` through `resolveTrackMetadata`;
  - wraps exceptions with `classifyYtDlpError`.
- `WorkManagerDownloadQueue`:
  - enqueues each track as a `DownloadWorker` in one unique chain (`APPEND_OR_REPLACE`), so downloads run one at a time;
  - tags each work with its `videoId`;
  - `statuses` comes from `getWorkInfosFlow` and maps progress data to `DownloadStatus`, with finished work dropped.
- `DownloadWorker` (a `CoroutineWorker` created through Koin):
  1. `setForeground(ForegroundInfo(dataSync))` with a notification that shows the title and progress. It uses its own channel.
  2. Runs yt-dlp with these options, reporting progress through the `YoutubeDL` callback → `setProgress`:
     - `-x --audio-format mp3 --audio-quality 0`
     - `--embed-thumbnail --convert-thumbnails jpg`
     - the ytdlnis square-crop `--ppa "ThumbnailsConvertor+FFmpeg_o:… crop=…"`
     - `--embed-metadata`
     - `--parse-metadata` with the **same** title and artist the card showed, passed as input data through `ytDlpLiteral`. The tags, the database row and the match check then all use one source.
     - `-o cacheDir/downloads/<videoId>.%(ext)s`
  3. `MusicFolderWriter` copies the file into `Music/` under the name from `mp3FileName(title, artist, isTaken)`. `isTaken` checks MediaStore for a `DISPLAY_NAME` in `Music/` on API 29+, and `File.exists()` on API 28. We pick the name ourselves, so MediaStore's automatic ` (1)` renaming doesn't apply.
     - **API 29+:** MediaStore insert with `RELATIVE_PATH = "Music/"` and `IS_PENDING`, following the pattern in `core/data/.../image/MediaStoreImageGallery.kt`.
     - **API 28:** a direct file write, then `MediaScannerConnection.scanFile`.
     - The content URI is built the same way the scanner builds it.
     - The temp file is deleted in `finally`.
  4. `DownloadedSongImporter` reads the file with `MusicFileReader`, builds a `Song` (UUID id,
     `createdAt = now`), and calls `SongLocalDataSource.upsertScannedSongs(listOf(song))`.
  5. On failure it returns `Result.failure(workDataOf(error))`.
- `di/DownloaderDataModule.kt`: the engine, resolver, queue, writer, importer and `worker { DownloadWorker(...) }`.

## `:feature:downloader:presentation`
- **Navigation:** `@Serializable DownloaderGraph` / `DownloaderRoute` and `NavGraphBuilder.downloaderGraph(mainNavigation, onOpenPlayer)`.
- **MVI** (`DownloaderState`, `DownloaderAction`, `DownloaderEvent`, `DownloaderViewModel`, and `DownloaderRoot` + `DownloaderScreen` in one file):
  - **State:**
    - `url`, kept in `SavedStateHandle`;
    - `isResolving`;
    - `tracks: List<DownloadTrackUi>`, where each track's state is `NotDownloaded`, `Queued`, `Downloading(p)`, `Downloaded` or `Failed`;
    - `showDownloadAll`, which is true when there is more than one result and `tracksToDownload` is not empty;
    - `nowPlaying`.
  - `tracks` combines the resolved results, `songDataSource.songs` and `downloadQueue.statuses`, so a
    card turns into a check mark as soon as the importer's upsert lands.
  - **Actions:** URL change, paste, clear, submit (validated with `isYoutubeLink` first), download one, download all, and the mini-player actions.
  - **Events:**
    - `Error(UiText)`, shown as a Toast like `LibraryEvent`;
    - `RequestPermissions`: before the first download, ask for `POST_NOTIFICATIONS` on API 33+ (optional; downloads still run if it's denied) and `WRITE_EXTERNAL_STORAGE` on API 28 (required).
  - Add `DownloadError.toUiText()` and its strings.
- **UI:**
  - `VibeMainTopBar` titled "Downloader";
  - a URL field based on `VibeSearchField` (paste, clear, and IME action Go);
  - a `VibeLoader` while resolving;
  - a `LazyColumn` of new `DownloadableSongCard` rows (in `SongCard.kt`, on the shared `SongCardContent`) with `key = videoId`;
  - card trailing slot:
    - `VibeIconButton(VibeIcons.Download)` when not downloaded;
    - a small determinate progress ring while queued or downloading;
    - `VibeIcons.Check` when downloaded;
  - a `VibeFab` "Download all";
  - an empty or hint state built by hand, like `NoMusicFoundContent`;
  - the `MiniPlayer`;
  - Mobile/Tablet previews.
- `di/DownloaderPresentationModule.kt`.

## Tab switching (`:app` + Library)
- `LibraryScreen` and `DownloaderScreen` gain a `mainNavigation` slot that defaults to empty. On
  mobile, the slot's content is placed in the Scaffold's `bottomBar`. On tablet, it goes in a
  `Row { rail; Scaffold }`.
- The content `Box` calls `consumeWindowInsets(innerPadding)`, so `MiniPlayer` and the FAB (both
  inset-aware through `navigationBarsPadding()`) sit directly above the bar.
- `SongsScreen` and `PlaylistScreen` keep computing their own bottom padding as nav inset +
  `MiniPlayerHeight`, but read the nav inset in a consumption-aware way. When the bottom bar already
  covers the inset, it counts as 0, which avoids a double gap. On tablet it stays unchanged.
- In `:app`, add a `MainTab` enum (Library, Downloader) and a composable that builds the bar or rail. Pass it into both graphs.
- `NavigationRoot` switches tabs with
  `navigate(route) { popUpTo<LibraryRoute> { saveState = true }; launchSingleTop = true; restoreState = true }`.
- `openPlayer()` now pops up to the topmost tab route (Library or Downloader) instead of always going to `LibraryRoute`.

## Docs
- `CLAUDE.md`:
  - replace "Offline only" with "Network is allowed only in `:feature:downloader`";
  - add the downloader to the module layout;
  - add a `DownloadError` note;
  - correct "Song ID = file name" to UUID + `(title, artist)` match key, which is what the code does.
- `specs/vibe-player-requirements.md`: update the offline statements (lines 4 and 37) and add a Downloader section based on `music-downloader-feature.md`.

## Tests (JUnit5, AssertK, Turbine, fakes)
- **Domain:** `isYoutubeLink`, `resolveTrackMetadata`, `isInLibrary`, `tracksToDownload`, `mp3FileName`, `ytDlpLiteral`, `classifyYtDlpError`.
- **Data:** yt-dlp JSON DTO → `RemoteTrack` mapping, using sample single-video and playlist JSON fixtures; `DownloadedSongImporter` with `FakeSongLocalDataSource`.
- **`DownloaderViewModel`:**
  - resolving states;
  - an invalid link produces an error event;
  - the card flips to Downloaded when songs arrive;
  - Download all enqueues only songs that aren't downloaded yet;
  - FAB visibility.
- **Shared fakes:** add `FakeMediaLinkResolver` and `FakeDownloadQueue` under `:core:testing` only if they're shared; they're feature-specific, so they most likely stay in the downloader's test sources.
- The existing scanner and library tests must still pass after the `MusicFileReader` extraction.

## Verification
1. `./gradlew assembleDebug`
2. `./gradlew testDebugUnitTest`, plus `./gradlew :feature:downloader:domain:test`
3. `./gradlew assembleRelease` (or `:app:minifyReleaseWithR8`) to check the R8 keep rules.
4. The user checks these on a device (I won't launch one):
   - a single video and a playlist link;
   - card check marks against the library;
   - Download all skipping songs that already exist;
   - the notification progress;
   - backgrounding the app during a download;
   - the file appearing in `Music/` as `[title].mp3` with square artwork and correct tags;
   - the song showing up in Library immediately;
   - the tablet rail.

## Risks to note
- The APK grows by about 30–40 MB per ABI. youtubedl-android's native libraries may not meet
  Google Play's 16 KB page-size requirement. That doesn't matter for sideloading, but it would matter
  for a Play release.
- yt-dlp breaks when YouTube changes its site. The daily self-update mitigates this, and
  `EXTRACTOR_FAILED` tells the user to try again later.
- Downloading from YouTube may violate its Terms of Service. This is intended for personal use.

## Implementation notes (where the code differs from the plan above)
- **One draining worker instead of a chain of per-track jobs.** Chaining one WorkManager job per
  track would make every job after the first call `setForeground` from the background, which
  Android 12+ refuses. Instead:
  - `DownloadQueueStore` keeps the queue in a small JSON file (`filesDir/download_queue.json`).
    Progress is held in memory only.
  - A single `DownloadWorker` downloads every waiting track in turn.
  - `WorkManagerDownloadQueue` enqueues it with `APPEND_OR_REPLACE`.

  A failed track is marked and skipped, and the worker keeps going. Failures are forgotten on the
  next launch.
- **yt-dlp helpers are internal to `:feature:downloader:data`.** `classifyYtDlpError`, the request
  builders and `regexReplacementLiteral` live in `ytdlp/`, not in the domain module.
- **Literal title/artist tags.** yt-dlp treats a `--parse-metadata` FROM made only of letters as a
  field name, so the tags are set in two steps:
  1. copy `title` into `meta_title`/`meta_artist`;
  2. replace the whole value with `--replace-in-metadata … "(?s)^.*$" <literal>`.

  A missing artist is cleared with `:(?P<meta_artist>)`.
- **Watch links carrying `list=`** resolve to the single video (`--no-playlist`). Only `/playlist?list=` links list a playlist.
- **Tab placement.** `MainNavigationLayout` (`:core:presentation`) places the Library/Downloader
  switch for both screens.
- **List padding.** `RemainingNavigationBarInset` lets the Songs and Playlist tabs keep their
  "nav inset + `MiniPlayerHeight`" padding without doubling it under the bottom bar.
- **APK size.** The debug APK is about 158 MB and the unsigned release about 113 MB, with
  `arm64-v8a` + `x86_64`.
