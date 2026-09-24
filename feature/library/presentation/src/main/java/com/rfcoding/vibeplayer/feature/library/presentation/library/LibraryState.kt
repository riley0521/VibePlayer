package com.rfcoding.vibeplayer.feature.library.presentation.library

import androidx.compose.runtime.Stable
import com.rfcoding.vibeplayer.core.presentation.NowPlayingUi

@Stable
data class LibraryState(
    val status: LibraryStatus = LibraryStatus.Scanning,
    val selectedTab: LibraryTab = LibraryTab.Songs,
    val nowPlaying: NowPlayingUi? = null
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
