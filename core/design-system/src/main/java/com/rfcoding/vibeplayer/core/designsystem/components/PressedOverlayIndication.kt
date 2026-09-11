package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import com.rfcoding.vibeplayer.core.designsystem.theme.ButtonHover
import kotlinx.coroutines.launch

/**
 * The Figma "Pressed" state: a flat `Button/Hover` overlay drawn over the component while it's pressed.
 * Clip the component to its shape before `clickable` so the overlay follows the shape.
 */
object PressedOverlayIndication : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        PressedOverlayNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = System.identityHashCode(this)
}

private class PressedOverlayNode(
    private val interactionSource: InteractionSource,
) : Modifier.Node(), DrawModifierNode {

    private var isPressed = false

    override fun onAttach() {
        coroutineScope.launch {
            val presses = mutableListOf<PressInteraction.Press>()
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> presses.add(interaction)
                    is PressInteraction.Release -> presses.remove(interaction.press)
                    is PressInteraction.Cancel -> presses.remove(interaction.press)
                }
                val pressed = presses.isNotEmpty()
                if (pressed != isPressed) {
                    isPressed = pressed
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (isPressed) {
            drawRect(color = ButtonHover)
        }
    }
}
