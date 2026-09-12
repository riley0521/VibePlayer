package com.rfcoding.vibeplayer.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rfcoding.vibeplayer.core.database.entity.SongEntity.Companion.UNKNOWN_ARTIST

@Entity(
    tableName = "songs",
    indices = [Index(value = ["title", "artistName"], unique = true)],
)
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    /**
     * [UNKNOWN_ARTIST] when the file has no artist tag. Kept non-null on purpose: SQLite treats
     * NULLs as distinct, so a nullable column would let duplicates slip past the unique index.
     */
    val artistName: String,
    val fileUri: String,
    val imageUri: String?,
    val durationMillis: Long,
    val isFavorite: Boolean,
    val createdAt: Long,
) {
    companion object {
        const val UNKNOWN_ARTIST = ""
    }
}
