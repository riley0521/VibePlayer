package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.R

// The illustration's own color in Figma; it isn't a theme token.
private val RadarColor = Color(0xFFEBFE6F)

// Figma draws radar-img on a 140x140 frame.
private const val RadarFrame = 140f
private const val GlowDiameter = 102.487f
private const val GlowStrokeWidth = 1.5f
private const val SweepDurationMillis = 2000

/**
 * Figma "radar-img", the decorative scanning illustration. Rings, center dot and needle come from a vector;
 * the glow behind the needle is an angular gradient, which vectors can't express, so it's drawn here.
 *
 * [isSweeping] spins the whole illustration clockwise. Rings and dot are concentric, so only the needle and
 * its glow appear to move.
 */
@Composable
fun VibeRadar(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    isSweeping: Boolean = false,
) {
    val radar = painterResource(R.drawable.img_radar)
    val rotation = if (isSweeping) rememberSweepRotation() else null
    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation?.value ?: 0f },
    ) {
        drawRadarGlow(scale = this.size.minDimension / RadarFrame)
        with(radar) { draw(this@Canvas.size) }
    }
}

@Composable
private fun rememberSweepRotation(): State<Float> =
    rememberInfiniteTransition(label = "radarSweep").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SweepDurationMillis, easing = LinearEasing),
        ),
        label = "radarSweepAngle",
    )

/**
 * A full circle: a 4% fill, then a sweep that stays transparent for the first 64.35% of the turn and ramps
 * to 60% at the needle, which points 30deg left of 12 o'clock (240deg clockwise from 3 o'clock).
 */
private fun DrawScope.drawRadarGlow(scale: Float) {
    val radius = GlowDiameter / 2 * scale
    drawCircle(color = RadarColor.copy(alpha = 0.04f), radius = radius)
    rotate(degrees = 240f) {
        drawCircle(
            brush = Brush.sweepGradient(
                0f to RadarColor.copy(alpha = 0f),
                0.6435f to RadarColor.copy(alpha = 0f),
                1f to RadarColor.copy(alpha = 0.6f),
            ),
            radius = radius,
        )
    }
    val strokeWidth = GlowStrokeWidth * scale
    drawCircle(
        color = RadarColor,
        radius = radius - strokeWidth / 2,
        style = Stroke(width = strokeWidth),
    )
}

@Preview
@Composable
private fun VibeRadarPreview() {
    PreviewSurface {
        VibeRadar()
        VibeRadar(isSweeping = true)
    }
}
