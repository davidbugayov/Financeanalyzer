package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.DialogStateTransaction
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.EditingState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.TransactionData
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.ValidationError
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.WalletState
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem

/**
 * Состояние экрана редактирования транзакции.
 * Реализует базовый интерфейс и добавляет специфичные для редактирования транзакции поля.
 */
data class EditTransactionState(
    // Основные данные транзакции
    override val transactionData: TransactionData = TransactionData(),
    
    // Состояние диалогов
    override val dialogStateTransaction: DialogStateTransaction = DialogStateTransaction(),
    
    // Состояние кошелька
    override val walletState: WalletState = WalletState(),
    
    // Временные состояния редактирования
    override val editingState: EditingState = EditingState(),
    
    // Состояние загрузки и результата
    override val isLoading: Boolean = false,
    override val validationError: ValidationError? = null,
    override val isSuccess: Boolean = false,
    val successMessage: String = "Изменения сохранены",
    
    // Категории и источники
    override val expenseCategories: List<CategoryItem> = emptyList(),
    override val incomeCategories: List<CategoryItem> = emptyList(),
    override val sources: List<SourceItem> = emptyList(),
    
    // Режим редактирования - всегда true для этого класса
    override val editMode: Boolean = true,
    val transactionToEdit: Transaction? = null,
    
    // Флаг для UI логики - зависит от типа редактируемой транзакции
    override val forceExpense: Boolean = false,
    
    // Специфичные для редактирования транзакции поля
    val originalData: TransactionData? = null, // Оригинальные данные транзакции для отмены изменений
    val hasUnsavedChanges: Boolean = false, // Флаг наличия несохраненных изменений
    
    // ID транзакции для интерфейса BaseTransactionState
    override val transactionId: String? = null
) : BaseTransactionState 