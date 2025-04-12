package com.davidbugayov.financeanalyzer.presentation.add

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import timber.log.Timber

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    application: Application,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val categoriesViewModel: CategoriesViewModel,
    private val sourcePreferences: SourcePreferences
) : AndroidViewModel(application), KoinComponent {

    // Расширение для преобразования строки в Double
    private fun String.toDouble(): Double {
        return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    // Храним категории, которые были использованы в этой сессии
    private val usedCategories = mutableSetOf<Pair<String, Boolean>>() // category to isExpense
    
    // Флаг для автоматического распределения дохода
    private var autoDistributeIncome = false
    private var budgetViewModel: com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel? = null

    /**
     * Флаг блокировки выбора типа транзакции "Расход" 
     */
    private var _lockExpenseSelection: Boolean = false
    val lockExpenseSelection: Boolean get() = _lockExpenseSelection

    /**
     * Метод для возврата к предыдущему экрану (будет вызываться из AddTransactionScreen)
     */
    var navigateBackCallback: (() -> Unit)? = null
    
    // Callback, который будет вызван после успешного добавления дохода
    var onIncomeAddedCallback: ((com.davidbugayov.financeanalyzer.domain.model.Money) -> Unit)? = null

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
        // Загружаем сохраненные источники из SourcePreferences
        val savedSources = sourcePreferences.getCustomSources()
        
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
                sourceColor = ColorUtils.SBER_COLOR,
                // Если установлена блокировка выбора расхода, принудительно устанавливаем тип "Доход"
                isExpense = if (_lockExpenseSelection) false else it.isExpense,
                // Если это доход и установлена блокировка, устанавливаем категорию дохода
                category = if (_lockExpenseSelection && !it.isExpense) "Зарплата" else ""
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
     * Предустанавливает параметры для добавления дохода
     * @param amount начальная сумма дохода (может быть пустой)
     * @param shouldDistribute флаг, указывающий, должен ли доход быть автоматически распределён
     * @param lockExpenseSelection флаг, блокирующий возможность переключения на расход
     */
    fun setupForIncomeAddition(amount: String = "", shouldDistribute: Boolean = false, lockExpenseSelection: Boolean = false) {
        // Устанавливаем параметры транзакции как доход
        _state.update { 
            it.copy(
                isExpense = false,  // Принудительно установить тип "Доход"
                amount = amount,
                title = "Доход",
                category = "Зарплата" // Предустановленная категория дохода
            )
        }
        
        // Устанавливаем флаг автоматического распределения
        autoDistributeIncome = shouldDistribute
        
        // Устанавливаем флаг блокировки выбора расхода
        _lockExpenseSelection = lockExpenseSelection
        
        // Принудительно обновляем состояние ещё раз для гарантии
        if (lockExpenseSelection) {
            _state.update { 
                it.copy(isExpense = false)
            }
        }
        
        // Пытаемся получить экземпляр BudgetViewModel через Koin
        if (shouldDistribute && budgetViewModel == null) {
            try {
                budgetViewModel = org.koin.core.context.GlobalContext.get()
                    .getOrNull<com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel>()
            } catch (e: Exception) {
                Timber.e(e, "Не удалось получить BudgetViewModel")
            }
        }
    }

    /**
     * Отправляет транзакцию (добавляет новую или обновляет существующую)
     */
    fun submitTransaction() {
        // Показываем индикатор загрузки и блокируем кнопку
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            if (!validateInput()) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            // Если мы в режиме редактирования и у нас есть транзакция для редактирования
            val currentState = _state.value
            if (currentState.editMode && currentState.transactionToEdit != null) {
                updateTransaction()
            } else {
                addTransaction()
            }
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
            is AddTransactionEvent.ShowDeleteCategoryConfirmDialog -> {
                _state.update {
                    it.copy(
                        categoryToDelete = event.category,
                        showDeleteCategoryConfirmDialog = true
                    )
                }
            }

            is AddTransactionEvent.HideDeleteCategoryConfirmDialog -> {
                _state.update {
                    it.copy(
                        categoryToDelete = null,
                        showDeleteCategoryConfirmDialog = false
                    )
                }
            }

            is AddTransactionEvent.DeleteCategory -> {
                deleteCategory(event.category)
            }

            is AddTransactionEvent.ShowDeleteSourceConfirmDialog -> {
                _state.update {
                    it.copy(
                        sourceToDelete = event.source,
                        showDeleteSourceConfirmDialog = true
                    )
                }
            }

            is AddTransactionEvent.HideDeleteSourceConfirmDialog -> {
                _state.update {
                    it.copy(
                        sourceToDelete = null,
                        showDeleteSourceConfirmDialog = false
                    )
                }
            }

            is AddTransactionEvent.DeleteSource -> {
                deleteSource(event.source)
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
            is AddTransactionEvent.ForceSetIncomeType -> {
                // Принудительно устанавливаем тип "Доход"
                _state.update {
                    it.copy(
                        isExpense = false,
                        // Всегда устанавливаем категорию дохода - "Зарплата"
                        category = "Зарплата"
                    )
                }
                
                // Логируем принудительную установку режима дохода
                Timber.d("Forced income type selection")
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        viewModelScope.launch {
            var money = Money.zero()

            try {
                // Используем parseFormattedAmount для преобразования строки в Money
                money = Money.fromString(_state.value.amount)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при парсинге суммы: ${_state.value.amount}")
            }

            val hasInvalidAmount = money.isZero() || _state.value.amount.isBlank()
            val hasInvalidCategory = _state.value.category.isBlank()

            val hasErrors = hasInvalidAmount || hasInvalidCategory

            if (hasErrors) {
                isValid = false
                _state.update {
                    it.copy(
                        amountError = hasInvalidAmount,
                        categoryError = hasInvalidCategory,
                        error = when {
                            hasInvalidAmount && hasInvalidCategory -> "Введите сумму и категорию"
                            hasInvalidAmount -> "Введите сумму транзакции"
                            hasInvalidCategory -> "Выберите категорию"
                            else -> null
                        }
                    )
                }
            }
        }

        return isValid
    }

    /**
     * Добавляет новую транзакцию
     */
    private fun addTransaction() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentState = _state.value
                val amount = currentState.amount.toDouble()

                // Проверяем, что сумма введена
                if (amount <= 0.0) {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(error = "Введите сумму транзакции") }
                    }
                    return@launch
                }

                // Инвертируем сумму, если это расход
                val finalAmount = if (currentState.isExpense) -amount else amount

                // Проверяем, является ли категория "Переводы"
                val isTransfer = currentState.category == "Переводы"

                // Создаем объект транзакции
                val transaction = Transaction(
                    amount = Money(finalAmount),
                    date = currentState.selectedDate,
                    note = currentState.note.trim(),
                    category = currentState.category,
                    source = currentState.source,
                    isExpense = currentState.isExpense,
                    sourceColor = currentState.sourceColor,
                    isTransfer = isTransfer
                )

                addTransactionUseCase(transaction).fold(
                    onSuccess = {
                        // Сохраняем категорию, которую пользователь использовал
                        usedCategories.add(Pair(currentState.category, currentState.isExpense))

                        // Обновляем виджет
                        updateWidget()

                        // Запрашиваем обновление данных
                        requestDataRefresh()

                        // Обновляем категории
                        updateCategoryPositions()
                        
                        // Проверяем, нужно ли распределить доход по бюджету
                        if (!currentState.isExpense && autoDistributeIncome) {
                            val incomeAmount = Money(amount)
                            
                            // Вызываем callback для распределения дохода
                            onIncomeAddedCallback?.invoke(incomeAmount)
                            
                            // Или используем BudgetViewModel напрямую, если он доступен
                            budgetViewModel?.let { viewModel ->
                                viewModel.onEvent(
                                    com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent.DistributeIncome(
                                        incomeAmount
                                    )
                                )
                                Timber.d("Доход автоматически распределен: $amount")
                            }
                        }

                        // Обновляем UI в основном потоке
                        withContext(Dispatchers.Main) {
                            _state.update {
                                it.copy(
                                    isSuccess = true,
                                    error = null
                                )
                            }
                        }
                    },
                    onFailure = { exception ->
                        withContext(Dispatchers.Main) {
                            _state.update {
                                it.copy(
                                    error = exception.message ?: "Ошибка при добавлении транзакции",
                                    isSuccess = false
                                )
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            error = e.message ?: "Непредвиденная ошибка при добавлении транзакции",
                            isSuccess = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Обновляет существующую транзакцию
     */
    private fun updateTransaction() {
        viewModelScope.launch(Dispatchers.IO) { // Явно указываем Dispatchers.IO
            val currentState = _state.value

            try {
                // Проверяем, есть ли ID транзакции для обновления
                val transactionId = currentState.transactionToEdit?.id
                if (transactionId?.isBlank() == true) {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(error = "ID транзакции не указан") }
                    }
                    return@launch
                }

                val amount = currentState.amount.toDouble()

                // Проверяем, что сумма введена
                if (amount <= 0.0) {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(error = "Введите сумму транзакции") }
                    }
                    return@launch
                }

                // Инвертируем сумму, если это расход
                val finalAmount = if (currentState.isExpense) -amount else amount

                // Проверяем, является ли категория "Переводы"
                val isTransfer = currentState.category == "Переводы"

                // Создаем объект транзакции
                val transaction = Transaction(
                    id = transactionId ?: "",
                    amount = Money(finalAmount),
                    date = currentState.selectedDate,
                    note = currentState.note.trim(),
                    category = currentState.category,
                    source = currentState.source,
                    isExpense = currentState.isExpense,
                    sourceColor = currentState.sourceColor,
                    isTransfer = isTransfer
                )

                updateTransactionUseCase(transaction).fold(
                    onSuccess = {
                        // Обновляем виджет
                        updateWidget()

                        // Запрашиваем обновление данных (одним вызовом)
                        requestDataRefresh()

                        // Обновляем категории
                        updateCategoryPositions()

                        // Обновляем UI в основном потоке
                        withContext(Dispatchers.Main) {
                            _state.update {
                                it.copy(
                                    isSuccess = true,
                                    error = null
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            _state.update {
                                it.copy(
                                    error = error.message ?: "Ошибка при обновлении транзакции",
                                    isSuccess = false
                                )
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            error = e.message ?: "Непредвиденная ошибка при обновлении",
                            isSuccess = false
                        )
                    }
                }
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
            
            // Сохраняем обновленный список источников в SourcePreferences
            sourcePreferences.saveCustomSources(currentSources)
            
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
    private fun updateWidget() {
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

    /**
     * Удаляет категорию
     */
    private fun deleteCategory(category: String) {
        // Проверяем, что категория существует и не равна "Другое"
        if (category == "Другое") {
            _state.update { it.copy(error = "Категорию \"Другое\" нельзя удалить") }
            return
        }

        viewModelScope.launch {
            // Удаляем категорию с учетом типа транзакции
            if (_state.value.isExpense) {
                categoriesViewModel.deleteExpenseCategory(category)
            } else {
                categoriesViewModel.deleteIncomeCategory(category)
            }

            // Если это была выбранная категория, очищаем выбор
            if (_state.value.category == category) {
                _state.update { it.copy(category = "") }
            }

            // Логируем удаление категории
            AnalyticsUtils.logCategoryDeleted(category, _state.value.isExpense)
        }
    }

    /**
     * Удаляет источник
     */
    private fun deleteSource(source: String) {
        // Проверяем, что источник существует и не является стандартным
        if (source == "Сбер" || source == "Наличные" || source == "Т-Банк") {
            _state.update { it.copy(error = "Стандартные источники удалить нельзя") }
            return
        }

        val currentSources = _state.value.sources.toMutableList()
        val sourceToDelete = currentSources.find { it.name == source }

        if (sourceToDelete != null) {
            // Удаляем источник из списка
            currentSources.remove(sourceToDelete)

            // Сохраняем обновленный список источников в SourcePreferences
            sourcePreferences.saveCustomSources(currentSources)

            // Обновляем состояние
            _state.update { it.copy(sources = currentSources) }

            // Если это был выбранный источник, меняем на "Сбер"
            if (_state.value.source == source) {
                _state.update {
                    it.copy(
                        source = "Сбер",
                        sourceColor = 0xFF21A038.toInt() // Цвет Сбера
                    )
                }
            }

            // Логируем удаление источника
            AnalyticsUtils.logSourceDeleted(source)
        }
    }

    /**
     * Загружает транзакцию для редактирования
     */
    fun loadTransactionForEditing(transaction: Transaction) {
        // Преобразуем сумму в строку с правильным форматированием для поля ввода
        // Используем абсолютное значение суммы, чтобы не было знака минус перед расходами
        val amount = Math.abs(transaction.amount.amount.toDouble())
        val formattedAmount = String.format("%.0f", amount)

        // Логируем действия
        Timber.d("===== НАЧАЛО ЗАГРУЗКИ ТРАНЗАКЦИИ ДЛЯ РЕДАКТИРОВАНИЯ =====")
        Timber.d("ID: ${transaction.id}")
        Timber.d("Сумма оригинальная: ${transaction.amount}")
        Timber.d("Сумма форматированная: $formattedAmount")
        Timber.d("Категория: ${transaction.category}")
        Timber.d("Источник: ${transaction.source}")
        Timber.d("Дата: ${transaction.date}")
        Timber.d("Тип транзакции: ${if (transaction.isExpense) "Расход" else "Доход"}")
        
        _state.update {
            it.copy(
                amount = formattedAmount,
                category = transaction.category,
                isExpense = transaction.isExpense,
                selectedDate = transaction.date,
                note = transaction.note ?: "",
                source = transaction.source,
                sourceColor = it.sources.find { source -> source.name == transaction.source }?.color ?: ColorUtils.SBER_COLOR,
                editMode = true,
                transactionToEdit = transaction
            )
        }

        // Логируем состояние после загрузки
        val state = _state.value
        Timber.d("Состояние после загрузки:")
        Timber.d("- Сумма в форме: ${state.amount}")
        Timber.d("- isExpense: ${state.isExpense}")
        Timber.d("- editMode: ${state.editMode}")
        Timber.d("- transactionToEdit ID: ${state.transactionToEdit?.id}")
        Timber.d("===== ЗАВЕРШЕНА ЗАГРУЗКА ТРАНЗАКЦИИ ДЛЯ РЕДАКТИРОВАНИЯ =====")
    }

    /**
     * Метод для возврата к предыдущему экрану
     */
    private fun returnToPreviousScreen() {
        navigateBackCallback?.invoke()
    }

    /**
     * Запрашивает принудительное обновление данных у репозитория
     * Это нужно для мгновенного обновления UI на других экранах
     */
    private suspend fun requestDataRefresh() {
        withContext(Dispatchers.IO) {
            try {
                getTransactionRepositoryInstance().notifyDataChanged(null)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при запросе обновления данных")
                // Продолжаем выполнение даже при ошибке
            }
        }
    }

    /**
     * Получает экземпляр репозитория транзакций через Koin
     */
    private fun getTransactionRepositoryInstance(): TransactionRepository {
        return org.koin.core.context.GlobalContext.get().get()
    }

    /**
     * Запрашивает принудительное обновление данных в фоне
     */
    private fun requestDataRefreshInBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Одиночный запрос на обновление данных
                getTransactionRepositoryInstance().notifyDataChanged(null)

                // Пытаемся уведомить HomeViewModel (но только если это не приведет к ошибке)
                try {
                    val homeViewModel = org.koin.core.context.GlobalContext.get()
                        .getOrNull<com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel>()
                    homeViewModel?.initiateBackgroundDataRefresh()
                } catch (e: Exception) {
                    // Игнорируем ошибку, если HomeViewModel недоступен
                    Timber.d("HomeViewModel недоступен: ${e.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении данных в фоне")
            }
        }
    }
} 