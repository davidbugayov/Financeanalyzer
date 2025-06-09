package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.navigation.AppNavigation
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import org.koin.dsl.module

/**
 * Модуль для предоставления компонентов навигации.
 */
val navigationModule = module {
    // Один экземпляр NavigationManager на все приложение
    single { NavigationManager() }
    
    // Один экземпляр AppNavigation на все приложение
    single { AppNavigation(get()) }
} 