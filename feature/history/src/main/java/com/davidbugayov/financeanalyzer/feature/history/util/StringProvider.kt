package com.davidbugayov.financeanalyzer.feature.history.util

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.history.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Утилитный класс для получения строковых ресурсов в модуле history
 */
object StringProvider {
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

    val apply: String get() = getString(R.string.apply)
    val cancel: String get() = getString(UiR.string.cancel)

    // Недостающие строки
    val delete: String get() = getString(R.string.delete)

    fun deleteSourceConfirmationMessage(source: String): String =
        getString(
            R.string.deleteSourceConfirmationMessage,
            source,
        )
}
