package com.rfcoding.vibeplayer.feature.library.presentation.library

data class LibraryState(
    val status: LibraryStatus = LibraryStatus.Scanning,
)

enum class LibraryStatus {
    Scanning,
    NoMusicFound,
}
