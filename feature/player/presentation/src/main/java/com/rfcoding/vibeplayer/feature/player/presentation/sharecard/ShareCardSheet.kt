package com.rfcoding.vibeplayer.feature.player.presentation.sharecard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rfcoding.vibeplayer.core.designsystem.components.SongArtwork
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeLogo
import com.rfcoding.vibeplayer.core.designsystem.theme.bodyLargeMedium
import com.rfcoding.vibeplayer.core.presentation.SheetPreviewSurface
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.VibeBottomSheet
import com.rfcoding.vibeplayer.feature.player.presentation.PlayerAction
import com.rfcoding.vibeplayer.feature.player.presentation.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.rfcoding.vibeplayer.core.designsystem.R as DesignSystemR

private val CardMaxWidth = 320.dp
private val CardShape = RoundedCornerShape(24.dp)

/** The card is at least 4:5; long titles make it taller rather than being cut off. */
private const val CardMinHeightRatio = 5f / 4f

/**
 * Previews the song's share card; Save captures exactly what is shown, so the artwork is already
 * loaded, and hands the PNG to the ViewModel. On API 28 it first asks for the storage permission.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardSheet(
    song: SongUi,
    isSaving: Boolean,
    onAction: (PlayerAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cardLayer = rememberGraphicsLayer()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val saveCard = {
        scope.launch {
            val pngBytes = cardLayer.toImageBitmap().toPngBytes()
            onAction(PlayerAction.OnSaveCardClick(pngBytes))
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) saveCard() else onAction(PlayerAction.OnStoragePermissionDenied)
    }

    VibeBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        sheetState = sheetState
    ) {
        ShareCardSheetContent(
            song = song,
            isSaving = isSaving,
            onSaveClick = {
                if (context.needsLegacyStoragePermission()) {
                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    saveCard()
                }
            },
            onCancelClick = onDismiss,
            cardModifier = Modifier.drawWithContent {
                cardLayer.record { this@drawWithContent.drawContent() }
                drawLayer(cardLayer)
            },
        )
    }
}

@Composable
private fun ShareCardSheetContent(
    song: SongUi,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.share_card_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        ShareCard(song = song, modifier = cardModifier)
        Row(
            modifier = Modifier
                .widthIn(max = CardMaxWidth)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VibeButton(
                text = stringResource(R.string.cancel),
                onClick = onCancelClick,
                style = VibeButtonStyle.Outlined,
                modifier = Modifier.weight(1f),
            )
            VibeButton(
                text = stringResource(R.string.save_image),
                onClick = onSaveClick,
                isLoading = isSaving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** What ends up in the saved image: artwork, title, artist, then the app's logo and name. */
@Composable
fun ShareCard(
    song: SongUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .widthIn(max = CardMaxWidth)
            .fillMaxWidth()
            .minHeightFromWidth(CardMinHeightRatio),
        shape = CardShape,
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SongArtwork(
                imageUri = song.imageUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
            )
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            val artistName = song.artistName
            if (artistName != null) {
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                VibeLogo(size = 24.dp)
                Text(
                    text = stringResource(DesignSystemR.string.vibe_player),
                    style = MaterialTheme.typography.bodyLargeMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

/** A minimum height of `width * ratio`, still growing with its content. */
private fun Modifier.minHeightFromWidth(ratio: Float): Modifier = layout { measurable, constraints ->
    val minHeight = (constraints.maxWidth * ratio).toInt().coerceAtMost(constraints.maxHeight)
    val placeable = measurable.measure(
        constraints.copy(minHeight = maxOf(constraints.minHeight, minHeight)),
    )
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

private fun Context.needsLegacyStoragePermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED
}

/** A captured layer can be a hardware bitmap, which has to be copied to memory before encoding. */
private suspend fun ImageBitmap.toPngBytes(): ByteArray = withContext(Dispatchers.Default) {
    val bitmap = asAndroidBitmap().let { source ->
        if (source.config == Bitmap.Config.HARDWARE) source.copy(Bitmap.Config.ARGB_8888, false) else source
    }
    ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }
}

private val PreviewSong = SongUi(
    id = "do-i-wanna-know.mp3",
    title = "Do I Wanna Know? (Live at the Royal Albert Hall, London)",
    artistName = "Arctic Monkeys featuring a very long list of guest artists",
    imageUri = null,
    durationMillis = 272_000,
)

@Preview(name = "Mobile", widthDp = 412)
@Preview(name = "Tablet sheet", widthDp = 480)
@Composable
private fun ShareCardSheetPreview() {
    SheetPreviewSurface {
        ShareCardSheetContent(
            song = PreviewSong,
            isSaving = false,
            onSaveClick = {},
            onCancelClick = {},
        )
    }
}

@Preview(name = "Short title", widthDp = 412)
@Composable
private fun ShareCardShortTitlePreview() {
    SheetPreviewSurface {
        ShareCardSheetContent(
            song = PreviewSong.copy(title = "505", artistName = "Arctic Monkeys"),
            isSaving = true,
            onSaveClick = {},
            onCancelClick = {},
        )
    }
}
