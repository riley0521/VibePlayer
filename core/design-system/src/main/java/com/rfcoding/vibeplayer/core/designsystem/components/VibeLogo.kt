package com.rfcoding.vibeplayer.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfcoding.vibeplayer.core.designsystem.R

/**
 * The VibePlayer note logo: 72dp on the splash, 56dp on the permission screen, 24dp in the main top bar.
 */
@Composable
fun VibeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(R.drawable.ic_logo),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
    )
}

@Preview
@Composable
private fun VibeLogoPreview() {
    PreviewSurface {
        VibeLogo()
        VibeLogo(size = 56.dp)
        VibeLogo(size = 24.dp)
    }
}
