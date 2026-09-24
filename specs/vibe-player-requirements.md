# VibePlayer - A music player app - Requirements

## Purpose
A native Android application that plays music offline, scans your music folder. Purely local data; the only feature that needs internet is the Downloader tab, which downloads YouTube audio into the music folder.

---

## Figma links
Colors - https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-17830&t=ueBQQpEn6RsRAF8T-4

Icon set - https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-17537&t=ueBQQpEn6RsRAF8T-4

Reusable components
- https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-17568&t=ueBQQpEn6RsRAF8T-4
- https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-17697&t=ueBQQpEn6RsRAF8T-4
- https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-17751&t=ueBQQpEn6RsRAF8T-4
- https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-17795&t=ueBQQpEn6RsRAF8T-4
- https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-17782&t=ueBQQpEn6RsRAF8T-4

Mobile layouts - https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-18015&t=ueBQQpEn6RsRAF8T-4
Tablet layouts - https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-30599&t=ueBQQpEn6RsRAF8T-4

### Note
This app should be adaptive to both mobile and tablet devices.

---

## Splash screen
Use the splash screen API, change the background color and add the logo of the app.

## Screens
Note: Mobile layouts layers have the same name to Tablet layouts for easy lookup.

### 1. Permission Screen
- This will be shown if the user did not grant the access storage permission, we will only check the music folder
- When user click 'Allow Access' button, request the permission
- This permission is critical that needs to be granted because the app depends on the music folder access.

### 2. Main Screen

**First time** (Figma layer name: Main Page - Scanning / Main Page - No music found): 
- The first time user use the app, it will scan the music folder after granting the permission. Show the Scanning device layout.
  - Upsert all the detected music files to database, the unique ID of the row is the file name, update the row if already exists. 
  - Scan and sync in batches of 100 music files. Once the first batch is stored, hide the scanning UI; the remaining batches keep syncing in the background, and songs missing from the whole scan are deleted only after every batch succeeded. If a later batch can't be stored, tell the user how many songs were skipped (e.g. storage full).
- When the app does not detect any music, show the no music found layout, when the user press the 'Scan again' button, just scan the folder again.

**Top app bar**:
- Clicking the scan button in the top right corner will bring the user to **Scan music screen**
- Clicking the search icon on top right corner will open the **Search music screen**

**Songs Tab**:
- Shows the list of music available
- When the user clicks 'Shuffle' pick a random music and play it.
- When the user clicks 'Play' just play the first item in the list.
- The scroll up FAB will show when the user starts scrolling, clicking it will bring the user back to the first item.

**Playlist Tab**:
- 'Favourites' playlist cannot be deleted, always there.
- Show 'Create playlist' button if there are no user-created playlist yet.
- 'Create playlist' button will open the **Create playlist bottom sheet**
- Sort the playlist by their latest created date.

**Mini player** (Figma layer name: Main Page - Songs Tab - Mini player paused / Main Page - Songs Tab - Mini player playing):
- Clicking a list item will play that music and opens this mini player.
- The progress bar is draggable. Check the dragging state in this mockup: https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-18378&t=ueBQQpEn6RsRAF8T-4
- Clicking this mini player will open the **Player screen**

### 3. Scan music Screen (Figma layer name: Scan Music / Scan Music - Loading)
- Ignore duration less than 30s / 60s - It will skip the music file if it's duration is less than those.
- Ignore size less than 100kb / 500kb - Same logic, skip the music based on the file size.

### 4. Player screen (Figma layer name: Player page - paused / Player page - playing)
- Based on the mockup, it seems pretty intuitive for me what those buttons do, discuss if you have questions.
- In the top right corner, add the **Add to playlist button icon** and **Add to favorites button icon**. Mockup: https://www.figma.com/design/mvLba0HDOxJwaS2jnzlr0i/VibePlayer--Copy-?node-id=2029-21369&t=ueBQQpEn6RsRAF8T-4

### 5. Create playlist bottom sheet (Figma layer name: Create Playlist bottom sheet - Initial state / Create Playlist bottom sheet - Valid input)
- The max character for playlist name is 40, disable the Create button if the text field is empty.
- Clicking 'Create' will save the playlist to database and navigate the user to **Add songs screen**

### 6. Search music screen (Figma layer name: Main Page + Search / Main Page + Search - With Result / Main Page + Search - No result)
- The default state should show all music instead of empty.
- When the user starts typing then filter the list.
- Clicking 'Cancel' button will close this page.

### 7. Add songs screen (Figma layer name: Add songs to playlist page - None selected / Add songs to playlist page - Some music selected / Add songs to playlist page - Search result)
- Make the item selectable with the check mark, show the 'OK' button when there are selected music items, clicking it will add to playlist and then close this page.

### 8. Downloader screen (no Figma design; built from the existing components)
Full description: `specs/music-downloader-feature.md`.
- Library and Downloader are switched with a bottom navigation bar on mobile and a navigation rail on tablet.
- Paste a YouTube video or playlist link (or tap Paste), then Find. The results show as song cards with the thumbnail, title and artist.
- A song whose title and artist are already in the library shows a check; otherwise a download button, and a progress ring while it is queued or downloading.
- With more than one result, a "Download all" FAB queues every song not downloaded yet.
- Downloads run one at a time in the background with a progress notification, and continue when the app is left.
- Each file is an MP3 in `Music/` named `[title].mp3`, with a square-cropped thumbnail and the title and artist embedded, and is added to the library as soon as it finishes.
- The mini player shows here too.

---

## What is a music
- An **ID** should be the file name since it cannot be duplicated, but open for discussion.
- A **name** 
- An **artistName** if available
- A **fileUri** save the Uri string, not the blob file.
- An **imageUri** if the music file have image metadata
- A **durationMillis** duration of the music in milliseconds
- **isFavorite** if the user marked this music as their favorite
- A **created date**

Use **MediaMetadataRetriever** to retrieve the metadata from the audio files.

## Playlist
- A **name**
- A **created date**

Create a many-to-many table because N music can be added to N playlist.