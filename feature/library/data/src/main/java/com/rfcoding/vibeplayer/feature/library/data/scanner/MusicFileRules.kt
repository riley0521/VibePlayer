package com.rfcoding.vibeplayer.feature.library.data.scanner

/** MediaStore's `RELATIVE_PATH` for a file directly inside the `Music/` folder of a volume. */
internal const val MUSIC_RELATIVE_PATH = "Music/"

internal fun isMp3(displayName: String): Boolean {
    return displayName.endsWith(".mp3", ignoreCase = true)
}

/**
 * API 29+: [relativePath] is MediaStore's `RELATIVE_PATH`, which names the folder only, so files
 * in subfolders (`Music/Rock/`) don't match. Shared storage is case-insensitive, so neither is this.
 */
internal fun isDirectlyInMusicFolder(relativePath: String?): Boolean {
    return relativePath.equals(MUSIC_RELATIVE_PATH, ignoreCase = true)
}

/** API 28: [filePath] is MediaStore's absolute `DATA` path and [musicDirPath] the `Music/` folder. */
internal fun isDirectlyInMusicFolder(filePath: String, musicDirPath: String): Boolean {
    val parentPath = filePath.substringBeforeLast('/', missingDelimiterValue = "")
    return parentPath.equals(musicDirPath.trimEnd('/'), ignoreCase = true)
}
