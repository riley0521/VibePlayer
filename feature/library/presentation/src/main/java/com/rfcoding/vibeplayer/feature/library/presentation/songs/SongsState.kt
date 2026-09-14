package com.rfcoding.vibeplayer.feature.library.presentation.songs

import com.rfcoding.vibeplayer.core.presentation.SongUi

data class SongsState(
    val songs: List<SongUi> = emptyList()
)
