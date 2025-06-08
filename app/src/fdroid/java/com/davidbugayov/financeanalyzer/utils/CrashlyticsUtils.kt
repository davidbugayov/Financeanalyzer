package com.davidbugayov.financeanalyzer.utils

import timber.log.Timber

/**
 * Вспомогательные утилиты для работы с Crashlytics.
 * Этот класс - реализация-заглушка для F-Droid flavor.
 */
object CrashlyticsUtils {
    
    /**
     * Логирует нефатальное исключение в Crashlytics и Timber
     */
    fun logException(throwable: Throwable) {
        Timber.e(throwable, "Исключение (только Timber)")
    }
    
    /**
     * Логирует произвольное сообщение в Crashlytics и Timber
     */
    fun log(message: String) {
        Timber.d("Crashlytics log: $message (только Timber)")
    }
    
    /**
     * Устанавливает пользовательский идентификатор для Crashlytics
     */
    fun setUserId(userId: String) {
        Timber.d("Установка Crashlytics userId: $userId (только Timber)")
    }
    
    /**
     * Добавляет строковый ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: String) {
        Timber.d("Установка Crashlytics key: $key = $value (только Timber)")
    }
    
    /**
     * Добавляет целочисленный ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Int) {
        Timber.d("Установка Crashlytics key: $key = $value (только Timber)")
    }
    
    /**
     * Добавляет булевский ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Boolean) {
        Timber.d("Установка Crashlytics key: $key = $value (только Timber)")
    }
    
    /**
     * Добавляет число с плавающей точкой ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Float) {
        Timber.d("Установка Crashlytics key: $key = $value (только Timber)")
    }
    
    /**
     * Добавляет число с плавающей точкой ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Double) {
        Timber.d("Установка Crashlytics key: $key = $value (только Timber)")
    }
} 