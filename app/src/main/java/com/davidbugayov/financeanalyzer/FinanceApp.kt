package com.davidbugayov.financeanalyzer

import android.app.Application
import android.os.Build
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.di.allModules
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

/**
 * Основной класс приложения.
 * Инициализирует все необходимые компоненты, включая DI (Koin), логирование (Timber), и аналитику.
 */
class FinanceApp : Application() {

    // AppMetrica API ключ
    private val appMetricaApiKey = "d4ec51de-47c3-4997-812f-97b9a6663dad"

    override fun onCreate() {
        super.onCreate()

        // Настройка Timber для логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Инициализация Koin
        initKoin()
        
        // Инициализация аналитики
        initAppMetrica()
        
        // Логируем основные данные устройства для диагностики
        logDeviceInfo()
    }

    /**
     * Инициализирует Koin для внедрения зависимостей
     */
    private fun initKoin() {
        startKoin {
            // Логирование для отладки (только в DEBUG-режиме)
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            // Передаем контекст приложения
            androidContext(this@FinanceApp)
            // Загружаем все модули
            modules(allModules)
        }
        Timber.d("Koin успешно инициализирован")
    }

    /**
     * Инициализирует Яндекс.AppMetrica
     */
    private fun initAppMetrica() {
        try {
            // Настройка AppMetrica
            val config = AppMetricaConfig.newConfigBuilder(appMetricaApiKey)
                .withLogs()
                .withSessionTimeout(60)
                .withCrashReporting(true)
                .build()

            // Активация SDK
            AppMetrica.activate(this, config)

            // Включаем отправку статистики
            AppMetrica.enableActivityAutoTracking(this)

            // Добавляем AppMetrica в аналитику 
            // (остальные компоненты будут инициализированы через DI)
            val adapter = AppMetricaAnalyticsAdapter()
            val composite = CompositeAnalytics()
            composite.addAnalytics(adapter)
            AnalyticsUtils.init(composite)

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