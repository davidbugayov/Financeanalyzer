package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.FinancialGoalRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.FinancialGoalRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetFinancialGoalsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ManageFinancialGoalUseCase
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для внедрения зависимостей, связанных с профилем.
 * Следует принципам Dependency Injection.
 */
val profileModule = module {
    // Repositories
    single<FinancialGoalRepository> { FinancialGoalRepositoryImpl(get()) }
    
    // UseCases
    factory { ExportTransactionsToCSVUseCase(get()) }
    factory { GetFinancialGoalsUseCase(get()) }
    factory { ManageFinancialGoalUseCase(get()) }
    
    // ViewModel
    viewModel { 
        ProfileViewModel(
            exportTransactionsToCSVUseCase = get(),
            getFinancialGoalsUseCase = get(),
            manageFinancialGoalUseCase = get(),
            loadTransactionsUseCase = get(),
            notificationScheduler = get(),
            preferencesManager = get(),
            transactionRepository = get<TransactionRepository>()
        ) 
    }
} 