package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для добавления транзакций.
 * Следует принципу инверсии зависимостей (DIP) из SOLID.
 */
val addTransactionModule = module {
    // UseCases
    factory { AddTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }

    // ViewModels
    viewModel { AddTransactionViewModel(get(), get(), get(), get()) }
} 