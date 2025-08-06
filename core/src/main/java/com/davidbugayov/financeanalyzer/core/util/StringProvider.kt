package com.davidbugayov.financeanalyzer.core.util

import android.content.Context
import com.davidbugayov.financeanalyzer.core.R

/**
 * Утилитный класс для получения строковых ресурсов в обычных классах
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

    // Предопределенные строки для AppException
    val errorNetworkConnection: String get() = getString(R.string.error_network_connection)
    val errorValidation: String get() = getString(R.string.error_validation)
    val errorDataNotFound: String get() = getString(R.string.error_data_not_found)
    val errorFileRead: String get() = getString(R.string.error_file_read)
    val errorUnknown: String get() = getString(R.string.error_unknown)
    val errorApplication: String get() = getString(R.string.error_application)
}
