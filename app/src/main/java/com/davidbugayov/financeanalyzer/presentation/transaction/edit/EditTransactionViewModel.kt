package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.addCustomSource
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.deleteCustomSource
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import timber.log.Timber
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz

class EditTransactionViewModel(
    application: Application,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    override val categoriesViewModel: CategoriesViewModel,
    override val sourcePreferences: SourcePreferences,
    private val walletRepository: WalletRepository
) : BaseTransactionViewModel<EditTransactionState, BaseTransactionEvent>() {

    private val _wallets = kotlinx.coroutines.flow.MutableStateFlow<List<Wallet>>(emptyList())
    override val wallets: List<Wallet>
        get() = _wallets.value

    protected override val _state = MutableStateFlow(EditTransactionState())

    init {
        // Загружаем категории
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { categories ->
                _state.update { it.copy(expenseCategories = categories) }
            }
        }
        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { categories ->
                _state.update { it.copy(incomeCategories = categories) }
            }
        }
        // Загружаем источники
        viewModelScope.launch {
            val sources = getInitialSources(sourcePreferences)
            _state.update { it.copy(sources = sources) }
        }
        // Загружаем кошельки
        viewModelScope.launch {
            try {
                val walletsList = walletRepository.getAllWallets()
                _wallets.value = walletsList
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
            }
        }
    }

    override fun onEvent(event: BaseTransactionEvent, context: android.content.Context) {
        when (event) {
            is BaseTransactionEvent.LoadTransaction -> loadTransactionForEdit(event.id)
            is BaseTransactionEvent.SubmitEdit -> submitTransaction(context)
            else -> handleBaseEvent(event, context)
        }
    }

    private fun handleBaseEvent(event: BaseTransactionEvent, context: android.content.Context) {
        when (event) {
            is BaseTransactionEvent.LoadTransaction -> loadTransactionForEdit(event.id)
            is BaseTransactionEvent.SubmitEdit -> submitTransaction(context)
            is BaseTransactionEvent.SetAmount -> _state.update { it.copy(amount = event.amount) }
            is BaseTransactionEvent.SetTitle -> _state.update { it.copy(title = event.title) }
            is BaseTransactionEvent.SetCategory -> _state.update { it.copy(category = event.category, showCategoryPicker = false) }
            is BaseTransactionEvent.SetNote -> _state.update { it.copy(note = event.note) }
            is BaseTransactionEvent.SetDate -> _state.update { it.copy(selectedDate = event.date, showDatePicker = false) }
            is BaseTransactionEvent.ToggleTransactionType -> {
                val newIsExpense = !_state.value.isExpense
                val previousSource = _state.value.source
                val previousSourceColor = _state.value.sourceColor
                _state.update { it.copy(isExpense = newIsExpense, category = "") }
                viewModelScope.launch {
                    kotlinx.coroutines.delay(50)
                    _state.update { it.copy(source = previousSource, sourceColor = previousSourceColor) }
                }
            }
            is BaseTransactionEvent.ShowDatePicker -> _state.update { it.copy(showDatePicker = true) }
            is BaseTransactionEvent.HideDatePicker -> _state.update { it.copy(showDatePicker = false) }
            is BaseTransactionEvent.ShowCategoryPicker -> _state.update { it.copy(showCategoryPicker = true) }
            is BaseTransactionEvent.HideCategoryPicker -> _state.update { it.copy(showCategoryPicker = false) }
            is BaseTransactionEvent.ShowCustomCategoryDialog -> _state.update { it.copy(showCustomCategoryDialog = true) }
            is BaseTransactionEvent.HideCustomCategoryDialog -> _state.update { it.copy(showCustomCategoryDialog = false, customCategory = "") }
            is BaseTransactionEvent.ShowCancelConfirmation -> _state.update { it.copy(showCancelConfirmation = true) }
            is BaseTransactionEvent.HideCancelConfirmation -> _state.update { it.copy(showCancelConfirmation = false) }
            is BaseTransactionEvent.ClearError -> _state.update { it.copy(error = null) }
            is BaseTransactionEvent.HideSuccessDialog -> _state.update { it.copy(isSuccess = false) }
            is BaseTransactionEvent.ShowSourcePicker -> _state.update { it.copy(showSourcePicker = true) }
            is BaseTransactionEvent.HideSourcePicker -> _state.update { it.copy(showSourcePicker = false) }
            is BaseTransactionEvent.ShowCustomSourceDialog -> _state.update { it.copy(showCustomSourceDialog = true) }
            is BaseTransactionEvent.HideCustomSourceDialog -> _state.update { it.copy(showCustomSourceDialog = false, customSource = "") }
            is BaseTransactionEvent.ShowColorPicker -> _state.update { it.copy(showColorPicker = true) }
            is BaseTransactionEvent.HideColorPicker -> _state.update { it.copy(showColorPicker = false) }
            is BaseTransactionEvent.SetSource -> {
                val selectedSource = _state.value.sources.find { it.name == event.source }
                _state.update { it.copy(source = event.source, sourceColor = selectedSource?.color ?: it.sourceColor) }
            }
            is BaseTransactionEvent.SetCustomSource -> _state.update { it.copy(customSource = event.source) }
            is BaseTransactionEvent.AddCustomSource -> {
                try {
                    if (event.source.isBlank()) {
                        _state.update { it.copy(error = "Название источника не может быть пустым") }
                        return
                    }
                    val newSource = Source(name = event.source, color = event.color, isCustom = true)
                    val updatedSources = addCustomSource(sourcePreferences, _state.value.sources, newSource)
                    _state.update {
                        it.copy(
                            sources = updatedSources,
                            source = event.source,
                            sourceColor = event.color,
                            showSourcePicker = false,
                            showCustomSourceDialog = false,
                            customSource = ""
                        )
                    }
                    Timber.d("Custom source added directly: ${newSource.name} with color ${newSource.color}")
                } catch (e: Exception) {
                    Timber.e(e, "Error adding custom source: ${e.message}")
                    _state.update { it.copy(error = "Ошибка при добавлении источника: ${e.message}") }
                }
            }
            is BaseTransactionEvent.SetSourceColor -> _state.update { it.copy(sourceColor = event.color) }
            is BaseTransactionEvent.ShowWalletSelector -> _state.update { it.copy(showWalletSelector = true) }
            is BaseTransactionEvent.HideWalletSelector -> _state.update { it.copy(showWalletSelector = false) }
            is BaseTransactionEvent.ToggleAddToWallet -> _state.update { it.copy(addToWallet = !_state.value.addToWallet) }
            is BaseTransactionEvent.SelectWallet -> {
                val updated = if (event.selected) {
                    _state.value.selectedWallets + event.walletId
                } else {
                    _state.value.selectedWallets - event.walletId
                }
                _state.update { it.copy(selectedWallets = updated) }
            }
            is BaseTransactionEvent.SelectWallets -> _state.update { it.copy(selectedWallets = event.walletIds) }
            else -> {}
        }
    }

    override fun resetFields() {
        // TODO: реализовать сброс полей для редактирования
    }

    override fun updateCategoryPositions() {
        // TODO: реализовать обновление позиций категорий
    }

    override fun submitTransaction(context: android.content.Context) {
        // Проверка обязательных полей
        if (validateFields()) {
            // Показываем индикатор загрузки
            _state.update { it.copy(isLoading = true) }
            
            // Получаем транзакцию для обновления
            val transactionToUpdate = prepareTransactionForEdit()
            
            // Обновляем транзакцию и виджет
            if (transactionToUpdate != null) {
                viewModelScope.launch {
                    try {
                        updateTransactionUseCase(transactionToUpdate)
                        Timber.d("Транзакция успешно обновлена: ${transactionToUpdate.id}")
                        
                        // Обновляем кошельки, если нужно
                        val currentState = _state.value
                        if (currentState.addToWallet && currentState.selectedWallets.isNotEmpty()) {
                            updateWalletsAfterTransaction(
                                walletRepository = walletRepository,
                                walletIds = currentState.selectedWallets,
                                totalAmount = transactionToUpdate.amount,
                                isExpense = currentState.isExpense
                            )
                        }
                        // Обновляем виджет
                        updateWidget(context)
                        
                        // Показываем сообщение об успехе
                        _state.update { 
                            it.copy(
                                isSuccess = true,
                                error = null,
                                isLoading = false
                            ) 
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при обновлении транзакции")
                        _state.update { 
                            it.copy(
                                error = e.message ?: "Ошибка при обновлении транзакции",
                                isSuccess = false, 
                                isLoading = false
                            ) 
                        }
                    }
                }
            } else {
                // Если транзакция не найдена, сбрасываем индикатор загрузки
                _state.update { it.copy(isLoading = false, error = "Транзакция не найдена") }
            }
        }
    }

    // Validate required fields
    private fun validateFields(): Boolean {
        val currentState = _state.value
        return validateBaseFields(
            amount = currentState.amount,
            category = currentState.category,
            source = currentState.source
        ) { amountError, categoryError, sourceError, errorMsg ->
            _state.update {
                it.copy(
                    amountError = amountError,
                    categoryError = categoryError,
                    sourceError = sourceError,
                    error = errorMsg
                )
            }
        }
    }

    fun loadTransactionForEdit(transactionId: String) {
        Timber.d("Загрузка транзакции для редактирования, ID: $transactionId")
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                
                if (transaction != null) {
                    Timber.d("Транзакция найдена: $transaction")
                    
                    // Форматируем сумму с помощью метода Money.format
                    val formattedAmount = transaction.amount.abs().format(showCurrency = false)
                    Timber.d("Форматированная сумма: $formattedAmount (исходная: ${transaction.amount})")

                    // Проверяем, есть ли категория в списке
                    val isExpense = transaction.isExpense
                    val categories = if (isExpense) _state.value.expenseCategories else _state.value.incomeCategories
                    val categoryName = transaction.category ?: ""
                    val categoryExists = categories.any { it.name == categoryName }
                    val updatedCategories = if (!categoryExists && categoryName.isNotBlank()) {
                        categories + com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem(
                            name = categoryName,
                            icon = Icons.Default.MoreHoriz // дефолтная иконка для кастомной категории
                        )
                    } else categories

                    if (isExpense) {
                        _state.update { it.copy(expenseCategories = updatedCategories) }
                    } else {
                        _state.update { it.copy(incomeCategories = updatedCategories) }
                    }

                    _state.update { it.copy(
                        transactionToEdit = transaction,
                        title = transaction.title ?: "",
                        amount = formattedAmount,
                        category = categoryName,
                        note = transaction.note ?: "",
                        selectedDate = transaction.date,
                        isExpense = isExpense,
                        source = transaction.source,
                        sourceColor = transaction.sourceColor
                    ) }
                    
                    Timber.d("Состояние после загрузки: сумма=${_state.value.amount}, дата=${_state.value.selectedDate}")
                } else {
                    Timber.e("Транзакция с ID $transactionId не найдена")
                    _state.update { it.copy(error = "Транзакция не найдена") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке транзакции: ${e.message}")
                _state.update { it.copy(error = "Ошибка загрузки: ${e.message}") }
            }
        }
    }

    // Подготавливает транзакцию для обновления на основе текущего состояния
    private fun prepareTransactionForEdit(): Transaction? {
        val currentState = _state.value
        
        // Проверка обязательных полей
        if (currentState.category.isBlank()) {
            Timber.d("Category is blank, cannot prepare transaction")
            return null
        }
        
        if (currentState.source.isBlank()) {
            Timber.d("Source is blank, cannot prepare transaction")
            return null
        }
        
        // Получаем сумму из строки
        val amount = currentState.amount.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        
        // Инвертируем сумму, если это расход
        val finalAmount = if (currentState.isExpense) -amount else amount
        
        // Log the transaction preparation
        Timber.d("Preparing transaction for edit: category=${currentState.category}, source=${currentState.source}, amount=$finalAmount")
        
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

    override fun addCustomCategory(category: String) {
        if (category.isBlank()) return
        categoriesViewModel.addCustomCategory(category, _state.value.isExpense)
        _state.update {
            it.copy(
                category = category,
                showCategoryPicker = false,
                showCustomCategoryDialog = false,
                customCategory = ""
            )
        }
    }

    override fun deleteCategory(category: String) {
        if (category == "Другое") {
            _state.update { it.copy(error = "Категорию \"Другое\" нельзя удалить") }
            return
        }
        viewModelScope.launch {
            if (_state.value.isExpense) {
                categoriesViewModel.deleteExpenseCategory(category)
            } else {
                categoriesViewModel.deleteIncomeCategory(category)
            }
            if (_state.value.category == category) {
                _state.update { it.copy(category = "") }
            }
            com.davidbugayov.financeanalyzer.utils.AnalyticsUtils.logCategoryDeleted(category, _state.value.isExpense)
        }
    }

    override fun addCustomSource(source: String, color: Int) {
        try {
            if (source.isBlank()) {
                _state.update { it.copy(error = "Название источника не может быть пустым") }
                return
            }
            val newSource = com.davidbugayov.financeanalyzer.domain.model.Source(name = source, color = color, isCustom = true)
            val updatedSources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.addCustomSource(sourcePreferences, _state.value.sources, newSource)
            _state.update {
                it.copy(
                    sources = updatedSources,
                    source = source,
                    sourceColor = color,
                    showSourcePicker = false,
                    showCustomSourceDialog = false,
                    customSource = ""
                )
            }
            timber.log.Timber.d("Custom source added: ${newSource.name} with color ${newSource.color}. Updated sources: ${updatedSources.map { it.name }}")
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Error adding custom source: ${e.message}")
            _state.update { it.copy(error = "Ошибка при добавлении источника: ${e.message}") }
        }
    }

    override fun deleteSource(source: String) {
        if (source == "Сбер" || source == "Наличные" || source == "Т-Банк") {
            _state.update { it.copy(error = "Стандартные источники удалить нельзя") }
            return
        }
        val updatedSources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.deleteCustomSource(sourcePreferences, _state.value.sources, source)
        _state.update { it.copy(sources = updatedSources) }
        if (_state.value.source == source) {
            _state.update {
                it.copy(
                    source = "Сбер",
                    sourceColor = 0xFF21A038.toInt()
                )
            }
        }
        com.davidbugayov.financeanalyzer.utils.AnalyticsUtils.logSourceDeleted(source)
    }
} 