package com.davidbugayov.financeanalyzer

import android.app.Application
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import com.davidbugayov.financeanalyzer.di.allModules
import com.davidbugayov.financeanalyzer.feature.transaction.di.TransactionModuleInitializer
import com.davidbugayov.financeanalyzer.utils.CrashReporter
import com.davidbugayov.financeanalyzer.utils.MemoryUtils
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

/**
 * Базовый абстрактный класс приложения.
 * Инициализирует все необходимые компоненты, включая DI (Koin) и логирование (Timber).
 * Конкретные реализации для разных флейворов должны наследоваться от этого класса.
 */
abstract class BaseFinanceApp : Application(), DefaultLifecycleObserver, KoinComponent {

    // Получаем компоненты аналитики через Koin
    private val analyticsUtils: AnalyticsUtils by inject()
    private val performanceMetrics: PerformanceMetrics by inject()
    private val userEventTracker: UserEventTracker by inject()

    override fun onCreate() {
        // Start tracking app startup time
        PerformanceMetrics.startOperation(PerformanceMetrics.Operations.APP_STARTUP)

        super<Application>.onCreate()

        // Настройка Timber для логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Инициализация системы отчетов об ошибках
        CrashReporter.init(this)

        // Инициализация Koin
        initKoin()

        // Инициализация модулей
        initModules()

        // Логируем основные данные устройства для диагностики
        logDeviceInfo()

        // Инициализируем специфичные для флейвора компоненты
        initFlavor()

        // Регистрируем наблюдатель за жизненным циклом приложения
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Отправляем событие открытия приложения
        AnalyticsUtils.logAppOpen()

        // Отслеживаем использование памяти
        MemoryUtils.trackMemoryUsage(this)

        // End tracking app startup time
        PerformanceMetrics.endOperation(PerformanceMetrics.Operations.APP_STARTUP)
    }

    /**
     * Инициализирует Koin для внедрения зависимостей
     */
    private fun initKoin() {
        startKoin {
            // Логирование для отладки (только в DEBUG-режиме)
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            // Передаем контекст приложения
            androidContext(this@BaseFinanceApp)
            // Загружаем все модули
            modules(allModules)
        }
        Timber.d("Koin успешно инициализирован")
    }

    /**
     * Инициализирует дополнительные модули
     */
    private fun initModules() {
        // Инициализация модуля транзакций
        TransactionModuleInitializer.initialize()
        Timber.d("Модули успешно инициализированы")
    }

    /**
     * Логирует основную информацию об устройстве
     */
    private fun logDeviceInfo() {
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"
        val androidVersion = "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
        val appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        Timber.d("Device: $deviceInfo")
        Timber.d("Android version: $androidVersion")
        Timber.d("App version: $appVersion")

        // Отправляем данные устройства в аналитику
        AnalyticsUtils.setUserProperty("device_model", deviceInfo)
        AnalyticsUtils.setUserProperty("android_version", androidVersion)
        AnalyticsUtils.setUserProperty("app_version", appVersion)
        AnalyticsUtils.setUserProperty("app_flavor", BuildConfig.FLAVOR)
        AnalyticsUtils.setUserProperty("app_build_type", BuildConfig.BUILD_TYPE)
    }

    /**
     * Вызывается, когда приложение переходит на передний план
     */
    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        AnalyticsUtils.logAppForeground()
    }

    /**
     * Вызывается, когда приложение уходит на задний план
     */
    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        AnalyticsUtils.logAppBackground()

        // Отправляем статистику сессии
        userEventTracker.sendSessionStats()

        // Отслеживаем использование памяти
        MemoryUtils.trackMemoryUsage(this)
    }

    /**
     * Метод для инициализации специфичных для флейвора компонентов.
     * Должен быть реализован в конкретных классах для каждого флейвора.
     */
    protected abstract fun initFlavor()
}
