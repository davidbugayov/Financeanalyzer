package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.data.preferences.WalletPreferences
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.WalletRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodWithCacheUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.viewmodel.FinancialStatisticsViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
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

    // Preferences
    single { CategoryPreferences.getInstance(androidContext()) }
    single { SourcePreferences.getInstance(androidContext()) }
    single { CategoryUsagePreferences.getInstance(androidContext()) }
    single { WalletPreferences.getInstance(androidContext()) }
    single { PreferencesManager(androidContext()) }

    // Utils
    single { AnalyticsUtils }
    single { FinancialMetrics.getInstance() }
    
    // Repositories
    single<TransactionRepositoryImpl> { TransactionRepositoryImpl(get()) }
    single<TransactionRepository> { get<TransactionRepositoryImpl>() }
    single<ITransactionRepository> { get<TransactionRepositoryImpl>() }
    
    // Создаем WalletRepositoryImpl с доступом к TransactionRepository
    single<WalletRepository> {
        WalletRepositoryImpl(
            walletPreferences = get(),
            transactionRepository = get()
        )
    }

    // Use cases
    single { LoadTransactionsUseCase(get()) }
    single { AddTransactionUseCase(get()) }
    single { DeleteTransactionUseCase(get()) }
    single { UpdateTransactionUseCase(get()) }
    single { FilterTransactionsUseCase() }
    single { GroupTransactionsUseCase() }
    single { CalculateCategoryStatsUseCase(get()) }
    single { ExportTransactionsToCSVUseCase(get()) }
    single { ImportTransactionsManager(get(), androidContext(), get(), get()) }
    single { ValidateTransactionUseCase() }
    single { GetTransactionByIdUseCase(get()) }
    single { CalculateBalanceMetricsUseCase() }
    single { GetTransactionsForPeriodWithCacheUseCase(get()) }
    single { GetTransactionsForPeriodUseCase(get()) }
    single { UpdateWidgetsUseCase() }

    // ViewModels
    viewModel { CategoriesViewModel(androidApplication()) }
    viewModel {
        AddTransactionViewModel(
            addTransactionUseCase = get(),
            categoriesViewModel = get(),
            sourcePreferences = get(),
            walletRepository = get()
        )
    }
    viewModel {
        ProfileViewModel(
            exportTransactionsToCSVUseCase = get(),
            loadTransactionsUseCase = get(),
            notificationScheduler = get(),
            preferencesManager = get()
        )
    }
    
    viewModel {
        HomeViewModel(
            addTransactionUseCase = get(),
            deleteTransactionUseCase = get(),
            repository = get(),
            getTransactionsForPeriodWithCacheUseCase = get(),
            calculateBalanceMetricsUseCase = get()
        )
    }

    viewModel {
        com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel(
            updateTransactionUseCase = get(),
            getTransactionByIdUseCase = get(),
            categoriesViewModel = get(),
            sourcePreferences = get(),
            walletRepository = get()
        )
    }

    // Утилиты
    single { NotificationScheduler() }
}

val statisticsModule = module {
    single { androidContext().resources }
    viewModel { (startDate: Long, endDate: Long) ->
        FinancialStatisticsViewModel(get(), get(), startDate, endDate, get())
    }
}