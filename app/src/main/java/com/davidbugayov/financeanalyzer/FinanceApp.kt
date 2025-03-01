package com.davidbugayov.financeanalyzer

import android.app.Application
import com.davidbugayov.financeanalyzer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@FinanceApp)
            modules(appModule) // Укажите ваши модули
        }
    }
}