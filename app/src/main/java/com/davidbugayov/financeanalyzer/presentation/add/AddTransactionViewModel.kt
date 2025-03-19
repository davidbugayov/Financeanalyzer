package com.davidbugayov.financeanalyzer.presentation.add

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.components.parseFormattedAmount
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    application: Application,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val categoriesViewModel: CategoriesViewModel,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application), KoinComponent {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    // Храним категории, которые были использованы в этой сессии
    private val usedCategories = mutableSetOf<Pair<String, Boolean>>() // category to isExpense

    init {
        // Загружаем категории
        loadCategories()
        
        // Инициализируем список источников
        initSources()
    }

    /**
     * Инициализирует список источников
     */
    private fun initSources() {
        // Загружаем сохраненные источники из SharedPreferences
        val savedSources = preferencesManager.getCustomSources()
        
        // Если есть сохраненные источники, используем их
        // Иначе используем стандартные источники
        val sources = if (savedSources.isNotEmpty()) {
            savedSources
        } else {
            ColorUtils.defaultSources
        }
        
        _state.update { it.copy(sources = sources) }
    }

    /**
     * Загружает категории из CategoriesViewModel
     */
    private fun loadCategories() {
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
    }

    /**
     * Сбрасывает все поля формы до значений по умолчанию
     */
    fun resetFields() {
        _state.update {
            AddTransactionState(
                // Сохраняем списки категорий и источников
                expenseCategories = it.expenseCategories,
                incomeCategories = it.incomeCategories,
                sources = it.sources,
                // Устанавливаем значения по умолчанию для источника
                source = "Сбер",
                sourceColor = ColorUtils.SBER_COLOR
            )
        }
    }

    /**
     * Обновляет позиции всех использованных категорий
     */
    fun updateCategoryPositions() {
        viewModelScope.launch {
            usedCategories.forEach { (category, isExpense) ->
                categoriesViewModel.incrementCategoryUsage(category, isExpense)
            }
            // Очищаем список использованных категорий
            usedCategories.clear()
        }
    }

    /**
     * Обрабатывает события экрана добавления транзакции
     */
    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.SetAmount -> {
                _state.update { it.copy(amount = event.amount) }
            }
            is AddTransactionEvent.SetTitle -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddTransactionEvent.SetCategory -> {
                _state.update {
                    it.copy(
                        category = event.category,
                        showCategoryPicker = false,
                        categoryError = false // Сбрасываем ошибку при выборе категории
                    )
                }
                // Добавляем категорию в список использованных
                usedCategories.add(event.category to _state.value.isExpense)
            }
            is AddTransactionEvent.SetNote -> {
                _state.update { it.copy(note = event.note) }
            }
            is AddTransactionEvent.SetDate -> {
                _state.update {
                    it.copy(
                        selectedDate = event.date,
                        showDatePicker = false
                    )
                }
            }
            is AddTransactionEvent.SetCustomCategory -> {
                _state.update { it.copy(customCategory = event.category) }
            }
            is AddTransactionEvent.AddCustomCategory -> {
                addCustomCategory(event.category)
            }
            is AddTransactionEvent.ToggleTransactionType -> {
                _state.update {
                    it.copy(
                        isExpense = !it.isExpense,
                        // Сбрасываем категорию при переключении типа транзакции
                        category = "",
                        categoryError = false
                    )
                }
            }
            is AddTransactionEvent.ShowDatePicker -> {
                _state.update { it.copy(showDatePicker = true) }
            }
            is AddTransactionEvent.HideDatePicker -> {
                _state.update { it.copy(showDatePicker = false) }
            }
            is AddTransactionEvent.ShowCategoryPicker -> {
                _state.update { it.copy(showCategoryPicker = true) }
            }
            is AddTransactionEvent.HideCategoryPicker -> {
                _state.update { it.copy(showCategoryPicker = false) }
            }
            is AddTransactionEvent.ShowCustomCategoryDialog -> {
                _state.update { it.copy(showCustomCategoryDialog = true) }
            }
            is AddTransactionEvent.HideCustomCategoryDialog -> {
                _state.update {
                    it.copy(
                        showCustomCategoryDialog = false,
                        customCategory = ""
                    )
                }
            }
            is AddTransactionEvent.ShowCancelConfirmation -> {
                _state.update { it.copy(showCancelConfirmation = true) }
            }
            is AddTransactionEvent.HideCancelConfirmation -> {
                _state.update { it.copy(showCancelConfirmation = false) }
            }
            is AddTransactionEvent.Submit -> {
                if (!validateInput()) {
                    return
                }
                addTransaction()
            }
            is AddTransactionEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            is AddTransactionEvent.HideSuccessDialog -> {
                _state.update { it.copy(isSuccess = false) }
                // Сбрасываем поля при нажатии "Добавить еще"
                resetFields()
            }
            is AddTransactionEvent.ShowSourcePicker -> {
                _state.update { it.copy(showSourcePicker = true) }
            }
            is AddTransactionEvent.HideSourcePicker -> {
                _state.update { it.copy(showSourcePicker = false) }
            }
            is AddTransactionEvent.ShowCustomSourceDialog -> {
                _state.update { it.copy(showCustomSourceDialog = true) }
            }
            is AddTransactionEvent.HideCustomSourceDialog -> {
                _state.update {
                    it.copy(
                        showCustomSourceDialog = false,
                        customSource = ""
                    )
                }
            }
            is AddTransactionEvent.ShowColorPicker -> {
                _state.update { it.copy(showColorPicker = true) }
            }
            is AddTransactionEvent.HideColorPicker -> {
                _state.update { it.copy(showColorPicker = false) }
            }
            is AddTransactionEvent.SetSource -> {
                _state.update {
                    it.copy(
                        source = event.source,
                        showSourcePicker = false
                    )
                }
            }
            is AddTransactionEvent.SetCustomSource -> {
                _state.update { it.copy(customSource = event.source) }
            }
            is AddTransactionEvent.AddCustomSource -> {
                addCustomSource(event.source, event.color)
            }
            is AddTransactionEvent.SetSourceColor -> {
                _state.update { it.copy(sourceColor = event.color) }
            }
            is AddTransactionEvent.AttachReceipt -> {
                attachReceipt()
            }
        }
    }

    private fun validateInput(): Boolean {
        // Используем parseFormattedAmount для преобразования строки в Money
        val money = parseFormattedAmount(_state.value.amount)
        
        val hasErrors = money.isZero() || _state.value.category.isBlank()

        _state.update {
            it.copy(
                amountError = money.isZero(),
                categoryError = _state.value.category.isBlank()
            )
        }

        return !hasErrors
    }

    private fun addTransaction() {
        if (!validateInput()) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Преобразуем строку в объект Money
                val money = parseFormattedAmount(_state.value.amount)
                
                val transaction = Transaction(
                    amount = money.amount.toDouble(),
                    category = _state.value.category,
                    isExpense = _state.value.isExpense,
                    date = _state.value.selectedDate,
                    note = _state.value.note.ifBlank { null },
                    source = _state.value.source.ifBlank { "Сбер" }
                )

                addTransactionUseCase(transaction)

                // Увеличиваем счетчик использования категории
                categoriesViewModel.incrementCategoryUsage(_state.value.category, _state.value.isExpense)

                // Логируем добавление транзакции в аналитику
                AnalyticsUtils.logTransactionAdded(
                    type = if (_state.value.isExpense) "expense" else "income",
                    amount = money,
                    category = _state.value.category
                )
                
                _state.update { it.copy(isSuccess = true) }

                // Уведомляем другие компоненты о добавлении транзакции
                EventBus.emit(Event.TransactionAdded)

                // Обновляем виджеты баланса
                updateBalanceWidget()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }

                // Логируем ошибку в аналитику
                AnalyticsUtils.logError(
                    errorType = "transaction_add_error",
                    errorMessage = e.message ?: "Unknown error"
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Добавляет новую пользовательскую категорию
     */
    private fun addCustomCategory(category: String) {
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

    /**
     * Добавляет новый пользовательский источник
     */
    private fun addCustomSource(source: String, color: Int) {
        try {
            if (source.isBlank()) {
                _state.update { it.copy(error = "Название источника не может быть пустым") }
                return
            }

            // Создаем новый источник
            val newSource = Source(
                name = source,
                color = color,
                isCustom = true
            )

            // Добавляем источник в список
            val currentSources = _state.value.sources.toMutableList()
            currentSources.add(newSource)
            
            // Сохраняем обновленный список источников в SharedPreferences
            preferencesManager.saveCustomSources(currentSources)
            
            // Обновляем состояние
            _state.update { it.copy(sources = currentSources) }

            // Обновляем состояние
            _state.update {
                it.copy(
                    source = source,
                    sourceColor = color,
                    showSourcePicker = false,
                    showCustomSourceDialog = false,
                    customSource = ""
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message) }
        }
    }

    /**
     * Обновляет виджет баланса после изменения данных, но только если виджеты добавлены на домашний экран
     */
    private fun updateBalanceWidget() {
        val context = getApplication<Application>().applicationContext
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, "com.davidbugayov.financeanalyzer.widget.BalanceWidget")
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)

        if (widgetIds.isNotEmpty()) {
            val intent = Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.BalanceWidget"))
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }

        // Обновляем малый виджет баланса
        val smallWidgetComponent = ComponentName(context, "com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget")
        val smallWidgetIds = widgetManager.getAppWidgetIds(smallWidgetComponent)

        if (smallWidgetIds.isNotEmpty()) {
            val intent = Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget"))
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallWidgetIds)
            context.sendBroadcast(intent)
        }
    }

    /**
     * Обрабатывает прикрепление чека
     */
    private fun attachReceipt() {
        // Здесь будет логика прикрепления чека
        // Пока просто логируем действие
        Timber.d("Прикрепление чека")
        
        // Можно показать сообщение пользователю
        _state.update { 
            it.copy(
                note = if (it.note.isBlank()) "Чек прикреплен" else "${it.note} (Чек прикреплен)"
            ) 
        }
    }
} 