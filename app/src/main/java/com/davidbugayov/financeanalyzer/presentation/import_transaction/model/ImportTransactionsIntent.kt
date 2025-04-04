package com.davidbugayov.financeanalyzer.presentation.import_transaction.model

import android.net.Uri

/**
 * Набор интентов для экрана ImportTransactions.
 * Следует принципам MVI и Clean Architecture.
 */
sealed class ImportTransactionsIntent {
    /**
     * Интент для начала импорта транзакций из выбранного файла.
     * @param uri URI выбранного файла для импорта
     */
    data class StartImport(val uri: Uri) : ImportTransactionsIntent()

    /**
     * Интент для обновления логов импорта.
     */
    data object RefreshLogs : ImportTransactionsIntent()

    /**
     * Интент для сброса состояния импорта.
     */
    data object ResetState : ImportTransactionsIntent()
} 