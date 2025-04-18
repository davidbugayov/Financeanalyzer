package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

/**
 * Состояние экрана редактирования транзакции.
 */
data class EditTransactionState(
    val title: String = "",
    val amount: String = "",
    val category: String = "",
    val note: String = "",
    val selectedDate: Date = Date(),
    val isExpense: Boolean = true,
    val transactionToEdit: Transaction? = null,
    val isEdited: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
    // ...другие поля, если нужны
) 