package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для внедрения зависимостей, связанных с историей транзакций.
 * Предоставляет все необходимые зависимости для компонентов истории.
 */
val historyModule = module {
    // Use cases для истории транзакций
    factory { FilterTransactionsUseCase() }
    factory { GroupTransactionsUseCase() }
    factory { CalculateCategoryStatsUseCase(get()) }

    // ViewModel для экрана истории транзакций
    viewModel {
        TransactionHistoryViewModel(
            loadTransactionsUseCase = get(),
            filterTransactionsUseCase = get(),
            groupTransactionsUseCase = get(),
            calculateCategoryStatsUseCase = get(),
            deleteTransactionUseCase = get(),
            eventBus = get(),
            analyticsUtils = get(),
            categoriesViewModel = get()
        )
    }
} 