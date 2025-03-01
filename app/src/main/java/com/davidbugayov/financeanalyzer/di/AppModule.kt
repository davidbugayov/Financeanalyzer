package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.SharedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Регистрируем TransactionRepository, передавая Context
    single { TransactionRepository(androidContext()) }

    // Регистрируем UseCases, передавая Repository
    single { LoadTransactionsUseCase(get()) }
    single { AddTransactionUseCase(get()) }

    // Регистрируем ViewModel, передавая UseCases
    viewModel { SharedViewModel(get(), get()) }
}