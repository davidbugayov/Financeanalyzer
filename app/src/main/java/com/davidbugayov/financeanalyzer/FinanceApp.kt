package com.davidbugayov.financeanalyzer

import android.app.Application
import com.davidbugayov.financeanalyzer.di.appModule
import com.davidbugayov.financeanalyzer.di.budgetModule
import com.davidbugayov.financeanalyzer.di.chartModule
import com.davidbugayov.financeanalyzer.di.historyModule
import com.davidbugayov.financeanalyzer.di.importModule
import com.davidbugayov.financeanalyzer.di.onboardingModule
import com.davidbugayov.financeanalyzer.di.statisticsModule
import com.davidbugayov.financeanalyzer.utils.CrashlyticsUtils
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TimberInitializer
import com.davidbugayov.financeanalyzer.utils.logging.FileLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

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
            // Сначала инициализируем параметры для POI, Log4j и других библиотек
            initializeExternalLibraries()
            
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
                    historyModule,
                    budgetModule,
                    onboardingModule,
                    importModule,
                    chartModule,
                    statisticsModule
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
            Timber.e(e, "Ошибка инициализации приложения: ${e.message}")
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
     * Инициализирует финансовые метрики в отдельном потоке
     */
    private fun initializeFinancialMetrics() {
        appScope.launch {
            try {
                Timber.i("Initializing financial metrics...")
                val metrics = FinancialMetrics.getInstance()
                // Начинаем первоначальный расчет метрик
                metrics.recalculateStats()
            } catch (e: Exception) {
                Timber.e(e, "Error initializing financial metrics")
                CrashlyticsUtils.recordException(e)
            }
        }
    }

    /**
     * Инициализирует настройки для внешних библиотек
     * Позволяет избежать проблем с логированием при сборке Release
     */
    private fun initializeExternalLibraries() {
        try {
            // Отключаем логирование для Apache POI
            System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.NullLogger")
            
            // Блокируем доступ к javax.xml.transform в Log4j, который вызывает проблемы в Android
            System.setProperty("javax.xml.transform.TransformerFactory", 
                               "org.apache.xalan.processor.TransformerFactoryImpl")
            
            // Копируем конфигурацию Log4j в доступное место
            copyLog4jConfigFile()
            
            // Отключаем Log4j полностью
            System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "OFF")
            System.setProperty("org.apache.logging.log4j.level", "OFF")
            System.setProperty("log4j2.disable.jmx", "true")
            System.setProperty("log4j.configurationFile", getLog4jConfigPath())
            System.setProperty("org.apache.logging.log4j.LogManager.StatusLogger.level", "OFF")
            
            // Отключаем другие логгеры
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog")
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF")
            
            // Не пытаемся явно загружать проблемные классы
            Timber.d("Внешние библиотеки инициализированы, логирование отключено")
        } catch (e: Exception) {
            // Игнорируем любые ошибки, чтобы не блокировать запуск приложения
            Timber.e(e, "Ошибка при инициализации внешних библиотек")
        }
    }

    /**
     * Копирует файл конфигурации log4j2-off.xml из assets в директорию приложения
     */
    private fun copyLog4jConfigFile() {
        try {
            val configFile = getLog4jConfigPath()
            val file = java.io.File(configFile)
            
            // Если файл уже существует, пропускаем копирование
            if (file.exists()) {
                return
            }
            
            // Создаем директорию, если она не существует
            file.parentFile?.mkdirs()
            
            // Копируем файл из assets
            assets.open("log4j2-off.xml").use { input ->
                java.io.FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            Timber.d("Файл конфигурации log4j2-off.xml скопирован в ${file.absolutePath}")
        } catch (e: Exception) {
            Timber.w(e, "Не удалось скопировать файл конфигурации log4j2-off.xml")
        }
    }
    
    /**
     * Возвращает путь к файлу конфигурации Log4j
     */
    private fun getLog4jConfigPath(): String {
        return "${filesDir.absolutePath}/log4j2-off.xml"
    }
} 