package com.rfcoding.vibeplayer.core.presentation

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        @param:StringRes val id: Int,
        val args: Array<Any> = emptyArray(),
    ) : UiText

    class PluralsResource(
        @param:PluralsRes val id: Int,
        val quantity: Int,
        val args: Array<Any> = arrayOf(quantity),
    ) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(id, *args)
        is PluralsResource -> pluralStringResource(id, quantity, *args)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(id, *args)
        is PluralsResource -> context.resources.getQuantityString(id, quantity, *args)
    }
}
