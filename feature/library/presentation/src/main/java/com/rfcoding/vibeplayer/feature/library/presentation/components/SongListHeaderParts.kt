package com.rfcoding.vibeplayer.feature.library.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.feature.library.presentation.R

/** The Shuffle / Play pair and the song count shared by the Songs tab and the Playlist Page headers. */
@Composable
internal fun ShuffleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    VibeButton(
        text = stringResource(R.string.shuffle),
        onClick = onClick,
        style = VibeButtonStyle.Outlined,
        leadingIcon = VibeIcons.Shuffle,
        modifier = modifier,
    )
}

@Composable
internal fun PlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    VibeButton(
        text = stringResource(R.string.play),
        onClick = onClick,
        style = VibeButtonStyle.Outlined,
        leadingIcon = VibeIcons.PlayLinear,
        modifier = modifier,
    )
}

@Composable
internal fun SongCountText(songCount: Int, modifier: Modifier = Modifier) {
    Text(
        text = pluralStringResource(R.plurals.songs_count, songCount, songCount),
        style = MaterialTheme.typography.bodyLargeMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
