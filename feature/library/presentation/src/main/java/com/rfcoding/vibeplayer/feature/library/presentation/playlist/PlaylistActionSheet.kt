package com.rfcoding.vibeplayer.feature.library.presentation.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.PlaylistCard
import com.rfcoding.vibeplayer.core.designsystem.components.VibeActionSheetButton
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheet
import com.rfcoding.vibeplayer.feature.library.presentation.R
import com.rfcoding.vibeplayer.feature.library.presentation.components.SheetPreviewSurface

/**
 * Figma "Main Page - Playlist + Action Sheet": the playlist card repeated as a static header, then the
 * actions. The Favourites variant keeps only Play, because a virtual playlist can't be renamed,
 * re-covered or deleted.
 */
@Composable
fun PlaylistActionSheet(
    sheet: PlaylistSheet.PlaylistActions,
    onAction: (PlaylistAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VibeBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        PlaylistActionSheetContent(sheet = sheet, onAction = onAction)
    }
}

@Composable
internal fun PlaylistActionSheetContent(
    sheet: PlaylistSheet.PlaylistActions,
    onAction: (PlaylistAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // The sheet keeps 16dp side padding on tablets too; only the screen behind it grows to 24dp.
            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val playlist = sheet.playlist
        PlaylistCard(
            title = playlist?.name ?: stringResource(R.string.favourites),
            subtitle = pluralStringResource(
                R.plurals.playlist_song_count,
                sheet.songCount,
                sheet.songCount,
            ),
            artwork = if (playlist == null) {
                PlaylistArtwork.Favourites
            } else {
                playlist.imageUri?.let(PlaylistArtwork::Image) ?: PlaylistArtwork.Default
            },
        )
        VibeActionSheetButton(
            text = stringResource(R.string.action_play),
            icon = VibeIcons.PlayLinear,
            onClick = { onAction(sheet.playAction) },
        )
        if (playlist != null) {
            VibeActionSheetButton(
                text = stringResource(R.string.action_rename),
                icon = VibeIcons.Pen,
                onClick = { onAction(PlaylistAction.OnRenamePlaylistClick(playlist.id)) },
            )
            VibeActionSheetButton(
                text = stringResource(R.string.action_change_cover),
                icon = VibeIcons.ImageEdit,
                onClick = { onAction(PlaylistAction.OnChangePlaylistCoverClick(playlist.id)) },
            )
            VibeActionSheetButton(
                text = stringResource(R.string.action_delete),
                icon = VibeIcons.Bin,
                onClick = { onAction(PlaylistAction.OnDeletePlaylistClick(playlist.id)) },
            )
        }
    }
}

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun PlaylistActionSheetPreview() {
    SheetPreviewSurface {
        PlaylistActionSheetContent(
            sheet = PlaylistSheet.PlaylistActions(playlist = PreviewPlaylists.first()),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun FavouritesActionSheetPreview() {
    SheetPreviewSurface {
        PlaylistActionSheetContent(
            sheet = PlaylistSheet.PlaylistActions(playlist = null, favouriteSongCount = 24),
            onAction = {},
        )
    }
}
