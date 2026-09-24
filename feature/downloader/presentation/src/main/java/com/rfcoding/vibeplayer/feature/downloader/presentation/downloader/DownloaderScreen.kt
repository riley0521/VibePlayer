package com.rfcoding.vibeplayer.feature.downloader.presentation.downloader

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.vibeplayer.core.designsystem.components.DownloadableSongCard
import com.rfcoding.vibeplayer.core.designsystem.components.SongDownloadState
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButton
import com.rfcoding.vibeplayer.core.designsystem.components.VibeButtonStyle
import com.rfcoding.vibeplayer.core.designsystem.components.VibeFab
import com.rfcoding.vibeplayer.core.designsystem.components.VibeLoader
import com.rfcoding.vibeplayer.core.designsystem.components.VibeMainTopBar
import com.rfcoding.vibeplayer.core.designsystem.components.VibeSearchField
import com.rfcoding.vibeplayer.core.designsystem.components.bottomFade
import com.rfcoding.vibeplayer.core.designsystem.icons.VibeIcons
import com.rfcoding.vibeplayer.core.designsystem.theme.VibePlayerTheme
import com.rfcoding.vibeplayer.core.presentation.MainNavigationLayout
import com.rfcoding.vibeplayer.core.presentation.MiniPlayer
import com.rfcoding.vibeplayer.core.presentation.MiniPlayerHeight
import com.rfcoding.vibeplayer.core.presentation.NowPlayingUi
import com.rfcoding.vibeplayer.core.presentation.ObserveAsEvents
import com.rfcoding.vibeplayer.core.presentation.SongUi
import com.rfcoding.vibeplayer.core.presentation.TabletMiniPlayerWidth
import com.rfcoding.vibeplayer.core.presentation.currentDeviceConfiguration
import com.rfcoding.vibeplayer.core.presentation.rememberRemainingNavigationBarInset
import com.rfcoding.vibeplayer.feature.downloader.presentation.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val MobileTopBarPadding = PaddingValues(start = 16.dp, end = 10.dp)
private val TabletTopBarPadding = PaddingValues(start = 24.dp, end = 18.dp)

/** Room for the Download all FAB below the last card. */
private val FabClearance = 72.dp

/**
 * @param navigation the app's Library/Downloader switch: placed below the content on mobile and at
 * the start on tablet.
 */
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun DownloaderRoot(
    onMiniPlayerClick: () -> Unit,
    navigation: @Composable () -> Unit,
    viewModel: DownloaderViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        val message = when (event) {
            is DownloaderEvent.Error -> event.message.asString(context)
            is DownloaderEvent.DownloadsFailed -> {
                val reason = event.reason.asString(context)
                if (event.title != null) {
                    context.getString(R.string.download_failed, event.title, reason)
                } else {
                    context.resources.getQuantityString(R.plurals.downloads_failed, event.count, event.count, reason)
                }
            }
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // A download asks for the permissions it needs first, then goes ahead with this action.
    var pendingDownload by remember { mutableStateOf<DownloaderAction?>(null) }
    // Notifications are optional, so they're asked for once per screen, not before every download.
    var hasAskedForNotifications by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val action = pendingDownload ?: return@rememberLauncherForActivityResult
        pendingDownload = null
        if (context.needsLegacyStoragePermission()) {
            Toast.makeText(context, R.string.error_storage_permission, Toast.LENGTH_LONG).show()
        } else {
            viewModel.onAction(action)
        }
    }

    DownloaderScreen(
        state = state,
        onAction = { action ->
            when (action) {
                DownloaderAction.OnMiniPlayerClick -> onMiniPlayerClick()
                is DownloaderAction.OnDownloadClick, DownloaderAction.OnDownloadAllClick -> {
                    val missing = buildList {
                        if (context.needsLegacyStoragePermission()) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        if (!hasAskedForNotifications && context.needsNotificationPermission()) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (missing.isEmpty()) {
                        viewModel.onAction(action)
                    } else {
                        hasAskedForNotifications = true
                        pendingDownload = action
                        permissionLauncher.launch(missing.toTypedArray())
                    }
                }
                else -> viewModel.onAction(action)
            }
        },
        navigation = navigation,
    )
}

@Composable
fun DownloaderScreen(
    state: DownloaderState,
    onAction: (DownloaderAction) -> Unit,
    modifier: Modifier = Modifier,
    navigation: @Composable () -> Unit = {},
) {
    val isMobile = currentDeviceConfiguration().isMobile
    val hasMiniPlayer = state.nowPlaying != null

    MainNavigationLayout(navigation = navigation, modifier = modifier) { bottomBar ->
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            // The mini player and the navigation bar inset themselves.
            contentWindowInsets = WindowInsets(0),
            topBar = {
                VibeMainTopBar(contentPadding = if (isMobile) MobileTopBarPadding else TabletTopBarPadding)
            },
            bottomBar = bottomBar,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                val horizontalPadding = if (isMobile) 16.dp else 24.dp
                Column(modifier = Modifier.fillMaxSize()) {
                    LinkField(
                        url = state.url,
                        isResolving = state.isResolving,
                        onAction = onAction,
                        modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
                    )
                    when {
                        state.isResolving -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            VibeLoader(size = 32.dp)
                        }
                        state.tracks.isEmpty() -> EmptyContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        )
                        else -> TrackList(
                            tracks = state.tracks,
                            horizontalPadding = horizontalPadding,
                            onDownloadClick = { onAction(DownloaderAction.OnDownloadClick(it)) },
                            modifier = Modifier.bottomFade(MaterialTheme.colorScheme.background),
                        )
                    }
                }

                DownloadAllFab(
                    visible = state.canDownloadAll && !state.isResolving,
                    bottomPadding = if (isMobile && hasMiniPlayer) MiniPlayerHeight + 12.dp else 20.dp,
                    onClick = { onAction(DownloaderAction.OnDownloadAllClick) },
                )

                state.nowPlaying?.let { nowPlaying ->
                    MiniPlayer(
                        song = nowPlaying.song,
                        isPlaying = nowPlaying.isPlaying,
                        positionMillis = nowPlaying.positionMillis,
                        canSkipToPrevious = nowPlaying.canSkipToPrevious,
                        onClick = { onAction(DownloaderAction.OnMiniPlayerClick) },
                        onSkipToPreviousClick = { onAction(DownloaderAction.OnSkipToPreviousClick) },
                        onPlayPauseClick = { onAction(DownloaderAction.OnPlayPauseClick) },
                        onSkipNextClick = { onAction(DownloaderAction.OnSkipNextClick) },
                        onSeek = { onAction(DownloaderAction.OnSeek(it)) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .widthIn(max = if (isMobile) Dp.Unspecified else TabletMiniPlayerWidth),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.DownloadAllFab(
    visible: Boolean,
    bottomPadding: Dp,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(end = 16.dp, bottom = bottomPadding),
    ) {
        VibeFab(
            icon = VibeIcons.Download,
            contentDescription = stringResource(R.string.download_all),
            onClick = onClick,
        )
    }
}

/** The link field, with Paste while it's empty and Find once there's a link. */
@Composable
private fun LinkField(
    url: String,
    isResolving: Boolean,
    onAction: (DownloaderAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val find = {
        focusManager.clearFocus()
        onAction(DownloaderAction.OnFindClick)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VibeSearchField(
            query = url,
            onQueryChange = { onAction(DownloaderAction.OnUrlChange(it)) },
            placeholder = stringResource(R.string.paste_link),
            onClearClick = { onAction(DownloaderAction.OnClearClick) },
            keyboardActions = KeyboardActions(onSearch = { find() }),
            modifier = Modifier.weight(1f),
        )
        if (url.isBlank()) {
            VibeButton(
                text = stringResource(R.string.paste),
                style = VibeButtonStyle.Outlined,
                onClick = {
                    scope.launch {
                        val text = clipboard.getClipEntry()?.clipData
                            ?.takeIf { it.itemCount > 0 }
                            ?.getItemAt(0)
                            ?.coerceToText(context)
                            ?.toString()
                        if (!text.isNullOrBlank()) {
                            focusManager.clearFocus()
                            onAction(DownloaderAction.OnPaste(text))
                        }
                    }
                },
            )
        } else {
            VibeButton(
                text = stringResource(R.string.find),
                onClick = find,
                isLoading = isResolving,
            )
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<DownloadTrackUi>,
    horizontalPadding: Dp,
    onDownloadClick: (videoId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Room for the mini player (whether or not it shows, as on the Songs tab) and the FAB above it.
    val navigationBarInset = rememberRemainingNavigationBarInset()
    val bottomContentPadding = navigationBarInset.bottom() + MiniPlayerHeight + FabClearance

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .then(navigationBarInset.modifier),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            bottom = bottomContentPadding,
        ),
    ) {
        item(key = "header") {
            Text(
                text = pluralStringResource(R.plurals.found_song_count, tracks.size, tracks.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        items(items = tracks, key = { it.videoId }) { track ->
            DownloadableSongCard(
                title = track.title,
                artistName = track.artistName,
                imageUri = track.thumbnailUrl,
                downloadState = track.downloadState,
                onDownloadClick = { onDownloadClick(track.videoId) },
            )
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.downloader_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.downloader_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 400.dp),
        )
    }
}

private fun Context.needsLegacyStoragePermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED
}

private fun Context.needsNotificationPermission(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
}

private val PreviewTracks = listOf(
    DownloadTrackUi("v1", "Midnight Drive", "The Night Owls", null, SongDownloadState.Downloaded),
    DownloadTrackUi("v2", "Neon Rain", "Synthwave Co.", null, SongDownloadState.Downloading(0.6f)),
    DownloadTrackUi("v3", "Last Nite", "The Strokes", null, SongDownloadState.Queued),
    DownloadTrackUi("v4", "Golden Hour", "JVKE", null, SongDownloadState.NotDownloaded),
    DownloadTrackUi("v5", "505", "Arctic Monkeys", null, SongDownloadState.NotDownloaded),
)

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun DownloaderScreenEmptyPreview() {
    VibePlayerTheme {
        DownloaderScreen(state = DownloaderState(), onAction = {})
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun DownloaderScreenResolvingPreview() {
    VibePlayerTheme {
        DownloaderScreen(
            state = DownloaderState(url = "https://youtu.be/dQw4w9WgXcQ", isResolving = true),
            onAction = {},
        )
    }
}

@Preview(name = "Mobile", widthDp = 412, heightDp = 917)
@Preview(name = "Tablet", widthDp = 840, heightDp = 917)
@Composable
private fun DownloaderScreenResultsPreview() {
    VibePlayerTheme {
        DownloaderScreen(
            state = DownloaderState(
                url = "https://www.youtube.com/playlist?list=PL123",
                tracks = PreviewTracks,
                canDownloadAll = true,
                nowPlaying = NowPlayingUi(song = SongUi("song.mp3", "Midnight Drive", "The Night Owls", null, 225_000)),
            ),
            onAction = {},
        )
    }
}
