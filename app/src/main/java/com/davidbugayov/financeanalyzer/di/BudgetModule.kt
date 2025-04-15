package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.WalletRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    // Repositories
    single<WalletRepository> { WalletRepositoryImpl(get()) }

    viewModel { BudgetViewModel(get(), get()) }

    viewModel { WalletTransactionsViewModel(get(), get()) }
} 