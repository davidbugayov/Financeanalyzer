package com.davidbugayov.financeanalyzer.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.koin.dsl.module
import timber.log.Timber

/**
 * Модуль Koin для предоставления зависимостей аналитики для Google flavor
 */
val analyticsModule = module {

    // Предоставляем Firebase Analytics
    single { Firebase.analytics }

    // Предоставляем композитную аналитику как реализацию IAnalytics
    single<IAnalytics> {
        val composite = CompositeAnalytics()

        // Добавляем Firebase Analytics
        try {
            val firebaseAnalytics = get<FirebaseAnalytics>()
            composite.addAnalytics(FirebaseAnalyticsAdapter(firebaseAnalytics))
            Timber.d("Firebase Analytics добавлена в композитную аналитику")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка инициализации Firebase Analytics")
        }

        // Добавляем AppMetrica
        composite.addAnalytics(AppMetricaAnalyticsAdapter())

        // Инициализируем глобальный AnalyticsUtils
        AnalyticsUtils.init(composite)

        Timber.d("Google AnalyticsModule initialized")

        composite
    }
} 
