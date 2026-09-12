package com.rfcoding.vibeplayer.feature.permission.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Decides what the permission screen shows. It never touches the Android permission APIs itself:
 * the Root reads them and feeds the answers in through [onPermissionChecked] and
 * [onPermissionResult], which keeps the decision table below unit-testable.
 */
class PermissionViewModel : ViewModel() {

    private val _state = MutableStateFlow(PermissionState())
    val state = _state.asStateFlow()

    fun onAction(action: PermissionAction) {
        when (action) {
            // Every action leaves the sheet: two of them hand off to the system, one is a dismiss.
            PermissionAction.OnAllowAccessClick,
            PermissionAction.OnOpenSettingsClick,
            PermissionAction.OnRationaleDismiss,
                -> _state.update { it.copy(rationale = null) }
        }
    }

    /**
     * The permission as the system currently reports it. Re-checked on every resume, so a grant
     * made in system Settings lands here — that route sends nothing back to the request launcher.
     */
    fun onPermissionChecked(isGranted: Boolean) {
        _state.update {
            it.copy(isGranted = isGranted, rationale = if (isGranted) null else it.rationale)
        }
    }

    /**
     * The system's answer to a request we launched. [canAskAgain] is
     * `shouldShowRequestPermissionRationale`, which is true after the first denial and false once
     * the system stops showing its dialog.
     */
    fun onPermissionResult(isGranted: Boolean, canAskAgain: Boolean) {
        _state.update {
            it.copy(
                isGranted = isGranted,
                rationale = when {
                    isGranted -> null
                    canAskAgain -> PermissionRationale.Retry
                    else -> PermissionRationale.Settings
                },
            )
        }
    }
}
