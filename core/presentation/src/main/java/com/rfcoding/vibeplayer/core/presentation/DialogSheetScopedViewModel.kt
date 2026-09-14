package com.rfcoding.vibeplayer.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

/** Holds one [ViewModelStore] per open dialog or sheet, so their ViewModels outlive recomposition. */
class ScopedStoreRegistryViewModel : ViewModel() {

    private val stores = mutableMapOf<String, ViewModelStore>()

    fun getOrCreate(id: String): ViewModelStore = stores.getOrPut(id) { ViewModelStore() }

    fun clear(id: String) {
        stores.remove(id)?.clear()
    }

    override fun onCleared() {
        super.onCleared()
        stores.values.forEach { it.clear() }
        stores.clear()
    }
}

/**
 * Gives a dialog or bottom sheet its own ViewModel scope: ViewModels created in [content] live while
 * [visible] is true and are cleared whenever it is false, including a scope left behind when the host
 * left composition while it was still visible.
 *
 * @param scopeId Pass the scopeId from the outside if you want the same instance of the Dialog or
 * Bottom sheet with the same ViewModel instance.
 */
@Composable
fun DialogSheetScopedViewModel(
    visible: Boolean,
    scopeId: String = rememberSaveable { UUID.randomUUID().toString() },
    content: @Composable () -> Unit,
) {
    val parentOwner = LocalViewModelStoreOwner.current
        ?: throw IllegalStateException("No parent owner found.")

    val registry = koinViewModel<ScopedStoreRegistryViewModel>(
        viewModelStoreOwner = parentOwner,
    )

    var owner by remember { mutableStateOf<ViewModelStoreOwner?>(null) }

    LaunchedEffect(visible, scopeId) {
        if (visible && owner == null) {
            owner = ScopedViewModelStoreOwner(registry.getOrCreate(scopeId), parentOwner)
        } else if (!visible) {
            // Not only when this composition opened the scope: navigating away stops lifecycle-aware
            // collection before the close arrives, so the host can leave composition with the scope
            // still open and come back with `owner` reset but the same saved scopeId.
            registry.clear(scopeId)
            owner = null
        }
    }

    owner?.let { dialogOwner ->
        CompositionLocalProvider(LocalViewModelStoreOwner provides dialogOwner) {
            content()
        }
    }
}

/**
 * Borrows the parent's (e.g. the NavBackStackEntry's) factory and creation extras, without which Koin
 * can't build a `SavedStateHandle` for a ViewModel in the scope. [VIEW_MODEL_STORE_OWNER_KEY] points
 * back at this owner so the handles live in the scoped store and are cleared with it: reopening the
 * sheet starts from empty instead of the last input.
 */
private class ScopedViewModelStoreOwner(
    override val viewModelStore: ViewModelStore,
    private val parent: ViewModelStoreOwner,
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = (parent as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory
            ?: throw IllegalStateException("The parent ViewModelStoreOwner has no default factory.")

    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras(
            (parent as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras ?: CreationExtras.Empty
        ).apply {
            this[VIEW_MODEL_STORE_OWNER_KEY] = this@ScopedViewModelStoreOwner
        }
}
