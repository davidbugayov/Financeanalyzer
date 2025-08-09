package com.davidbugayov.financeanalyzer.feature.utils

import android.content.Context
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.R as UiR
import timber.log.Timber

/**
 * Утилитарный класс для обработки ошибок импорта.
 */
object ImportErrorHandler {
    /**
     * Обрабатывает ошибку импорта и возвращает сообщение для пользователя.
     *
     * @param context Контекст приложения
     * @param error Ошибка или исключение
     * @return Сообщение об ошибке для отображения пользователю
     */
    fun handleError(
        context: Context,
        error: Throwable?,
    ): String {
        // Логирование ошибки
        Timber.e(error)
        CrashLoggerProvider.crashLogger.logException(error ?: Exception("Unknown import error"))

        return when {
            error == null -> context.getString(UiR.string.import_error_unknown)
            error.message?.contains("unsupported format", ignoreCase = true) == true ->
                context.getString(UiR.string.import_error_unsupported_format)
            error.message?.contains("unknown format", ignoreCase = true) == true ->
                context.getString(UiR.string.import_error_unknown_format)
            error.message?.contains("file read", ignoreCase = true) == true ->
                context.getString(UiR.string.import_error_file_read)
            error.message?.contains("no transactions", ignoreCase = true) == true ->
                context.getString(UiR.string.import_error_no_transactions)
            error.message?.contains("date format", ignoreCase = true) == true ->
                context.getString(UiR.string.import_error_date_format)
            error.message?.contains("csv format", ignoreCase = true) == true ->
                context.getString(UiR.string.import_error_csv_format)
            error.message?.contains("statistics file", ignoreCase = true) == true ->
                context.getString(UiR.string.import_error_statistics_file)
            else -> error.message ?: context.getString(UiR.string.import_error_unknown)
        }
    }
}
