package com.rfcoding.vibeplayer.feature.library.presentation.search

sealed interface SearchEvent {
    /** A result started playing, so the Player screen opens on it. */
    data object NavigateToPlayer : SearchEvent
}
