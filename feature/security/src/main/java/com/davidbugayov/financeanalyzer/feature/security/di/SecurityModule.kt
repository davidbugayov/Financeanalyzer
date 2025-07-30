package com.davidbugayov.financeanalyzer.feature.security.di

import com.davidbugayov.financeanalyzer.feature.security.manager.SecurityManager
import com.davidbugayov.financeanalyzer.feature.security.viewmodel.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль DI для компонентов безопасности
 */
val securityModule =
    module {
        // SecurityManager
        single { SecurityManager(androidContext(), get()) }

        // ViewModels
        viewModel { AuthViewModel(get()) }
    }
