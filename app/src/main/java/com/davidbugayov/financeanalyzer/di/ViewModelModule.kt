package com.davidbugayov.financeanalyzer.di

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
import com.davidbugayov.financeanalyzer.presentation.budget.subwallets.SubWalletsViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.setup.WalletSetupViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.viewModel

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

    viewModel { BudgetViewModel(get(), get(), get(), get(), get()) }

    viewModelOf(::WalletTransactionsViewModel)

    viewModelOf(::WalletSetupViewModel)

    viewModel { parameters -> SubWalletsViewModel(parameters.get(), get()) }

    viewModelOf(::ImportTransactionsViewModel)

    viewModelOf(::OnboardingViewModel)

    viewModelOf(::AchievementsViewModel)

    viewModel { parameters -> FinancialDetailStatisticsViewModel(parameters.get(), parameters.get(), get(), get()) }
}
