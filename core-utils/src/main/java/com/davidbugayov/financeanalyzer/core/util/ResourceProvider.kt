package com.davidbugayov.financeanalyzer.core.util

import androidx.annotation.StringRes

/**
 * Провайдер строковых ресурсов для ViewModel и других компонентов через DI
 */
interface ResourceProvider {
    /**
     * Возвращает строку по идентификатору ресурса, поддерживает подстановку аргументов
     * @param id Идентификатор строкового ресурса
     * @param args Аргументы форматирования (если есть)
     */
    fun getString(@StringRes id: Int, vararg args: Any?): String
}
