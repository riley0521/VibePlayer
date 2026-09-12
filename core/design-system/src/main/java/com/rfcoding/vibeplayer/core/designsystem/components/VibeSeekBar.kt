package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors
import kotlin.math.roundToInt

private val HitAreaHeight = 16.dp
private val TooltipHeight = 16.dp
private val TooltipShadowColor = Color(0x4D0A131D)

/**
 * Figma "seekbar": a pill track whose played portion has no knob — its flat end is the thumb.
 *
 * [progress] is only ever read in the draw and layout phases, so a moving playback position never
 * recomposes the screen around it. While the user drags, [label] renders the Figma time pill
 * (e.g. "2:07 / 4:14") centred on the scrub position; [onSeek] fires on release and on a tap.
 */
@Composable
fun VibeSeekBar(
    progress: () -> Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: ((Float) -> String)? = null,
    trackHeight: Dp = 6.dp,
    inactiveColor: Color = MaterialTheme.colorScheme.outline,
    activeColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    // Only `isDragging` is read during composition: the fraction itself stays out of it.
    var isDragging by remember { mutableStateOf(false) }
    val dragFraction = remember { mutableFloatStateOf(0f) }
    var trackWidth by remember { mutableIntStateOf(0) }
    val fraction = { (if (isDragging) dragFraction.floatValue else progress()).coerceIn(0f, 1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HitAreaHeight)
            .onSizeChanged { trackWidth = it.width }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction.floatValue = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragFraction.floatValue)
                    },
                    onDragCancel = { isDragging = false },
                ) { change, _ ->
                    dragFraction.floatValue = (change.position.x / size.width).coerceIn(0f, 1f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset -> onSeek((offset.x / size.width).coerceIn(0f, 1f)) }
            }
            .drawBehind {
                val height = trackHeight.toPx()
                val top = (size.height - height) / 2f
                val corner = CornerRadius(height / 2f)
                drawRoundRect(
                    color = inactiveColor,
                    topLeft = Offset(0f, top),
                    size = Size(size.width, height),
                    cornerRadius = corner,
                )
                val played = size.width * fraction()
                if (played > 0f) {
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(0f, top),
                        size = Size(played, height),
                        cornerRadius = corner,
                    )
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (label != null && isDragging) {
            SeekTooltip(fraction = fraction, trackWidth = { trackWidth }, label = label)
        }
    }
}

/** The white time pill. Its text changes every drag frame, so it owns the smallest possible scope. */
@Composable
private fun SeekTooltip(
    fraction: () -> Float,
    trackWidth: () -> Int,
    label: (Float) -> String,
    modifier: Modifier = Modifier,
) {
    var tooltipWidth by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .onSizeChanged { tooltipWidth = it.width }
            .offset {
                val maxX = (trackWidth() - tooltipWidth).coerceAtLeast(0)
                val centered = trackWidth() * fraction() - tooltipWidth / 2f
                IntOffset(centered.coerceIn(0f, maxX.toFloat()).roundToInt(), 0)
            }
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(radius = 2.dp, color = TooltipShadowColor),
            )
            .background(MaterialTheme.colorScheme.onSurface, CircleShape)
            .height(TooltipHeight)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label(fraction()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.surface,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun VibeSeekBarPreview() {
    PreviewSurface {
        VibeSeekBar(progress = { 0f }, onSeek = {})
        VibeSeekBar(progress = { 0.5f }, onSeek = {})
        VibeSeekBar(
            progress = { 0.5f },
            onSeek = {},
            label = {
                "2:07 / 4:14"
            },
            trackHeight = 4.dp,
            inactiveColor = MaterialTheme.extendedColors.onSurfaceOverlay,
        )
    }
}
