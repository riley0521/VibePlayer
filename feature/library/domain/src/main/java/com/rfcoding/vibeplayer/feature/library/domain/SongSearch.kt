package com.rfcoding.vibeplayer.feature.library.domain

import com.rfcoding.vibeplayer.core.domain.song.Song

/**
 * Songs whose title or artist contains the trimmed [query], ignoring case. A blank query keeps every
 * song, since the requirements show the whole library before anything is typed.
 */
fun List<Song>.filterByQuery(query: String): List<Song> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return this
    return filter { song ->
        song.title.contains(trimmed, ignoreCase = true) ||
            song.artistName?.contains(trimmed, ignoreCase = true) == true
    }
}
