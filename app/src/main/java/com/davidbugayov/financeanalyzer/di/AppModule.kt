package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CurrencyPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.data.repository.FinancialGoalRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryAdapter
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.FinancialGoalRepository
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetFinancialGoalsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ManageFinancialGoalUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.EventBus
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Основной модуль Koin для внедрения зависимостей.
 * Следует принципу инверсии зависимостей (Dependency Inversion Principle).
 */
val appModule = module {
    // Database
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().financialGoalDao() }

    // Preferences
    single { CurrencyPreferences.getInstance(androidContext()) }
    single { CategoryPreferences.getInstance(androidContext()) }
    single { CategoryUsagePreferences.getInstance(androidContext()) }
    single { PreferencesManager(androidContext()) }

    // Utils
    single { EventBus }
    single { AnalyticsUtils }
    
    // Repositories
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<ITransactionRepository> { TransactionRepositoryAdapter(get()) }
    single<FinancialGoalRepository> { FinancialGoalRepositoryImpl(get()) }

    // Use cases
    single { LoadTransactionsUseCase(get()) }
    single { AddTransactionUseCase(get()) }
    single { DeleteTransactionUseCase(get()) }
    single { UpdateTransactionUseCase(get()) }
    factory { ExportTransactionsToCSVUseCase(get()) }
    factory { GetFinancialGoalsUseCase(get()) }
    factory { ManageFinancialGoalUseCase(get()) }

    // ViewModels
    viewModel { CategoriesViewModel(androidApplication()) }
    viewModel { ChartViewModel() }
    viewModel {
        AddTransactionViewModel(
            application = androidApplication(),
            addTransactionUseCase = get(),
            categoriesViewModel = get(),
            preferencesManager = get()
        )
    }
    viewModel {
        ProfileViewModel(
            exportTransactionsToCSVUseCase = get(),
            getFinancialGoalsUseCase = get(),
            manageFinancialGoalUseCase = get(),
            loadTransactionsUseCase = get(),
            notificationScheduler = get(),
            preferencesManager = get(),
            transactionRepository = get<TransactionRepository>()
        )
    }

    // Утилиты
    single { NotificationScheduler() }
}