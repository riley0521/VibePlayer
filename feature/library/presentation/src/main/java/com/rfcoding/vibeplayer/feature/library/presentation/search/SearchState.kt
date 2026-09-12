package com.rfcoding.vibeplayer.feature.library.presentation.search

import androidx.compose.runtime.Stable
import com.rfcoding.vibeplayer.core.presentation.SongUi

@Stable
data class SearchState(
    val query: String = "",
    /** Already filtered for [query]; an empty query keeps every song, per the requirements. */
    val songs: List<SongUi> = emptyList(),
) {
    val isEmptyResult: Boolean get() = songs.isEmpty() && query.isNotBlank()
}
