package com.rfcoding.vibeplayer.feature.permission.presentation

sealed interface PermissionAction {
    data object OnAllowAccessClick : PermissionAction
}
