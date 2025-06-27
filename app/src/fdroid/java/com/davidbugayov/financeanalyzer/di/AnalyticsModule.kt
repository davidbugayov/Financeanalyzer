package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import org.koin.dsl.module
import org.koin.dsl.single

/**
 * DI-модуль аналитики для F-Droid flavor
 */
val analyticsModule = module {
    single<IAnalytics> {
        val composite = CompositeAnalytics()
        // AppMetrica only
        composite.addAnalytics(AppMetricaAnalyticsAdapter())
        AnalyticsUtils.init(composite)
        composite
    }
} 