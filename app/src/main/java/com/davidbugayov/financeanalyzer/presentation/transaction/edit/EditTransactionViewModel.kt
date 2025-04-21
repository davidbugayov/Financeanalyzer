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
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.model.Result as DomainResult
import kotlin.Result
import com.davidbugayov.financeanalyzer.presentation.transaction.validation.ValidationBuilder
import java.util.Date

class EditTransactionViewModel(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val validateTransactionUseCase: ValidateTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel,
    sourcePreferences: com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences,
    walletRepository: com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
) : BaseTransactionViewModel<EditTransactionState, BaseTransactionEvent>(
    categoriesViewModel,
    sourcePreferences,
    walletRepository,
    validateTransactionUseCase
) {

    protected override val _state = MutableStateFlow(EditTransactionState())
    // Флаг для блокировки автоматической отправки формы
    private var blockAutoSubmit = false
    
    override val wallets: List<Wallet>
        get() = emptyList() // Используйте loadWallets из базового класса

    init {
        loadInitialData()
        // Можно вызвать loadWallets(walletRepository) при необходимости
    }

    fun loadTransaction(id: String) {
        viewModelScope.launch {
            try {
                val result = getTransactionByIdUseCase(id)
                if (result is DomainResult.Success) {
                    val transaction = result.data
                    if (transaction != null) {
                        _state.update { it.copy(transactionToEdit = transaction) }
                    } else {
                        _state.update { it.copy(error = "Транзакция не найдена") }
                    }
                } else if (result is DomainResult.Error) {
                    _state.update { it.copy(error = result.exception.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Неизвестная ошибка") }
            }
        }
    }

    private fun validateInput(
        walletId: String?,
        amount: String,
        note: String,
        date: Date,
        sourceColor: Int,
        source: String,
        categoryId: String,
        isExpense: Boolean
    ): Boolean {
        val validationBuilder = ValidationBuilder()
        // Reset errors
        _state.update {
            it.copy(
                walletError = false,
                amountError = false,
                categoryError = false,
                sourceError = false
            )
        }
        // Check wallet
        if (walletId.isNullOrBlank()) {
            validationBuilder.addWalletError()
        }
        // Check amount
        if (amount.isBlank()) {
            validationBuilder.addAmountError()
        } else {
            try {
                val amountValue = amount.replace(",", ".").toDouble()
                if (amountValue <= 0) {
                    validationBuilder.addAmountError()
                }
            } catch (e: Exception) {
                validationBuilder.addAmountError()
            }
        }
        // Check category
        if (categoryId.isBlank()) {
            validationBuilder.addCategoryError()
        }
        // No validation for source - allowing empty sources
        val validationResult = validationBuilder.build()
        _state.update {
            it.copy(
                walletError = validationResult.hasWalletError,
                amountError = validationResult.hasAmountError,
                categoryError = validationResult.hasCategoryError,
                sourceError = validationResult.hasSourceError
            )
        }
        return validationResult.isValid
    }

    fun validateInputData(amount: String, category: String, source: String, updateState: (ValidateTransactionUseCase.Result) -> Unit): Boolean {
        val result = validateTransactionUseCase(amount, category, source)
        updateState(result)
        return result.isValid
    }

    fun submit(preventAutoSubmit: Boolean = false) {
        viewModelScope.launch {
            val currentState = _state.value
            val transaction = prepareTransactionForEdit() ?: return@launch

            // Validate input
            val isValid = validateInput(
                walletId = currentState.targetWalletId,
                amount = currentState.amount,
                note = currentState.note,
                date = currentState.selectedDate,
                sourceColor = currentState.sourceColor,
                source = currentState.source,
                categoryId = currentState.category,
                isExpense = currentState.isExpense
            )
            if (!isValid) return@launch

            try {
                val result = updateTransactionUseCase(transaction)
                // You can update state or show a message here if needed
            } catch (e: Exception) {
                // You can update state or show a message here if needed
            }
        }
    }

    override fun submitTransaction(context: android.content.Context) {
        submit()
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
                loadTransaction(transactionId)
                val transaction = _state.value.transactionToEdit
                if (transaction != null) {
                    loadTransactionForEdit(transaction)
                } else {
                    _state.update { it.copy(error = "Транзакция не найдена") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке транзакции: ${e.message}")
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
        when (event) {
            is BaseTransactionEvent.SubmitEdit -> {
                submit()
            }
            is BaseTransactionEvent.ResetAmountOnly -> {
                _state.update { it.copy(amount = "", amountError = false) }
            }
            is BaseTransactionEvent.PreventAutoSubmit -> {
                blockAutoSubmit = true
            }
            else -> handleBaseEvent(event, context)
        }
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
        sourceError: Boolean,
        preventAutoSubmit: Boolean,
        selectedExpenseCategory: String,
        selectedIncomeCategory: String,
        customCategoryIcon: androidx.compose.ui.graphics.vector.ImageVector,
        availableCategoryIcons: List<androidx.compose.ui.graphics.vector.ImageVector>
    ): EditTransactionState {
        return state.copy(
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
            sourceError = sourceError,
            preventAutoSubmit = preventAutoSubmit,
            selectedExpenseCategory = selectedExpenseCategory,
            selectedIncomeCategory = selectedIncomeCategory
        )
    }
}