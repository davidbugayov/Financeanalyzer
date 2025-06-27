package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import android.os.SystemClock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Класс для отслеживания пользовательских событий и активности.
 * Собирает данные о вовлеченности пользователя, длительности сессий,
 * частоте использования функций и т.д.
 */
object UserEventTracker {
    private val screenTimeMap = ConcurrentHashMap<String, Long>()
    private val featureUsageCount = ConcurrentHashMap<String, Int>()
    private val featureLastUsed = ConcurrentHashMap<String, Long>()
    private val sessionStartTime = SystemClock.elapsedRealtime()
    
    private var currentScreen: String? = null
    private var currentScreenStartTime: Long = 0
    
    /**
     * Отслеживать открытие экрана
     * @param screenName Название экрана
     */
    fun trackScreenOpen(screenName: String) {
        // Закрываем предыдущий экран, если он был
        currentScreen?.let {
            trackScreenClose(it)
        }
        
        currentScreen = screenName
        currentScreenStartTime = SystemClock.elapsedRealtime()
        
        Timber.d("Screen opened: $screenName")
    }
    
    /**
     * Отслеживать закрытие экрана
     * @param screenName Название экрана
     */
    fun trackScreenClose(screenName: String) {
        if (currentScreen == screenName && currentScreenStartTime > 0) {
            val duration = SystemClock.elapsedRealtime() - currentScreenStartTime
            
            // Добавляем время к общему времени на экране
            screenTimeMap[screenName] = (screenTimeMap[screenName] ?: 0) + duration
            
            // Логируем время на экране
            AnalyticsUtils.logUserEngagement(duration, screenName)
            
            Timber.d("Screen closed: $screenName, duration: $duration ms")
            
            currentScreen = null
            currentScreenStartTime = 0
        }
    }
    
    /**
     * Отслеживать использование функции
     * @param featureName Название функции
     * @param result Результат использования (успех, ошибка и т.д.)
     */
    fun trackFeatureUsage(featureName: String, result: String = AnalyticsConstants.Values.RESULT_SUCCESS) {
        // Увеличиваем счетчик использования функции
        val count = (featureUsageCount[featureName] ?: 0) + 1
        featureUsageCount[featureName] = count
        
        // Запоминаем время последнего использования
        featureLastUsed[featureName] = System.currentTimeMillis()
        
        // Логируем использование функции
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.FEATURE_NAME, featureName)
            putString(AnalyticsConstants.Params.FEATURE_RESULT, result)
            putInt(AnalyticsConstants.Params.FEATURE_USAGE_COUNT, count)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.FEATURE_USED, params)
        
        Timber.d("Feature used: $featureName, result: $result, count: $count")
    }
    
    /**
     * Отслеживать действие пользователя
     * @param actionName Название действия
     * @param params Дополнительные параметры
     */
    fun trackUserAction(actionName: String, params: Map<String, Any> = emptyMap()) {
        val startTime = SystemClock.elapsedRealtime()
        
        // Подготавливаем параметры для аналитики
        val analyticsParams = Bundle().apply {
            putString(AnalyticsConstants.Params.ACTION_NAME, actionName)
            putString(AnalyticsConstants.Params.SCREEN_NAME, currentScreen ?: "unknown")
            
            // Добавляем дополнительные параметры
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.USER_ACTION, analyticsParams)
        
        Timber.d("User action: $actionName on screen ${currentScreen ?: "unknown"}")
    }
    
    /**
     * Отслеживать отзыв пользователя
     * @param score Оценка (от 1 до 5)
     * @param feedback Текстовый отзыв (опционально)
     */
    fun trackUserFeedback(score: Int, feedback: String? = null) {
        AnalyticsUtils.logUserFeedback(score, feedback)
        
        Timber.d("User feedback: score=$score, feedback=${feedback ?: "none"}")
    }
    
    /**
     * Отслеживать рейтинг приложения
     * @param rating Оценка (от 1 до 5)
     * @param source Источник оценки (внутренний, Google Play, RuStore и т.д.)
     */
    fun trackAppRating(rating: Int, source: String) {
        val params = Bundle().apply {
            putInt(AnalyticsConstants.Params.USER_RATING, rating)
            putString(AnalyticsConstants.Params.SOURCE, source)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.USER_RATING, params)
        
        Timber.d("App rating: $rating from $source")
    }
    
    /**
     * Получить общее время, проведенное на экране
     * @param screenName Название экрана
     * @return Время в миллисекундах
     */
    fun getTotalScreenTime(screenName: String): Long {
        return screenTimeMap[screenName] ?: 0L
    }
    
    /**
     * Получить количество использований функции
     * @param featureName Название функции
     * @return Количество использований
     */
    fun getFeatureUsageCount(featureName: String): Int {
        return featureUsageCount[featureName] ?: 0
    }
    
    /**
     * Получить время последнего использования функции
     * @param featureName Название функции
     * @return Время в миллисекундах с начала эпохи или null, если функция не использовалась
     */
    fun getFeatureLastUsed(featureName: String): Long? {
        return featureLastUsed[featureName]
    }
    
    /**
     * Получить продолжительность текущей сессии
     * @return Время в миллисекундах
     */
    fun getSessionDuration(): Long {
        return SystemClock.elapsedRealtime() - sessionStartTime
    }
    
    /**
     * Отправить данные о сессии в аналитику
     */
    fun sendSessionStats() {
        val sessionDuration = getSessionDuration()
        val screenCount = screenTimeMap.size
        val totalScreenTime = screenTimeMap.values.sum()
        val featureCount = featureUsageCount.size
        
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.USER_ENGAGEMENT_TIME, sessionDuration)
            putInt("screen_count", screenCount)
            putLong("total_screen_time", totalScreenTime)
            putInt("feature_count", featureCount)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.USER_ENGAGEMENT, params)
        
        Timber.d("Session stats: duration=$sessionDuration ms, screens=$screenCount, features=$featureCount")
    }
} 