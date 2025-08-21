package com.davidbugayov.financeanalyzer.core.util

import androidx.annotation.StringRes

/**
 * Провайдер строковых ресурсов для ViewModel и других компонентов через DI
 */
interface ResourceProvider {
    fun getString(
        @StringRes id: Int,
        vararg args: Any?,
    ): String
    
    fun getStringByName(
        name: String,
        vararg args: Any?,
    ): String
}
