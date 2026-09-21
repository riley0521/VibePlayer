package com.rfcoding.vibeplayer.feature.player.presentation.sharecard

private const val MAX_TITLE_LENGTH = 60
private val IllegalFileNameChars = Regex("""[\\/:*?"<>|\p{Cntrl}]""")
private val Whitespace = Regex("""\s+""")

/**
 * The gallery file name for a song's share card, e.g. `VibePlayer_Do I Wanna Know_1700000000000.png`.
 * Characters that some file systems reject become `_`, and the timestamp keeps repeated saves apart.
 */
fun shareCardFileName(songTitle: String, epochMillis: Long): String {
    val title = songTitle
        // Whitespace first, so tabs and newlines become spaces rather than underscores.
        .replace(Whitespace, " ")
        .replace(IllegalFileNameChars, "_")
        .trim()
        .take(MAX_TITLE_LENGTH)
        .trim()
        .ifEmpty { "Song" }
    return "VibePlayer_${title}_$epochMillis.png"
}
