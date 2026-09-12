package com.rfcoding.vibeplayer.core.data.di

import com.rfcoding.vibeplayer.core.data.playlist.RoomPlaylistDataSource
import com.rfcoding.vibeplayer.core.data.song.RoomSongDataSource
import com.rfcoding.vibeplayer.core.domain.playlist.PlaylistLocalDataSource
import com.rfcoding.vibeplayer.core.domain.song.SongLocalDataSource
import org.koin.dsl.module

val coreDataModule = module {
    single<SongLocalDataSource> { RoomSongDataSource(get()) }
    single<PlaylistLocalDataSource> { RoomPlaylistDataSource(get()) }
}
