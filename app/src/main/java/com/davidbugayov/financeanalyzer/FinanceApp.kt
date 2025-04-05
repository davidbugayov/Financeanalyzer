package com.davidbugayov.financeanalyzer

import android.app.Application
import android.util.Log
import com.davidbugayov.financeanalyzer.di.addTransactionModule
import com.davidbugayov.financeanalyzer.di.appModule
import com.davidbugayov.financeanalyzer.di.chartModule
import com.davidbugayov.financeanalyzer.di.historyModule
import com.davidbugayov.financeanalyzer.di.homeModule
import com.davidbugayov.financeanalyzer.di.importModule
import com.davidbugayov.financeanalyzer.di.onboardingModule
import com.davidbugayov.financeanalyzer.di.profileModule
import com.davidbugayov.financeanalyzer.utils.CrashlyticsUtils
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TimberInitializer
import com.davidbugayov.financeanalyzer.utils.logging.FileLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FinanceApp : Application() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var crashlytics: FirebaseCrashlytics
    private val appScope = CoroutineScope(Dispatchers.IO)

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

        try {
            // Сначала инициализируем обычное логирование Timber
            TimberInitializer.init(BuildConfig.DEBUG)
            Timber.d("Timber инициализирован")
            
            // Затем инициализируем сохранение логов в файл
            FileLogger.init(this)
            Timber.d("FileLogger инициализирован, путь к файлу: ${FileLogger.getLogFilePath()}")

            // Инициализируем Firebase
            initializeFirebase()

            // Инициализируем аналитику и Crashlytics
            initializeAnalytics()
            initializeCrashlytics()

            startKoin {
                androidLogger()
                androidContext(this@FinanceApp)
                modules(
                    appModule,
                    chartModule,
                    homeModule,
                    addTransactionModule,
                    historyModule,
                    profileModule,
                    importModule,
                    onboardingModule
                )
            }
            
            // Инициализируем финансовые метрики
            initializeFinancialMetrics()

            // Логируем через Timber для проверки отправки в Crashlytics
            Timber.i("Application initialized")

            // Логируем информацию о запуске приложения
            CrashlyticsUtils.setCustomKey("app_start_time", System.currentTimeMillis())
            CrashlyticsUtils.setCustomKey("device_memory", getAvailableMemory())
            CrashlyticsUtils.setCustomKey("device_language", resources.configuration.locales[0].language)
        } catch (e: Exception) {
            // Логирование запуска может перехватить ошибку инициализации
            Log.e("FinanceApp", "Ошибка инициализации приложения: ${e.message}", e)
        }
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

    /**
     * Инициализирует финансовые метрики.
     * Использует отложенную инициализацию для предотвращения ANR.
     */
    private fun initializeFinancialMetrics() {
        try {
            Timber.d("Начало отложенной инициализации финансовых метрик")
            val metrics = FinancialMetrics.getInstance()
            
            // Используем отложенную инициализацию вместо блокирующего вызова
            metrics.lazyInitialize(priority = false)
            
            // Планируем повторную проверку с задержкой и высоким приоритетом
            appScope.launch {
                delay(1500) // Даем время для завершения инициализации приложения
                metrics.lazyInitialize(priority = true)
                Timber.d("Запланирована вторичная проверка метрик с высоким приоритетом")
            }
            
            Timber.d("Запущена отложенная инициализация финансовых метрик")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации финансовых метрик")
        }
    }
} 