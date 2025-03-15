package com.davidbugayov.financeanalyzer

import android.app.Application
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

        // Сначала инициализируем Firebase
        initializeFirebase()

        // Затем инициализируем логирование, когда Firebase уже доступен
        TimberInitializer.init(BuildConfig.DEBUG)

        // Инициализируем аналитику и Crashlytics
        initializeAnalytics()
        initializeCrashlytics()

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
        Timber.i("Application initialized")

        // Логируем информацию о запуске приложения
        CrashlyticsUtils.setCustomKey("app_start_time", System.currentTimeMillis())
        CrashlyticsUtils.setCustomKey("device_memory", getAvailableMemory())
        CrashlyticsUtils.setCustomKey("device_language", resources.configuration.locales[0].language)
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            isFirebaseInitialized = true
            Timber.d("Firebase initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Firebase")
            isFirebaseInitialized = false
        }
    }

    private fun initializeAnalytics() {
        if (!isFirebaseInitialized) {
            Timber.w("Skipping Analytics initialization as Firebase is not initialized")
            return
        }

        try {
            firebaseAnalytics = Firebase.analytics
            analytics = firebaseAnalytics

            // Устанавливаем пользовательские свойства для сегментации
            firebaseAnalytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            firebaseAnalytics.setUserProperty("build_type", if (BuildConfig.DEBUG) "debug" else "release")
            firebaseAnalytics.setUserProperty("device_model", android.os.Build.MODEL)
            firebaseAnalytics.setUserProperty("android_version", android.os.Build.VERSION.RELEASE)

            // TODO: Вернуть отключение аналитики для debug-сборок перед релизом
            // Включаем аналитику для всех сборок (временно)
            firebaseAnalytics.setAnalyticsCollectionEnabled(true)

            Timber.d("Analytics initialized successfully, collection enabled: true")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Analytics")
        }
    }

    private fun initializeCrashlytics() {
        if (!isFirebaseInitialized) {
            Timber.w("Skipping Crashlytics initialization as Firebase is not initialized")
            return
        }

        try {
            crashlytics = FirebaseCrashlytics.getInstance()

            // Включаем Crashlytics для всех сборок
            crashlytics.setCrashlyticsCollectionEnabled(true)

            // Добавляем ключевую информацию для отладки
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
            crashlytics.setCustomKey("device_model", android.os.Build.MODEL)
            crashlytics.setCustomKey("android_version", android.os.Build.VERSION.RELEASE)
            crashlytics.setCustomKey("device_manufacturer", android.os.Build.MANUFACTURER)
            crashlytics.setCustomKey("device_brand", android.os.Build.BRAND)

            Timber.d("Crashlytics initialized successfully")
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