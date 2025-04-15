package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль для внедрения зависимостей, связанных с добавлением и редактированием транзакций
 */
val transactionModule = module {
    // ViewModels
    viewModel {
        AddTransactionViewModel(
            application = androidApplication(),
            addTransactionUseCase = get(),
            updateTransactionUseCase = get(),
            categoriesViewModel = get(),
            walletRepository = get(),
            txRepository = get()
        )
    }
    
    viewModel {
        EditTransactionViewModel(
            application = androidApplication(),
            addTransactionUseCase = get(),
            updateTransactionUseCase = get(),
            categoriesViewModel = get(),
            walletRepository = get(),
            txRepository = get()
        )
    }
} 