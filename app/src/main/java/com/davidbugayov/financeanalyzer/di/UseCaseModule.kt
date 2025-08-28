package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.debt.CreateDebtUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.GetDebtsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.RepayDebtUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.GetSubcategoryByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateExpenseDisciplineIndexUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateFinancialHealthScoreUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculatePeerComparisonUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.CalculateRetirementForecastUseCase
import com.davidbugayov.financeanalyzer.shared.usecase.PredictFutureExpensesUseCase
import com.davidbugayov.financeanalyzer.widget.AndroidWidgetRefresher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val useCaseModule =
    module {
        // Widgets
        single<com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher> {
            AndroidWidgetRefresher(androidContext())
        }
        single {
            com.davidbugayov.financeanalyzer.domain.usecase.widgets
                .UpdateWidgetsUseCase(get())
        }

        // Import / Export
        single<ImportTransactionsUseCase> {
            ImportTransactionsUseCaseImpl(get<ImportTransactionsManager>())
        }
        single { ExportTransactionsToCSVUseCase(get()) }

        // Transactions
        single { LoadTransactionsUseCase(get()) }

        // Subcategories
        single { GetSubcategoryByIdUseCase(get()) }

        // Debts
        single { CreateDebtUseCase(get()) }
        single { GetDebtsUseCase(get()) }
        single { RepayDebtUseCase(get()) }

        // Wallet
        single {
            com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase(
                get(),
                get(),
            )
        }

        // Analytics
        single { CalculateBalanceMetricsUseCase() }
        single { CalculateCategoryStatsUseCase() }
        single { CalculateFinancialHealthScoreUseCase() }
        single { CalculateExpenseDisciplineIndexUseCase() }
        single { CalculateRetirementForecastUseCase() }
        single { CalculatePeerComparisonUseCase() }
        single { PredictFutureExpensesUseCase() }
        single {
            CalculateEnhancedFinancialMetricsUseCase(
                calculateFinancialHealthScore = get(),
                calculateExpenseDisciplineIndex = get(),
                calculateRetirementForecast = get(),
                calculatePeerComparison = get(),
            )
        }
    }
