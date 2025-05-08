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
import com.davidbugayov.financeanalyzer.domain.usecase.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodWithCacheUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.viewmodel.FinancialStatisticsViewModel
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.import_transaction.ImportTransactionsViewModel
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Единый DI-модуль приложения. Все зависимости, ViewModel, use-case, менеджеры и утилиты.
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

    // Utils & Managers
    single { AnalyticsUtils }
    single { FinancialMetrics.getInstance() }
    single { NotificationScheduler() }
    single { OnboardingManager(androidContext()) }

    // Repositories
    single<TransactionRepositoryImpl> { TransactionRepositoryImpl(get()) }
    single<TransactionRepository> { get<TransactionRepositoryImpl>() }
    single<ITransactionRepository> { get<TransactionRepositoryImpl>() }
    single<WalletRepository> { WalletRepositoryImpl(get(), get()) }

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
    single { GetCategoriesWithAmountUseCase(get()) }
    single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }

    // ViewModels
    viewModel { CategoriesViewModel(androidApplication()) }
    viewModel { AddTransactionViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { EditTransactionViewModel(get(), get(), get(), get(), get()) }
    viewModel { TransactionHistoryViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { BudgetViewModel(get(), get()) }
    viewModel { WalletTransactionsViewModel(get(), get()) }
    viewModel { ImportTransactionsViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }

    // New single for UserPreferences and NotificationPreferences
    single { com.davidbugayov.financeanalyzer.domain.UserPreferences() }
    single { com.davidbugayov.financeanalyzer.domain.NotificationPreferences() }
}

// Для параметризованных ViewModel (пример: статистика за период)
val statisticsModule = module {
    single { androidContext().resources }
    viewModel { (startDate: Long, endDate: Long) ->
        FinancialStatisticsViewModel(get(), get(), startDate, endDate, get())
    }
}