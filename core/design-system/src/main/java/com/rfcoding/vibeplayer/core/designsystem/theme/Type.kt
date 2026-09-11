package com.rfcoding.vibeplayer.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rfcoding.vibeplayer.core.designsystem.R

// A single variable font file: each Font() sets the wght axis from its weight.
val HostGrotesk = FontFamily(
    Font(R.font.host_grotesk, FontWeight.Normal),
    Font(R.font.host_grotesk, FontWeight.Medium),
    Font(R.font.host_grotesk, FontWeight.Bold),
)

private fun hostGrotesk(weight: FontWeight, fontSize: Int, lineHeight: Int) = TextStyle(
    fontFamily = HostGrotesk,
    fontWeight = weight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)

private val MaterialDefaults = Typography()

internal val VibeTypography = Typography(
    // Slots the designs don't use keep the Material metrics, in Host Grotesk.
    displayLarge = MaterialDefaults.displayLarge.copy(fontFamily = HostGrotesk),
    displayMedium = MaterialDefaults.displayMedium.copy(fontFamily = HostGrotesk),
    displaySmall = MaterialDefaults.displaySmall.copy(fontFamily = HostGrotesk),
    headlineLarge = MaterialDefaults.headlineLarge.copy(fontFamily = HostGrotesk),
    headlineMedium = MaterialDefaults.headlineMedium.copy(fontFamily = HostGrotesk),
    headlineSmall = MaterialDefaults.headlineSmall.copy(fontFamily = HostGrotesk),
    titleSmall = MaterialDefaults.titleSmall.copy(fontFamily = HostGrotesk),
    labelSmall = MaterialDefaults.labelSmall.copy(fontFamily = HostGrotesk),

    titleLarge = hostGrotesk(FontWeight.Medium, fontSize = 28, lineHeight = 32), // Title/Title-Large
    titleMedium = hostGrotesk(FontWeight.Bold, fontSize = 20, lineHeight = 24), // Title/Title-Medium
    bodyLarge = hostGrotesk(FontWeight.Normal, fontSize = 16, lineHeight = 22), // Body/Body-Large-Regular
    bodyMedium = hostGrotesk(FontWeight.Normal, fontSize = 14, lineHeight = 18), // Body/Body-Medium-Regular
    bodySmall = hostGrotesk(FontWeight.Normal, fontSize = 12, lineHeight = 16), // Body/Body-Small-Regular
    labelLarge = hostGrotesk(FontWeight.Medium, fontSize = 16, lineHeight = 22), // Body/Body-Large-Medium
    labelMedium = hostGrotesk(FontWeight.Medium, fontSize = 14, lineHeight = 18), // Body/Body-Medium-Medium
)

/** Figma "Body/Body-Large-Medium". */
val Typography.bodyLargeMedium: TextStyle
    get() = labelLarge

/** Figma "Body/Body-Meidum-Medium" (sic). */
val Typography.bodyMediumMedium: TextStyle
    get() = labelMedium
