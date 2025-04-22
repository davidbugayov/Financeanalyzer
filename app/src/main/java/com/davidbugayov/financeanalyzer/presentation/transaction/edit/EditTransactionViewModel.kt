package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.validation.ValidationBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import com.davidbugayov.financeanalyzer.domain.model.Result as DomainResult

class EditTransactionViewModel(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    validateTransactionUseCase: ValidateTransactionUseCase,
    categoriesViewModel: CategoriesViewModel,
    sourcePreferences: SourcePreferences,
    walletRepository: WalletRepository
) : BaseTransactionViewModel<EditTransactionState, BaseTransactionEvent>(
    categoriesViewModel,
    sourcePreferences,
    walletRepository,
    validateTransactionUseCase
) {

    override val _state = MutableStateFlow(EditTransactionState())

    // Флаг для блокировки автоматической отправки формы
    private var blockAutoSubmit = false

    override val wallets: List<Wallet>
        get() = emptyList() // Используйте loadWallets из базового класса

    init {
        loadInitialData()
        // Можно вызвать loadWallets(walletRepository) при необходимости
    }

    fun loadTransaction(id: String) {
        Timber.d("ТРАНЗАКЦИЯ: Внутри loadTransaction, ID=$id")
        
        viewModelScope.launch {
            try {
                Timber.d("ТРАНЗАКЦИЯ: Вызов getTransactionByIdUseCase для ID=$id")
                val result = getTransactionByIdUseCase(id)
                Timber.d("ТРАНЗАКЦИЯ: Результат getTransactionByIdUseCase: $result")
                
                if (result is DomainResult.Success) {
                    val transaction = result.data
                    Timber.d("ТРАНЗАКЦИЯ: Успешно получена транзакция: сумма=${transaction.amount}, категория=${transaction.category}")
                    
                    // Устанавливаем режим редактирования и саму транзакцию
                    _state.update { it.copy(
                        transactionToEdit = transaction,
                        editMode = true
                    )}
                    
                    Timber.d("ТРАНЗАКЦИЯ: После обновления state: transactionToEdit=${_state.value.transactionToEdit?.id}, editMode=${_state.value.editMode}")
                } else if (result is DomainResult.Error) {
                    Timber.e("ТРАНЗАКЦИЯ: Ошибка в useCase: ${result.exception.message}")
                    _state.update { it.copy(
                        error = result.exception.message,
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Исключение в loadTransaction: ${e.message}")
                _state.update { it.copy(
                    error = e.message ?: "Неизвестная ошибка",
                    isLoading = false
                )}
            }
        }
    }

    private fun validateInput(
        walletId: String?,
        amount: String,
        categoryId: String
    ): Boolean {
        val validationBuilder = ValidationBuilder()
        val currentState = _state.value
        
        // Reset errors
        _state.update {
            it.copy(
                walletError = false,
                amountError = false,
                categoryError = false,
                sourceError = false
            )
        }
        
        // Check wallet only if addToWallet is true
        if (currentState.addToWallet && walletId.isNullOrBlank()) {
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

    fun submit() {
        viewModelScope.launch {
            val currentState = _state.value
            
            // Установим флаг загрузки
            _state.update { it.copy(isLoading = true) }
            
            val transaction = prepareTransactionForEdit() ?: run {
                // Если транзакция не подготовлена, снимаем флаг загрузки
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            // Validate input
            val isValid = validateInput(
                walletId = currentState.targetWalletId,
                amount = currentState.amount,
                categoryId = currentState.category
            )
            
            if (!isValid) {
                // Если валидация не прошла, снимаем флаг загрузки
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            try {
                Timber.d("ТРАНЗАКЦИЯ: Обновление транзакции начато: ${transaction.id}")
                // Обновляем транзакцию через useCase
                updateTransactionUseCase(transaction)
                
                // Показываем успешное обновление
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = "Транзакция успешно обновлена"
                    ) 
                }
                Timber.d("ТРАНЗАКЦИЯ: Обновление транзакции успешно завершено")
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Ошибка при обновлении транзакции: ${e.message}")
                // Показываем ошибку и снимаем флаг загрузки
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка при обновлении транзакции: ${e.message}"
                    ) 
                }
            }
        }
    }

    override fun submitTransaction(context: android.content.Context) {
        submit()
    }

    // Загрузка транзакции для редактирования
    fun loadTransactionForEdit(transaction: Transaction) {
        Timber.d("ТРАНЗАКЦИЯ: Начало loadTransactionForEdit: $transaction")

        // Форматируем сумму как строку без знака минус
        val formattedAmount = transaction.amount.abs().amount.toString()
        Timber.d("ТРАНЗАКЦИЯ: Форматированная сумма: $formattedAmount (исходная: ${transaction.amount})")

        // Определяем какую категорию установить в зависимости от типа транзакции
        val selectedExpenseCategory = if (transaction.isExpense) transaction.category else ""
        val selectedIncomeCategory = if (!transaction.isExpense) transaction.category else ""
        
        Timber.d("ТРАНЗАКЦИЯ: selectedExpenseCategory=$selectedExpenseCategory, selectedIncomeCategory=$selectedIncomeCategory")

        _state.update {
            it.copy(
                transactionToEdit = transaction,
                title = transaction.title,
                amount = formattedAmount,
                category = transaction.category,
                note = transaction.note ?: "",
                selectedDate = transaction.date,
                isExpense = transaction.isExpense,
                source = transaction.source,
                sourceColor = transaction.sourceColor,
                editMode = true,
                selectedExpenseCategory = selectedExpenseCategory,
                selectedIncomeCategory = selectedIncomeCategory
            )
        }

        Timber.d("ТРАНЗАКЦИЯ: После loadTransactionForEdit: сумма=${_state.value.amount}, " +
                "дата=${_state.value.selectedDate}, editMode=${_state.value.editMode}, " +
                "source=${_state.value.source}, category=${_state.value.category}, " +
                "isExpense=${_state.value.isExpense}, " +
                "selectedExpenseCategory=${_state.value.selectedExpenseCategory}, " +
                "selectedIncomeCategory=${_state.value.selectedIncomeCategory}")
    }

    // Загрузка транзакции для редактирования по ID
    fun loadTransactionForEditById(transactionId: String) {
        Timber.d("ТРАНЗАКЦИЯ: Начало загрузки транзакции ID=$transactionId")
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                Timber.d("ТРАНЗАКЦИЯ: Вызов loadTransaction для ID=$transactionId")
                loadTransaction(transactionId)
                
                // Добавляем задержку для завершения асинхронной загрузки транзакции
                kotlinx.coroutines.delay(500)
                
                val transaction = _state.value.transactionToEdit
                Timber.d("ТРАНЗАКЦИЯ: Результат загрузки transaction=${transaction?.id}, state.editMode=${_state.value.editMode}")
                
                if (transaction != null) {
                    Timber.d("ТРАНЗАКЦИЯ: Загружена, id=${transaction.id}, сумма=${transaction.amount}, категория=${transaction.category}")
                    loadTransactionForEdit(transaction)
                    // Обязательно отключаем индикатор загрузки после успешной загрузки
                    _state.update { it.copy(isLoading = false) }
                    Timber.d("ТРАНЗАКЦИЯ: После loadTransactionForEdit, editMode=${_state.value.editMode}, сумма=${_state.value.amount}")
                } else {
                    Timber.e("ТРАНЗАКЦИЯ: НЕ НАЙДЕНА с ID=$transactionId")
                    _state.update { 
                        it.copy(
                            error = "Транзакция не найдена",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Ошибка при загрузке транзакции: ${e.message}")
                _state.update { 
                    it.copy(
                        error = "Ошибка при загрузке транзакции: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun prepareTransactionForEdit(): Transaction? {
        val currentState = _state.value
        // Only category is required, source can be kept from original transaction
        if (currentState.category.isBlank()) {
            // Display validation error
            _state.update { it.copy(categoryError = true) }
            return null
        }
        
        // Convert amount to double
        val amount = currentState.amount.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            // Display validation error
            _state.update { it.copy(amountError = true) }
            return null
        }
        
        val finalAmount = if (currentState.isExpense) -amount else amount
        val sourceToUse = if (currentState.source.isBlank()) currentState.transactionToEdit?.source ?: "" else currentState.source
        val sourceColorToUse = if (currentState.source.isBlank()) currentState.transactionToEdit?.sourceColor ?: 0 else currentState.sourceColor
        
        return currentState.transactionToEdit?.copy(
            title = currentState.title,
            amount = Money(finalAmount),
            category = currentState.category,
            note = currentState.note,
            date = currentState.selectedDate,
            isExpense = currentState.isExpense,
            source = sourceToUse,
            sourceColor = sourceColorToUse
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
            
            is BaseTransactionEvent.SetExpenseCategory -> _state.update { state ->
                val newState = state.copy(
                    category = event.category,
                    selectedExpenseCategory = event.category
                )
                newState
            }

            is BaseTransactionEvent.SetIncomeCategory -> _state.update { state ->
                val newState = state.copy(
                    category = event.category,
                    selectedIncomeCategory = event.category
                )
                newState
            }
            
            is BaseTransactionEvent.ToggleTransactionType -> {
                Timber.d("ТРАНЗАКЦИЯ: Переключение типа транзакции с ${_state.value.isExpense} на ${!_state.value.isExpense}")
                
                // Сохраняем ID транзакции для лога
                val transactionId = _state.value.transactionToEdit?.id
                
                _state.update { 
                    it.copy(
                        isExpense = !it.isExpense,
                        category = "" // Сбрасываем категорию при смене типа
                    ) 
                }
                
                // Устанавливаем категорию по умолчанию для нового типа транзакции
                setDefaultCategoryIfNeeded(force = true)
                
                Timber.d("ТРАНЗАКЦИЯ: После переключения типа - isExpense=${_state.value.isExpense}, category=${_state.value.category}, transactionId=$transactionId")
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
        selectedDate: Date,
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
        transactionToEdit: Transaction?,
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

    // Установка ошибки с отключением индикатора загрузки
    fun setError(errorMessage: String) {
        Timber.e("ТРАНЗАКЦИЯ: установка ошибки: $errorMessage")
        _state.update { 
            it.copy(
                error = errorMessage,
                isLoading = false
            )
        }
    }
}