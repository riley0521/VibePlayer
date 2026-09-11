package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

private val PrimaryShadowColor = Color(0x40C277FF)

/** The purple glow under the Filled button and the FAB. */
internal fun Modifier.primaryDropShadow(shape: Shape): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = 8.dp,
        color = PrimaryShadowColor,
        offset = DpOffset(x = 0.dp, y = 2.dp),
    ),
)

internal fun Modifier.bottomBorder(color: Color, width: Dp = 1.dp): Modifier = drawBehind {
    val strokeWidth = width.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth,
    )
}
