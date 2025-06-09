package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourceUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.WalletPreferences
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.WalletRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.factory.ImportFactory
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodWithCacheUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.validation.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsUiViewModel
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsViewModel
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
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.analyticsModule
import com.davidbugayov.financeanalyzer.utils.INotificationScheduler
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
    single { CategoryUsagePreferences.getInstance(androidContext()) }
    single { SourcePreferences.getInstance(androidContext()) }
    single { SourceUsagePreferences.getInstance(androidContext()) }
    single { WalletPreferences.getInstance(androidContext()) }
    single { PreferencesManager(androidContext()) }

    // Utils & Managers
    // Получаем аналитику из отдельного модуля analyticsModule, определенного по флейворам
    single { get<IAnalytics>() }
    single { OnboardingManager(androidContext()) }

    // Factories and managers
    single { ImportTransactionsManager(get()) }
    single { ImportFactory(androidContext(), get()) }

    // Use cases
    single { LoadTransactionsUseCase(get()) }
    single { AddTransactionUseCase(get()) }
    single { DeleteTransactionUseCase(get()) }
    single { UpdateTransactionUseCase(get()) }
    single { FilterTransactionsUseCase() }
    single { GroupTransactionsUseCase() }
    single { CalculateCategoryStatsUseCase(get()) }
    single { ExportTransactionsToCSVUseCase(get(), get()) }
    single { ValidateTransactionUseCase() }
    single { GetTransactionByIdUseCase(get()) }
    single { CalculateBalanceMetricsUseCase() }
    single { GetTransactionsForPeriodWithCacheUseCase(get()) }
    single { GetTransactionsForPeriodUseCase(get()) }
    single { UpdateWidgetsUseCase() }
    single { GetCategoriesWithAmountUseCase(get()) }
    single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }
    single<INotificationScheduler> { NotificationScheduler(androidApplication(), get()) }
    factory { UpdateWalletBalancesUseCase(get()) }
    factory {
        val repo = get<TransactionRepositoryImpl>()
        val metrics = get<CalculateBalanceMetricsUseCase>()
        GetProfileAnalyticsUseCase(androidContext(), repo, metrics)
    }
    single<ImportTransactionsUseCase> { ImportTransactionsUseCaseImpl(get()) }

    // ViewModels
    viewModel { CategoriesViewModel(androidApplication()) }
    viewModel { AchievementsUiViewModel() }
    viewModel { (achievementsUiViewModel: AchievementsUiViewModel) ->
        AddTransactionViewModel(
            addTransactionUseCase = get(),
            categoriesViewModel = get(),
            sourcePreferences = get(),
            walletRepository = get(),
            updateWidgetsUseCase = get(),
            application = androidApplication(),
            updateWalletBalancesUseCase = get(),
            achievementsRepository = get(),
            achievementsUiViewModel = achievementsUiViewModel,
        )
    }
    viewModel { ProfileViewModel(get(), get(), get(), get(), androidContext()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel {
        EditTransactionViewModel(
            getTransactionByIdUseCase = get(),
            updateTransactionUseCase = get(),
            categoriesViewModel = get(),
            sourcePreferences = get(),
            walletRepository = get(),
            updateWidgetsUseCase = get(),
            application = androidApplication(),
            updateWalletBalancesUseCase = get(),
        )
    }
    viewModel {
        TransactionHistoryViewModel(get(), get(), get(), get(), get(), get(), get(), get(), androidApplication())
    }
    viewModel { BudgetViewModel(get(), get()) }
    viewModel { WalletTransactionsViewModel(get(), get()) }
    viewModel { ImportTransactionsViewModel(get(), androidApplication()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { AchievementsViewModel(get()) }
}

// Для параметризованных ViewModel (пример: статистика за период)
val statisticsModule = module {
    single { androidContext().resources }
    viewModel { (startDate: Long, endDate: Long) ->
        FinancialStatisticsViewModel(get(), get(), startDate, endDate, get())
    }
}

// Все модули приложения
val allModules = listOf(appModule, statisticsModule, analyticsModule, repositoryModule)
