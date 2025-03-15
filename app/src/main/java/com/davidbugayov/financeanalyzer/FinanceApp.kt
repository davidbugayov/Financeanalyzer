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
    }

    override fun onCreate() {
        super.onCreate()

        // Инициализация логирования
        TimberInitializer.init()

        try {
            // Initialize Firebase
            if (!FirebaseApp.getApps(this).isEmpty()) {
                Timber.d("Firebase already initialized")
            } else {
                Timber.d("Initializing Firebase")
                FirebaseApp.initializeApp(this)
            }

            // Инициализация Analytics
            initializeAnalytics()

            // Инициализация Crashlytics
            initializeCrashlytics()

        } catch (e: Exception) {
            // В случае ошибки инициализации Firebase, логируем ее и продолжаем работу
            Timber.e(e, "Failed to initialize Firebase")

            // Создаем заглушку для аналитики в debug-сборке
            if (BuildConfig.DEBUG) {
                createDummyAnalytics()
            }
        }

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
    }

    private fun initializeAnalytics() {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке создаем заглушку для аналитики
                createDummyAnalytics()
            } else {
                // В release-сборке используем реальную аналитику
                firebaseAnalytics = Firebase.analytics
                analytics = firebaseAnalytics

                // Устанавливаем пользовательские свойства для сегментации
                firebaseAnalytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
                firebaseAnalytics.setUserProperty("build_type", "release")

                // Включаем аналитику только для релизных сборок
                firebaseAnalytics.setAnalyticsCollectionEnabled(true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Analytics")
            createDummyAnalytics()
        }
    }

    private fun createDummyAnalytics() {
        // Создаем заглушку для аналитики в debug-сборке
        Timber.d("Creating dummy analytics for debug build")

        // Получаем экземпляр FirebaseAnalytics, но отключаем сбор данных
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)
            analytics = firebaseAnalytics

            // Устанавливаем пользовательские свойства для отладки
            firebaseAnalytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            firebaseAnalytics.setUserProperty("build_type", "debug")
        } catch (e: Exception) {
            Timber.e(e, "Failed to create dummy analytics, using empty implementation")

            // Создаем пустую реализацию для логирования событий
            val dummyAnalytics = FirebaseAnalytics.getInstance(this)
            dummyAnalytics.setAnalyticsCollectionEnabled(false)
            analytics = dummyAnalytics

            // Логируем события в Timber вместо отправки в Firebase
            Timber.d("Created dummy analytics that will log events to Timber")
        }
    }

    private fun initializeCrashlytics() {
        try {
            crashlytics = FirebaseCrashlytics.getInstance()

            // Включаем Crashlytics только для релизных сборок
            if (BuildConfig.DEBUG) {
                crashlytics.setCrashlyticsCollectionEnabled(false)
                Timber.d("Crashlytics collection disabled in debug build")
            } else {
                crashlytics.setCrashlyticsCollectionEnabled(true)

                // Добавляем ключевую информацию для отладки только в релизной сборке
                CrashlyticsUtils.setCustomKey("app_version", BuildConfig.VERSION_NAME)
                CrashlyticsUtils.setCustomKey("build_type", "release")
                CrashlyticsUtils.setCustomKey("device_model", android.os.Build.MODEL)
                CrashlyticsUtils.setCustomKey("android_version", android.os.Build.VERSION.RELEASE)

                Timber.d("Crashlytics collection enabled in release build")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Crashlytics")
        }
    }
} 