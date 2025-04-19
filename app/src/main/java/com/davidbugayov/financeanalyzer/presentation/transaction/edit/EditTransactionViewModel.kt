package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.EditTransactionEvent
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

class EditTransactionViewModel(
    application: Application,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val categoriesViewModel: CategoriesViewModel,
    private val sourcePreferences: SourcePreferences
) : BaseTransactionViewModel<EditTransactionState, EditTransactionEvent>() {

    override val wallets: List<Wallet> = emptyList()

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
    }

    override fun onEvent(event: EditTransactionEvent, context: android.content.Context) {
        when (event) {
            is EditTransactionEvent.LoadTransaction -> loadTransactionForEdit(event.id)
            is EditTransactionEvent.SubmitEdit -> submitTransaction(context)
            is EditTransactionEvent.SetSource -> {
                _state.update { it.copy(source = event.source) }
            }
            is EditTransactionEvent.SetSourceColor -> {
                _state.update { it.copy(sourceColor = event.color) }
            }
            is EditTransactionEvent.ShowSourcePicker -> {
                _state.update { it.copy(showSourcePicker = true) }
            }
            is EditTransactionEvent.HideSourcePicker -> {
                _state.update { it.copy(showSourcePicker = false) }
            }
            is EditTransactionEvent.ShowCustomSourceDialog -> {
                _state.update { it.copy(showCustomSourceDialog = true) }
            }
            is EditTransactionEvent.HideCustomSourceDialog -> {
                _state.update { it.copy(showCustomSourceDialog = false) }
            }
            is EditTransactionEvent.AddCustomSource -> {
                val newSource = Source(name = event.source, color =  event.color)
                val updatedSources = addCustomSource(sourcePreferences, _state.value.sources, newSource)
                _state.update {
                    it.copy(
                        sources = updatedSources,
                        source = event.source,
                        sourceColor = event.color,
                        showCustomSourceDialog = false,
                        customSource = ""
                    )
                }
            }
            is EditTransactionEvent.SetCustomSource -> {
                _state.update { it.copy(customSource = event.source) }
            }
            is EditTransactionEvent.ShowDeleteSourceConfirmDialog -> {
                _state.update {
                    it.copy(
                        showDeleteSourceConfirmDialog = true,
                        sourceToDelete = event.source
                    )
                }
            }
            is EditTransactionEvent.HideDeleteSourceConfirmDialog -> {
                _state.update {
                    it.copy(
                        showDeleteSourceConfirmDialog = false,
                        sourceToDelete = null
                    )
                }
            }
            is EditTransactionEvent.DeleteSource -> {
                val updatedSources = deleteCustomSource(sourcePreferences, _state.value.sources, event.source)
                _state.update {
                    it.copy(
                        sources = updatedSources,
                        showDeleteSourceConfirmDialog = false,
                        sourceToDelete = null
                    )
                }
                // Если удалён выбранный источник — сбросить на дефолтный
                if (_state.value.source == event.source) {
                    val defaultSource = getInitialSources(sourcePreferences).firstOrNull() ?: ""
                    _state.update {
                        it.copy(
                            source = if (defaultSource is Source) defaultSource.name else "Сбер",
                            sourceColor = if (defaultSource is Source) defaultSource.color else 0xFF21A038.toInt()
                        )
                    }
                }
            }
            is EditTransactionEvent.ShowCustomCategoryDialog -> {
                _state.update { it.copy(showCustomCategoryDialog = true) }
            }
            is EditTransactionEvent.HideCustomCategoryDialog -> {
                _state.update { it.copy(showCustomCategoryDialog = false, customCategory = "") }
            }
            is EditTransactionEvent.SetCustomCategory -> {
                _state.update { it.copy(customCategory = event.category) }
            }
            is EditTransactionEvent.AddCustomCategory -> {
                if (event.category.isNotBlank()) {
                    categoriesViewModel.addCustomCategory(event.category, _state.value.isExpense)
                    _state.update {
                        it.copy(
                            category = event.category,
                            showCategoryPicker = false,
                            showCustomCategoryDialog = false,
                            customCategory = ""
                        )
                    }
                }
            }
            is EditTransactionEvent.ShowDatePicker -> {
                _state.update { it.copy(showDatePicker = true) }
            }
            is EditTransactionEvent.HideDatePicker -> {
                _state.update { it.copy(showDatePicker = false) }
            }
            is EditTransactionEvent.SetDate -> {
                _state.update { it.copy(selectedDate = event.date, showDatePicker = false) }
                Timber.d("Установлена новая дата: ${event.date}")
            }
            // Обрабатываем ToggleTransactionType
            is EditTransactionEvent.ToggleTransactionType -> {
                _state.update { 
                    it.copy(
                        isExpense = !it.isExpense,
                        // Сбрасываем категорию при переключении типа транзакции
                        category = ""
                    )
                }
            }
            // Обрабатываем SetCategory 
            is EditTransactionEvent.SetCategory -> {
                _state.update {
                    it.copy(
                        category = event.category,
                        showCategoryPicker = false
                    )
                }
            }
            // Обрабатываем SetAmount
            is EditTransactionEvent.SetAmount -> {
                _state.update { it.copy(amount = event.amount) }
            }
            // Обрабатываем SetNote
            is EditTransactionEvent.SetNote -> {
                _state.update { it.copy(note = event.note) }
            }
            else -> { /* обработка других событий */ }
        }
    }

    override fun resetFields() {
        // TODO: реализовать сброс полей для редактирования
    }

    override fun updateCategoryPositions() {
        // TODO: реализовать обновление позиций категорий
    }

    override fun submitTransaction(context: android.content.Context) {
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

    fun loadTransactionForEdit(transactionId: String) {
        Timber.d("Загрузка транзакции для редактирования, ID: $transactionId")
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                
                if (transaction != null) {
                    Timber.d("Транзакция найдена: $transaction")
                    
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
        
        // Получаем сумму из строки
        val amount = currentState.amount.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        
        // Инвертируем сумму, если это расход
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
} 