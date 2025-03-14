package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CurrencyPreferences
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для внедрения зависимостей.
 * Следует принципу инверсии зависимостей (Dependency Inversion Principle).
 */
val appModule = module {
    // Database
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().transactionDao() }

    // Preferences
    single { CurrencyPreferences.getInstance(androidContext()) }
    single { CategoryPreferences.getInstance(androidContext()) }
    
    // Repositories
    single<ITransactionRepository> { TransactionRepositoryImpl(get()) }

    // Use cases
    single { LoadTransactionsUseCase(get()) }
    single { FilterTransactionsUseCase() }
    single { GroupTransactionsUseCase() }
    single { CalculateCategoryStatsUseCase(get()) }
    single { AddTransactionUseCase(get()) }
    single { DeleteTransactionUseCase(get()) }
    single { UpdateTransactionUseCase(get()) }

    // ViewModels
    viewModel { CategoriesViewModel(androidApplication()) }
    viewModel {
        TransactionHistoryViewModel(
            loadTransactionsUseCase = get(),
            filterTransactionsUseCase = get(),
            groupTransactionsUseCase = get(),
            calculateCategoryStatsUseCase = get(),
            deleteTransactionUseCase = get(),
            categoriesViewModel = get()
        )
    }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ChartViewModel() }
    viewModel {
        AddTransactionViewModel(
            application = androidApplication(),
            addTransactionUseCase = get(),
            categoriesViewModel = get()
        )
    }
}