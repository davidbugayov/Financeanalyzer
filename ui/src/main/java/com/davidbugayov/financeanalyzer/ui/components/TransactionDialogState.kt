package com.davidbugayov.financeanalyzer.ui.components

import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Состояние диалогов для работы с транзакциями.
 * Используется для отслеживания показываемых диалогов и выбранных транзакций.
 */
data class TransactionDialogState(
    /** Транзакция для редактирования */
    val transactionToEdit: String? = null,

    /** Показывать ли диалог редактирования */
    val showEditDialog: Boolean = false,

    /** Транзакция для удаления */
    val transactionToDelete: Transaction? = null,

    /** Показывать ли диалог подтверждения удаления */
    val showDeleteConfirmDialog: Boolean = false,
)
