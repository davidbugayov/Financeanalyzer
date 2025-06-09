package com.davidbugayov.financeanalyzer

import android.app.Application
import android.os.Build
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import timber.log.Timber

/**
 * Основной класс приложения для F-Droid flavor
 */
class FinanceApp : BaseFinanceApp() {

    // AppMetrica API ключ
    private val APP_METRICA_API_KEY = "d4ec51de-47c3-4997-812f-97b9a6663dad"

    // Составной адаптер для объединения всех систем аналитики
    private val compositeAnalytics = CompositeAnalytics()

    /**
     * Инициализирует компоненты, специфичные для F-Droid flavor
     */
    override fun initFlavor() {
        // Инициализация аналитики
        initAnalytics()

        // Логируем событие открытия приложения
        AnalyticsUtils.logAppOpen()
    }

    /**
     * Инициализирует аналитику
     */
    private fun initAnalytics() {
        // Инициализация AppMetrica для всех версий
        initAppMetrica()

        // Устанавливаем составной адаптер аналитики в AnalyticsUtils
        AnalyticsUtils.init(compositeAnalytics)

        Timber.d("Аналитика инициализирована для F-Droid версии (только AppMetrica)")
    }

    /**
     * Инициализирует Яндекс.AppMetrica
     */
    private fun initAppMetrica() {
        try {
            // Настройка AppMetrica
            val config = AppMetricaConfig.newConfigBuilder(APP_METRICA_API_KEY)
                .withLogs()
                .withSessionTimeout(60)
                .withCrashReporting(true)
                .build()

            // Активация SDK
            AppMetrica.activate(this, config)

            // Включаем отправку статистики
            AppMetrica.enableActivityAutoTracking(this)

            // Добавляем адаптер в составную аналитику
            compositeAnalytics.addAnalytics(AppMetricaAnalyticsAdapter())

            Timber.d("AppMetrica успешно инициализирована")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка инициализации AppMetrica")
        }
    }

    /**
     * Логирует основную информацию об устройстве
     */
    private fun logDeviceInfo() {
        Timber.d("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        Timber.d("Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        Timber.d("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    }
}
