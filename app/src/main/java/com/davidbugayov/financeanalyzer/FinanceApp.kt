package com.davidbugayov.financeanalyzer

import android.app.Application
import android.os.Bundle
import com.davidbugayov.financeanalyzer.di.appModule
import com.davidbugayov.financeanalyzer.di.chartModule
import com.davidbugayov.financeanalyzer.di.homeModule
import com.davidbugayov.financeanalyzer.utils.CrashlyticsUtils
import com.davidbugayov.financeanalyzer.utils.TimberInitializer
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class FinanceApp : Application() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var crashlytics: FirebaseCrashlytics

    // Делаем аналитику доступной глобально
    companion object {

        lateinit var analytics: FirebaseAnalytics
            private set

        // Флаг, указывающий, инициализирован ли Firebase
        var isFirebaseInitialized = false
            private set
    }
    
    override fun onCreate() {
        super.onCreate()

        // Инициализируем логирование перед всем остальным
        TimberInitializer.init(BuildConfig.DEBUG)
        
        // Логируем начало инициализации
        Timber.i("Application initialization started")

        // Инициализируем Firebase
        initializeFirebase()

        // Инициализируем аналитику и Crashlytics
        initializeAnalytics()
        initializeCrashlytics()

        // Инициализируем Koin
        startKoin {
            androidLogger()
            androidContext(this@FinanceApp)
            modules(
                appModule,
                chartModule,
                homeModule
            )
        }

        // Логируем через Timber для проверки отправки в Crashlytics
        Timber.i("Application initialized successfully")

        // Логируем информацию о запуске приложения
        CrashlyticsUtils.setCustomKey("app_start_time", System.currentTimeMillis())
        CrashlyticsUtils.setCustomKey("device_memory", getAvailableMemory())
        CrashlyticsUtils.setCustomKey("device_language", resources.configuration.locales[0].language)
        
        // Принудительно вызываем сбор и отправку данных
        if (isFirebaseInitialized) {
            try {
                crashlytics.sendUnsentReports()
            } catch (e: Exception) {
                Timber.e(e, "Failed to send unsent reports")
            }
        }
    }

    private fun initializeFirebase() {
        try {
            // Проверяем, что Firebase еще не инициализирован
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Timber.d("Firebase initialized successfully")
            } else {
                Timber.d("Firebase already initialized")
            }
            isFirebaseInitialized = true
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Firebase")
            isFirebaseInitialized = false
        }
    }

    private fun initializeAnalytics() {
        try {
            // Получаем экземпляр Analytics независимо от флага isFirebaseInitialized
            firebaseAnalytics = Firebase.analytics
            analytics = firebaseAnalytics

            // Устанавливаем пользовательские свойства для сегментации
            firebaseAnalytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            firebaseAnalytics.setUserProperty("build_type", if (BuildConfig.DEBUG) "debug" else "release")
            firebaseAnalytics.setUserProperty("device_model", android.os.Build.MODEL)
            firebaseAnalytics.setUserProperty("android_version", android.os.Build.VERSION.RELEASE)

            // Включаем аналитику для всех сборок
            firebaseAnalytics.setAnalyticsCollectionEnabled(true)

            // Отправляем тестовое событие
            val params = Bundle().apply {
                putString("test_param", "test_value")
            }
            firebaseAnalytics.logEvent("app_initialized", params)

            Timber.d("Analytics initialized successfully, collection enabled: true")
            isFirebaseInitialized = true
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Analytics")
        }
    }

    private fun initializeCrashlytics() {
        try {
            // Получаем экземпляр Crashlytics независимо от флага isFirebaseInitialized
            crashlytics = FirebaseCrashlytics.getInstance()

            // Всегда включаем сбор данных о крешах
            crashlytics.setCrashlyticsCollectionEnabled(true)

            // Добавляем ключевую информацию для отладки
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
            crashlytics.setCustomKey("device_model", android.os.Build.MODEL)
            crashlytics.setCustomKey("android_version", android.os.Build.VERSION.RELEASE)
            crashlytics.setCustomKey("device_manufacturer", android.os.Build.MANUFACTURER)
            crashlytics.setCustomKey("device_brand", android.os.Build.BRAND)
            
            // Отправляем тестовый креш при запуске в отладочной сборке
            if (BuildConfig.DEBUG) {
                crashlytics.log("Sending test crash from FinanceApp.initializeCrashlytics")
                crashlytics.recordException(Exception("Test exception from FinanceApp.initializeCrashlytics"))
            }

            Timber.d("Crashlytics initialized successfully")
            isFirebaseInitialized = true
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Crashlytics")
        }
    }

    /**
     * Получает доступную память устройства в МБ
     */
    private fun getAvailableMemory(): Long {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024) // В МБ
        return maxMemory
    }
} 