package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.compose.runtime.Stable
import com.rfcoding.vibeplayer.core.presentation.SongUi

@Stable
data class LibraryState(
    val status: LibraryStatus = LibraryStatus.Scanning,
    val songs: List<SongUi> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.Songs,
    val nowPlaying: NowPlayingUi? = null,
)

enum class LibraryStatus {
    Scanning,
    NoMusicFound,
    Loaded,
}

enum class LibraryTab {
    Songs,
    Playlist,
}

data class NowPlayingUi(
    val song: SongUi,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    /** False on the first song of the list, which hides the mini player's previous button. */
    val canSkipToPrevious: Boolean = false,
)
