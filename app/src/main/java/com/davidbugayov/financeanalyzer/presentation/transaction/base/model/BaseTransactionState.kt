package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import java.util.Date
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem

/**
 * Базовый интерфейс состояния для экранов транзакций.
 * Содержит общие свойства для экранов добавления и редактирования транзакций.
 */
interface BaseTransactionState {
    /**
     * Данные транзакции, которые редактируются в данный момент.
     */
    val transactionData: TransactionData
    
    /**
     * Текущие категории расходов.
     */
    val expenseCategories: List<CategoryItem>
    
    /**
     * Текущие категории доходов.
     */
    val incomeCategories: List<CategoryItem>
    
    /**
     * Список источников транзакций.
     */
    val sources: List<SourceItem>
    
    /**
     * Состояние, связанное с диалогами.
     */
    val dialogStateTransaction: DialogStateTransaction
    
    /**
     * Текущая ошибка валидации, если есть.
     */
    val validationError: ValidationError?
    
    /**
     * Флаг показывающий, что транзакция успешно добавлена/изменена.
     */
    val isSuccess: Boolean
    
    /**
     * Флаг показывающий, что данные загружаются.
     */
    val isLoading: Boolean
    
    /**
     * Состояние, связанное с редактированием элементов.
     */
    val editingState: EditingState
    
    /**
     * Состояние выбора кошельков.
     */
    val walletState: WalletState
    
    /**
     * Флаг, указывающий, что находимся в режиме редактирования транзакции.
     */
    val editMode: Boolean
    
    /**
     * Флаг, указывающий, что транзакция может быть только расходом.
     */
    val forceExpense: Boolean
    
    /**
     * ID транзакции (для редактирования)
     */
    val transactionId: String?
}

/**
 * Типы ошибок валидации для экранов работы с транзакциями.
 */
sealed class ValidationError {
    object AmountMissing : ValidationError()
    object CategoryMissing : ValidationError()
    data class General(val message: String) : ValidationError()
}

/**
 * Данные транзакции, которые могут быть изменены пользователем.
 */
data class TransactionData(
    val amount: String = "",
    val category: String = "",
    val note: String = "",
    val date: Date = Date(),
    val selectedDate: Date = Date(),
    val isExpense: Boolean = true,
    val source: String = "",
    val sourceColor: Int = 0,
    val targetWalletId: String = "" // основной кошелек, в котором отражается транзакция
)
/**
 * Состояние диалогов транзакции
 */
data class DialogStateTransaction(
    val showDatePicker: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showSourcePicker: Boolean = false,
    val showWalletSelector: Boolean = false,
    val showCancelConfirmation: Boolean = false,
    val showColorPicker: Boolean = false,
    val showCustomCategoryDialog: Boolean = false,
    val showCustomSourceDialog: Boolean = false,
    val showDeleteCategoryConfirmation: Boolean = false,
    val showDeleteSourceConfirmation: Boolean = false,
    val categoryToDelete: String? = null,
    val sourceToDelete: String? = null
)
/**
 * Состояние кошельков для транзакций.
 */
data class WalletState(
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletIds: List<String> = emptyList(),
    val selectedWallets: List<String> = emptyList(),
    val targetWalletId: String = "",
    val addToWallet: Boolean = false,
    val addToWallets: Boolean = false
)

/**
 * Состояние редактирования для кастомных элементов.
 */
data class EditingState(
    val customCategory: String = "",
    val customSource: String = "",
    val selectedColor: Int = 0,
    val categoryToDelete: String? = null,
    val sourceToDelete: String? = null,
    val sourceName: String = "",
    val sourceColor: Int = 0
)

/**
 * Источник для отображения в UI.
 */
data class SourceItem(
    val name: String,
    val color: Int,
    val isCustom: Boolean
) 