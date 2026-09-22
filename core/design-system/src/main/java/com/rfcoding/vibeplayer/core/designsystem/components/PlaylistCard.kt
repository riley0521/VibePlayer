package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rfcoding.vibeplayer.core.designsystem.R
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
 * Passing [onMenuClick] adds the trailing options button; the "create playlist" row leaves it out.
 * [trailingContent] is drawn in the same place, for a trailing icon that isn't a button of its own.
 * A null [onClick] leaves the card inert, which is how the action sheet reuses it as a header.
 */
@Composable
fun PlaylistCard(
    title: String,
    subtitle: String?,
    artwork: PlaylistArtwork,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    VibeListItemRow(onClick = onClick, modifier = modifier) {
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
        if (onMenuClick != null) {
            // Figma leaves the card's options button unfilled, unlike the top bar's icon buttons.
            VibeIconButton(
                icon = VibeIcons.MenuDots,
                contentDescription = stringResource(R.string.playlist_options),
                onClick = onMenuClick,
                containerColor = Color.Transparent,
            )
        }
        trailingContent?.invoke()
    }
}

/**
 * The artwork of a playlist, unclipped and unsized: the card draws it as a 64dp circle, the Playlist
 * Page as a 200dp one.
 */
@Composable
fun PlaylistArtworkImage(
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
        PlaylistCard(
            title = "Favourites",
            subtitle = "24 songs",
            artwork = PlaylistArtwork.Favourites,
            onClick = {},
            onMenuClick = {},
        )
        PlaylistCard(
            title = "Road trip",
            subtitle = "8 songs",
            artwork = PlaylistArtwork.Default,
            onClick = {},
            onMenuClick = {},
        )
        PlaylistCard(title = "Create playlist", subtitle = null, artwork = PlaylistArtwork.Create, onClick = {})
        PlaylistCard(
            title = "Friday Chill",
            subtitle = "3 songs",
            artwork = PlaylistArtwork.Default,
            onClick = {},
            trailingContent = {
                Icon(
                    imageVector = VibeIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}
