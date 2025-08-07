package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.analytics.di.analyticsUtilsModule
import com.davidbugayov.financeanalyzer.feature.security.di.securityModule
import com.davidbugayov.financeanalyzer.feature.statistics.di.statisticsModule
import com.davidbugayov.financeanalyzer.feature.transaction.di.transactionModule

/**
 * Contains a single list of all Koin modules that should be loaded in ].
 */
val allModules =
    listOf(
        appModule,
        databaseModule,
        preferencesModule,
        repositoryModule,
        utilsModule,
        useCaseModule,
        viewModelModule,
        transactionModule,
        analyticsModule,
        analyticsUtilsModule,
        securityModule,
        statisticsModule,
    )
