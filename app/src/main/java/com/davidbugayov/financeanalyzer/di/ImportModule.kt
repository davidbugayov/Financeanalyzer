package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.presentation.ui.ImportTransactionsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для функционала импорта транзакций.
 * Следует принципу инверсии зависимостей (DIP) из SOLID.
 */
val importModule = module {
    // ViewModel для экрана импорта
    viewModel {
        ImportTransactionsViewModel(
            repository = get(),
            context = androidContext()
        )
    }
} 