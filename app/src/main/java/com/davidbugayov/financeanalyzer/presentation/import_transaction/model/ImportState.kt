package com.davidbugayov.financeanalyzer.presentation.import_transaction.model

/**
 * Состояние для экрана импорта транзакций.
 * Содержит всю необходимую информацию для отображения UI.
 */
data class ImportState(
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val progressMessage: String = "",
    val successCount: Int = 0,
    val skippedCount: Int = 0,
    val successMessage: String = "",
    val error: String? = null,
    val fileName: String = "", // Имя импортируемого файла
    val bankName: String? = null, // Название банка
)
