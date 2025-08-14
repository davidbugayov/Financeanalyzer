package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateExpenseDisciplineIndexUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateFinancialHealthScoreUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculatePeerComparisonUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateRetirementForecastUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.GetSubcategoryByIdUseCase
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

        // Subcategories
        single { GetSubcategoryByIdUseCase(get()) }

        // Analytics
        single { CalculateBalanceMetricsUseCase() }
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
    }
