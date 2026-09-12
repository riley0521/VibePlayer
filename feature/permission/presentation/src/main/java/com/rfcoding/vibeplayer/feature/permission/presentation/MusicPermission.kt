package com.rfcoding.vibeplayer.feature.permission.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * The single permission VibePlayer needs: reading the audio files in the device's `Music/` folder.
 *
 * `minSdk` is 28, so the framework methods below are always available and none of this needs
 * `ContextCompat`, which the feature convention plugin doesn't put on the classpath.
 */
object MusicPermission {

    /** API 33 split `READ_EXTERNAL_STORAGE` into per-media permissions; VibePlayer only reads audio. */
    val NAME: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

fun Context.hasMusicPermission(): Boolean =
    checkSelfPermission(MusicPermission.NAME) == PackageManager.PERMISSION_GRANTED

/**
 * True while the system will still show its own dialog, i.e. the user has denied at most once.
 * It flips to false on the second denial, which is when the user needs the Settings route instead.
 */
fun Activity.canAskForMusicPermissionAgain(): Boolean =
    shouldShowRequestPermissionRationale(MusicPermission.NAME)

/** The app's entry in system Settings, the only way back once the system stops asking. */
fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
