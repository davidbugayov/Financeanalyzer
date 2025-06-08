package com.davidbugayov.financeanalyzer.presentation.import_transaction.model

import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Состояние UI для экрана импорта транзакций.
 * Следует принципам MVI и Clean Architecture.
 */
data class ImportTransactionsState(
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val totalCount: Int = 0,
    val currentStep: String = "",
    val successCount: Int = 0,
    val skippedCount: Int = 0,
    val error: String? = null,
    val isImportCompleted: Boolean = false,
    val totalAmount: Money = Money.zero(),
    val logs: List<String> = emptyList(),
)
