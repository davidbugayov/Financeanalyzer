package com.davidbugayov.financeanalyzer

import android.app.Application
import android.os.Build
import com.davidbugayov.financeanalyzer.di.allModules
import com.davidbugayov.financeanalyzer.feature.di.TransactionModuleInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

/**
 * Базовый абстрактный класс приложения.
 * Инициализирует все необходимые компоненты, включая DI (Koin) и логирование (Timber).
 * Конкретные реализации для разных флейворов должны наследоваться от этого класса.
 */
abstract class BaseFinanceApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Настройка Timber для логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Инициализация Koin
        initKoin()

        // Инициализация модулей
        initModules()

        // Логируем основные данные устройства для диагностики
        logDeviceInfo()

        // Инициализируем специфичные для флейвора компоненты
        initFlavor()
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
        Timber.d("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        Timber.d("Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        Timber.d("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    }

    /**
     * Метод для инициализации специфичных для флейвора компонентов.
     * Должен быть реализован в конкретных классах для каждого флейвора.
     */
    protected abstract fun initFlavor()
}
