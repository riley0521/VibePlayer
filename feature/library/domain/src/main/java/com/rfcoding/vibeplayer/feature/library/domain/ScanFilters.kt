package com.rfcoding.vibeplayer.feature.library.domain

/** Minimum duration a music file must have to be kept by a scan. */
enum class MinDuration(val millis: Long) {
    ThirtySeconds(30_000),
    SixtySeconds(60_000),
}

/** Minimum file size a music file must have to be kept by a scan. */
enum class MinSize(val bytes: Long) {
    OneHundredKb(100 * 1024),
    FiveHundredKb(500 * 1024),
}

/** The defaults are what the silent background scan uses. */
data class ScanFilters(
    val minDuration: MinDuration = MinDuration.ThirtySeconds,
    val minSize: MinSize = MinSize.OneHundredKb,
) {
    fun acceptsSize(bytes: Long): Boolean = bytes >= minSize.bytes

    fun acceptsDuration(millis: Long): Boolean = millis >= minDuration.millis
}
