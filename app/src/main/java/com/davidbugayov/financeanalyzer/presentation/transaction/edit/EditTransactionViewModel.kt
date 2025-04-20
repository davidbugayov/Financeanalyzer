package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.addCustomSource
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import timber.log.Timber
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz

class EditTransactionViewModel(
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel,
    sourcePreferences: com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences,
    walletRepository: com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
) : BaseTransactionViewModel<EditTransactionState, BaseTransactionEvent>(
    categoriesViewModel,
    sourcePreferences,
    walletRepository
) {

    protected override val _state = MutableStateFlow(EditTransactionState())
    override val wallets: List<Wallet>
        get() = emptyList() // Используйте loadWallets из базового класса

    init {
        loadInitialData()
        // Можно вызвать loadWallets(walletRepository) при необходимости
    }

    override fun submitTransaction(context: android.content.Context) {
        if (validateInput()) {
            _state.update { it.copy(isLoading = true) }
            val transactionToUpdate = prepareTransactionForEdit()
            if (transactionToUpdate != null) {
                viewModelScope.launch {
                    try {
                        updateTransactionUseCase(transactionToUpdate)
                        updateWidget(context)
                        _state.update { it.copy(isSuccess = true, error = null, isLoading = false) }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = e.message ?: "Ошибка при обновлении транзакции", isSuccess = false, isLoading = false) }
                    }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Транзакция не найдена") }
            }
        }
    }

    // Загрузка транзакции для редактирования
    fun loadTransactionForEdit(transaction: Transaction) {
        Timber.d("Загрузка транзакции для редактирования: $transaction")

        // Форматируем сумму как строку без знака минус
        val formattedAmount = transaction.amount.abs().amount.toString()
        Timber.d("Форматированная сумма: $formattedAmount (исходная: ${transaction.amount})")

        _state.update { it.copy(
            transactionToEdit = transaction,
            title = transaction.title ?: "",
            amount = formattedAmount,
            category = transaction.category ?: "",
            note = transaction.note ?: "",
            selectedDate = transaction.date,
            isExpense = transaction.isExpense,
            source = transaction.source,
            sourceColor = transaction.sourceColor,
            editMode = true
        ) }

        Timber.d("Состояние после загрузки: сумма=${_state.value.amount}, дата=${_state.value.selectedDate}, режим редактирования=${_state.value.editMode}")
    }
    
    // Загрузка транзакции для редактирования по ID
    fun loadTransactionForEditById(transactionId: String) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    loadTransactionForEdit(transaction)
                } else {
                    _state.update { it.copy(error = "Транзакция не найдена") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Ошибка при загрузке транзакции: ${e.message}") }
            }
        }
    }

    private fun prepareTransactionForEdit(): Transaction? {
        val currentState = _state.value
        if (currentState.category.isBlank() || currentState.source.isBlank()) return null
        val amount = currentState.amount.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        val finalAmount = if (currentState.isExpense) -amount else amount
        return currentState.transactionToEdit?.copy(
            title = currentState.title,
            amount = Money(finalAmount),
            category = currentState.category,
            note = currentState.note,
            date = currentState.selectedDate,
            isExpense = currentState.isExpense,
            source = currentState.source,
            sourceColor = currentState.sourceColor
        )
    }

    override fun onEvent(event: BaseTransactionEvent, context: android.content.Context) {
        // Обрабатываем события UI
        handleBaseEvent(event, context)
    }

    /**
     * Реализация проверки валидации
     */
    override protected fun validateInput(): Boolean {
        val currentState = _state.value
        var isValid = true
        val amountError = currentState.amount.isBlank()
        val categoryError = currentState.category.isBlank()
        val sourceError = currentState.source.isBlank()
        var errorMsg: String? = null
        if (amountError) isValid = false
        if (categoryError) isValid = false
        if (sourceError) isValid = false
        errorMsg = when {
            amountError && categoryError && sourceError -> "Заполните сумму, категорию и источник"
            amountError && categoryError -> "Заполните сумму и категорию"
            amountError && sourceError -> "Заполните сумму и источник"
            categoryError && sourceError -> "Заполните категорию и источник"
            amountError -> "Введите сумму транзакции"
            categoryError -> "Выберите категорию"
            sourceError -> "Выберите источник"
            else -> null
        }
        _state.update { state ->
            copyState(
                state,
                amountError = amountError,
                categoryError = categoryError,
                sourceError = sourceError,
                error = errorMsg
            )
        }
        return isValid
    }

    override fun updateCategoryPositions() {
        viewModelScope.launch {
            usedCategories.forEach { (category, isExpense) ->
                categoriesViewModel.incrementCategoryUsage(category, isExpense)
            }
            usedCategories.clear()
        }
    }

    override fun copyState(
        state: EditTransactionState,
        title: String,
        amount: String,
        amountError: Boolean,
        category: String,
        categoryError: Boolean,
        note: String,
        selectedDate: java.util.Date,
        isExpense: Boolean,
        showDatePicker: Boolean,
        showCategoryPicker: Boolean,
        showCustomCategoryDialog: Boolean,
        showCancelConfirmation: Boolean,
        customCategory: String,
        showSourcePicker: Boolean,
        showCustomSourceDialog: Boolean,
        customSource: String,
        source: String,
        sourceColor: Int,
        showColorPicker: Boolean,
        isLoading: Boolean,
        error: String?,
        isSuccess: Boolean,
        successMessage: String,
        expenseCategories: List<com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem>,
        incomeCategories: List<com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem>,
        sources: List<com.davidbugayov.financeanalyzer.domain.model.Source>,
        categoryToDelete: String?,
        sourceToDelete: String?,
        showDeleteCategoryConfirmDialog: Boolean,
        showDeleteSourceConfirmDialog: Boolean,
        editMode: Boolean,
        transactionToEdit: com.davidbugayov.financeanalyzer.domain.model.Transaction?,
        addToWallet: Boolean,
        selectedWallets: List<String>,
        showWalletSelector: Boolean,
        targetWalletId: String?,
        forceExpense: Boolean,
        sourceError: Boolean
    ): EditTransactionState {
        return EditTransactionState(
            title = title,
            amount = amount,
            amountError = amountError,
            category = category,
            categoryError = categoryError,
            note = note,
            selectedDate = selectedDate,
            isExpense = isExpense,
            showDatePicker = showDatePicker,
            showCategoryPicker = showCategoryPicker,
            showCustomCategoryDialog = showCustomCategoryDialog,
            showCancelConfirmation = showCancelConfirmation,
            customCategory = customCategory,
            showSourcePicker = showSourcePicker,
            showCustomSourceDialog = showCustomSourceDialog,
            customSource = customSource,
            source = source,
            sourceColor = sourceColor,
            showColorPicker = showColorPicker,
            isLoading = isLoading,
            error = error,
            isSuccess = isSuccess,
            successMessage = successMessage,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            sources = sources,
            categoryToDelete = categoryToDelete,
            sourceToDelete = sourceToDelete,
            showDeleteCategoryConfirmDialog = showDeleteCategoryConfirmDialog,
            showDeleteSourceConfirmDialog = showDeleteSourceConfirmDialog,
            editMode = editMode,
            transactionToEdit = transactionToEdit,
            addToWallet = addToWallet,
            selectedWallets = selectedWallets,
            showWalletSelector = showWalletSelector,
            targetWalletId = targetWalletId,
            forceExpense = forceExpense,
            sourceError = sourceError
        )
    }
}