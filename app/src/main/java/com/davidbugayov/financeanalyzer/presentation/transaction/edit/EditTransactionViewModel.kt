package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.TransactionData
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.ValidationError
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для экрана редактирования транзакции.
 * Наследуется от базового класса и реализует специфичную логику редактирования.
 */
class EditTransactionViewModel(
    application: Application,
    addTransactionUseCase: AddTransactionUseCase,
    updateTransactionUseCase: UpdateTransactionUseCase,
    categoriesViewModel: CategoriesViewModel,
    walletRepository: WalletRepository,
    override val txRepository: TransactionRepository
) : BaseTransactionViewModel(
    application,
    addTransactionUseCase,
    updateTransactionUseCase,
    categoriesViewModel,
    walletRepository,
    txRepository
) {

    // Состояние ViewModel с приведением к специфичному типу
    override val _state: MutableStateFlow<EditTransactionState> by lazy { 
        MutableStateFlow(EditTransactionState()) 
    }
    override val state by lazy { 
        _state.asStateFlow() 
    }

    /**
     * Находит цвет источника по его имени
     */
    private fun findSourceColor(sourceName: String): Int {
        // Ищем источник в списке источников
        return _state.value.sources.find { it.name == sourceName }?.color ?: 0
    }

    /**
     * Публичный метод для загрузки транзакции по ID для редактирования
     */
    fun loadTransaction(transactionId: String) {
        loadTransactionForEdit(transactionId)
    }

    /**
     * Обновляет категории расходов
     */
    override fun updateExpenseCategories(categories: List<Any>) {
        try {
            if (_state != null) {
                _state.update { it.copy(expenseCategories = categories.filterIsInstance<CategoryItem>()) }
            } else {
                Timber.e("_state is null in updateExpenseCategories")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating expense categories")
        }
    }

    /**
     * Обновляет категории доходов
     */
    override fun updateIncomeCategories(categories: List<Any>) {
        try {
            if (_state != null) {
                _state.update { it.copy(incomeCategories = categories.filterIsInstance<CategoryItem>()) }
            } else {
                Timber.e("_state is null in updateIncomeCategories")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating income categories")
        }
    }

    /**
     * Устанавливает ошибку валидации
     */
    override fun setValidationError(error: ValidationError) {
        _state.update { it.copy(validationError = error) }
    }

    /**
     * Загружает данные транзакции для редактирования
     */
    fun loadTransactionForEdit(transactionId: String) {
        viewModelScope.launch {
            try {
                val transaction = txRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    // Логируем начало загрузки
                    Timber.d("loadTransactionForEdit: загружаем транзакцию с ID=$transactionId, isExpense=${transaction.isExpense}")
                    
                    // Преобразуем сумму для отображения
                    val amount = if (transaction.amount.isNegative()) {
                        transaction.amount.abs().toString()
                    } else {
                        transaction.amount.toString()
                    }
                    
                    // Создаем объект с данными транзакции
                    val transactionData = TransactionData(
                        amount = amount,
                        category = transaction.category ?: "",
                        note = transaction.note ?: "",
                        selectedDate = transaction.date,
                        isExpense = transaction.isExpense,
                        source = transaction.source ?: "",
                        sourceColor = transaction.sourceColor
                    )
                    
                    // Обновляем состояние
                    _state.update { 
                        it.copy(
                            transactionData = transactionData,
                            originalData = transactionData, // Сохраняем оригинальные данные
                            editMode = true,
                            transactionToEdit = transaction,
                            forceExpense = transaction.isExpense, // Устанавливаем forceExpense в соответствии с типом транзакции
                            validationError = null,
                            hasUnsavedChanges = false
                        )
                    }
                    
                    // Логируем завершение загрузки
                    Timber.d("Транзакция загружена для редактирования: $transactionId, тип: isExpense=${_state.value.transactionData.isExpense}, forceExpense=${_state.value.forceExpense}")
                } else {
                    Timber.e("Транзакция не найдена: $transactionId")
                    _state.update { 
                        it.copy(validationError = ValidationError.General("Транзакция не найдена"))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке транзакции: $transactionId")
                _state.update { 
                    it.copy(validationError = ValidationError.General("Ошибка: ${e.message ?: "Неизвестная ошибка"}"))
                }
            }
        }
    }

    /**
     * Обработчик событий
     */
    override fun onEvent(event: BaseTransactionEvent) {
        when (event) {
            // Обработка специфичных событий редактирования
            is BaseTransactionEvent.SubmitChanges -> {
                submitChanges()
            }
            is BaseTransactionEvent.CancelEditing -> {
                navigateBackCallback?.invoke()
            }
            is BaseTransactionEvent.RevertChanges -> {
                revertChanges()
            }
            is BaseTransactionEvent.HideSuccessDialog -> {
                _state.update { it.copy(isSuccess = false) }
            }
            is BaseTransactionEvent.SetHasUnsavedChanges -> {
                _state.update { it.copy(hasUnsavedChanges = event.hasChanges) }
            }
            is BaseTransactionEvent.SetOriginalData -> {
                _state.update { it.copy(originalData = event.data) }
            }
            is BaseTransactionEvent.SetCustomSourceName -> {
                _state.update {
                    it.copy(editingState = it.editingState.copy(sourceName = event.name))
                }
            }
            is BaseTransactionEvent.ToggleAddAnotherOption -> {
                // EditTransactionViewModel не поддерживает добавление еще одной транзакции
                Timber.d("ToggleAddAnotherOption не поддерживается в режиме редактирования")
            }
            
            // Обработка изменений данных транзакции
            is BaseTransactionEvent.SetAmount -> {
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(amount = event.amount),
                        validationError = if (it.validationError is ValidationError.AmountMissing) null else it.validationError,
                        hasUnsavedChanges = true
                    )
                }
            }
            
            is BaseTransactionEvent.SetCategory -> {
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(category = event.category),
                        validationError = if (it.validationError is ValidationError.CategoryMissing) null else it.validationError,
                        hasUnsavedChanges = true
                    )
                }
                
                // Добавляем категорию в список использованных
                usedCategories.add(Pair(event.category, _state.value.transactionData.isExpense))
            }
            
            is BaseTransactionEvent.SetNote -> {
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(note = event.note),
                        hasUnsavedChanges = true
                    )
                }
            }
            
            is BaseTransactionEvent.SetDate -> {
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(selectedDate = event.date),
                        hasUnsavedChanges = true
                    )
                }
            }
            
            is BaseTransactionEvent.ToggleTransactionType -> {
                val currentIsExpense = _state.value.transactionData.isExpense
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(isExpense = !currentIsExpense),
                        hasUnsavedChanges = true
                    )
                }
            }
            
            // Обработка источников
            is BaseTransactionEvent.SetSource -> {
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(
                            source = event.source,
                            sourceColor = findSourceColor(event.source)
                        ),
                        hasUnsavedChanges = true
                    )
                }
            }
            
            is BaseTransactionEvent.SetSourceColor -> {
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(sourceColor = event.color),
                        hasUnsavedChanges = true
                    )
                }
            }
            
            is BaseTransactionEvent.SetCustomSource -> {
                _state.update { it.copy(editingState = it.editingState.copy(customSource = event.source)) }
            }
            
            is BaseTransactionEvent.AddCustomSource -> {
                // Логика добавления источника
                Timber.d("Добавление нового источника: ${event.name} с цветом ${event.color}")
                // Тут будет сохранение нового источника в репозиторий
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(
                            source = event.name,
                            sourceColor = event.color
                        ),
                        editingState = it.editingState.copy(customSource = ""),
                        hasUnsavedChanges = true
                    )
                }
            }
            
            // Управление категориями
            is BaseTransactionEvent.SetCustomCategory -> {
                _state.update { it.copy(editingState = it.editingState.copy(customCategory = event.category)) }
            }
            
            is BaseTransactionEvent.AddCustomCategory -> {
                val currentState = _state.value
                val isExpense = currentState.transactionData.isExpense
                val newCategory = event.name
                
                viewModelScope.launch {
                    if (isExpense) {
                        categoriesViewModel.addExpenseCategory(newCategory)
                    } else {
                        categoriesViewModel.addIncomeCategory(newCategory)
                    }
                    
                    _state.update { 
                        it.copy(
                            transactionData = it.transactionData.copy(category = newCategory),
                            editingState = it.editingState.copy(customCategory = ""),
                            hasUnsavedChanges = true
                        )
                    }
                }
            }
            
            is BaseTransactionEvent.DeleteCategory -> {
                val currentState = _state.value
                val isExpense = currentState.transactionData.isExpense
                
                viewModelScope.launch {
                    if (isExpense) {
                        categoriesViewModel.removeExpenseCategory(event.name)
                    } else {
                        categoriesViewModel.removeIncomeCategory(event.name)
                    }
                }
            }
            
            is BaseTransactionEvent.SetCategoryToDelete -> {
                _state.update { 
                    it.copy(
                        editingState = it.editingState.copy(categoryToDelete = event.category)
                    )
                }
            }
            
            is BaseTransactionEvent.ShowDeleteCategoryConfirmation -> {
                _state.update { 
                    it.copy(
                        dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteCategoryConfirmation = true)
                    )
                }
            }
            
            is BaseTransactionEvent.HideDeleteCategoryConfirmation -> {
                _state.update { 
                    it.copy(
                        dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteCategoryConfirmation = false)
                    )
                }
            }
            
            is BaseTransactionEvent.SetSourceToDelete -> {
                _state.update { 
                    it.copy(
                        editingState = it.editingState.copy(sourceToDelete = event.source)
                    )
                }
            }
            
            is BaseTransactionEvent.DeleteSource -> {
                // Логика удаления источника
                Timber.d("Удаление источника: ${event.name}")
            }
            
            is BaseTransactionEvent.ShowDeleteSourceConfirmation -> {
                _state.update { 
                    it.copy(
                        dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteSourceConfirmation = true)
                    )
                }
            }
            
            is BaseTransactionEvent.HideDeleteSourceConfirmation -> {
                _state.update { 
                    it.copy(
                        dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteSourceConfirmation = false)
                    )
                }
            }
            
            // Управление диалогами
            is BaseTransactionEvent.ShowDatePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDatePicker = true)) }
            }
            
            is BaseTransactionEvent.HideDatePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDatePicker = false)) }
            }
            
            is BaseTransactionEvent.ShowCategoryPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCategoryPicker = true)) }
            }
            
            is BaseTransactionEvent.HideCategoryPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCategoryPicker = false)) }
            }
            
            is BaseTransactionEvent.ShowSourcePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showSourcePicker = true)) }
            }
            
            is BaseTransactionEvent.HideSourcePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showSourcePicker = false)) }
            }
            
            is BaseTransactionEvent.ShowCustomCategoryDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomCategoryDialog = true)) }
            }
            
            is BaseTransactionEvent.HideCustomCategoryDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomCategoryDialog = false)) }
            }
            
            is BaseTransactionEvent.ShowCustomSourceDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomSourceDialog = true)) }
            }
            
            is BaseTransactionEvent.HideCustomSourceDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomSourceDialog = false)) }
            }
            
            is BaseTransactionEvent.ShowColorPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showColorPicker = true)) }
            }
            
            is BaseTransactionEvent.HideColorPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showColorPicker = false)) }
            }
            
            is BaseTransactionEvent.ShowCancelConfirmation -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCancelConfirmation = true)) }
            }
            
            is BaseTransactionEvent.HideCancelConfirmation -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCancelConfirmation = false)) }
            }
            
            // Управление кошельками
            is BaseTransactionEvent.ShowWalletSelector -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showWalletSelector = true)) }
            }
            
            is BaseTransactionEvent.HideWalletSelector -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showWalletSelector = false)) }
            }
            
            is BaseTransactionEvent.SetTargetWalletId -> {
                _state.update { it.copy(walletState = it.walletState.copy(targetWalletId = event.walletId)) }
            }
            
            is BaseTransactionEvent.ToggleAddToWallet -> {
                _state.update { it.copy(walletState = it.walletState.copy(addToWallet = event.add)) }
            }
            
            is BaseTransactionEvent.ToggleWalletSelection -> {
                val currentState = _state.value
                val currentSelectedWallets = currentState.walletState.selectedWalletIds.toMutableList()
                
                if (currentSelectedWallets.contains(event.walletId)) {
                    currentSelectedWallets.remove(event.walletId)
                } else {
                    currentSelectedWallets.add(event.walletId)
                }
                
                _state.update { it.copy(walletState = it.walletState.copy(selectedWalletIds = currentSelectedWallets)) }
            }
            
            is BaseTransactionEvent.SelectWallet -> {
                val currentState = _state.value
                val currentSelectedWallets = currentState.walletState.selectedWalletIds.toMutableList()
                
                if (event.selected) {
                    if (!currentSelectedWallets.contains(event.walletId)) {
                        currentSelectedWallets.add(event.walletId)
                    }
                } else {
                    currentSelectedWallets.remove(event.walletId)
                }
                
                _state.update { it.copy(walletState = it.walletState.copy(selectedWalletIds = currentSelectedWallets)) }
            }
            
            is BaseTransactionEvent.SelectWallets -> {
                _state.update { it.copy(walletState = it.walletState.copy(selectedWalletIds = event.walletIds)) }
            }
            
            // Прочие события
            is BaseTransactionEvent.ClearError -> {
                _state.update { it.copy(validationError = null) }
            }
            
            is BaseTransactionEvent.ForceSetExpenseType -> {
                _state.update { it.copy(transactionData = it.transactionData.copy(isExpense = true)) }
            }
            
            is BaseTransactionEvent.ForceSetIncomeType -> {
                _state.update { it.copy(transactionData = it.transactionData.copy(isExpense = false)) }
            }
            
            is BaseTransactionEvent.Cancel -> {
                navigateBackCallback?.invoke()
            }
            
            is BaseTransactionEvent.Reset -> {
                resetFields()
            }
            
            is BaseTransactionEvent.Load -> {
                // Уже обрабатывается в loadTransaction
            }
            
            is BaseTransactionEvent.SaveTransaction -> {
                submitChanges()
            }
            
            is BaseTransactionEvent.SubmitAddTransaction -> {
                // Не используется в редактировании
            }
            
            is BaseTransactionEvent.ToggleExpense -> {
                _state.update { 
                    it.copy(
                        transactionData = it.transactionData.copy(isExpense = event.isExpense),
                        hasUnsavedChanges = true
                    )
                }
            }
            
            // Управление остальными диалогами и видами
            is BaseTransactionEvent.ShowDatePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDatePicker = true)) }
            }
            is BaseTransactionEvent.HideDatePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDatePicker = false)) }
            }
            is BaseTransactionEvent.ShowCategoryPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCategoryPicker = true)) }
            }
            is BaseTransactionEvent.HideCategoryPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCategoryPicker = false)) }
            }
            is BaseTransactionEvent.ShowCancelConfirmation -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCancelConfirmation = true)) }
            }
            is BaseTransactionEvent.HideCancelConfirmation -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCancelConfirmation = false)) }
            }
            is BaseTransactionEvent.ShowWalletSelector -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showWalletSelector = true)) }
            }
            is BaseTransactionEvent.HideWalletSelector -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showWalletSelector = false)) }
            }
            is BaseTransactionEvent.ShowSourcePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showSourcePicker = true)) }
            }
            is BaseTransactionEvent.HideSourcePicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showSourcePicker = false)) }
            }
            is BaseTransactionEvent.ShowCustomSourceDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomSourceDialog = true)) }
            }
            is BaseTransactionEvent.HideCustomSourceDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomSourceDialog = false)) }
            }
            is BaseTransactionEvent.ShowCustomCategoryDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomCategoryDialog = true)) }
            }
            is BaseTransactionEvent.HideCustomCategoryDialog -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomCategoryDialog = false)) }
            }
            is BaseTransactionEvent.ShowColorPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showColorPicker = true)) }
            }
            is BaseTransactionEvent.HideColorPicker -> {
                _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showColorPicker = false)) }
            }
            // Сброс состояния
            is BaseTransactionEvent.Reset -> {
                resetFields()
            }
            // Пустые события для EditTransactionViewModel
            is BaseTransactionEvent.SubmitAddTransaction,
            is BaseTransactionEvent.Load,
            is BaseTransactionEvent.Cancel,
            is BaseTransactionEvent.ClearError,
            is BaseTransactionEvent.ForceSetIncomeType,
            is BaseTransactionEvent.ForceSetExpenseType,
            is BaseTransactionEvent.ToggleExpense,
            is BaseTransactionEvent.ToggleWalletSelection,
            is BaseTransactionEvent.SetTargetWalletId,
            is BaseTransactionEvent.SelectWallet,
            is BaseTransactionEvent.SaveTransaction -> {
                // Игнорируем эти события в режиме редактирования
                Timber.d("Игнорируем событие $event в режиме редактирования")
            }
            else -> {
                Timber.d("Неизвестное событие в EditTransactionViewModel: $event")
            }
        }
    }

    /**
     * Отменяет все изменения и восстанавливает оригинальные данные
     */
    private fun revertChanges() {
        val originalData = _state.value.originalData
        if (originalData != null) {
            _state.update { 
                it.copy(
                    transactionData = originalData,
                    hasUnsavedChanges = false
                )
            }
            Timber.d("Изменения отменены, восстановлены оригинальные данные")
        }
    }

    /**
     * Сохраняет изменения транзакции
     */
    private fun submitChanges() {
        if (!validateInput(_state.value)) { 
            return
        }

        // Показываем индикатор загрузки
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val currentState = _state.value
                val transactionToEdit = currentState.transactionToEdit ?: return@launch

                // Обновляем транзакцию с новыми данными
                val amount = Money.fromString(currentState.transactionData.amount).amount
                val finalAmount = if (currentState.transactionData.isExpense) -amount else amount
                val isTransfer = currentState.transactionData.category == "Переводы" // TODO: Константа

                val updatedTransaction = transactionToEdit.copy(
                    amount = Money(finalAmount),
                    date = currentState.transactionData.selectedDate,
                    note = currentState.transactionData.note.trim(),
                    category = currentState.transactionData.category,
                    source = currentState.transactionData.source,
                    isExpense = currentState.transactionData.isExpense,
                    sourceColor = currentState.transactionData.sourceColor,
                    isTransfer = isTransfer
                )

                Timber.d("Обновление транзакции: id=${updatedTransaction.id}, amount=${updatedTransaction.amount}, isExpense=${updatedTransaction.isExpense}")

                try {
                    val result = updateTransactionUseCase(updatedTransaction)
                    
                    when (result) {
                        is Result.Success -> {
                            Timber.d("Транзакция успешно обновлена: ${updatedTransaction.id}")
                            updateWidget()
                            requestDataRefresh()
                            usedCategories.add(Pair(currentState.transactionData.category, currentState.transactionData.isExpense))
                            updateCategoryPositions()

                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    validationError = null,
                                    hasUnsavedChanges = false
                                )
                            }
                        }
                        is Result.Error -> {
                            val error = result.exception
                            Timber.e("Ошибка при обновлении транзакции: ${error.message}")
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    validationError = ValidationError.General("Ошибка: ${error.message ?: "Неизвестная ошибка"}"),
                                    isSuccess = false
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при обновлении транзакции: ${updatedTransaction.id}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            validationError = ValidationError.General("Ошибка: ${e.message ?: "Неизвестная ошибка"}"),
                            isSuccess = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении транзакции")
                _state.update {
                    it.copy(
                        isLoading = false,
                        validationError = ValidationError.General("Ошибка: ${e.message ?: "Неизвестная ошибка"}"),
                        isSuccess = false
                    )
                }
            }
        }
    }

    private fun resetFields() {
        _state.update { 
            it.copy(
                transactionData = TransactionData(),
                validationError = null,
                isSuccess = false,
                hasUnsavedChanges = false
            )
        }
    }

    /**
     * Метод для получения репозитория транзакций.
     * Переопределен для BudgetScreen
     */
    override fun getTransactionRepository(): TransactionRepository {
        return txRepository
    }
} 