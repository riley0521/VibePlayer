package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.R
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.extendedColors

/**
 * Figma "search-input": search icon, query text and a clear button once the query isn't empty.
 */
@Composable
fun VibeSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colorScheme = MaterialTheme.colorScheme
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface),
        cursorBrush = SolidColor(colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            VibeFieldContainer {
                Icon(
                    imageVector = VibeIcons.Search,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
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
                if (query.isNotEmpty()) {
                    // 24dp touch target, shifted so the 16dp glyph keeps Figma's 16dp end padding.
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = null,
                                indication = PressedOverlayIndication,
                                role = Role.Button,
                                onClick = onClearClick,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = VibeIcons.Close,
                            contentDescription = stringResource(R.string.clear_search),
                            tint = MaterialTheme.extendedColors.textDisabled,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun VibeSearchFieldPreview() {
    PreviewSurface {
        VibeSearchField(query = "", onQueryChange = {}, placeholder = "Search", onClearClick = {})
        VibeSearchField(query = "Lo-fi beats", onQueryChange = {}, placeholder = "Search", onClearClick = {})
    }
}
