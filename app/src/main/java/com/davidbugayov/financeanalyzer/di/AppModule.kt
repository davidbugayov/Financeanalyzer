package com.davidbugayov.financeanalyzer.di

import android.content.Context
import androidx.room.Room
import com.davidbugayov.financeanalyzer.data.local.FinanceDatabase
import com.davidbugayov.financeanalyzer.data.repository.BudgetRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.SavingGoalRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import com.davidbugayov.financeanalyzer.domain.repository.SavingGoalRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetTransactionAnalyticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repositories
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<SavingGoalRepository> { SavingGoalRepositoryImpl(get()) }

    // Use Cases
    single { AddTransactionUseCase(get()) }
    single { GetTransactionsUseCase(get()) }
    single { GetTransactionAnalyticsUseCase(get()) }

    // ViewModels
    viewModel { SharedViewModel(get(), get(), get()) }
    viewModel { AddTransactionViewModel(get()) }
    viewModel { AnalyticsViewModel(get()) }
    viewModel { BudgetViewModel(get()) }
    viewModel { GoalsViewModel(get()) }

    // Database
    single { 
        Room.databaseBuilder(
            get<Context>(),
            FinanceDatabase::class.java,
            "finance_database"
        ).build()
    }
    single { get<FinanceDatabase>().transactionDao() }
    single { get<FinanceDatabase>().budgetDao() }
    single { get<FinanceDatabase>().savingGoalDao() }
}