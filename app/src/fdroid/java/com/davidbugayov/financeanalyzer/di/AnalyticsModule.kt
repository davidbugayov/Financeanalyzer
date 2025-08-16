package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import org.koin.dsl.module

/**
 * DI-модуль аналитики для F-Droid flavor
 */
val analyticsModule =
    module {
        single<IAnalytics> {
            val composite = CompositeAnalytics()
            // AppMetrica only
            composite.addAnalytics(AppMetricaAnalyticsAdapter())
            composite
        }

        // Добавляем трекеры для аналитики
        single { UserEventTracker }
    }
