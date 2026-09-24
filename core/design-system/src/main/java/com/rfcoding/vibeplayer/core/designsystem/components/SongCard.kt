package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rfcoding.vibeplayer.core.designsystem.R
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons

private val DragHandleIconSize = 24.dp

/** The handle is dragged, not tapped, so it gets the same touch target as the X beside it. */
private val DragHandleTouchTargetSize = 48.dp

/**
 * Figma "song-card". [duration] is already formatted by the caller (e.g. "3:45").
 */
@Composable
fun SongCard(
    title: String,
    artistName: String?,
    duration: String,
    imageUri: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeListItemRow(onClick = onClick, modifier = modifier) {
        SongCardContent(title = title, artistName = artistName, imageUri = imageUri) {
            DurationText(duration = duration)
        }
    }
}

/**
 * Figma "song-card" with the Add songs screen's leading checkbox. The row owns the toggle, so the
 * checkbox itself takes no click.
 */
@Composable
fun SelectableSongCard(
    title: String,
    artistName: String?,
    duration: String,
    imageUri: String?,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeSelectableListItemRow(
        selected = selected,
        onSelectedChange = onSelectedChange,
        modifier = modifier,
    ) {
        VibeCheckbox(checked = selected, onCheckedChange = null)
        SongCardContent(title = title, artistName = artistName, imageUri = imageUri) {
            DurationText(duration = duration)
        }
    }
}

/**
 * Figma "song-card" as the Edit playlist screen shows it: the duration gives way to a drag handle,
 * and an X in front removes the song. The row takes no click of its own.
 *
 * [dragHandleModifier] is where the caller's reorder gesture goes, so the reordering library stays
 * out of the design system.
 */
@Composable
fun EditableSongCard(
    title: String,
    artistName: String?,
    imageUri: String?,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
    VibeListItemRow(modifier = modifier) {
        VibeIconButton(
            icon = VibeIcons.Close,
            contentDescription = stringResource(R.string.remove_song, title),
            onClick = onRemoveClick,
        )
        SongCardContent(title = title, artistName = artistName, imageUri = imageUri) {
            DragHandle(title = title, modifier = dragHandleModifier)
        }
    }
}

/**
 * The Queue sheet's first row: the song playing now, closed by a play/pause button instead of the
 * duration. The row takes no click of its own.
 */
@Composable
fun NowPlayingSongCard(
    title: String,
    artistName: String?,
    imageUri: String?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeListItemRow(modifier = modifier) {
        SongCardContent(title = title, artistName = artistName, imageUri = imageUri) {
            VibeIconButton(
                icon = if (isPlaying) VibeIcons.Pause else VibeIcons.PlayFilled,
                contentDescription = stringResource(if (isPlaying) R.string.pause_song else R.string.play_song, title),
                onClick = onPlayPauseClick,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * The Queue sheet's upcoming song: tapping the row plays it, and the duration gives way to a drag
 * handle. Like [EditableSongCard], [dragHandleModifier] carries the caller's reorder gesture.
 */
@Composable
fun ReorderableSongCard(
    title: String,
    artistName: String?,
    imageUri: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
    VibeListItemRow(onClick = onClick, modifier = modifier) {
        SongCardContent(title = title, artistName = artistName, imageUri = imageUri) {
            DragHandle(title = title, modifier = dragHandleModifier)
        }
    }
}

/** Where a [DownloadableSongCard]'s song stands; the card's trailing control follows it. */
@Immutable
sealed interface SongDownloadState {
    data object NotDownloaded : SongDownloadState
    data object Queued : SongDownloadState
    data class Downloading(val progress: Float) : SongDownloadState
    data object Downloaded : SongDownloadState
}

/**
 * The Downloader's search result: the duration gives way to a download button, a progress ring
 * while the song downloads, or a check once it's in the library. The row takes no click of its own.
 */
@Composable
fun DownloadableSongCard(
    title: String,
    artistName: String?,
    imageUri: String?,
    downloadState: SongDownloadState,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeListItemRow(modifier = modifier) {
        SongCardContent(title = title, artistName = artistName, imageUri = imageUri) {
            when (downloadState) {
                SongDownloadState.NotDownloaded -> VibeIconButton(
                    icon = VibeIcons.Download,
                    contentDescription = stringResource(R.string.download_song, title),
                    onClick = onDownloadClick,
                )
                SongDownloadState.Queued -> DownloadStatusBox(
                    contentDescription = stringResource(R.string.song_download_queued, title),
                ) {
                    VibeLoader(tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is SongDownloadState.Downloading -> DownloadStatusBox(
                    contentDescription = stringResource(R.string.song_downloading, title),
                ) {
                    CircularProgressIndicator(
                        progress = { downloadState.progress },
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        trackColor = MaterialTheme.colorScheme.outline,
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                    )
                }
                SongDownloadState.Downloaded -> DownloadStatusBox(
                    contentDescription = stringResource(R.string.song_downloaded, title),
                ) {
                    Icon(
                        imageVector = VibeIcons.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/** Same 48dp footprint as the download button it replaces, so the row doesn't shift. */
@Composable
private fun DownloadStatusBox(
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(DragHandleTouchTargetSize)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun DragHandle(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(DragHandleTouchTargetSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = VibeIcons.Menu,
            contentDescription = stringResource(R.string.reorder_song, title),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DragHandleIconSize),
        )
    }
}

/** Artwork and titles, shared so the cards can't drift apart; [trailing] closes the row. */
@Composable
private fun RowScope.SongCardContent(
    title: String,
    artistName: String?,
    imageUri: String?,
    trailing: @Composable () -> Unit,
) {
    SongArtwork(imageUri = imageUri, modifier = Modifier.size(64.dp))
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (artistName != null) {
            Text(
                text = artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    trailing()
}

@Composable
private fun DurationText(duration: String) {
    Text(
        text = duration,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        maxLines = 1,
        modifier = Modifier.widthIn(min = 40.dp),
    )
}

/**
 * Song cover art, or the music-note placeholder while [imageUri] is null, loading or unreadable.
 */
@Composable
fun SongArtwork(
    imageUri: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
) {
    Box(modifier = modifier.clip(shape)) {
        ArtworkPlaceholder(
            icon = VibeIcons.Music,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.fillMaxSize(),
        )
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun SongCardPreview() {
    PreviewSurface {
        SongCard(
            title = "Midnight Drive",
            artistName = "The Night Owls",
            duration = "3:45",
            imageUri = null,
            onClick = {},
        )
        SongCard(
            title = "A song with a really long title that doesn't fit on one line",
            artistName = null,
            duration = "12:05",
            imageUri = null,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SelectableSongCardPreview() {
    PreviewSurface {
        SelectableSongCard(
            title = "Midnight Drive",
            artistName = "The Night Owls",
            duration = "3:45",
            imageUri = null,
            selected = true,
            onSelectedChange = {},
        )
        SelectableSongCard(
            title = "Last Nite",
            artistName = "The Strokes",
            duration = "3:12",
            imageUri = null,
            selected = false,
            onSelectedChange = {},
        )
    }
}

@Preview
@Composable
private fun EditableSongCardPreview() {
    PreviewSurface {
        EditableSongCard(
            title = "Midnight Drive",
            artistName = "The Night Owls",
            imageUri = null,
            onRemoveClick = {},
        )
        EditableSongCard(
            title = "A song with a really long title that doesn't fit on one line",
            artistName = null,
            imageUri = null,
            onRemoveClick = {},
        )
    }
}

@Preview
@Composable
private fun DownloadableSongCardPreview() {
    PreviewSurface {
        DownloadableSongCard(
            title = "Midnight Drive",
            artistName = "The Night Owls",
            imageUri = null,
            downloadState = SongDownloadState.NotDownloaded,
            onDownloadClick = {},
        )
        DownloadableSongCard(
            title = "Neon Rain",
            artistName = "Synthwave Co.",
            imageUri = null,
            downloadState = SongDownloadState.Queued,
            onDownloadClick = {},
        )
        DownloadableSongCard(
            title = "Last Nite",
            artistName = "The Strokes",
            imageUri = null,
            downloadState = SongDownloadState.Downloading(progress = 0.4f),
            onDownloadClick = {},
        )
        DownloadableSongCard(
            title = "Golden Hour",
            artistName = null,
            imageUri = null,
            downloadState = SongDownloadState.Downloaded,
            onDownloadClick = {},
        )
    }
}

@Preview
@Composable
private fun QueueSongCardsPreview() {
    PreviewSurface {
        NowPlayingSongCard(
            title = "Midnight Drive",
            artistName = "The Night Owls",
            imageUri = null,
            isPlaying = true,
            onPlayPauseClick = {},
        )
        ReorderableSongCard(title = "Neon Rain", artistName = "Synthwave Co.", imageUri = null, onClick = {})
    }
}
