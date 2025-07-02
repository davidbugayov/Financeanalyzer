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
import com.davidbugayov.financeanalyzer.presentation.categories.AppCategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.module.dsl.bind
import org.koin.androidx.viewmodel.dsl.viewModel

val viewModelModule = module {
    // Categories ViewModel is shared across app
    viewModelOf(::AppCategoriesViewModel)
    singleOf(::AppCategoriesViewModel) { bind<CategoriesViewModel>() }

    viewModelOf(::AchievementsUiViewModel)

    viewModelOf(::AddTransactionViewModel)

    viewModelOf(::ProfileViewModel)

    viewModelOf(::HomeViewModel)

    viewModelOf(::EditTransactionViewModel)

    viewModelOf(::TransactionHistoryViewModel)

    viewModelOf(::BudgetViewModel)

    viewModelOf(::WalletTransactionsViewModel)

    viewModelOf(::ImportTransactionsViewModel)

    viewModelOf(::OnboardingViewModel)

    viewModelOf(::AchievementsViewModel)

    viewModel { parameters ->
        FinancialDetailStatisticsViewModel(
            startDate = parameters.get(),
            endDate = parameters.get(),
            transactionRepository = get(),
            calculateCategoryStatsUseCase = get(),
        )
    }
} 