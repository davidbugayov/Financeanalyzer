package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.factory.ImportFactory
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.*
import com.davidbugayov.financeanalyzer.domain.usecase.validation.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.widget.AndroidWidgetRefresher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val useCaseModule = module {
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
    single { com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodFlowUseCase(get()) }
    single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }

    // Wallet / balance
    factory { UpdateWalletBalancesUseCase(get()) }

    // Widgets
    single<com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher> { AndroidWidgetRefresher(androidContext()) }
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

    // Import / Export
    single { ExportTransactionsToCSVUseCase(get(), androidContext()) }
    single<ImportTransactionsUseCase> {
        ImportTransactionsUseCaseImpl(get<ImportTransactionsManager>())
    }
} 