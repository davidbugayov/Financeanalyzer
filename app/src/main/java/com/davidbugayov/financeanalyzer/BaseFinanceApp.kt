package com.davidbugayov.financeanalyzer

import android.app.Application
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
 
import com.davidbugayov.financeanalyzer.di.allModules
import com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.domain.usecase.AchievementEngine
 
import com.davidbugayov.financeanalyzer.feature.transaction.di.TransactionModuleInitializer
 
import com.davidbugayov.financeanalyzer.ui.components.AchievementEngineProvider
 
import com.davidbugayov.financeanalyzer.utils.CrashReporter
import com.davidbugayov.financeanalyzer.utils.MemoryUtils
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
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
    private val userEventTracker: UserEventTracker by inject()

    // Получаем движок достижений через Koin
    private val achievementEngine: AchievementEngine by inject()

    override fun onCreate() {
        // Start tracking app startup time
        PerformanceMetrics.startOperation(PerformanceMetrics.Operations.APP_STARTUP)

        super<Application>.onCreate()

        // Инициализация StringProvider удалена. Используется ResourceProvider через Koin и stringResource в UI.

        // Настройка Timber для логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Инициализация AppMetrica только для релизных билдов
        if (!BuildConfig.DEBUG) {
            try {
                val config =
                    AppMetricaConfig.newConfigBuilder(BuildConfig.APPMETRICA_API_KEY)
                        .withLogs()
                        .withSessionTimeout(60)
                        .withCrashReporting(true)
                        .build()
                AppMetrica.activate(this, config)
                AppMetrica.enableActivityAutoTracking(this)
                Timber.d("AppMetrica успешно инициализирована (release build)")
                CrashReporter.isAppMetricaInitialized = true
            } catch (e: Exception) {
                Timber.e(e, getString(R.string.appmetrica_init_error))
                CrashReporter.isAppMetricaInitialized = false
            }
        } else {
            CrashReporter.isAppMetricaInitialized = false
        }

        // Инициализация системы отчетов об ошибках
        CrashReporter.init(this)
        com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider.crashLogger = CrashReporter.instance

        try {
            // Инициализация Koin
            initKoin()
        } catch (e: Exception) {
            Timber.e(e, getString(R.string.koin_init_error))
            CrashReporter.trackError("KoinInit", getString(R.string.koin_init_error_detail, e.message ?: ""))
        }

        try {
            // Инициализация модулей
            initModules()
        } catch (e: Exception) {
            Timber.e(e, getString(R.string.modules_init_error))
            CrashReporter.trackError("ModuleInit", getString(R.string.modules_init_error_detail, e.message ?: ""))
        }

        try {
            // Инициализация системы достижений
            initAchievements()
        } catch (e: Exception) {
            Timber.e(e, getString(R.string.achievements_init_error))
            CrashReporter.trackError(
                "AchievementsInit",
                getString(R.string.achievements_init_error_detail, e.message ?: ""),
            )
        }

        try {
            // Логируем основные данные устройства для диагностики
            logDeviceInfo()
        } catch (e: Exception) {
            Timber.e(e, getString(R.string.device_info_log_error))
            CrashReporter.trackError("DeviceInfoLog", getString(R.string.device_info_log_error_detail, e.message ?: ""))
        }

        try {
            // Инициализируем специфичные для флейвора компоненты
            initFlavor()
        } catch (e: Exception) {
            Timber.e(e, getString(R.string.flavor_init_error))
            CrashReporter.trackError("FlavorInit", getString(R.string.flavor_init_error_detail, e.message ?: ""))
        }

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
        Timber.d(getString(R.string.modules_initialized_success))
    }

    /**
     * Инициализирует систему достижений
     */
    private fun initAchievements() {
        try {
            // Инициализируем триггер достижений с движком
            AchievementTrigger.initialize(achievementEngine)

            // Инициализируем провайдер для доступа из UI
            AchievementEngineProvider.initialize(achievementEngine)
        } catch (e: Exception) {
            Timber.e(e, getString(R.string.achievements_system_init_error))
        }
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

        // Триггеры достижений за активность
        checkActivityMilestones()
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
     * Проверяет вехи активности пользователя
     */
    private fun checkActivityMilestones() {
        try {
            val prefs = getSharedPreferences("user_activity", MODE_PRIVATE)
            val currentTime = System.currentTimeMillis()
            prefs.getLong("last_open_time", 0)
            val firstOpenTime = prefs.getLong("first_open_time", currentTime)

            // Сохраняем время первого открытия если это первый запуск
            if (firstOpenTime == currentTime) {
                prefs.edit {
                    putLong("first_open_time", currentTime)
                }
            }

            // Сохраняем текущее время открытия
            prefs.edit {
                putLong("last_open_time", currentTime)
            }

            // Проверяем недельную активность (7 дней)
            val weekInMillis = 7 * 24 * 60 * 60 * 1000L
            if (currentTime - firstOpenTime >= weekInMillis) {
                AchievementTrigger.onMilestoneReached("week_streak")
            }

            // Проверяем месячную активность (30 дней)
            val monthInMillis = 30 * 24 * 60 * 60 * 1000L
            if (currentTime - firstOpenTime >= monthInMillis) {
                AchievementTrigger.onMilestoneReached("month_active")
            }
        } catch (e: Exception) {
            Timber.e(e, getString(R.string.user_activity_check_error))
        }
    }

    /**
     * Метод для инициализации специфичных для флейвора компонентов.
     * Должен быть реализован в конкретных классах для каждого флейвора.
     */
    protected abstract fun initFlavor()
}
