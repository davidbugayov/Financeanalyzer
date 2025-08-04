package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourceUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.WalletPreferences
import com.davidbugayov.financeanalyzer.data.repository.WalletRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AchievementEngine
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.factory.ImportFactory
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.validation.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.utils.CrashReporter
import com.davidbugayov.financeanalyzer.utils.INotificationScheduler
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import com.davidbugayov.financeanalyzer.utils.PermissionManager
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.GlobalScope
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Единый DI-модуль приложения. Все зависимости, ViewModel, use-case, менеджеры и утилиты.
 */
val appModule =
    module {
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
        single { OnboardingManager(androidContext()) }
        single { PermissionManager(androidContext()) }

        // Repositories
        single<WalletRepository> { WalletRepositoryImpl(get()) }

        // Utils
        single { NotificationScheduler(androidContext(), get()) }
        single<INotificationScheduler> { get<NotificationScheduler>() }
        single { NavigationManager() }

        // CrashReporter
        single {
            CrashReporter.apply {
                init(androidApplication())
            }
        }

        // Import/Export
        single { ImportTransactionsManager(androidContext()) }
        single { ImportFactory(androidContext(), get()) }

        // Use cases
        single { LoadTransactionsUseCase(get()) }
        single { AddTransactionUseCase(get()) }
        single { DeleteTransactionUseCase(get()) }
        single { UpdateTransactionUseCase(get()) }
        single { FilterTransactionsUseCase() }
        single { GroupTransactionsUseCase() }
        single { CalculateCategoryStatsUseCase() }
        single { ExportTransactionsToCSVUseCase(get()) }
        single { ValidateTransactionUseCase() }
        single { GetTransactionByIdUseCase(get()) }
        single { CalculateBalanceMetricsUseCase() }
        single { GetTransactionsForPeriodUseCase(get()) }
        single<com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher> {
            com.davidbugayov.financeanalyzer.widget.AndroidWidgetRefresher(androidContext())
        }
        single { com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase(get()) }
        single { GetCategoriesWithAmountUseCase() }
        single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }
        factory { UpdateWalletBalancesUseCase(get()) }
        factory {
            GetProfileAnalyticsUseCase(
                transactionRepository = get(),
                walletRepository = get(),
            )
        }
        single<ImportTransactionsUseCase> {
            com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCaseImpl(
                get<ImportTransactionsManager>(),
            )
        }

        // Achievement system
        @Suppress("DelicateCoroutinesApi")
        single<AchievementEngine> {
            AchievementEngine(
                achievementsRepository = get(),
                scope = GlobalScope,
            )
        }
    }
