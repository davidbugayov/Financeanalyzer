package com.davidbugayov.financeanalyzer

import android.app.Application
import com.davidbugayov.financeanalyzer.di.appModule
import com.davidbugayov.financeanalyzer.utils.TimberInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Инициализируем Timber
        TimberInitializer.init(BuildConfig.DEBUG)
        
        // Инициализируем Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FinanceApp)
            modules(appModule)
        }
    }
} 