package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

/**
 * Figma "input": a single-line field with a "length/[maxLength]" counter. The counter is only a hint;
 * callers decide what to do with longer input.
 */
@Composable
fun VibeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colorScheme = MaterialTheme.colorScheme
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface),
        cursorBrush = SolidColor(colorScheme.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            VibeFieldContainer {
                // Inner row so the text and the smaller counter share a baseline while staying centered.
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).alignByBaseline()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                    Text(
                        text = "${value.length}/$maxLength",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .alignByBaseline(),
                    )
                }
            }
        },
    )
}

/** The pill container shared by [VibeTextField] and [VibeSearchField]. */
@Composable
internal fun VibeFieldContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(MaterialTheme.extendedColors.buttonHover, CircleShape)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Preview
@Composable
private fun VibeTextFieldPreview() {
    PreviewSurface {
        VibeTextField(value = "", onValueChange = {}, placeholder = "Enter playlist name", maxLength = 40)
        VibeTextField(value = "Chill vibes", onValueChange = {}, placeholder = "Enter playlist name", maxLength = 40)
    }
}
