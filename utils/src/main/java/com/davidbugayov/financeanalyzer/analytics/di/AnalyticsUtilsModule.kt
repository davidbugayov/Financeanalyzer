package com.davidbugayov.financeanalyzer.analytics.di

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.NoOpAnalytics
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import org.koin.dsl.module

/**
 * Модуль внедрения зависимостей для компонентов аналитики
 */
val analyticsUtilsModule =
    module {
        // Базовая реализация аналитики (будет переопределена в конкретных flavor)
        single<IAnalytics> { NoOpAnalytics() }

        // Инициализация AnalyticsUtils
        single {
            val analytics = get<IAnalytics>()
            AnalyticsUtils.init(analytics)
            AnalyticsUtils
        }

        // Компоненты аналитики
        single { PerformanceMetrics }
        single { UserEventTracker }
    }
