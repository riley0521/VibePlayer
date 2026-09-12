# Add songs screen + playlist action sheet + rename playlist: UI only

## Context
The next batch of Figma screens. As with the previous rounds this is **UI work only**: composables, state/action classes and previews, mobile and tablet. There is still no ViewModel, no navigation, no domain or data layer anywhere in the project, so every screen is driven purely from `@Preview` state and every action is a no-op sink.

Three things ship together:
1. **Add songs to playlist screen** (None selected / Some music selected / Search result).
2. **Playlist action sheet** on the Main page, plus its Delete confirmation and its Favourites variant.
3. **Rename playlist**, which reuses the existing Create-playlist sheet.

They are one batch because they share new plumbing: a reusable bottom-sheet shell, a selectable list row, and a destructive button style.

### Figma facts (node ids: mobile / tablet)

**Add songs** — `2029:18109`/`2029:30693` (none), `2029:18150`/`2029:30734` (some), `2029:18136`/`2029:30720` (search result).
- Top bar row 1 (64dp): back arrow left, centered title, and a mirrored **invisible** arrow on the right purely for symmetry — there is no trailing action, and no Cancel/Done. `CenterAlignedTopAppBar` already centers the title without the spacer, so `VibeInnerTopBar` matches as-is. Mobile side padding 10dp, tablet 18dp — the same delta `ScanMusicScreen` and `PlayerScreen` already handle with a `TabletTopBarPadding = 8.dp`.
- Title text: **"Add Songs"** at zero selected, **"{n} Selected"** (capital S) as soon as one is checked.
- Top bar row 2: the search pill (`h-44`, `Button/Hover` fill, `Surface/Outline` border, leading search icon, placeholder "Search", trailing `x` once non-empty) with 16dp side padding and 8dp bottom padding — **16dp on tablet too**, unlike the body.
- Body side padding: mobile 16dp, tablet 24dp.
- First list row is **"Select All"**: 52dp tall, leading checkbox, label in `Title-Medium`, bottom divider. (28dp checkbox + the shared 12dp vertical row padding = 52dp exactly.)
- Song rows: 88dp (64dp artwork + 12dp/12dp padding — exactly today's `SongCard`), with a **leading** checkbox before the artwork. A selected row changes **only** the checkbox; no background tint.
- Bottom fade over the list: 100dp — today's `bottomFade`.
- Bottom **"OK"** button appears **only when at least one song is selected**: container `px-16 py-8`, filled primary pill, full width on mobile, capped at **480dp centered** on tablet.

**Playlist action sheet** — `2029:21140`/`2029:43302` (base), `2029:21172`/`2029:43334` (Favourites), `2029:21270`/`2029:44306` (Delete).
- All three use the **same sheet shell** the Create-playlist sheet already uses: `Surface/Surface-Highest` fill, 12dp top corners only, `Surface/Overlay` scrim, no drag handle, full width on mobile and 480dp centered on tablet. **Delete is not a dialog** — it is the same sheet with different content.
- Base sheet: a header row that is the playlist card **without** the trailing menu button and **not clickable** (64dp circular artwork, name in `Title-Medium`, "N songs" below, bottom divider), then four `action-sheet-button` rows, `gap-2`, sheet padding `top 2 / sides 16 / bottom 32`:
  1. **Play** — `icon/linear/play`
  2. **Rename** — `icon/linear/pen`
  3. **Change Cover** — `icon/linear/img-edit`
  4. **Delete** — `icon/linear/bin`
- Favourites variant: same shell and header, but **only the Play row** — Rename, Change Cover and Delete are all removed, matching the rule that Favourites is virtual and undeletable.
- Delete variant: sheet padding `top 24 / sides 16 / bottom 48`, `gap-20`. A centered text block (`max-width 400dp`, `gap-4`): title **"Delete Playlist"** (`Title-Medium`, `Text/Primary`) and body **"Are you sure you want to delete playlist {name}?"** (`Body-Medium-Regular`, `Text/Secondary`). Then a 12dp-gap row of two equal-weight 44dp pills: **Cancel** (outlined) on the left, **Delete** on the right filled with the new `Button/Destructive` `#FF5667`.
- Tablet keeps the sheet's **internal** padding at 16dp; only the screen behind it uses the 24dp gutter.

**New token**: `Button/Destructive` = `#FF5667`. Figma gives the destructive button the *purple* `0 2 8 rgba(194,119,255,0.25)` glow, evidently copy-pasted from the primary button. Match Figma exactly (reuse `primaryDropShadow`) and leave a comment saying so — worth raising with design, but the mock is the mock.

**Rename** has no Figma frame; it reuses the Create sheet's layout with the title **"Rename Playlist"** and the confirm button **"Rename"**.

## Changes

### 0. Save this plan to `docs/`
Before any code, copy this plan unchanged to `docs/add-songs-and-playlist-actions-ui-plan.md`, as was done for the previous UI batch.

### 1. `:core:design-system` — theme
- `theme/Color.kt`: add `internal val ButtonDestructive = Color(0xFFFF5667)`.
- `theme/ExtendedColors.kt`: add `buttonDestructive: Color` to `ExtendedColors` and to `DarkExtendedColors`, documented as "Button/Destructive: the Delete confirmation's filled button". Material 3 has an `error` slot, but the app's single palette is expressed through `ExtendedColors` for every non-Material Figma token, so this follows suit.

### 2. `:core:design-system` — `components/VibeButton.kt`
Add `Destructive` to `VibeButtonStyle`. It behaves exactly like `Filled` — same 44dp height, same `CircleShape`, same `primaryDropShadow`, same `onPrimary` label, same disabled treatment — but fills with `extendedColors.buttonDestructive`. Fold it into the existing `when (style)` branches rather than adding a parallel path, and add it to the component preview.

### 3. `:core:design-system` — `components/ListItemParts.kt`
The Add-songs rows need `toggleable` semantics, and the action sheet's header row must not be clickable at all. Promote the shared row to public API:
- Extract the current modifier chain (full width, bottom divider, 12dp vertical padding, `spacedBy(12.dp)`, centered) into a private helper so the two public rows can't drift.
- `VibeListItemRow(modifier, onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit)` — replaces the internal `ListItemRow`; a null `onClick` renders a plain, non-interactive row.
- `VibeSelectableListItemRow(selected: Boolean, onSelectedChange: (Boolean) -> Unit, modifier, content)` — same shape, but `Modifier.toggleable(..., role = Role.Checkbox, indication = PressedOverlayIndication)` so the whole row is one accessible checkbox target.

Update the two existing call sites (`SongCard.kt`, `PlaylistCard.kt`) to the new name.

### 4. `:core:design-system` — `components/SongCard.kt`
Add, beside the existing `SongCard`:
```kotlin
@Composable
fun SelectableSongCard(
    title: String,
    artistName: String?,
    duration: String,
    imageUri: String?,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
)
```
A `VibeSelectableListItemRow` holding `VibeCheckbox(checked = selected, onCheckedChange = null)` — the row owns the toggle — then the same 64dp `SongArtwork`, title/artist column and trailing duration as `SongCard`. Factor the artwork + text + duration into a private `SongCardContent` shared by both so they can't diverge. Add a preview showing selected and unselected.

### 5. `:core:design-system` — `components/PlaylistCard.kt`
Make `onClick` nullable (`onClick: (() -> Unit)? = null`) so the action sheet's header can reuse the card as a static row. Existing callers pass a lambda and are unaffected; move `onClick` below the required parameters if the default forces it.

### 6. `:core:presentation` — new `VibeBottomSheet.kt`
All four sheets (name, actions, Favourites actions, delete) share one shell, and the shell needs `currentDeviceConfiguration()`, which lives here and cannot be reached from `:core:design-system` (the dependency runs the other way). So this belongs next to `MiniPlayer`, for the same reason `MiniPlayer` does.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)
```
Lift the body verbatim out of today's `CreatePlaylistSheet`: `sheetMaxWidth = if (isMobile) Dp.Unspecified else 480.dp`, `RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)`, `containerColor = surfaceContainerHighest`, `scrimColor = scrim`, `dragHandle = null`. Keep the comments explaining the `Dp.Unspecified` and the missing drag handle.

### 7. `:feature:library:presentation` — rename `createplaylist/` to `playlistname/`
Nothing references these files yet, so this is a clean rename.
- `PlaylistNameState.kt`: keep `const val MaxPlaylistNameLength = 40`; add `enum class PlaylistNameMode { Create, Rename }`; `data class PlaylistNameState(val mode: PlaylistNameMode = PlaylistNameMode.Create, val name: String = "")` with `val canConfirm: Boolean get() = name.isNotBlank()`.
- `PlaylistNameAction.kt`: `OnNameChange(name)`, `OnCancelClick`, `OnConfirmClick` (was `OnCreateClick`).
- `PlaylistNameSheet.kt`: `PlaylistNameSheet(state, onAction, onDismiss, modifier)` now just wraps `VibeBottomSheet { PlaylistNameSheetContent(...) }` — the ~20 lines of `ModalBottomSheet` configuration move to step 6. The content keeps its 16dp/24dp padding, 20dp gaps, `imePadding()`, `VibeTextField` and the Cancel/confirm row; the title and confirm label now come from `state.mode`:
  - `Create` → `create_new_playlist` / `create`
  - `Rename` → `rename_playlist` / `rename`

  Keep both existing previews (initial + valid input) and add a Rename preview, all at 412dp and 480dp.

### 8. `:feature:library:presentation` — new `addsongs/` package
- `AddSongsState.kt`:
  ```kotlin
  @Stable
  data class AddSongsState(
      val query: String = "",
      /** Already filtered for [query]; an empty query keeps every song, as on the Search screen. */
      val songs: List<SongUi> = emptyList(),
      val selectedSongIds: Set<String> = emptySet(),
  ) {
      val selectedCount: Int get() = selectedSongIds.size
      val hasSelection: Boolean get() = selectedSongIds.isNotEmpty()
      /** An empty list is never "all selected", so Select All stays unchecked on a no-result search. */
      val isAllSelected: Boolean get() = songs.isNotEmpty() && songs.all { it.id in selectedSongIds }
  }
  ```
- `AddSongsAction.kt`: `OnBackClick`, `OnQueryChange(query)`, `OnClearQueryClick`, `OnSelectAllChange(selected)`, `OnSongSelectedChange(songId, selected)`, `OnOkClick`.
- `AddSongsScreen.kt`: `AddSongsScreen(state, onAction, modifier)`.
  - `Scaffold(containerColor = background)`.
  - `topBar`: a `Column` of `VibeInnerTopBar(title = …, onBackClick = …)` — title is `add_songs` at zero selected and `selected_count` otherwise, with the tablet's extra 8dp side padding exactly as `ScanMusicScreen` does — then a `VibeSearchField` in a `Row` padded `horizontal = 16.dp, bottom = 8.dp` on both breakpoints.
  - `bottomBar`: shown only when `state.hasSelection` — a `VibeButton(text = ok)` inside a container padded `horizontal = 16.dp, vertical = 8.dp`, `navigationBarsPadding()`, `fillMaxWidth()` and `widthIn(max = 480.dp)` on tablet, centred.
  - Body: a `LazyColumn` with `bottomFade(background)` and `contentPadding` horizontal 16dp/24dp; first `item(key = "selectAll")` is the Select All row, then `items(state.songs, key = { it.id })` of `SelectableSongCard`.
  - The Select All row is a private composable in this file: `VibeSelectableListItemRow` + `VibeCheckbox(checked = state.isAllSelected, onCheckedChange = null)` + the label in `titleMedium`.
  - Previews at 412×917 and 840×917 for all three Figma states: none selected, some selected (title reads "4 Selected", OK visible), and search result (query "Les", three songs).

### 9. `:feature:library:presentation` — the sheets, in `library/`
They are shown by `LibraryScreen`, so they live beside it.
- `library/PlaylistActionSheet.kt`: `PlaylistActionSheet(target, onAction, onDismiss, modifier)` wrapping `VibeBottomSheet`, plus an `internal PlaylistActionSheetContent(...)` that previews render directly (a `ModalBottomSheet` does not appear in previews — the same reason today's create sheet previews its content, and the same `SheetPreviewSurface` trick moves here). The header is `PlaylistCard(onClick = null, onMenuClick = null)`; the rows are `VibeActionSheetButton`s, and the Favourites target renders Play only.
- `library/DeletePlaylistSheet.kt`: the same pair, with the centred `max-width 400dp` text block and the Cancel / `VibeButtonStyle.Destructive` Delete row.
- Previews: base, Favourites and delete, each at 412dp and 480dp.

### 10. `:feature:library:presentation` — `library/LibraryState.kt` and `LibraryAction.kt`
`LibraryScreen` currently has no way to show any sheet — `OnCreatePlaylistClick`, `OnFavouritesMenuClick` and `OnPlaylistMenuClick` already exist but nothing handles them. Add one nullable slot rather than a boolean per sheet:
```kotlin
val activeSheet: LibrarySheet? = null

sealed interface LibrarySheet {
    /** Favourites is virtual and has no row id, so it is its own target. */
    data object FavouritesActions : LibrarySheet
    data class PlaylistActions(val playlist: PlaylistUi) : LibrarySheet
    data class DeletePlaylist(val playlist: PlaylistUi) : LibrarySheet
    data class PlaylistName(val state: PlaylistNameState, val playlistId: Long? = null) : LibrarySheet
}
```
`PlaylistName` covers both Create (`playlistId == null`) and Rename with one branch, which also finally gives `OnCreatePlaylistClick` somewhere to land.

New `LibraryAction` entries: `OnSheetDismiss`; `OnPlayFavouritesClick`; `OnPlayPlaylistClick(playlistId)`, `OnRenamePlaylistClick(playlistId)`, `OnChangePlaylistCoverClick(playlistId)`, `OnDeletePlaylistClick(playlistId)` (opens the confirmation) and `OnConfirmDeletePlaylistClick(playlistId)`; and `OnPlaylistNameAction(action: PlaylistNameAction)` so the name sheet keeps the single `onAction` funnel.

> Note: **Change Cover** is in the Figma sheet but has no destination anywhere in the designs or the requirements. The row and its action are included for design fidelity; the action is a no-op like every other action at this stage. Flagging it so it is not mistaken for something already wired.

### 11. `:feature:library:presentation` — `library/LibraryScreen.kt`
After the `Scaffold`, a `when (state.activeSheet)` renders the matching sheet with `onDismiss = { onAction(LibraryAction.OnSheetDismiss) }`. Add previews for the base action sheet, the Favourites action sheet and the delete confirmation over the Playlist tab, at 412×917 and 840×917, so the sheets can be checked against the full Figma frames and not only in isolation.

### 12. Strings — `feature/library/presentation/src/main/res/values/strings.xml`
`add_songs` "Add Songs", `selected_count` "%d Selected", `select_all` "Select All", `ok` "OK"; `action_play` "Play", `action_rename` "Rename", `action_change_cover` "Change Cover", `action_delete` "Delete"; `delete_playlist_title` "Delete Playlist", `delete_playlist_message` "Are you sure you want to delete playlist %s?", `delete` "Delete"; `rename_playlist` "Rename Playlist", `rename` "Rename". Reuse the existing `search`, `cancel`, `create`, `create_new_playlist` and `playlist_name_placeholder`. Content descriptions for the checkbox-bearing rows come from the row's `Role.Checkbox` semantics plus its label, so no extra strings are needed there.

No Gradle changes: `vibeplayer.android.feature` already wires `:core:presentation`, `:core:design-system` and Compose, and every library used is already in `libs.versions.toml`.

## Tests
Per the definition of done, only non-trivial logic gets a test. New file `feature/library/presentation/src/test/java/.../addsongs/AddSongsStateTest.kt` (JUnit5 + AssertK, already configured by the convention plugin) covering `AddSongsState`:
- `isAllSelected` is false for an empty song list, so Select All stays unchecked on a no-result search;
- `isAllSelected` is true only when every visible song is in `selectedSongIds`;
- `isAllSelected` ignores selected ids that are not in the currently filtered list (a song selected before the query was typed must not tick Select All);
- `hasSelection` and `selectedCount` track the set.

Everything else added here is layout or a one-line derivation, so it is skipped.

## Verification
1. `./gradlew assembleDebug` succeeds.
2. `./gradlew testDebugUnitTest` passes, including the new `AddSongsStateTest`.
3. No emulator: the user checks the Android Studio previews against Figma —
   - `AddSongsScreen` × 3 states × mobile/tablet,
   - `PlaylistActionSheet` base and Favourites, `DeletePlaylistSheet`, at 412dp and 480dp,
   - `PlaylistNameSheet` in Create and Rename modes,
   - `LibraryScreen` with each sheet open, mobile and tablet,
   - and the design-system previews for `SelectableSongCard` and the new destructive `VibeButton`.
