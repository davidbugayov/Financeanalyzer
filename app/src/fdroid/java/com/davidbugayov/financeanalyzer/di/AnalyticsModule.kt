package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.NoOpAnalytics
import org.koin.dsl.module

/**
 * Модуль Koin для предоставления зависимостей аналитики для F-Droid flavor
 */
val analyticsModule = module {

    // Предоставляем экземпляр CompositeAnalytics как IAnalytics
    single<IAnalytics> {
        val composite = CompositeAnalytics()

        // Добавляем AppMetrica
        composite.addAnalytics(AppMetricaAnalyticsAdapter())

        // Инициализируем AnalyticsUtils с этим экземпляром
        AnalyticsUtils.init(composite)

        composite
    }

    // Предоставляем NoOpAnalytics для использования, когда аналитика отключена
    factory { NoOpAnalytics() }
} 
