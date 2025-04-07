package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    viewModel { BudgetViewModel(get()) }
} 