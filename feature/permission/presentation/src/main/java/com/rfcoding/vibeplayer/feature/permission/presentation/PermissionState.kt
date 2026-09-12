package com.rfcoding.vibeplayer.feature.permission.presentation

import androidx.compose.runtime.Stable

@Stable
data class PermissionState(
    val isGranted: Boolean = false,
    /** The rationale sheet the screen puts over itself; null while no sheet is open. */
    val rationale: PermissionRationale? = null,
)

/**
 * Which rationale sheet the user gets after denying. Both draw the same sheet; only the button
 * differs, because [Settings] is reached once the system refuses to show its dialog again.
 */
enum class PermissionRationale {
    /** The first denial: the system will still ask, so the sheet can retry the request. */
    Retry,

    /** A second denial: the request is a no-op now, so the sheet sends the user to Settings. */
    Settings,
}
