package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import android.app.Application
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionByIdUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.validation.ValidationBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import com.davidbugayov.financeanalyzer.domain.model.Result as DomainResult

class EditTransactionViewModel(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    categoriesViewModel: CategoriesViewModel,
    sourcePreferences: SourcePreferences,
    walletRepository: WalletRepository,
    private val updateWidgetsUseCase: UpdateWidgetsUseCase,
    private val application: Application
) : BaseTransactionViewModel<EditTransactionState, BaseTransactionEvent>(
    categoriesViewModel,
    sourcePreferences,
    walletRepository,
) {

    override val _state = MutableStateFlow(
        EditTransactionState()
    )

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
        loadSources()
    }

    // Override loadWallets to update our local wallets state
    override fun loadWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletsList = walletRepository.getAllWallets()
                _wallets.value = walletsList
                Timber.d("ТРАНЗАКЦИЯ: Загружено %d кошельков", walletsList.size)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
                _wallets.value = emptyList()
            }
        }
    }

    override fun loadInitialData() {
        // Подпишемся на изменения категорий из CategoriesViewModel
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { expenseCategories ->
                _state.update { it.copy(expenseCategories = expenseCategories) }
            }
        }
        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { incomeCategories ->
                _state.update { it.copy(incomeCategories = incomeCategories) }
            }
        }
        _state.update { it.copy(availableCategoryIcons = availableCategoryIcons) }
    }

    fun loadTransaction(id: String) {
        Timber.d("ТРАНЗАКЦИЯ: Внутри loadTransaction, ID=%s", id)
        
        viewModelScope.launch {
            try {
                Timber.d("ТРАНЗАКЦИЯ: Вызов getTransactionByIdUseCase для ID=%s", id)
                val result = getTransactionByIdUseCase(id)
                Timber.d("ТРАНЗАКЦИЯ: Результат getTransactionByIdUseCase: %s", result)
                
                if (result is DomainResult.Success) {
                    val transaction = result.data
                    Timber.d("ТРАНЗАКЦИЯ: Успешно получена транзакция: сумма=%s, категория=%s", transaction.amount, transaction.category)
                    
                    // Устанавливаем режим редактирования и саму транзакцию
                    _state.update { it.copy(
                        transactionToEdit = transaction,
                        editMode = true
                    )}

                    Timber.d(
                        "ТРАНЗАКЦИЯ: После обновления state: transactionToEdit=%s, editMode=%b",
                        _state.value.transactionToEdit?.id,
                        _state.value.editMode
                    )
                } else if (result is DomainResult.Error) {
                    Timber.e("ТРАНЗАКЦИЯ: Ошибка в useCase: %s", result.exception.message)
                    _state.update { it.copy(
                        error = result.exception.message,
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Исключение в loadTransaction: %s", e.message)
                _state.update { it.copy(
                    error = e.message ?: "Неизвестная ошибка",
                    isLoading = false
                )}
            }
        }
    }

    private fun validateInput(
        amount: String,
        categoryId: String
    ): Boolean {
        val validationBuilder = ValidationBuilder()
        
        // Reset errors
        _state.update {
            it.copy(
                amountError = false,
                categoryError = false,
                sourceError = false
            )
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
                    Timber.d("ТРАНЗАКЦИЯ: Ошибка валидации - сумма меньше или равна нулю: %f", amountValue)
                }
            } catch (e: Exception) {
                validationBuilder.addAmountError()
                Timber.e("ТРАНЗАКЦИЯ: Ошибка валидации при парсинге суммы: %s", e.message)
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
                amountError = validationResult.hasAmountError,
                categoryError = validationResult.hasCategoryError,
                sourceError = validationResult.hasSourceError
            )
        }

        Timber.d(
            "ТРАНЗАКЦИЯ: Результат валидации: isValid=%b, hasAmountError=%b, hasCategoryError=%b, hasSourceError=%b",
            validationResult.isValid,
            validationResult.hasAmountError,
            validationResult.hasCategoryError,
            validationResult.hasSourceError
        )
                
        return validationResult.isValid
    }

    fun submit() {
        viewModelScope.launch {
            val currentState = _state.value

            Timber.d(
                "ТРАНЗАКЦИЯ: Начало сохранения изменений, isExpense=%b, category=%s, selectedIncomeCategory=%s",
                currentState.isExpense,
                currentState.category,
                currentState.selectedIncomeCategory
            )
            
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
                    Timber.d("ТРАНЗАКЦИЯ: Установлена категория из selectedCategory: %s", categoryToUse)
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
                Timber.d("ТРАНЗАКЦИЯ: Обновление транзакции начато: %s", transaction.id)
                
                // Сохраняем исходную транзакцию для сравнения
                val originalTransaction = currentState.transactionToEdit
                
                // Обновляем транзакцию через useCase
                val result = updateTransactionUseCase(transaction)
                
                if (result is DomainResult.Success) {
                    // Обновляем балансы кошельков, если это доход и выбраны кошельки
                    if (!transaction.isExpense && transaction.walletIds != null && transaction.walletIds.isNotEmpty()) {
                        updateWalletsBalance(transaction.walletIds, transaction.amount, originalTransaction)
                    }
                    
                    // Показываем успешное обновление
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                    updateWidgetsUseCase(application.applicationContext)
                    Timber.d("ТРАНЗАКЦИЯ: Успешно обновлена, ID=%s", transaction.id)
                } else if (result is DomainResult.Error) {
                    Timber.e(result.exception, "ТРАНЗАКЦИЯ: Ошибка при обновлении: %s", result.exception.message)
                    _state.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Ошибка при обновлении транзакции: %s", e.message)
                // Показываем ошибку и снимаем флаг загрузки
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка при обновлении транзакции: %s"
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
        Timber.d("ТРАНЗАКЦИЯ: Начало loadTransactionForEdit: %s", transaction)
        // Загружаем категории и источники для актуальности
        loadInitialData()
        loadSources()
        // Форматируем сумму как строку без знака минус
        val formattedAmount = transaction.amount.abs().amount.toString()
        Timber.d("ТРАНЗАКЦИЯ: Форматированная сумма: %s (исходная: %s)", formattedAmount, transaction.amount)

        // Определяем какую категорию установить в зависимости от типа транзакции
        val selectedExpenseCategory = if (transaction.isExpense) transaction.category else ""
        val selectedIncomeCategory = if (!transaction.isExpense) transaction.category else ""
        
        // Если это доход (не расход), устанавливаем настройки кошелька
        val addToWallet = !transaction.isExpense // For income transactions, enable wallets

        Timber.d("ТРАНЗАКЦИЯ: selectedExpenseCategory=%s, selectedIncomeCategory=%s", selectedExpenseCategory, selectedIncomeCategory)
        Timber.d("ТРАНЗАКЦИЯ: установка кошельков: addToWallet=%b", addToWallet)

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

        Timber.d(
            "ТРАНЗАКЦИЯ: После loadTransactionForEdit: сумма=%s, дата=%s, editMode=%b, source=%s, category=%s, isExpense=%b, selectedExpenseCategory=%s, selectedIncomeCategory=%s",
            _state.value.amount,
            _state.value.selectedDate,
            _state.value.editMode,
            _state.value.source,
            _state.value.category,
            _state.value.isExpense,
            _state.value.selectedExpenseCategory,
            _state.value.selectedIncomeCategory
        )
    }

    // Загрузка кошельков, связанных с транзакцией
    private fun loadTransactionWallets(transactionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем список кошельков, связанных с транзакцией
                val linkedWallets = walletRepository.getWalletsForTransaction(transactionId)
                
                if (linkedWallets.isNotEmpty()) {
                    val walletIds = linkedWallets.map { it.id }
                    Timber.d("ТРАНЗАКЦИЯ: Найдены связанные кошельки: %d", walletIds.size)
                    
                    // Обновляем состояние с выбранными кошельками
                    _state.update {
                        it.copy(
                            selectedWallets = walletIds
                        )
                    }
                } else {
                    Timber.d("ТРАНЗАКЦИЯ: Нет связанных кошельков для транзакции %s", transactionId)
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Ошибка при загрузке связанных кошельков: %s", e.message)
            }
        }
    }

    // Загрузка транзакции для редактирования по ID
    fun loadTransactionForEditById(transactionId: String) {
        Timber.d("ТРАНЗАКЦИЯ: Начало загрузки транзакции ID=%s", transactionId)
        _state.update { it.copy(isLoading = true) }
        // Загружаем категории и источники для актуальности
        loadInitialData()
        loadSources()
        viewModelScope.launch {
            try {
                Timber.d("ТРАНЗАКЦИЯ: Вызов loadTransaction для ID=%s", transactionId)
                loadTransaction(transactionId)
                
                // Добавляем задержку для завершения асинхронной загрузки транзакции
                kotlinx.coroutines.delay(500)
                
                val transaction = _state.value.transactionToEdit
                Timber.d("ТРАНЗАКЦИЯ: Результат загрузки transaction=%s, state.editMode=%b", transaction?.id, _state.value.editMode)
                
                if (transaction != null) {
                    Timber.d("ТРАНЗАКЦИЯ: Загружена, id=%s, сумма=%s, категория=%s", transaction.id, transaction.amount, transaction.category)
                    loadTransactionForEdit(transaction)
                    // Обязательно отключаем индикатор загрузки после успешной загрузки
                    _state.update { it.copy(isLoading = false) }
                    Timber.d("ТРАНЗАКЦИЯ: После loadTransactionForEdit, editMode=%b, сумма=%s", _state.value.editMode, _state.value.amount)
                } else {
                    Timber.e("ТРАНЗАКЦИЯ: НЕ НАЙДЕНА с ID=%s", transactionId)
                    _state.update { 
                        it.copy(
                            error = "Транзакция не найдена",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ: Ошибка при загрузке транзакции: %s", e.message)
                _state.update { 
                    it.copy(
                        error = "Ошибка при загрузке транзакции: %s",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun prepareTransactionForEdit(): Transaction? {
        val currentState = _state.value

        Timber.d(
            "ТРАНЗАКЦИЯ: Подготовка транзакции к редактированию: category=%s, isExpense=%b, selectedIncomeCategory=%s, selectedExpenseCategory=%s",
            currentState.category,
            currentState.isExpense,
            currentState.selectedIncomeCategory,
            currentState.selectedExpenseCategory
        )
        
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
                Timber.d("ТРАНЗАКЦИЯ: Использую категорию из selectedCategory: %s", categoryToUse)
            }
        }
        
        // Convert amount to double
        val amount = currentState.amount.replace(" ", "").replace(",", ".").toDoubleOrNull() 
        if (amount == null || amount <= 0) {
            // Display validation error
            _state.update { it.copy(amountError = true) }
            Timber.e("ТРАНЗАКЦИЯ: Ошибка - некорректная сумма: %s", currentState.amount)
            return null
        }
        
        val finalAmount = if (currentState.isExpense) -amount else amount
        val sourceToUse = if (currentState.source.isBlank()) currentState.transactionToEdit?.source ?: "" else currentState.source
        val sourceColorToUse = if (currentState.source.isBlank()) currentState.transactionToEdit?.sourceColor ?: 0 else currentState.sourceColor
        val categoryToUse = currentState.category

        Timber.d(
            "ТРАНЗАКЦИЯ: Готова к обновлению: amount=%s, category=%s, source=%s",
            finalAmount,
            categoryToUse,
            sourceToUse
        )
        
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
                Timber.d("ТРАНЗАКЦИЯ: Выбрана категория расхода: %s", event.category)
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
                Timber.d("ТРАНЗАКЦИЯ: Выбрана категория дохода: %s", event.category)
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
                    currentAddToWallet = _state.value.addToWallet
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
                Timber.d("ТРАНЗАКЦИЯ: Переключение типа транзакции с %b на %b", _state.value.isExpense, !_state.value.isExpense)
                
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

                Timber.d(
                    "ТРАНЗАКЦИЯ: После переключения типа - isExpense=%b, category=%s, transactionId=%s",
                    _state.value.isExpense,
                    _state.value.category,
                    transactionId
                )
            }

            else -> handleBaseEvent(event, context)
        }
    }

    override fun updateCategoryPositions() {
        viewModelScope.launch {
            // Implementation needed
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
        expenseCategories: List<com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory>,
        incomeCategories: List<com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory>,
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
        availableCategoryIcons: List<ImageVector>,
        customCategoryIcon: ImageVector?
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
            selectedIncomeCategory = selectedIncomeCategory,
            availableCategoryIcons = availableCategoryIcons,
            customCategoryIcon = customCategoryIcon
        )
    }

    // Установка ошибки с отключением индикатора загрузки
    fun setError(errorMessage: String) {
        Timber.e("ТРАНЗАКЦИЯ: установка ошибки: %s", errorMessage)
        _state.update { 
            it.copy(
                error = errorMessage,
                isLoading = false
            )
        }
    }
}