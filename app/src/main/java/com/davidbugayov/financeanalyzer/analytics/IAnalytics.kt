package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle

/**
 * Интерфейс для работы с аналитикой.
 * Позволяет абстрагироваться от конкретной реализации (Firebase, Яндекс.Метрика и т.д.)
 */
interface IAnalytics {
    /**
     * Логирует событие без параметров
     *
     * @param eventName название события
     */
    fun logEvent(eventName: String)

    /**
     * Логирует событие с параметрами
     *
     * @param eventName название события
     * @param params параметры события
     */
    fun logEvent(eventName: String, params: Bundle)

    /**
     * Устанавливает пользовательское свойство
     *
     * @param name название свойства
     * @param value значение свойства
     */
    fun setUserProperty(name: String, value: String)

    /**
     * Устанавливает ID пользователя для аналитики
     *
     * @param userId ID пользователя
     */
    fun setUserId(userId: String)
} 