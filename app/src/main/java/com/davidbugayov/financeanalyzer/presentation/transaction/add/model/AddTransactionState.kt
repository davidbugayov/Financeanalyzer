package com.davidbugayov.financeanalyzer.presentation.transaction.add.model

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
 * Состояние экрана добавления транзакции.
 * Реализует базовый интерфейс и добавляет специфичные для добавления транзакции поля.
 */
data class AddTransactionState(
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
    val successMessage: String = "Транзакция успешно добавлена",
    
    // Категории и источники
    override val expenseCategories: List<CategoryItem> = emptyList(),
    override val incomeCategories: List<CategoryItem> = emptyList(),
    override val sources: List<SourceItem> = emptyList(),
    
    // Режим редактирования
    override val editMode: Boolean = false,
    val transactionToEdit: Transaction? = null,
    
    // Флаг для UI логики
    override val forceExpense: Boolean = true, // По умолчанию расход
    
    // Специфичные для добавления транзакции поля
    val canAddAnother: Boolean = true, // Можно ли добавить еще одну транзакцию
    val showAddAnotherOption: Boolean = true, // Показывать ли опцию добавления еще одной транзакции
    
    // ID транзакции для интерфейса BaseTransactionState
    override val transactionId: String? = null
) : BaseTransactionState 