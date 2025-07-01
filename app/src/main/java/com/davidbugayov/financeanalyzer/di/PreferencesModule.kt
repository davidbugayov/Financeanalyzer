package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourceUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.WalletPreferences
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Module that provides shared preferences singletons.
 */
val preferencesModule = module {
    single { CategoryPreferences.getInstance(androidContext()) }
    single { CategoryUsagePreferences.getInstance(androidContext()) }
    single { SourcePreferences.getInstance(androidContext()) }
    single { SourceUsagePreferences.getInstance(androidContext()) }
    single { WalletPreferences.getInstance(androidContext()) }

    single { PreferencesManager(androidContext()) }
    single { OnboardingManager(androidContext()) }
} 