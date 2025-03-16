package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для внедрения зависимостей, связанных с домашним экраном.
 * Предоставляет все необходимые зависимости для компонентов домашнего экрана.
 */
val homeModule = module {
    // Use cases для домашнего экрана
    factory<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }

    // ViewModel для домашнего экрана
    viewModel {
        HomeViewModel(
            getTransactionsUseCase = get(),
            addTransactionUseCase = get(),
            deleteTransactionUseCase = get(),
            eventBus = get()
        )
    }
} 