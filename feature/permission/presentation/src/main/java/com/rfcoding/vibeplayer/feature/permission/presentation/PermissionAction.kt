package com.rfcoding.vibeplayer.feature.permission.presentation

sealed interface PermissionAction {
    /** The screen's "Allow Access" button and the rationale sheet's retry button. */
    data object OnAllowAccessClick : PermissionAction
    data object OnOpenSettingsClick : PermissionAction
    data object OnRationaleDismiss : PermissionAction
}
