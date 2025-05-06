package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodWithCacheUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCaseImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Модуль Koin для графиков и связанных компонентов
 */
val chartModule: Module = module {
    
    // UseCases
    factory { GetCategoriesWithAmountUseCase(get()) }
    single<GetTransactionsUseCase> { GetTransactionsUseCaseImpl(get()) }
    factory { GetTransactionsForPeriodWithCacheUseCase(get()) }
    
}