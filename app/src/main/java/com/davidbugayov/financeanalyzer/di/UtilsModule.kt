package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.factory.ImportFactory
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.util.AndroidResourceProvider
import com.davidbugayov.financeanalyzer.utils.CrashReporter
import com.davidbugayov.financeanalyzer.utils.INotificationScheduler
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Module with various utility singletons.
 */
val utilsModule =
    module {
        single { NotificationScheduler(androidContext(), get()) }
        single<INotificationScheduler> { get<NotificationScheduler>() }

        single { NavigationManager() }

        // ResourceProvider for safe access to string resources via DI
        single<ResourceProvider> { AndroidResourceProvider(androidContext()) }

        // Crash reporter (initialized immediately)
        single {
            CrashReporter.apply {
                init(androidApplication())
            }
        }

        // Import / Export helpers
        single { ImportTransactionsManager(androidContext()) }
        single { ImportFactory(androidContext(), get()) }
    }
