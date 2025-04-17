package com.davidbugayov.financeanalyzer.presentation.transaction.add

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.DialogStateTransaction
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.EditingState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.TransactionData
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.ValidationError
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.WalletState
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category

/**
 * ViewModel для экрана добавления транзакции.
 * Расширяет BaseTransactionViewModel и добавляет специфичную логику для добавления новых транзакций.
 */
class AddTransactionViewModel (
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
    override val _state: MutableStateFlow<AddTransactionState> by lazy { 
        MutableStateFlow(AddTransactionState()) 
    }
    override val state: StateFlow<AddTransactionState> by lazy { 
        _state.asStateFlow() 
    }
    
    // Коллбэки для обновления кошельков
    override var onIncomeAddedCallback: ((Money) -> Unit)? = null
    override var onExpenseAddedCallback: ((Money) -> Unit)? = null
    
    /**
     * Устанавливает целевой кошелек для транзакции
     */
    fun setTargetWalletId(walletId: String) {
        _state.update { 
            it.copy(
                transactionData = it.transactionData.copy(targetWalletId = walletId) 
            ) 
        }
        Timber.d("Установлен целевой кошелек: $walletId")
    }
    
    /**
     * Настраивает ViewModel для добавления дохода
     */
    fun setupForIncomeAddition(amount: String, shouldDistribute: Boolean) {
        _state.update {
            it.copy(
                transactionData = it.transactionData.copy(
                    amount = amount,
                    isExpense = false // Доход, а не расход
                ),
                forceExpense = false,
                walletState = it.walletState.copy(addToWallet = !shouldDistribute) 
            )
        }
        Timber.d("Настроен для добавления дохода: amount=$amount, shouldDistribute=$shouldDistribute")
    }
    
    /**
     * Настраивает ViewModel для добавления расхода
     */
    fun setupForExpenseAddition(amount: String, walletCategory: String) {
        _state.update {
            it.copy(
                transactionData = it.transactionData.copy(
                    amount = amount,
                    category = walletCategory,
                    isExpense = true // Расход
                ),
                forceExpense = true,
                walletState = it.walletState.copy(addToWallet = true) 
            )
        }
        Timber.d("Настроен для добавления расхода: amount=$amount, category=$walletCategory")
    }
    
    /**
     * Очищает список выбранных кошельков
     */
    fun clearSelectedWallets() {
        _state.update {
            it.copy(
                walletState = it.walletState.copy(
                    selectedWalletIds = emptyList(),
                    selectedWallets = emptyList()
                )
            )
        }
        Timber.d("Очищен список выбранных кошельков")
    }
    
    /**
     * Выбирает все кошельки без показа диалога
     */
    fun selectAllWalletsWithoutDialog() {
        val allWalletIds = state.value.walletState.wallets.map { it.id }
        _state.update {
            it.copy(
                walletState = it.walletState.copy(
                    selectedWalletIds = allWalletIds,
                    selectedWallets = allWalletIds.mapNotNull { id -> 
                        state.value.walletState.wallets.find { wallet -> wallet.id == id }?.name
                    }
                )
            )
        }
        Timber.d("Выбраны все кошельки без диалога: ${allWalletIds.size} кошельков")
    }

    /**
     * Сбрасывает состояние до значений по умолчанию
     */
    fun resetToDefaultState() {
        _state.value = AddTransactionState(forceExpense = false)
        Timber.d("State reset: forceExpense=${_state.value.forceExpense}")
    }

    /**
     * Сбрасывает поля формы, но сохраняет категории и источники
     */
    fun resetFields() {
        val currentState = _state.value
        _state.update {
            it.copy(
                transactionData = it.transactionData.copy(
                    amount = "",
                    category = "",
                    note = "",
                    isExpense = true
                ),
                dialogStateTransaction = it.dialogStateTransaction.copy(
                    showDatePicker = false,
                    showCategoryPicker = false,
                    showCustomCategoryDialog = false,
                    showCancelConfirmation = false,
                    showSourcePicker = false,
                    showCustomSourceDialog = false,
                    showColorPicker = false,
                    showDeleteCategoryConfirmation = false,
                    showDeleteSourceConfirmation = false
                ),
                validationError = null,
                isSuccess = false
            )
        }
    }

    /**
     * Обновляет категории расходов
     */
    override fun updateExpenseCategories(categories: List<Any>) {
        try {
            val convertedCategories = categories.map { category ->
                if (category is com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem) {
                   CategoryItem(
                        name = category.name,
                        count = category.count,
                        image = Icons.Default.Category,
                        isCustom = category.isCustom
                    )
                } else {
                    category as CategoryItem
                }
            }
            
            if (_state != null) {
                _state.update { it.copy(expenseCategories = convertedCategories) }
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
            val convertedCategories = categories.map { category ->
                if (category is com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem) {
                    CategoryItem(
                        name = category.name,
                        count = category.count,
                        image = Icons.Default.Category,
                        isCustom = category.isCustom
                    )
                } else {
                    category as CategoryItem
                }
            }
            
            if (_state != null) {
                _state.update { it.copy(incomeCategories = convertedCategories) }
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
     * Обработчик событий - ПЕРЕОПРЕДЕЛЕН
     */
    override fun onEvent(event: BaseTransactionEvent) {
        when (event) {
            // === Обработка событий специфичных для AddTransactionViewModel ===
            is BaseTransactionEvent.SubmitAddTransaction -> {
                submitTransaction()
            }
            is BaseTransactionEvent.ToggleAddAnotherOption -> {
                _state.update { it.copy(canAddAnother = !it.canAddAnother) }
            }
            is BaseTransactionEvent.ToggleAddToWallet -> {
                _state.update {
                    it.copy(
                        walletState = it.walletState.copy(addToWallet = event.add)
                    )
                }
                Timber.d("AddToWallet toggled: ${event.add}")
            }
            is BaseTransactionEvent.SelectWallets -> {
                _state.update {
                    it.copy(
                        walletState = it.walletState.copy(
                            selectedWalletIds = event.walletIds,
                            selectedWallets = event.walletIds.mapNotNull { id ->
                                state.value.walletState.wallets.find { wallet -> wallet.id == id }?.name
                            }
                        )
                    )
                }
                Timber.d("Selected wallets: ${event.walletIds}")
            }
            is BaseTransactionEvent.SetCustomSourceName -> {
                 _state.update {
                    it.copy(editingState = it.editingState.copy(sourceName = event.name))
                 }
            }

            // === Обработка базовых событий ===
            is BaseTransactionEvent.SetAmount -> {
                _state.update { it.copy(
                    transactionData = it.transactionData.copy(amount = event.amount),
                    validationError = if (it.validationError is ValidationError.AmountMissing) null else it.validationError
                )}
            }
            is BaseTransactionEvent.SetCategory -> {
                _state.update { it.copy(
                    transactionData = it.transactionData.copy(category = event.category),
                    validationError = if (it.validationError is ValidationError.CategoryMissing) null else it.validationError
                )}
                usedCategories.add(Pair(event.category, _state.value.transactionData.isExpense))
            }
            is BaseTransactionEvent.SetNote -> {
                _state.update { it.copy(transactionData = it.transactionData.copy(note = event.note)) }
            }
            is BaseTransactionEvent.SetDate -> {
                _state.update { it.copy(transactionData = it.transactionData.copy(selectedDate = event.date)) }
            }
            is BaseTransactionEvent.ToggleTransactionType -> {
                val currentIsExpense = _state.value.transactionData.isExpense
                val currentForceExpense = _state.value.forceExpense
                Timber.d("BEFORE TOGGLE: isExpense=$currentIsExpense, forceExpense=$currentForceExpense")
                
                _state.update { it.copy(
                    transactionData = it.transactionData.copy(
                        isExpense = !currentIsExpense,
                        category = ""
                    ),
                    forceExpense = false,
                    validationError = if (it.validationError is ValidationError.CategoryMissing) null else it.validationError
                )}
                
                Timber.d("AFTER TOGGLE: isExpense=${!currentIsExpense}, forceExpense=false")
                Timber.d("Transaction type toggled to: ${if (!currentIsExpense) "Expense" else "Income"}")
            }
            is BaseTransactionEvent.SetSource -> {
                _state.update { it.copy(
                    transactionData = it.transactionData.copy(source = event.source)
                )}
            }
            is BaseTransactionEvent.SetSourceColor -> {
                 _state.update {
                    it.copy(
                        transactionData = it.transactionData.copy(sourceColor = event.color),
                        editingState = it.editingState.copy(sourceColor = event.color)
                    )
                 }
            }
            is BaseTransactionEvent.AddCustomCategory -> {
                viewModelScope.launch {
                    val isExpense = state.value.transactionData.isExpense
                    categoriesViewModel.addCustomCategory(event.name, isExpense)
                    onEvent(BaseTransactionEvent.SetCategory(event.name))
                }
            }
            is BaseTransactionEvent.AddCustomSource -> {
                viewModelScope.launch {
                    val newSource = Source(name = event.name, color = event.color, isCustom = true)
                    Timber.d("Adding custom source: $newSource - Repository logic needed")
                    _state.update {
                        it.copy(sources = it.sources + SourceItem(name = newSource.name, color = newSource.color, isCustom = true))
                    }
                    onEvent(BaseTransactionEvent.SetSource(newSource.name))
                    onEvent(BaseTransactionEvent.SetSourceColor(newSource.color))
                }
            }
            is BaseTransactionEvent.SetCategoryToDelete -> {
                _state.update { it.copy(editingState = it.editingState.copy(categoryToDelete = event.category)) }
            }
            is BaseTransactionEvent.DeleteCategory -> {
                 viewModelScope.launch {
                    val isExpense = state.value.expenseCategories.any { it.name == event.name }
                    categoriesViewModel.removeCategory(event.name, isExpense)
                 }
            }
            is BaseTransactionEvent.SetSourceToDelete -> {
                 _state.update { it.copy(editingState = it.editingState.copy(sourceToDelete = event.source)) }
            }
            is BaseTransactionEvent.DeleteSource -> {
                 Timber.d("Deleting source: ${event.name} - Repository logic needed")
                 _state.update {
                     it.copy(sources = it.sources.filterNot { source -> source.name == event.name })
                 }
            }
            // Dialog visibility toggles
            is BaseTransactionEvent.ShowDatePicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDatePicker = true)) }
            is BaseTransactionEvent.HideDatePicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDatePicker = false)) }
            is BaseTransactionEvent.ShowCategoryPicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCategoryPicker = true)) }
            is BaseTransactionEvent.HideCategoryPicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCategoryPicker = false)) }
            is BaseTransactionEvent.ShowCustomCategoryDialog -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomCategoryDialog = true, showCategoryPicker = false)) }
            is BaseTransactionEvent.HideCustomCategoryDialog -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomCategoryDialog = false)) }
            is BaseTransactionEvent.ShowCancelConfirmation -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCancelConfirmation = true)) }
            is BaseTransactionEvent.HideCancelConfirmation -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCancelConfirmation = false)) }
            is BaseTransactionEvent.ShowSourcePicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showSourcePicker = true)) }
            is BaseTransactionEvent.HideSourcePicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showSourcePicker = false)) }
            is BaseTransactionEvent.ShowCustomSourceDialog -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomSourceDialog = true, showSourcePicker = false)) }
            is BaseTransactionEvent.HideCustomSourceDialog -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showCustomSourceDialog = false)) }
            is BaseTransactionEvent.ShowColorPicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showColorPicker = true)) }
            is BaseTransactionEvent.HideColorPicker -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showColorPicker = false)) }
            is BaseTransactionEvent.ShowDeleteCategoryConfirmation -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteCategoryConfirmation = true, showCategoryPicker = false)) }
            is BaseTransactionEvent.HideDeleteCategoryConfirmation -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteCategoryConfirmation = false)) }
            is BaseTransactionEvent.ShowDeleteSourceConfirmation -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteSourceConfirmation = true, showSourcePicker = false)) }
            is BaseTransactionEvent.HideDeleteSourceConfirmation -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showDeleteSourceConfirmation = false)) }
            is BaseTransactionEvent.ShowWalletSelector -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showWalletSelector = true)) }
            is BaseTransactionEvent.HideWalletSelector -> _state.update { it.copy(dialogStateTransaction = it.dialogStateTransaction.copy(showWalletSelector = false)) }
            is BaseTransactionEvent.HideSuccessDialog -> _state.update { it.copy(isSuccess = false) }
            is BaseTransactionEvent.Reset -> resetFields()
            // Другие события, которые могут встречаться
            else -> {
                Timber.d("Unhandled event in AddTransactionViewModel: $event")
            }
        }
    }
    
    /**
     * Подготавливает и добавляет новую транзакцию
     */
    private fun submitTransaction() {
        if (!validateInput(_state.value)) {
            return
        }
        
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val currentState = _state.value
                val amount = Money.fromString(currentState.transactionData.amount).amount
                val finalAmount = if (currentState.transactionData.isExpense) -amount else amount
                val isTransfer = currentState.transactionData.category == "Переводы"
                
                val newTransaction = Transaction(
                    id = "",
                    amount = Money(finalAmount),
                    date = currentState.transactionData.selectedDate,
                    note = currentState.transactionData.note.trim(),
                    category = currentState.transactionData.category,
                    source = currentState.transactionData.source,
                    isExpense = currentState.transactionData.isExpense,
                    sourceColor = currentState.transactionData.sourceColor,
                    isTransfer = isTransfer
                )
                
                Timber.d("Добавление транзакции: amount=${newTransaction.amount}, isExpense=${newTransaction.isExpense}")
                
                try {
                    val result = addTransactionUseCase(newTransaction)
                    
                    when (result) {
                        is Result.Success<*> -> {
                            Timber.d("Транзакция успешно добавлена: ${result.data}")
                            updateWidget()
                            requestDataRefresh()
                            usedCategories.add(Pair(currentState.transactionData.category, currentState.transactionData.isExpense))
                            updateCategoryPositions()
                            
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    validationError = null
                                )
                            }
                        }
                        is Result.Error -> {
                            val error = result.exception
                            Timber.e("Ошибка при добавлении транзакции: ${error.message}")
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
                    Timber.e(e, "Ошибка при добавлении транзакции")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            validationError = ValidationError.General("Ошибка: ${e.message ?: "Неизвестная ошибка"}"),
                            isSuccess = false
                        )
                    }
                }
            } catch (e: NumberFormatException) { 
                 Timber.e(e, "Ошибка парсинга суммы: ${state.value.transactionData.amount}")
                 _state.update {
                     it.copy(
                         isLoading = false,
                         validationError = ValidationError.AmountMissing,
                         isSuccess = false
                     )
                 }
            } catch (e: Exception) {
                Timber.e(e, "Неожиданная ошибка при добавлении транзакции")
                _state.update {
                    it.copy(
                        isLoading = false,
                        validationError = ValidationError.General("Неожиданная ошибка: ${e.message ?: "Неизвестная ошибка"}"),
                        isSuccess = false
                    )
                }
            }
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
