package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.analytics.FirebaseAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.IAnalytics
import com.davidbugayov.financeanalyzer.analytics.NoOpAnalytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.koin.dsl.module

/**
 * Модуль Koin для предоставления зависимостей аналитики для Google flavor
 */
val analyticsModule = module {

    // Предоставляем Firebase Analytics
    single { Firebase.analytics }

    // Предоставляем экземпляр CompositeAnalytics как IAnalytics
    single<IAnalytics> {
        val composite = CompositeAnalytics()

        // Добавляем Firebase Analytics
        composite.addAnalytics(FirebaseAnalyticsAdapter(get<FirebaseAnalytics>()))

        // Добавляем AppMetrica
        composite.addAnalytics(AppMetricaAnalyticsAdapter())

        // Инициализируем AnalyticsUtils с этим экземпляром
        AnalyticsUtils.init(composite)

        composite
    }

    // Предоставляем NoOpAnalytics для использования, когда аналитика отключена
    factory { NoOpAnalytics() }
} 
