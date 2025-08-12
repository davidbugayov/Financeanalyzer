package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCaseImpl
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.manager.ImportTransactionsManager
import com.davidbugayov.financeanalyzer.widget.AndroidWidgetRefresher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val useCaseModule =
    module {
        // Widgets
        single<com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher> {
            AndroidWidgetRefresher(androidContext())
        }
        single { com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase(get()) }

        // Import / Export
        single<ImportTransactionsUseCase> {
            ImportTransactionsUseCaseImpl(get<ImportTransactionsManager>())
        }
    }
