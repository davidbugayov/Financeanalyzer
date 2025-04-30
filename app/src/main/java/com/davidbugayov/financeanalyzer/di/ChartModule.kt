package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.CategoryColorProvider
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.CategoryColorProviderImpl
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.viewmodel.PieChartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Модуль Koin для графиков и связанных компонентов
 */
val chartModule: Module = module {
    // ViewModels
    viewModel { PieChartViewModel(get(), get()) }
    
    // UseCases
    factory { GetCategoriesWithAmountUseCase(get()) }
    single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }
    
    // Providers
    single<CategoryColorProvider> { CategoryColorProviderImpl() }
} 