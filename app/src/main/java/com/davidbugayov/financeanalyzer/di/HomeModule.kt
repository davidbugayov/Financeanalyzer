package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel { HomeViewModel(get(), get()) }
    single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }
    single { AddTransactionUseCase(get()) }
} 