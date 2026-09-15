package com.rfcoding.vibeplayer.feature.library.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeCheckbox
import com.rfcoding.vibeplayer.core.designsystem.components.VibeSelectableListItemRow
import com.rfcoding.vibeplayer.feature.library.presentation.R

/** Figma keeps the tablet bottom button a centered 480dp pill rather than letting it span the screen. */
internal val TabletBottomActionButtonWidth = 480.dp

/**
 * The 52dp Select All header of a selectable song list: the shared 12dp row padding around a 28dp
 * checkbox. Used by Add Songs and the Playlist Page's delete mode.
 */
@Composable
internal fun SelectAllRow(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeSelectableListItemRow(
        selected = selected,
        onSelectedChange = onSelectedChange,
        modifier = modifier,
    ) {
        VibeCheckbox(checked = selected, onCheckedChange = null)
        Text(
            text = stringResource(R.string.select_all),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** The pill button a selection screen puts in its bottom bar (Add Songs' OK, the Playlist Page's Delete). */
@Composable
internal fun BottomActionButton(
    text: String,
    onClick: () -> Unit,
    maxWidth: Dp,
    modifier: Modifier = Modifier,
    style: VibeButtonStyle = VibeButtonStyle.Filled,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VibeButton(
            text = text,
            onClick = onClick,
            style = style,
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
        )
    }
}
