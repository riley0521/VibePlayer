package com.rfcoding.vibeplayer.feature.downloader.data.di

import android.net.Uri
import androidx.work.WorkManager
import com.rfcoding.vibeplayer.core.data.song.MusicFileReader
import com.rfcoding.vibeplayer.feature.downloader.data.library.DownloadedSongImporter
import com.rfcoding.vibeplayer.feature.downloader.data.library.MusicFileSource
import com.rfcoding.vibeplayer.feature.downloader.data.library.MusicFolderWriter
import com.rfcoding.vibeplayer.feature.downloader.data.queue.DownloadNotifications
import com.rfcoding.vibeplayer.feature.downloader.data.queue.DownloadQueueStore
import com.rfcoding.vibeplayer.feature.downloader.data.queue.DownloadWorker
import com.rfcoding.vibeplayer.feature.downloader.data.queue.WorkManagerDownloadQueue
import com.rfcoding.vibeplayer.feature.downloader.data.ytdlp.YoutubeDlEngine
import com.rfcoding.vibeplayer.feature.downloader.data.ytdlp.YoutubeDlLinkResolver
import com.rfcoding.vibeplayer.feature.downloader.domain.DownloadQueue
import com.rfcoding.vibeplayer.feature.downloader.domain.MediaLinkResolver
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import androidx.core.net.toUri

val downloaderDataModule = module {
    single { YoutubeDlEngine(androidContext()) }
    single<MediaLinkResolver> { YoutubeDlLinkResolver(get()) }

    singleOf(::DownloadQueueStore)
    singleOf(::DownloadNotifications)
    single { WorkManager.getInstance(androidContext()) }
    single<DownloadQueue> { WorkManagerDownloadQueue(get(), get()) }

    singleOf(::MusicFolderWriter)
    single {
        val reader = get<MusicFileReader>()
        DownloadedSongImporter(
            musicFileSource = { fileUri, mediaId -> reader.read(fileUri.toUri(), mediaId) },
            songDataSource = get(),
        )
    }
    workerOf(::DownloadWorker)
}
