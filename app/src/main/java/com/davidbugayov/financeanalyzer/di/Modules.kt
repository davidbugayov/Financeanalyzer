package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.feature.transaction.di.transactionModule
import com.davidbugayov.financeanalyzer.analytics.di.analyticsUtilsModule

/**
 * Contains a single list of all Koin modules that should be loaded in [BaseFinanceApp].
 */
val allModules = listOf(
    databaseModule,
    preferencesModule,
    repositoryModule,
    utilsModule,
    useCaseModule,
    viewModelModule,
    transactionModule,
    analyticsModule,
    analyticsUtilsModule,
) 