package com.rfcoding.vibeplayer.feature.downloader.data.queue

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import com.rfcoding.vibeplayer.feature.downloader.data.R

/** The one ongoing notification of the download worker. */
class DownloadNotifications(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val manager = NotificationManagerCompat.from(appContext)

    fun foregroundInfo(title: String?, remaining: Int, progress: Float?): ForegroundInfo {
        val notification = build(title, remaining, progress)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    /** Silently does nothing while notifications are off; the download goes on either way. */
    @SuppressLint("MissingPermission")
    fun update(title: String, remaining: Int, progress: Float?) {
        if (!manager.areNotificationsEnabled()) return
        try {
            manager.notify(NOTIFICATION_ID, build(title, remaining, progress))
        } catch (_: SecurityException) {
        }
    }

    private fun build(title: String?, remaining: Int, progress: Float?): Notification {
        ensureChannel()
        val openApp = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)?.let {
            PendingIntent.getActivity(appContext, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title ?: appContext.getString(R.string.download_notification_preparing))
            .setContentText(
                if (remaining > 0) appContext.getString(R.string.download_notification_remaining, remaining) else null,
            )
            .setProgress(PROGRESS_MAX, ((progress ?: 0f) * PROGRESS_MAX).toInt(), progress == null)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun ensureChannel() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(appContext.getString(R.string.download_channel_name))
                .setDescription(appContext.getString(R.string.download_channel_description))
                .build(),
        )
    }

    private companion object {
        const val CHANNEL_ID = "downloads"
        const val NOTIFICATION_ID = 4_201
        const val PROGRESS_MAX = 100
    }
}
