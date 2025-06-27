package com.davidbugayov.financeanalyzer.utils.di

import com.davidbugayov.financeanalyzer.utils.logging.Logger
import com.davidbugayov.financeanalyzer.utils.logging.TimberLogger
import org.koin.dsl.module

val loggingModule = module {
    single<Logger> { TimberLogger() }
} 