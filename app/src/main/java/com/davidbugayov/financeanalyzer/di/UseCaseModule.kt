package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateExpenseDisciplineIndexUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateFinancialHealthScoreUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculatePeerComparisonUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateRetirementForecastUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetExpenseOptimizationRecommendationsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetSmartExpenseTipsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.AddSubcategoryUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.DeleteSubcategoryUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.GetSubcategoriesByCategoryIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetPagedTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodFlowUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GroupTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.validation.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.subcategories.InitializeDefaultSubcategoriesUseCase
import com.davidbugayov.financeanalyzer.widget.AndroidWidgetRefresher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val useCaseModule =
    module {
        // Transaction-related
        single { LoadTransactionsUseCase(get()) }
        single { AddTransactionUseCase(get()) }
        single { DeleteTransactionUseCase(get()) }
        single { UpdateTransactionUseCase(get()) }
        single { FilterTransactionsUseCase() }
        single { GroupTransactionsUseCase() }
        single { ValidateTransactionUseCase() }
        single { GetTransactionByIdUseCase(get()) }
        single { GetTransactionsForPeriodUseCase(get()) }
        single { GetTransactionsForPeriodFlowUseCase(get()) }
        single { GetPagedTransactionsUseCase(get()) }
        single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }

        // Wallet / balance
        factory { UpdateWalletBalancesUseCase(get()) }

        // Распределение дохода
        factory { com.davidbugayov.financeanalyzer.domain.usecase.wallet.AllocateIncomeUseCase(get()) }

        // Widgets
        single<com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher> {
            AndroidWidgetRefresher(androidContext())
        }
        single { UpdateWidgetsUseCase(get()) }

        // Analytics
        single { CalculateCategoryStatsUseCase() }
        single { CalculateBalanceMetricsUseCase() }
        single { GetCategoriesWithAmountUseCase() }
        factory {
            GetProfileAnalyticsUseCase(
                transactionRepository = get(),
                walletRepository = get(),
            )
        }
        single { GetSmartExpenseTipsUseCase() }
        single { GetExpenseOptimizationRecommendationsUseCase() }

        // Financial Health Analytics
        single { CalculateFinancialHealthScoreUseCase() }
        single { CalculateExpenseDisciplineIndexUseCase() }
        single { CalculateRetirementForecastUseCase() }
        single { CalculatePeerComparisonUseCase() }
        single {
            CalculateEnhancedFinancialMetricsUseCase(
                calculateFinancialHealthScoreUseCase = get(),
                calculateExpenseDisciplineIndexUseCase = get(),
                calculateRetirementForecastUseCase = get(),
                calculatePeerComparisonUseCase = get(),
                walletRepository = get(),
            )
        }

        // Import / Export
        single { ExportTransactionsToCSVUseCase(get()) }
        single<ImportTransactionsUseCase> {
            ImportTransactionsUseCaseImpl(get<ImportTransactionsManager>())
        }

        factory { com.davidbugayov.financeanalyzer.domain.usecase.wallet.GoalProgressUseCase() }

        // Subcategory-related
        single { GetSubcategoriesByCategoryIdUseCase(get()) }
        single { AddSubcategoryUseCase(get()) }
        single { DeleteSubcategoryUseCase(get()) }
        single { InitializeDefaultSubcategoriesUseCase(get()) }
    }
