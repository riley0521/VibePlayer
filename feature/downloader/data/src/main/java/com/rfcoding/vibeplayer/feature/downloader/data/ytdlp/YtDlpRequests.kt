package com.rfcoding.vibeplayer.feature.downloader.data.ytdlp

import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

/**
 * Center-crops the thumbnail to a square before it's embedded, so it fills the app's square
 * artwork. The same filter ytdlnis uses.
 */
private const val SquareThumbnailArgs =
    "ThumbnailsConvertor+FFmpeg_o:-c:v mjpeg -vf crop=\"'if(gt(ih,iw),iw,ih)':'if(gt(iw,ih),ih,iw)'\""

/** Matches a whole field value, so `--replace-in-metadata` swaps it out for a literal. */
private const val WholeValue = "(?s)^.*$"

/**
 * Lists a link's songs without downloading anything. A watch link that also carries `list=` is
 * treated as the video the user was looking at, not the playlist or mix around it.
 */
internal fun infoRequest(url: String): YoutubeDLRequest = YoutubeDLRequest(url)
    .addOption("--dump-single-json")
    .addOption("--flat-playlist")
    .addOption("--no-warnings")
    .apply { if ("watch?" in url) addOption("--no-playlist") }

/**
 * Downloads one video as an MP3 at `[outputDir]/[videoId].mp3`, with the cropped thumbnail and the
 * tags embedded. The title and artist tags are forced to [title] and [artistName], so the file,
 * the library row and the card's "already downloaded" check all agree.
 */
internal fun downloadRequest(
    url: String,
    videoId: String,
    title: String,
    artistName: String?,
    outputDir: File,
): YoutubeDLRequest = YoutubeDLRequest(url)
    .addOption("--no-playlist")
    .addOption("--no-warnings")
    .addOption("--newline")
    .addOption("-x")
    .addOption("--audio-format", "mp3")
    .addOption("--audio-quality", "0")
    .addOption("--embed-thumbnail")
    .addOption("--convert-thumbnails", "jpg")
    .addOption("--postprocessor-args", SquareThumbnailArgs)
    .addOption("--embed-metadata")
    .addOption("-o", File(outputDir, "$videoId.%(ext)s").absolutePath)
    .addCommands(forcedTagCommands(title, artistName))

/**
 * yt-dlp writes `meta_*` fields over the tags it would pick itself. Each is first copied from the
 * title (so it exists), then replaced whole by the literal; a missing artist is cleared instead.
 */
internal fun forcedTagCommands(title: String, artistName: String?): List<String> = buildList {
    addAll(listOf("--parse-metadata", "title:%(meta_title)s"))
    addAll(listOf("--replace-in-metadata", "meta_title", WholeValue, regexReplacementLiteral(title)))
    if (artistName != null) {
        addAll(listOf("--parse-metadata", "title:%(meta_artist)s"))
        addAll(listOf("--replace-in-metadata", "meta_artist", WholeValue, regexReplacementLiteral(artistName)))
    } else {
        addAll(listOf("--parse-metadata", ":(?P<meta_artist>)"))
    }
}

/** Python's `re.sub` reads backslashes in the replacement as escapes; everything else is literal. */
internal fun regexReplacementLiteral(text: String): String = text.replace("\\", "\\\\")
