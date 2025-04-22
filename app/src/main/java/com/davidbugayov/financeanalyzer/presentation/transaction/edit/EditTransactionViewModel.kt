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
import kotlinx.coroutines.Dispatchers

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

    // List to store loaded wallets
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    override val wallets: List<Wallet>
        get() = _wallets.value

    init {
        loadInitialData()
        // Load wallets from repository using base class method
        loadWallets()
    }

    // Override loadWallets to update our local wallets state
    override fun loadWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletsList = walletRepository.getAllWallets()
                _wallets.value = walletsList 
                Timber.d("ТРАНЗАКЦИЯ: Загружено ${walletsList.size} кошельков")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
                _wallets.value = emptyList()
            }
        }
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
        
        // Check wallet only if addToWallet is true and it's an income transaction
        if (!currentState.isExpense && currentState.addToWallet && currentState.selectedWallets.isEmpty()) {
            validationBuilder.addWalletError()
            Timber.d("ТРАНЗАКЦИЯ: Ошибка валидации - не выбраны кошельки")
        }
        
        // Check amount
        if (amount.isBlank()) {
            validationBuilder.addAmountError()
            Timber.d("ТРАНЗАКЦИЯ: Ошибка валидации - пустая сумма")
        } else {
            try {
                val amountValue = amount.replace(",", ".").toDouble()
                if (amountValue <= 0) {
                    validationBuilder.addAmountError()
                    Timber.d("ТРАНЗАКЦИЯ: Ошибка валидации - сумма меньше или равна нулю: $amountValue")
                }
            } catch (e: Exception) {
                validationBuilder.addAmountError()
                Timber.e("ТРАНЗАКЦИЯ: Ошибка валидации при парсинге суммы: ${e.message}")
            }
        }
        
        // Check category
        if (categoryId.isBlank()) {
            validationBuilder.addCategoryError()
            Timber.d("ТРАНЗАКЦИЯ: Ошибка валидации - пустая категория")
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
        
        Timber.d("ТРАНЗАКЦИЯ: Результат валидации: isValid=${validationResult.isValid}, " +
                "hasWalletError=${validationResult.hasWalletError}, " +
                "hasAmountError=${validationResult.hasAmountError}, " +
                "hasCategoryError=${validationResult.hasCategoryError}")
                
        return validationResult.isValid
    }

    fun submit() {
        viewModelScope.launch {
            val currentState = _state.value
            
            Timber.d("ТРАНЗАКЦИЯ: Начало сохранения изменений, isExpense=${currentState.isExpense}, " +
                    "category=${currentState.category}, " +
                    "selectedIncomeCategory=${currentState.selectedIncomeCategory}, " +
                    "selectedExpenseCategory=${currentState.selectedExpenseCategory}")
            
            // Установим флаг загрузки
            _state.update { it.copy(isLoading = true) }
            
            // Check if category is blank but we have a selected category based on transaction type
            if (currentState.category.isBlank()) {
                val categoryToUse = if (currentState.isExpense) {
                    currentState.selectedExpenseCategory
                } else {
                    currentState.selectedIncomeCategory
                }
                
                if (categoryToUse.isNotBlank()) {
                    _state.update { it.copy(category = categoryToUse) }
                    Timber.d("ТРАНЗАКЦИЯ: Установлена категория из selectedCategory: $categoryToUse")
                }
            }
            
            val transaction = prepareTransactionForEdit() ?: run {
                // Если транзакция не подготовлена, снимаем флаг загрузки
                _state.update { it.copy(isLoading = false) }
                Timber.e("ТРАНЗАКЦИЯ: Не удалось подготовить транзакцию к редактированию")
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
                Timber.e("ТРАНЗАКЦИЯ: Валидация не прошла")
                return@launch
            }

            try {
                Timber.d("ТРАНЗАКЦИЯ: Обновление транзакции начато: ${transaction.id}")
                
                // Сохраняем исходную транзакцию для сравнения
                val originalTransaction = currentState.transactionToEdit
                
                // Обновляем транзакцию через useCase
                updateTransactionUseCase(transaction)
                
                // Обновляем балансы кошельков, если это доход и выбраны кошельки
                if (!transaction.isExpense && transaction.walletIds != null && transaction.walletIds.isNotEmpty()) {
                    updateWalletsBalance(transaction.walletIds, transaction.amount, originalTransaction)
                }
                
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
        
        // Если это доход (не расход), устанавливаем настройки кошелька
        val addToWallet = !transaction.isExpense // For income transactions, enable wallets
        
        Timber.d("ТРАНЗАКЦИЯ: selectedExpenseCategory=$selectedExpenseCategory, selectedIncomeCategory=$selectedIncomeCategory")
        Timber.d("ТРАНЗАКЦИЯ: установка кошельков: addToWallet=$addToWallet")

        // Загружаем связанные с транзакцией кошельки
        loadTransactionWallets(transaction.id)

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
                selectedIncomeCategory = selectedIncomeCategory,
                addToWallet = addToWallet
            )
        }

        Timber.d("ТРАНЗАКЦИЯ: После loadTransactionForEdit: сумма=${_state.value.amount}, " +
                "дата=${_state.value.selectedDate}, editMode=${_state.value.editMode}, " +
                "source=${_state.value.source}, category=${_state.value.category}, " +
                "isExpense=${_state.value.isExpense}, " +
                "selectedExpenseCategory=${_state.value.selectedExpenseCategory}, " +
                "selectedIncomeCategory=${_state.value.selectedIncomeCategory}, " +
                "addToWallet=${_state.value.addToWallet}")
    }

    // Загрузка кошельков, связанных с транзакцией
    private fun loadTransactionWallets(transactionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем список кошельков, связанных с транзакцией
                val linkedWallets = walletRepository.getWalletsForTransaction(transactionId)
                
                if (linkedWallets.isNotEmpty()) {
                    val walletIds = linkedWallets.map { it.id }
                    Timber.d("ТРАНЗАКЦИЯ: Найдены связанные кошельки: ${walletIds.size}")
                    
                    // Обновляем состояние с выбранными кошельками
                    _state.update {
                        it.copy(
                            selectedWallets = walletIds
                        )
                    }
                } else {
                    Timber.d("ТРАНЗАКЦИЯ: Нет связанных кошельков для транзакции $transactionId")
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Ошибка при загрузке связанных кошельков: ${e.message}")
            }
        }
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
        
        Timber.d("ТРАНЗАКЦИЯ: Подготовка транзакции к редактированию: category=${currentState.category}, " +
                "isExpense=${currentState.isExpense}, " +
                "selectedIncomeCategory=${currentState.selectedIncomeCategory}, " +
                "selectedExpenseCategory=${currentState.selectedExpenseCategory}, " +
                "selectedWallets=${currentState.selectedWallets.size}")
        
        // Make sure category is not blank
        if (currentState.category.isBlank()) {
            // Try to use the appropriate category based on transaction type
            val categoryToUse = if (currentState.isExpense) {
                currentState.selectedExpenseCategory
            } else {
                currentState.selectedIncomeCategory
            }
            
            if (categoryToUse.isBlank()) {
                // Display validation error if still blank
                _state.update { it.copy(categoryError = true) }
                Timber.e("ТРАНЗАКЦИЯ: Ошибка - категория не выбрана")
                return null
            } else {
                // Update state with the selected category
                _state.update { it.copy(category = categoryToUse) }
                Timber.d("ТРАНЗАКЦИЯ: Использую категорию из selectedCategory: $categoryToUse")
            }
        }
        
        // Convert amount to double
        val amount = currentState.amount.replace(" ", "").replace(",", ".").toDoubleOrNull() 
        if (amount == null || amount <= 0) {
            // Display validation error
            _state.update { it.copy(amountError = true) }
            Timber.e("ТРАНЗАКЦИЯ: Ошибка - некорректная сумма: ${currentState.amount}")
            return null
        }
        
        val finalAmount = if (currentState.isExpense) -amount else amount
        val sourceToUse = if (currentState.source.isBlank()) currentState.transactionToEdit?.source ?: "" else currentState.source
        val sourceColorToUse = if (currentState.source.isBlank()) currentState.transactionToEdit?.sourceColor ?: 0 else currentState.sourceColor
        val categoryToUse = currentState.category
        
        Timber.d("ТРАНЗАКЦИЯ: Готова к обновлению: amount=$finalAmount, category=$categoryToUse, source=$sourceToUse, " +
                "выбранные кошельки: ${currentState.selectedWallets.size}")
        
        // Получаем список ID кошельков для сохранения в транзакции
        val selectedWalletIds = getWalletIdsForTransaction(
            isExpense = currentState.isExpense,
            addToWallet = currentState.addToWallet,
            selectedWallets = currentState.selectedWallets
        )
        
        return currentState.transactionToEdit?.copy(
            title = currentState.title,
            amount = Money(finalAmount),
            category = categoryToUse,
            note = currentState.note,
            date = currentState.selectedDate,
            isExpense = currentState.isExpense,
            source = sourceToUse,
            sourceColor = sourceColorToUse,
            walletIds = selectedWalletIds
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
            
            is BaseTransactionEvent.SetExpenseCategory -> {
                Timber.d("ТРАНЗАКЦИЯ: Выбрана категория расхода: ${event.category}")
                _state.update { state ->
                    val newState = state.copy(
                        category = event.category,
                        selectedExpenseCategory = event.category,
                        categoryError = false // Clear any previous category error
                    )
                    newState
                }
            }

            is BaseTransactionEvent.SetIncomeCategory -> {
                Timber.d("ТРАНЗАКЦИЯ: Выбрана категория дохода: ${event.category}")
                _state.update { state ->
                    val newState = state.copy(
                        category = event.category,
                        selectedIncomeCategory = event.category,
                        categoryError = false // Clear any previous category error
                    )
                    newState
                }
            }
            
            is BaseTransactionEvent.ToggleAddToWallet -> {
                val (newAddToWallet, newSelectedWallets) = handleToggleAddToWallet(
                    currentAddToWallet = _state.value.addToWallet,
                    currentSelectedWallets = _state.value.selectedWallets
                )
                
                _state.update {
                    it.copy(
                        addToWallet = newAddToWallet,
                        selectedWallets = newSelectedWallets
                    )
                }
            }
            
            is BaseTransactionEvent.SelectWallet -> {
                val updatedWallets = handleSelectWallet(
                    walletId = event.walletId,
                    selected = event.selected,
                    currentSelectedWallets = _state.value.selectedWallets
                )
                
                _state.update {
                    it.copy(selectedWallets = updatedWallets)
                }
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