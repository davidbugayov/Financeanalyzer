package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chartModule = module {
    single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }
    viewModel { ChartViewModel() }
} 