package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.BudgetRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    // Репозиторий для бюджетных категорий
    single { get<com.davidbugayov.financeanalyzer.data.local.database.AppDatabase>().budgetCategoryDao() }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }

    viewModel { BudgetViewModel(get()) }
} 