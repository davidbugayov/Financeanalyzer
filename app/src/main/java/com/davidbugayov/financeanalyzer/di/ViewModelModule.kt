package com.davidbugayov.financeanalyzer.di

import android.app.Application
import com.davidbugayov.financeanalyzer.feature.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.feature.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsUiViewModel
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.detail.viewmodel.FinancialDetailStatisticsViewModel
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import com.davidbugayov.financeanalyzer.feature.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.import_transaction.ImportTransactionsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Categories ViewModel is shared across app
    viewModel { com.davidbugayov.financeanalyzer.presentation.categories.AppCategoriesViewModel(androidApplication()) }
    single<com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel> { get<com.davidbugayov.financeanalyzer.presentation.categories.AppCategoriesViewModel>() }

    viewModel { AchievementsUiViewModel() }

    viewModel {
        AddTransactionViewModel(
            addTransactionUseCase = get(),
            categoriesViewModel = get(),
            sourcePreferences = get(),
            walletRepository = get(),
            updateWidgetsUseCase = get(),
            updateWalletBalancesUseCase = get(),
            navigationManager = get(),
            application = androidApplication(),
        )
    }

    viewModel {
        ProfileViewModel(
            exportTransactionsToCSVUseCase = get(),
            getProfileAnalyticsUseCase = get(),
            preferencesManager = get(),
            notificationScheduler = get(),
            navigationManager = get(),
            userEventTracker = get(),
            errorTracker = get()
        )
    }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }

    viewModel {
        EditTransactionViewModel(
            getTransactionByIdUseCase = get(),
            updateTransactionUseCase = get(),
            categoriesViewModel = get(),
            sourcePreferences = get(),
            walletRepository = get(),
            updateWidgetsUseCase = get(),
            updateWalletBalancesUseCase = get(),
            navigationManager = get(),
            application = androidApplication(),
        )
    }

    viewModel { TransactionHistoryViewModel(get(), get(), get(), get(), get(), get(), get(), androidApplication(), get(), get()) }

    viewModel { BudgetViewModel(get(), get(), get()) }

    viewModel { WalletTransactionsViewModel(get(), get(), get()) }

    viewModel { ImportTransactionsViewModel(get(), androidApplication()) }

    viewModel { OnboardingViewModel(get()) }

    viewModel { AchievementsViewModel() }

    viewModel { parameters ->
        FinancialDetailStatisticsViewModel(
            startDate = parameters.get(),
            endDate = parameters.get(),
            transactionRepository = get(),
            calculateCategoryStatsUseCase = get(),
        )
    }
} 