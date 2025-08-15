package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Module that provides database and DAO instances.
 */
val databaseModule =
    module {
        single { AppDatabase.getInstance(androidContext()) }
        single { get<AppDatabase>().transactionDao() }
        single { get<AppDatabase>().subcategoryDao() }
        single { get<AppDatabase>().debtDao() }
    }
