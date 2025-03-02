package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Модуль Koin для внедрения зависимостей.
 * Следует принципу инверсии зависимостей (Dependency Inversion Principle).
 */
val appModule = module {
    // Repositories
    single<ITransactionRepository> { TransactionRepositoryImpl(androidContext()) }

    // Use cases
    single { LoadTransactionsUseCase(get()) }
    single { AddTransactionUseCase(get()) }
    single { DeleteTransactionUseCase(get()) }
    single { UpdateTransactionUseCase(get()) }

    // ViewModels
    viewModel { HomeViewModel(get(), get<AddTransactionUseCase>()) }
    viewModel { ChartViewModel(get()) }
    viewModel { AddTransactionViewModel(androidApplication(), get()) }
}