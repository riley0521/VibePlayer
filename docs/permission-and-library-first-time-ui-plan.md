# Permission Screen + Main Page (Scanning / No music found): UI only

## Context
These are the first feature screens. The user asked for **UI work only**: composables plus previews, for mobile and tablet. There's no ViewModel, no permission launcher, no manifest permission, no NavHost and no scanner yet; those get wired up later. The design system in `:core:design-system` already has everything these screens need. `:core:presentation` has no source files yet.

Figma facts (mobile 412w / tablet 840w; all three screens are portrait only):
- **Permission Screen**: no top bar. The content is centered vertically and horizontally with 16dp padding and a 20dp gap. It shows a 56dp logo, then a text block (8dp top padding, 4dp gap, centered, max width 400dp) with "VibePlayer" (`titleLarge`, `onSurface`) and "VibePlayer needs access to your music files to build your library and play songs" (`bodyMedium`, `onSurfaceVariant`), then a Filled button "Allow Access" that hugs its content.
- **Main Page - Scanning**: the main top bar (logo lockup plus one scan icon button, no search, no tabs). The content is centered with 16dp padding and a 20dp gap: a 140dp radar, then "Scanning your device for music..." (`bodyMedium`, `onSurfaceVariant`, centered).
- **Main Page - No music found**: the same top bar. The content is centered with 16dp padding and a 20dp gap: a text block (4dp gap, max width 400dp, no top padding) with "No music found" (`titleLarge`) and "Try scanning again or check your folders." (`bodyMedium`, `onSurfaceVariant`), then a Filled button "Scan again".
- **Only difference between mobile and tablet**: the top bar's horizontal padding. Mobile uses start 16 / end 10 and tablet uses start 24 / end 18. Everything else keeps the same fixed sizes and is recentered.

## Changes

### 0. Save this plan to `docs/`
First, before any code, copy this plan unchanged into `docs/permission-and-library-first-time-ui-plan.md` so it can be referred to later. The `docs/` folder doesn't exist yet, so create it.

### 1. `:core:presentation`: `DeviceConfiguration`
New file `core/presentation/src/main/java/com/rfcoding/vibeplayer/core/presentation/DeviceConfiguration.kt`:
- `enum class DeviceConfiguration` with `MOBILE_PORTRAIT`, `MOBILE_LANDSCAPE`, `TABLET_PORTRAIT`, `TABLET_LANDSCAPE` and `DESKTOP`, plus `isMobile`, `isWideScreen` and `fromWindowSizeClass(...)`. Copied exactly from the `android-compose-ui` skill.
- `@Composable fun currentDeviceConfiguration()` using `currentWindowAdaptiveInfo().windowSizeClass`.
- The module already depends on `material3.adaptive` (adaptive 1.3.0 is in the Gradle cache), so no new dependency is needed.

### 2. `:core:design-system`: two small component extensions
- `VibeTopBars.kt`, `VibeMainTopBar`: add `contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 10.dp)` and use it in place of the hard-coded `.padding(start = 16.dp, end = 10.dp)`. The design system can't see `DeviceConfiguration`, because `:core:presentation` depends on the design system, so the feature passes the tablet values.
- `VibeRadar.kt`: add `isSweeping: Boolean = false`. When it's true, a `rememberInfiniteTransition` animates the angle from 0 to 360° linearly and repeats forever, and the angle is applied through `Modifier.graphicsLayer { rotationZ = angle }`, which avoids recomposition (skill rule). The rings, glow circle and center dot are concentric, so only the needle and sweep appear to move. Add a sweeping preview.

### 3. `:feature:permission:presentation`
New file `feature/permission/presentation/src/main/java/com/rfcoding/vibeplayer/feature/permission/presentation/PermissionScreen.kt`:
- `sealed interface PermissionAction { data object OnAllowAccessClick }`. The design has no state, so there's no State class and no Root yet; the Root and ViewModel get added with the real permission flow.
- `@Composable fun PermissionScreen(onAction: (PermissionAction) -> Unit, modifier: Modifier = Modifier)`:
  - A `Column` with `fillMaxSize`, `background(colorScheme.background)`, `windowInsetsPadding(WindowInsets.safeDrawing)`, 16dp padding, `Arrangement.spacedBy(20.dp, Alignment.CenterVertically)` and `CenterHorizontally`.
  - `VibeLogo(size = 56.dp)` (decorative, `contentDescription = null`), then the text block (`widthIn(max = 400.dp)`, `padding(top = 8.dp)`, 4dp gap, `TextAlign.Center`), then `VibeButton(text = "Allow Access")`.
- The headline reuses the design system's `R.string.vibe_player`, imported as `com.rfcoding.vibeplayer.core.designsystem.R as DesignSystemR`. New strings go in `feature/permission/presentation/src/main/res/values/strings.xml`: `permission_description` and `allow_access`.
- Previews: `@Preview(widthDp = 412, heightDp = 917)` and `@Preview(widthDp = 840, heightDp = 917)`, both wrapped in `VibePlayerTheme`. (`PreviewSurface` is internal to the design system.)

### 4. `:feature:library:presentation`
Package `com.rfcoding.vibeplayer.feature.library.presentation.library`:
- `LibraryState.kt`: `data class LibraryState(val status: LibraryStatus = LibraryStatus.Scanning)` and `enum class LibraryStatus { Scanning, NoMusicFound }`. A `Loaded` status (tabs and song list) gets added with the Songs and Playlists work.
- `LibraryAction.kt`: `OnScanClick` (top bar, which goes to the Scan music screen later) and `OnScanAgainClick`.
- `LibraryScreen.kt`: `LibraryScreen(state, onAction, modifier)`:
  - A `Scaffold` whose `containerColor` is the background and whose `topBar` is `VibeMainTopBar`. `contentPadding` comes from `currentDeviceConfiguration()`: when `isMobile` it's `PaddingValues(start = 16.dp, end = 10.dp)`, otherwise `PaddingValues(start = 24.dp, end = 18.dp)`. The top bar's action is a `VibeIconButton(VibeIcons.Scan, contentDescription = "Scan music")`.
  - The body is a `when (state.status)` that shows the private composables `ScanningContent()` (`VibeRadar(isSweeping = true)` plus the text) or `NoMusicFoundContent(onScanAgainClick)`, each centered in `fillMaxSize().padding(innerPadding).padding(16.dp)` with a 20dp gap. It's a plain `when` rather than `AnimatedContent`, to keep this change small.
- `feature/library/presentation/src/main/res/values/strings.xml`: `scanning_device`, `no_music_found`, `no_music_found_description`, `scan_again`, `scan_music` (content description).
- Previews: Scanning and No music found, each at 412×917 and 840×917, wrapped in `VibePlayerTheme`.

Not touched: `:app` (`MainActivity` stays as it is), the manifest, and navigation.

## Verification
1. `./gradlew assembleDebug` succeeds.
2. `./gradlew testDebugUnitTest` still passes. There's no new logic, so no new tests: these are purely visual composables, and `DeviceConfiguration` is copied from the skill.
3. The user checks the mobile and tablet previews in Android Studio against the Figma frames. No emulator is launched.
