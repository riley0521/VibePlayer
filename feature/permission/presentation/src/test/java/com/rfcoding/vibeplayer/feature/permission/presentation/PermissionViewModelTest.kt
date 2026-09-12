package com.rfcoding.vibeplayer.feature.permission.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PermissionViewModelTest {

    private lateinit var viewModel: PermissionViewModel

    @BeforeEach
    fun setUp() {
        viewModel = PermissionViewModel()
    }

    @Test
    fun `starts without permission and without a sheet`() {
        assertThat(viewModel.state.value.isGranted).isFalse()
        assertThat(viewModel.state.value.rationale).isNull()
    }

    @Test
    fun `granting closes any sheet`() {
        viewModel.onPermissionResult(isGranted = false, canAskAgain = true)

        viewModel.onPermissionResult(isGranted = true, canAskAgain = false)

        assertThat(viewModel.state.value.isGranted).isTrue()
        assertThat(viewModel.state.value.rationale).isNull()
    }

    @Test
    fun `first denial asks for a retry`() {
        viewModel.onPermissionResult(isGranted = false, canAskAgain = true)

        assertThat(viewModel.state.value.isGranted).isFalse()
        assertThat(viewModel.state.value.rationale).isEqualTo(PermissionRationale.Retry)
    }

    @Test
    fun `denying again sends the user to settings`() {
        viewModel.onPermissionResult(isGranted = false, canAskAgain = true)

        // The system stops showing its dialog, which is how a permanent denial reports itself.
        viewModel.onPermissionResult(isGranted = false, canAskAgain = false)

        assertThat(viewModel.state.value.rationale).isEqualTo(PermissionRationale.Settings)
    }

    @Test
    fun `a denial with no dialog left goes straight to settings`() {
        viewModel.onPermissionResult(isGranted = false, canAskAgain = false)

        assertThat(viewModel.state.value.rationale).isEqualTo(PermissionRationale.Settings)
    }

    @Test
    fun `every action closes the sheet`() {
        val actions = listOf(
            PermissionAction.OnAllowAccessClick,
            PermissionAction.OnOpenSettingsClick,
            PermissionAction.OnRationaleDismiss,
        )

        actions.forEach { action ->
            viewModel.onPermissionResult(isGranted = false, canAskAgain = true)

            viewModel.onAction(action)

            assertThat(viewModel.state.value.rationale).isNull()
        }
    }

    @Test
    fun `returning from settings with the permission granted closes the sheet`() {
        viewModel.onPermissionResult(isGranted = false, canAskAgain = false)

        viewModel.onPermissionChecked(isGranted = true)

        assertThat(viewModel.state.value.isGranted).isTrue()
        assertThat(viewModel.state.value.rationale).isNull()
    }

    @Test
    fun `returning from settings still denied keeps the sheet open`() {
        viewModel.onPermissionResult(isGranted = false, canAskAgain = false)

        viewModel.onPermissionChecked(isGranted = false)

        assertThat(viewModel.state.value.isGranted).isFalse()
        assertThat(viewModel.state.value.rationale).isEqualTo(PermissionRationale.Settings)
    }
}
