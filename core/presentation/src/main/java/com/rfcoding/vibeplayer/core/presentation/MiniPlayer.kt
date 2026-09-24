package com.rfcoding.vibeplayer.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.PressedOverlayIndication
import com.rfcoding.vibeplayer.core.designsystem.components.SongArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.VibeIconButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeSeekBar
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

private val MiniPlayerShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
private val MiniPlayerShadowColor = Color(0x4D0A131D)

/** The height Figma gives the card, which callers use to inset whatever scrolls behind it. */
val MiniPlayerHeight = 112.dp

/** On a tablet the mini player keeps to this width, centered at the bottom. */
val TabletMiniPlayerWidth = 480.dp

/**
 * Figma "Main Page - Songs Tab - Mini player". Docked to the bottom edge: full width on mobile,
 * a 480dp card centred on tablets (the caller constrains it through [modifier]).
 *
 * The seek bar spans the text and controls column only, level with the bottom of the artwork.
 */
@Composable
fun MiniPlayer(
    song: SongUi,
    isPlaying: Boolean,
    positionMillis: Long,
    canSkipToPrevious: Boolean,
    onClick: () -> Unit,
    onSkipToPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val durationText = song.durationText
    val positionFormat = stringResource(R.string.seek_position)
    val progress = { if (song.durationMillis > 0) positionMillis.toFloat() / song.durationMillis else 0f }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = MiniPlayerShape,
                shadow = Shadow(radius = 4.dp, color = MiniPlayerShadowColor),
            )
            .clip(MiniPlayerShape)
            .background(colorScheme.surfaceContainerHigh)
            .clickable(
                interactionSource = null,
                indication = PressedOverlayIndication,
                onClickLabel = stringResource(R.string.open_player),
                onClick = onClick,
            )
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SongArtwork(imageUri = song.imageUri, modifier = Modifier.size(64.dp))
        Column(modifier = Modifier.height(64.dp)) {
            Row(
                modifier = Modifier.height(44.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (song.artistName != null) {
                        Text(
                            text = song.artistName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (canSkipToPrevious) {
                    VibeIconButton(
                        icon = VibeIcons.SkipPrevious,
                        contentDescription = stringResource(R.string.skip_to_previous),
                        onClick = onSkipToPreviousClick,
                        containerSize = 44.dp,
                        containerColor = Color.Transparent,
                    )
                }
                VibeIconButton(
                    icon = if (isPlaying) VibeIcons.Pause else VibeIcons.PlayFilled,
                    contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                    onClick = onPlayPauseClick,
                    tint = colorScheme.surface,
                    containerSize = 44.dp,
                    containerColor = colorScheme.onSurface,
                )
                VibeIconButton(
                    icon = VibeIcons.SkipNext,
                    contentDescription = stringResource(R.string.skip_to_next),
                    onClick = onSkipNextClick,
                    containerSize = 44.dp,
                    containerColor = Color.Transparent,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            VibeSeekBar(
                progress = progress,
                onSeek = onSeek,
                label = { fraction ->
                    val position = (song.durationMillis * fraction).toLong().toDurationText()
                    positionFormat.format(position, durationText)
                },
                trackHeight = 4.dp,
                inactiveColor = MaterialTheme.extendedColors.onSurfaceOverlay,
            )
        }
    }
}

private val PreviewSong = SongUi(
    id = "505.mp3",
    title = "505",
    artistName = "Arctic Monkeys",
    imageUri = null,
    durationMillis = 254_000,
)

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet", widthDp = 480)
@Composable
private fun MiniPlayerPausedPreview() {
    VibePlayerTheme {
        MiniPlayer(
            song = PreviewSong,
            isPlaying = false,
            positionMillis = 0,
            canSkipToPrevious = false,
            onClick = {},
            onSkipToPreviousClick = {},
            onPlayPauseClick = {},
            onSkipNextClick = {},
            onSeek = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet", widthDp = 480)
@Composable
private fun MiniPlayerPlayingPreview() {
    VibePlayerTheme {
        MiniPlayer(
            song = PreviewSong,
            isPlaying = true,
            positionMillis = 127_000,
            canSkipToPrevious = true,
            onClick = {},
            onSkipToPreviousClick = {},
            onPlayPauseClick = {},
            onSkipNextClick = {},
            onSeek = {},
        )
    }
}
