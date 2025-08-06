package com.davidbugayov.financeanalyzer.ui.util

import android.content.Context
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Утилитный класс для получения строковых ресурсов в ViewModel'ях и других местах,
 * где нет прямого доступа к контексту.
 */
object StringResourceProvider {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
    ): String {
        return context?.getString(resId) ?: "String not found"
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
        vararg formatArgs: Any,
    ): String {
        return context?.getString(resId, *formatArgs) ?: "String not found"
    }

    // Строки для периодов
    val periodAllTime: String get() = getString(R.string.period_all_time)

    // Строки для ошибок
    val errorUnknown: String get() = getString(R.string.error_unknown)
    val errorUnknownType: String get() = getString(R.string.error_unknown_type)

}
