package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для внедрения зависимостей, связанных с профилем.
 * Следует принципам Dependency Injection.
 */
val profileModule = module {
    // UseCases
    factory { ExportTransactionsToCSVUseCase(get()) }
    
    // ViewModel
    viewModel { 
        ProfileViewModel(
            exportTransactionsToCSVUseCase = get(),
            loadTransactionsUseCase = get(),
            notificationScheduler = get(),
            preferencesManager = get()
        ) 
    }
} 