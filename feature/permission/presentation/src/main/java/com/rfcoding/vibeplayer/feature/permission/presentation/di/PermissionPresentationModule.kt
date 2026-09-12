package com.rfcoding.vibeplayer.feature.permission.presentation.di

import com.rfcoding.vibeplayer.feature.permission.presentation.PermissionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val permissionPresentationModule = module {
    viewModelOf(::PermissionViewModel)
}
