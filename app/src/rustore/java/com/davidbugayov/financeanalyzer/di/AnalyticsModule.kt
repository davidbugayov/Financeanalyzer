package com.davidbugayov.financeanalyzer.di

import android.app.Application
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.analytics.FirebaseAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.AppMetrica
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.dsl.single
import timber.log.Timber

/**
 * Koin module for RuStore flavor analytics.
 */
val analyticsModule = module {
    single<IAnalytics> {
        val app = androidContext() as Application
        val composite = CompositeAnalytics()

        // Firebase Analytics
        try {
            FirebaseApp.initializeApp(app)
            val firebaseAnalytics = FirebaseAnalytics.getInstance(app)
            composite.addAnalytics(FirebaseAnalyticsAdapter(firebaseAnalytics))
        } catch (e: Exception) {
            Timber.e(e, "Error initializing Firebase Analytics")
        }

        // AppMetrica Analytics
        try {
            val config = AppMetricaConfig.newConfigBuilder(BuildConfig.APPMETRICA_API_KEY)
                .withLogs()
                .withSessionTimeout(60)
                .withCrashReporting(true)
                .build()
            AppMetrica.activate(app, config)
            AppMetrica.enableActivityAutoTracking(app)
            composite.addAnalytics(AppMetricaAnalyticsAdapter())
        } catch (e: Exception) {
            Timber.e(e, "Error initializing AppMetrica Analytics")
        }

        // Initialize AnalyticsUtils
        AnalyticsUtils.init(composite)

        composite
    }
} 