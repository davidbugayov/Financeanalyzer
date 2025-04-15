package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль Koin для внедрения зависимостей, связанных с онбордингом.
 * Предоставляет необходимые зависимости для компонентов онбординга.
 */
val onboardingModule = module {
    // Manager
    single { OnboardingManager(androidContext()) }
    
    // ViewModel
    viewModel { 
        OnboardingViewModel(
            onboardingManager = get()
        ) 
    }
} 