package com.davidbugayov.financeanalyzer.di

import android.app.Application
import android.content.Context
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.analytics.FirebaseAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import timber.log.Timber

/**
 * Koin module for Huawei flavor analytics.
 */
val analyticsModule =
    module {
        single<IAnalytics> {
            val app = androidContext() as Application
            val composite = CompositeAnalytics()

            // Проверка наличия GMS
            fun isGmsAvailable(context: Context): Boolean =
                try {
                    val gms = Class.forName("com.google.android.gms.common.GoogleApiAvailability")
                    val method = gms.getMethod("isGooglePlayServicesAvailable", Context::class.java)
                    val result = method.invoke(gms.getDeclaredConstructor().newInstance(), context) as Int
                    result == 0
                } catch (e: Exception) {
                    false
                }

            // Firebase Analytics (только если есть GMS)
            if (isGmsAvailable(app)) {
                try {
                    FirebaseApp.initializeApp(app)
                    val firebaseAnalytics = FirebaseAnalytics.getInstance(app)
                    composite.addAnalytics(FirebaseAnalyticsAdapter(firebaseAnalytics))
                    Timber.d("Firebase Analytics initialized (GMS present)")
                } catch (e: Exception) {
                    Timber.e(e, "Error initializing Firebase Analytics")
                }

                // Firebase Performance
                try {
                    FirebasePerformance.getInstance()
                    Timber.d("Firebase Performance initialized successfully")
                } catch (e: Exception) {
                    Timber.e(e, "Error initializing Firebase Performance")
                }
            } else {
                Timber.w("GMS not available: Firebase Analytics will NOT be initialized on this device")
            }

            // AppMetrica Analytics
            try {
                val config =
                    AppMetricaConfig
                        .newConfigBuilder(BuildConfig.APPMETRICA_API_KEY)
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

        // Добавляем трекеры для аналитики
        single { UserEventTracker }
    }
