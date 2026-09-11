package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons

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
    ListItemRow(onClick = onClick, modifier = modifier) {
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
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 40.dp),
        )
    }
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
