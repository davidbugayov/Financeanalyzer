package com.davidbugayov.financeanalyzer.analytics

import org.koin.dsl.module
import timber.log.Timber

/**
 * Модуль Koin для предоставления зависимостей аналитики для F-Droid flavor
 */
val analyticsModule = module {
    
    // Предоставляем композитную аналитику как реализацию IAnalytics
    single<IAnalytics> {
        val composite = CompositeAnalytics()
        
        // Добавляем AppMetrica
        composite.addAnalytics(AppMetricaAnalyticsAdapter())
        
        // Инициализируем глобальный AnalyticsUtils
        AnalyticsUtils.init(composite)
        
        Timber.d("F-Droid AnalyticsModule initialized")
        
        composite
    }
} 