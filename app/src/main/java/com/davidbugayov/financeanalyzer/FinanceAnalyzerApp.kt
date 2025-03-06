package com.davidbugayov.financeanalyzer

import android.app.Application
import com.davidbugayov.financeanalyzer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FinanceAnalyzerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@FinanceAnalyzerApp)
            modules(appModule)
        }
    }
} 