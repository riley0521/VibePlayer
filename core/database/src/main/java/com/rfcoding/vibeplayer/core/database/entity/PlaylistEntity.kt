package com.rfcoding.vibeplayer.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    /** Added in version 2; a persisted content URI from the photo picker. */
    val coverUri: String? = null,
)
