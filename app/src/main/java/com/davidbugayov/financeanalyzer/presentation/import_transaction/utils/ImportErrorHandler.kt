package com.davidbugayov.financeanalyzer.presentation.import_transaction.utils

import android.content.Context
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import com.davidbugayov.financeanalyzer.presentation.import_transaction.components.ImportResults
import timber.log.Timber

/**
 * Класс для обработки ошибок импорта и преобразования их в понятные пользователю сообщения
 */
class ImportErrorHandler(private val context: Context) {

    /**
     * Преобразует техническое сообщение об ошибке в понятное пользователю
     *
     * @param originalMessage Исходное сообщение об ошибке
     * @return Сообщение для пользователя
     */
    fun getUserFriendlyErrorMessage(originalMessage: String): String {
        // Определяем тип ошибки по содержимому сообщения
        return when {
            originalMessage.contains("unsupported", ignoreCase = true) ->
                context.getString(R.string.import_error_unsupported_format)
            originalMessage.contains("format", ignoreCase = true) ->
                context.getString(R.string.import_error_unknown_format)
            originalMessage.contains("read", ignoreCase = true) ||
                originalMessage.contains("open", ignoreCase = true) ->
                context.getString(R.string.import_error_file_read)
            originalMessage.contains("no transaction", ignoreCase = true) ||
                originalMessage.contains("empty", ignoreCase = true) ->
                context.getString(R.string.import_error_no_transactions)
            originalMessage.contains("date", ignoreCase = true) ->
                context.getString(R.string.import_error_date_format)
            originalMessage.contains("csv", ignoreCase = true) ->
                context.getString(R.string.import_error_csv_format)
            originalMessage.contains("statistics", ignoreCase = true) ->
                context.getString(R.string.import_error_statistics_file)
            else -> context.getString(R.string.import_error_unknown, "", originalMessage)
        }
    }
}
