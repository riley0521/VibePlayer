package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons

/** The four artwork types of Figma's playlist-card. */
sealed interface PlaylistArtwork {
    data object Favourites : PlaylistArtwork
    data object Default : PlaylistArtwork
    /** The "create playlist" row. */
    data object Create : PlaylistArtwork
    data class Image(val uri: String) : PlaylistArtwork
}

/**
 * Figma "playlist-card". [subtitle] is already formatted by the caller (e.g. "12 songs").
 */
@Composable
fun PlaylistCard(
    title: String,
    subtitle: String?,
    artwork: PlaylistArtwork,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItemRow(onClick = onClick, modifier = modifier) {
        PlaylistArtworkImage(
            artwork = artwork,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
        )
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
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlaylistArtworkImage(
    artwork: PlaylistArtwork,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    when (artwork) {
        PlaylistArtwork.Favourites -> ArtworkPlaceholder(
            icon = VibeIcons.HeartDuotone,
            tint = colorScheme.primary,
            modifier = modifier,
        )
        PlaylistArtwork.Default -> ArtworkPlaceholder(
            icon = VibeIcons.PlaylistDuotone,
            tint = colorScheme.primary,
            modifier = modifier,
        )
        PlaylistArtwork.Create -> ArtworkPlaceholder(
            icon = VibeIcons.Plus,
            tint = colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        is PlaylistArtwork.Image -> Box(modifier = modifier) {
            ArtworkPlaceholder(
                icon = VibeIcons.PlaylistDuotone,
                tint = colorScheme.primary,
                modifier = Modifier.fillMaxSize(),
            )
            AsyncImage(
                model = artwork.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun PlaylistCardPreview() {
    PreviewSurface {
        PlaylistCard(title = "Favourites", subtitle = "24 songs", artwork = PlaylistArtwork.Favourites, onClick = {})
        PlaylistCard(title = "Road trip", subtitle = "8 songs", artwork = PlaylistArtwork.Default, onClick = {})
        PlaylistCard(title = "Create playlist", subtitle = null, artwork = PlaylistArtwork.Create, onClick = {})
    }
}
