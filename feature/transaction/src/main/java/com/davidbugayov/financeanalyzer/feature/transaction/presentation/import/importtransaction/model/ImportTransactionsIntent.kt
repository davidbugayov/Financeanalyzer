package com.davidbugayov.financeanalyzer.presentation.importtransaction.model

import android.net.Uri

/**
 * Намерения пользователя для экрана импорта транзакций.
 * Используется в рамках паттерна MVI (Model-View-Intent).
 */
sealed class ImportTransactionsIntent {
    /**
     * Намерение начать импорт транзакций из указанного файла.
     * @param uri URI файла для импорта
     */
    data class StartImport(
        val uri: Uri,
    ) : ImportTransactionsIntent()

    /**
     * Намерение обновить логи импорта.
     */
    object RefreshLogs : ImportTransactionsIntent()

    /**
     * Намерение сбросить состояние импорта.
     */
    object ResetState : ImportTransactionsIntent()
}
