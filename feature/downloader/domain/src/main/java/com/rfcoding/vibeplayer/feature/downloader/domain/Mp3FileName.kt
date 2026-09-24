package com.rfcoding.vibeplayer.feature.downloader.domain

private const val Mp3Extension = ".mp3"

/** Room for " (99)" and the extension within the usual 255-byte file-name limit. */
private const val MaxBaseNameLength = 120

private val IllegalFileNameChars = Regex("""[/\\:*?"<>|\p{Cntrl}]""")
private val Whitespace = Regex("""\s+""")

/**
 * [text] as a file name: characters shared storage rejects are dropped, whitespace is collapsed and
 * the result is capped. Null when nothing usable is left.
 */
fun sanitizeFileName(text: String): String? = text
    .replace(IllegalFileNameChars, "")
    .replace(Whitespace, " ")
    .trim()
    .trimStart('.')
    .take(MaxBaseNameLength)
    .trim()
    .takeIf { it.isNotEmpty() }

/**
 * `[title].mp3`. When another song already took that name, `[title] - [artist].mp3`; when that is
 * taken too or there's no artist, `[title] (1).mp3`, `[title] (2).mp3`, …
 *
 * [fallbackName] stands in for a title with no usable characters (e.g. the video id).
 */
fun mp3FileName(
    title: String,
    artistName: String?,
    fallbackName: String,
    isTaken: (fileName: String) -> Boolean,
): String {
    val base = sanitizeFileName(title) ?: sanitizeFileName(fallbackName) ?: "track"

    val plain = "$base$Mp3Extension"
    if (!isTaken(plain)) return plain

    val artist = artistName?.let(::sanitizeFileName)
    if (artist != null) {
        val withArtist = "$base - $artist".take(MaxBaseNameLength).trim() + Mp3Extension
        if (!isTaken(withArtist)) return withArtist
    }

    return generateSequence(1) { it + 1 }
        .map { "$base ($it)$Mp3Extension" }
        .first { !isTaken(it) }
}
