package com.davidbugayov.financeanalyzer

import android.app.Application
import com.davidbugayov.financeanalyzer.di.appModule
import com.davidbugayov.financeanalyzer.di.chartModule
import com.davidbugayov.financeanalyzer.di.homeModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        startKoin {
            androidLogger()
            androidContext(this@FinanceApp)
            modules(
                appModule,
                chartModule,
                homeModule
            )
        }
    }
} 