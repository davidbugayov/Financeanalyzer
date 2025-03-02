package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.SharedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val appModule = module {

    // Регистрируем TransactionRepository, передавая Context
    single { TransactionRepository(androidContext()) }

    // Регистрируем UseCases, передавая Repository
    single { LoadTransactionsUseCase(get()) }
    single { AddTransactionUseCase(get()) }

    // Регистрируем ViewModel, передавая UseCases
    viewModel { SharedViewModel(get(), get()) }
}