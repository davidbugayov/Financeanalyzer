package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.addCustomSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.davidbugayov.financeanalyzer.domain.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.PrepareTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.model.Result as DomainResult
import com.davidbugayov.financeanalyzer.presentation.transaction.validation.ValidationBuilder
import java.util.Date

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    private val validateTransactionUseCase: ValidateTransactionUseCase,
    private val prepareTransactionUseCase: PrepareTransactionUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel,
    sourcePreferences: com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences,
    walletRepository: com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
) : BaseTransactionViewModel<AddTransactionState, BaseTransactionEvent>(
    categoriesViewModel,
    sourcePreferences,
    walletRepository,
    validateTransactionUseCase
) {

    protected override val _state = MutableStateFlow(AddTransactionState())
    // Флаг для блокировки автоматической отправки формы при "Добавить еще"
    private var blockAutoSubmit = false

    // Расширение для преобразования строки в Double
    private fun String.toDouble(): Double {
        return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    // Список доступных кошельков с внутренним MutableStateFlow для обновлений
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    override val wallets: List<Wallet>
        get() = _wallets.value

    init {
        Timber.d("[VM] AddTransactionViewModel создан: $this, categoriesViewModel: $categoriesViewModel")
        // Сбросить категорию перед загрузкой
        _state.update { it.copy(category = "") }
        // Загружаем категории
        loadInitialData()
        // Принудительно выставить дефолтную категорию после инициализации (после collect)
        viewModelScope.launch {
            kotlinx.coroutines.delay(150)
            setDefaultCategoryIfNeeded(force = true)
        }
        // Загружаем список кошельков
        viewModelScope.launch {
            try {
                val walletsList = walletRepository.getAllWallets()
                _wallets.value = walletsList
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
            }
        }
    }

    /**
     * Инициализируем данные для экрана
     */
    override fun loadInitialData() {
        loadCategories()
        initSources()
    }

    /**
     * Инициализирует список источников
     */
    private fun initSources() {
        if (_state == null) {
            Timber.e("_state is null in initSources()")
            return
        }
        val sources = getInitialSources(sourcePreferences)
        _state.update { it.copy(sources = sources) }
    }

    /**
     * Загружает категории из CategoriesViewModel
     */
    private fun loadCategories() {
        Timber.d("[VM] loadCategories() вызван, categoriesViewModel: $categoriesViewModel")
        if (categoriesViewModel == null) {
            Timber.e("categoriesViewModel is null in loadCategories()")
            return
        }
        viewModelScope.launch {
            try {
                categoriesViewModel.expenseCategories.collect { categories ->
                    Timber.d("[VM] collect: expenseCategories обновились, size=${categories.size}, isExpense=${_state.value.isExpense}, category='${_state.value.category}'")
                    _state.update { it.copy(expenseCategories = categories) }
                    if (_state.value.isExpense && categories.isNotEmpty() && _state.value.category.isBlank()) {
                        Timber.d("[VM] collect: Выставляю первую категорию расходов: ${categories.first().name}")
                        _state.update { it.copy(category = categories.first().name) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке категорий расходов")
            }
        }
        viewModelScope.launch {
            try {
                categoriesViewModel.incomeCategories.collect { categories ->
                    Timber.d("[VM] collect: incomeCategories обновились, size=${categories.size}, isExpense=${_state.value.isExpense}, category='${_state.value.category}'")
                    _state.update { it.copy(incomeCategories = categories) }
                    if (!_state.value.isExpense && categories.isNotEmpty() && _state.value.category.isBlank()) {
                        Timber.d("[VM] collect: Выставляю первую категорию доходов: ${categories.first().name}")
                        _state.update { it.copy(category = categories.first().name) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке категорий доходов")
            }
        }
    }

    private fun setDefaultCategoryIfNeeded(force: Boolean = false) {
        _state.update { current ->
            if (current.isExpense && current.expenseCategories.isNotEmpty() &&
                (force || current.category.isBlank() || current.expenseCategories.none { it.name == current.category })
            ) {
                Timber.d("[VM] setDefaultCategoryIfNeeded: Выставляю первую категорию расходов: ${current.expenseCategories.first().name}")
                current.copy(category = current.expenseCategories.first().name)
            } else if (!current.isExpense && current.incomeCategories.isNotEmpty() &&
                (force || current.category.isBlank() || current.incomeCategories.none { it.name == current.category })
            ) {
                Timber.d("[VM] setDefaultCategoryIfNeeded: Выставляю первую категорию доходов: ${current.incomeCategories.first().name}")
                current.copy(category = current.incomeCategories.first().name)
            } else {
                current
            }
        }
    }

    /**
     * Отправляет транзакцию (добавляет новую или обновляет существующую)
     */
    override fun submitTransaction(context: android.content.Context) {
        submit()
    }

    /**
     * Добавляет новую транзакцию
     */
    private fun addTransaction(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentState = _state.value
                val amount = currentState.amount.toDouble()

                // Проверяем, что сумма введена
                if (amount <= 0.0) {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(error = "Введите сумму транзакции", isLoading = false) }
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
                        // Обновляем виджет
                        updateWidget(context)

                        // Запрашиваем обновление данных
                        requestDataRefresh()

                        // Обновляем категории
                        updateCategoryPositions()
                        
                        // Если это доход и установлен флаг добавления в кошелек
                        if (!currentState.isExpense && currentState.addToWallet) {
                            if (currentState.selectedWallets.isNotEmpty()) {
                                // Используем универсальный метод из Base
                                updateWalletsAfterTransaction(
                                    walletIds = currentState.selectedWallets,
                                    totalAmount = Money(amount),
                                    isExpense = false
                                )
                            }
                        }
                        
                        // Проверяем, нужно ли распределить доход по бюджету или обновить конкретный кошелек
                        if (!currentState.isExpense) {
                            val incomeAmount = Money(amount)
                            
                            // Вызываем callback для обновления баланса кошелька или распределения дохода
                            onIncomeAddedCallback?.invoke(incomeAmount)
                            Timber.d("Вызван callback для обновления баланса кошелька: ${currentState.targetWalletId}")
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
                                    isSuccess = false,
                                    isLoading = false
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
                            isSuccess = false,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Запрашивает принудительное обновление данных у репозитория
     */
    private fun requestDataRefresh() {
        viewModelScope.launch {
            try {
                getTransactionRepositoryInstance().notifyDataChanged(null)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при запросе обновления данных")
                // Продолжаем выполнение даже при ошибке
            }
        }
    }

    /**
     * Получает экземпляр TransactionRepository через Koin
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

    /**
     * Выбранная валюта (рубль по умолчанию)
     */
    private val selectedCurrency = Currency.RUB

    /**
     * Создает объект Transaction из текущего состояния
     */
    private fun createTransactionFromState(currentState: AddTransactionState): Transaction {
        // Получаем сумму из строки
        val amount = currentState.amount.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        
        // Инвертируем сумму, если это расход
        val finalAmount = if (currentState.isExpense) -amount else amount
        
        // Проверяем, является ли категория "Переводы"
        val isTransfer = currentState.category == "Переводы"
        
        // Создаем объект транзакции
        return Transaction(
            id = currentState.transactionToEdit?.id ?: "", // Используем ID существующей транзакции, если редактируем
            amount = Money(amount = finalAmount, currency = selectedCurrency),
            date = currentState.selectedDate,
            note = currentState.note.trim(),
            category = currentState.category,
            source = currentState.source,
            isExpense = currentState.isExpense,
            sourceColor = currentState.sourceColor,
            isTransfer = isTransfer
        )
    }

    /**
     * Сохраняет транзакцию
     */
    fun saveTransaction() {
        val s = _state.value
        validateInputData(s.amount, s.category, s.source) { result ->
            _state.update {
                it.copy(
                    amountError = result.amountError,
                    categoryError = result.categoryError,
                    sourceError = result.sourceError,
                    error = result.errorMessage
                )
            }
        }
        if (_state.value.amountError) {
            return
        }

        Timber.d("Запуск saveTransaction")
        viewModelScope.launch {
            try {
                // Указываем, что идет загрузка
                _state.update { it.copy(isLoading = true) }

                // Получаем текущие настройки
                val currentState = _state.value

                // Создаем объект транзакции
                val transaction = createTransactionFromState(currentState)

                Timber.d("Saving transaction: $transaction")
                
                // Сохраняем транзакцию
                addTransactionUseCase(transaction)

                // Расчитываем сумму для callback
                val amount = Money(
                    amount = transaction.amount.amount.toDouble(),
                    currency = selectedCurrency
                )

                // Обновляем состояние после сохранения
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = if (it.isExpense) {
                            "Расход успешно добавлен"
                        } else {
                            "Доход успешно добавлен"
                        }
                    )
                }

                // Вызываем соответствующий callback
                if (currentState.isExpense) {
                    // Отправляем событие о изменении данных перед вызовом колбэка
                    // чтобы убедиться, что все экраны обновят данные при возвращении
                    notifyDataChanged(transaction.id)
                    
                    // Вызываем callback о добавлении расхода
                    onExpenseAddedCallback?.invoke(amount)
                } else {
                    // Отправляем событие о изменении данных перед вызовом колбэка
                    // чтобы убедиться, что все экраны обновят данные при возвращении
                    notifyDataChanged(transaction.id)
                    
                    // Вызываем callback о добавлении дохода
                    onIncomeAddedCallback?.invoke(amount)
                }

            } catch (e: Exception) {
                Timber.e(e, "Ошибка при сохранении транзакции")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    /**
     * Запрашивает принудительное обновление данных у репозитория
     */
    private fun notifyDataChanged(transactionId: String) {
        viewModelScope.launch {
            try {
                getTransactionRepositoryInstance().notifyDataChanged(transactionId)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при запросе обновления данных")
            }
        }
    }

    /**
     * Публичный метод для доступа к репозиторию транзакций
     * Используется для подписки на события изменения данных
     */
    fun getTransactionRepository(): TransactionRepository {
        return getTransactionRepositoryInstance()
    }

    /**
     * Очищает список выбранных кошельков
     */
    override fun clearSelectedWallets() {
        Timber.d("Очистка списка выбранных кошельков")
        _state.update { it.copy(selectedWallets = emptyList()) }
    }

    
    override fun copyState(
        state: AddTransactionState,
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
        preventAutoSubmit: Boolean
    ): AddTransactionState {
        return AddTransactionState(
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
            preventAutoSubmit = preventAutoSubmit
        )
    }

    /**
     * Проверка валидации ввода
     */
    fun validateInputData(amount: String, category: String, source: String, updateState: (ValidateTransactionUseCase.Result) -> Unit): Boolean {
        val result = validateTransactionUseCase(amount, category, source)
        updateState(result)
        return result.isValid
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
                sourceError = false,
                dateError = false
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
        // Check date (не должна быть в будущем)
        var dateError = false
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        if (date.after(today)) {
            dateError = true
        }
        // No validation for source - allowing empty sources
        val validationResult = validationBuilder.build()
        _state.update {
            it.copy(
                walletError = validationResult.hasWalletError,
                amountError = validationResult.hasAmountError,
                categoryError = validationResult.hasCategoryError,
                sourceError = validationResult.hasSourceError,
                dateError = dateError
            )
        }
        return validationResult.isValid && !dateError
    }

    fun submit(preventAutoSubmit: Boolean = false) {
        if (preventAutoSubmit) {
            _state.update { it.copy(preventAutoSubmit = true) }
            return
        }

        try {
            val amountStr = _state.value.amount
            if (amountStr.isBlank()) {
                // Если сумма не введена, только подсветить поле суммы, но без диалога ошибки
                _state.update { state ->
                    state.copy(
                        amountError = true
                    )
                }
                return
            }

            // Проверяем валидацию
            val s = _state.value
            val validationResult = validateInput(
                walletId = s.targetWalletId,
                amount = s.amount,
                note = s.note,
                date = s.selectedDate,
                sourceColor = s.sourceColor,
                source = s.source,
                categoryId = s.category,
                isExpense = s.isExpense
            )
            
            if (!validationResult) {
                return
            }

            // Если сумма введена, продолжаем со стандартной логикой 
            // создания и сохранения транзакции
            val transaction = createTransactionFromState(_state.value)
            
            viewModelScope.launch {
                val result = addTransactionUseCase(transaction)
                if (result is DomainResult.Success) {
                    _state.update { it.copy(isSuccess = true, error = null) }
                } else if (result is DomainResult.Error) {
                    _state.update { it.copy(error = result.exception.message) }
                }
            }
        } catch (e: Exception) {
            // Общая обработка исключений, но не показываем диалог
            e.printStackTrace()
            _state.update { it.copy(error = e.message ?: "Неизвестная ошибка") }
        }
    }

    override fun onEvent(event: BaseTransactionEvent, context: android.content.Context) {
        when (event) {
            is BaseTransactionEvent.ToggleTransactionType -> {
                _state.update { it.copy(isExpense = !it.isExpense, category = "") }
                setDefaultCategoryIfNeeded()
            }
            is BaseTransactionEvent.Submit -> {
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
        // no-op: логика обновления категорий универсальна и реализована в базовом классе
    }
} 