package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources
import com.davidbugayov.financeanalyzer.presentation.transaction.validation.ValidationBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.util.Date
import com.davidbugayov.financeanalyzer.domain.model.Result as DomainResult

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    validateTransactionUseCase: ValidateTransactionUseCase,
    categoriesViewModel: CategoriesViewModel,
    sourcePreferences: SourcePreferences,
    walletRepository: WalletRepository
) : BaseTransactionViewModel<AddTransactionState, BaseTransactionEvent>(
    categoriesViewModel,
    sourcePreferences,
    walletRepository,
    validateTransactionUseCase
) {

    override val _state = MutableStateFlow(AddTransactionState())

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
        val sources = getInitialSources(sourcePreferences)
        _state.update { it.copy(sources = sources) }
    }

    /**
     * Загружает категории из CategoriesViewModel
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                categoriesViewModel.expenseCategories.collect { categories ->
                    Timber.d("[VM] collect: expenseCategories обновились, size=${categories.size}, isExpense=${_state.value.isExpense}, category='${_state.value.category}'")
                    _state.update { it.copy(expenseCategories = categories) }
                    if (_state.value.isExpense && categories.isNotEmpty() && _state.value.category.isBlank()) {
                        Timber.d("[VM] collect: Выставляю первую категорию расходов: ${categories.first().name}")
                        _state.update {
                            it.copy(
                                category = categories.first().name,
                                selectedExpenseCategory = categories.first().name
                            )
                        }
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
                        _state.update {
                            it.copy(
                                category = categories.first().name,
                                selectedIncomeCategory = categories.first().name
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке категорий доходов")
            }
        }
    }

    override fun setDefaultCategoryIfNeeded(force: Boolean) {
        _state.update { current ->
            if (current.isExpense && current.expenseCategories.isNotEmpty()) {
                // Если категория уже выбрана и есть в списке — не менять
                if (!force && current.selectedExpenseCategory.isNotBlank() && current.expenseCategories.any { it.name == current.selectedExpenseCategory }) {
                    current.copy(category = current.selectedExpenseCategory)
                } else {
                    Timber.d("[VM] setDefaultCategoryIfNeeded: Выставляю первую категорию расходов: ${current.expenseCategories.first().name}")
                    current.copy(
                        category = current.expenseCategories.first().name,
                        selectedExpenseCategory = current.expenseCategories.first().name
                    )
                }
            } else if (!current.isExpense && current.incomeCategories.isNotEmpty()) {
                if (!force && current.selectedIncomeCategory.isNotBlank() && current.incomeCategories.any { it.name == current.selectedIncomeCategory }) {
                    current.copy(category = current.selectedIncomeCategory)
                } else {
                    Timber.d("[VM] setDefaultCategoryIfNeeded: Выставляю первую категорию доходов: ${current.incomeCategories.first().name}")
                    current.copy(
                        category = current.incomeCategories.first().name,
                        selectedIncomeCategory = current.incomeCategories.first().name
                    )
                }
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
     * Получает экземпляр TransactionRepository через Koin
     */
    private fun getTransactionRepositoryInstance(): TransactionRepository {
        val repository: TransactionRepository by inject(TransactionRepository::class.java)
        return repository
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

        // Генерируем UUID для новой транзакции, если id не задан
        val transactionId = currentState.transactionToEdit?.id ?: java.util.UUID.randomUUID().toString()
        Timber.d("Используем ID транзакции: $transactionId (новый: ${currentState.transactionToEdit == null})")

        // Получаем список ID кошельков для сохранения в транзакции
        val selectedWalletIds = getWalletIdsForTransaction(
            isExpense = currentState.isExpense,
            addToWallet = currentState.addToWallet,
            selectedWallets = currentState.selectedWallets
        )

        // Создаем объект транзакции
        return Transaction(
            id = transactionId,
            amount = Money(amount = finalAmount, currency = selectedCurrency),
            date = currentState.selectedDate,
            note = currentState.note.trim(),
            category = currentState.category,
            source = currentState.source,
            isExpense = currentState.isExpense,
            sourceColor = currentState.sourceColor,
            isTransfer = isTransfer,
            walletIds = selectedWalletIds
        )
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

    private fun validateInput(
        walletId: String?,
        amount: String,
        date: Date,
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

        // Check wallet - only for income with addToWallet enabled
        if (!isExpense && _state.value.addToWallet && walletId.isNullOrBlank()) {
            Timber.d("Ошибка: не выбран кошелек для дохода с addToWallet=true")
            validationBuilder.addWalletError()
        }

        // Check amount
        if (amount.isBlank()) {
            Timber.d("Ошибка: сумма не введена")
            validationBuilder.addAmountError()
        } else {
            try {
                val amountValue = amount.replace(",", ".").toDouble()
                if (amountValue <= 0) {
                    Timber.d("Ошибка: сумма должна быть больше нуля")
                    validationBuilder.addAmountError()
                }
            } catch (e: Exception) {
                Timber.d("Ошибка: невозможно преобразовать сумму в число")
                validationBuilder.addAmountError()
            }
        }

        // Check category
        if (categoryId.isBlank()) {
            Timber.d("Ошибка: категория не выбрана")
            validationBuilder.addCategoryError()
        }

        // Check date (не должна быть в будущем)
        var dateError = false
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.time

        if (date.after(today)) {
            Timber.d("Ошибка: дата в будущем")
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

    private fun submit() {
        if (_state.value.preventAutoSubmit) {
            Timber.d("preventAutoSubmit is true, skipping submission")
            _state.update { it.copy(preventAutoSubmit = false) }
            return
        }

        Timber.d("Submit called, starting processing")

        try {
            val amountStr = _state.value.amount
            Timber.d("Checking amount: '$amountStr'")

            if (amountStr.isBlank()) {
                // Если сумма не введена, только подсветить поле суммы, но без диалога ошибки
                Timber.d("Amount is blank, highlighting error")
                _state.update { state ->
                    state.copy(
                        amountError = true
                    )
                }
                return
            }

            // Проверяем валидацию
            val s = _state.value
            Timber.d("Starting validation: walletId=${s.targetWalletId}, amount=${s.amount}, category=${s.category}, source=${s.source}")

            val validationResult = validateInput(
                walletId = s.targetWalletId,
                amount = s.amount,
                date = s.selectedDate,
                categoryId = s.category,
                isExpense = s.isExpense
            )

            Timber.d("Validation result: $validationResult")

            if (!validationResult) {
                Timber.d("Validation failed, aborting")
                return
            }

            // Если сумма введена, продолжаем со стандартной логикой 
            // создания и сохранения транзакции
            Timber.d("Validation passed, creating transaction")
            val transaction = createTransactionFromState(_state.value)
            Timber.d("Transaction created: id=${transaction.id}, amount=${transaction.amount}, category=${transaction.category}")

            viewModelScope.launch {
                Timber.d("Launching coroutine to add transaction")
                try {
                    val result = addTransactionUseCase(transaction)
                    Timber.d("Transaction add result: $result")

                    if (result is DomainResult.Success) {
                        Timber.d("Transaction added successfully")
                        _state.update { it.copy(isSuccess = true, error = null) }
                        
                        // Обновляем балансы кошельков, если это доход и выбраны кошельки
                        if (!transaction.isExpense && transaction.walletIds != null && transaction.walletIds.isNotEmpty()) {
                            updateWalletsBalance(transaction.walletIds, transaction.amount)
                        }
                    } else if (result is DomainResult.Error) {
                        Timber.e("Error adding transaction: ${result.exception.message}")
                        _state.update { it.copy(error = result.exception.message) }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Exception in coroutine while adding transaction")
                    _state.update { it.copy(error = e.message ?: "Unknown error adding transaction") }
                }
            }
        } catch (e: Exception) {
            // Общая обработка исключений, но не показываем диалог
            Timber.e(e, "Exception during submit processing")
            e.printStackTrace()
            _state.update { it.copy(error = e.message ?: "Unknown error") }
        }
    }

    override fun onEvent(event: BaseTransactionEvent, context: android.content.Context) {
        when (event) {
            is BaseTransactionEvent.ToggleTransactionType -> {
                _state.update { it.copy(isExpense = !it.isExpense, category = "") }
                setDefaultCategoryIfNeeded(force = true)
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

            is BaseTransactionEvent.Submit -> {
                submit()
            }

            is BaseTransactionEvent.ResetAmountOnly -> {
                _state.update { it.copy(amount = "", amountError = false, note = "", isSuccess = false, preventAutoSubmit = false) }
            }

            is BaseTransactionEvent.PreventAutoSubmit -> {
                _state.update { it.copy(preventAutoSubmit = true) }
            }

            is BaseTransactionEvent.AddCustomSource -> {
                val newSource = Source(
                    name = event.source,
                    color = event.color,
                    isCustom = true
                )
                val updatedSources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.addCustomSource(
                    sourcePreferences,
                    _state.value.sources,
                    newSource
                )
                _state.update {
                    it.copy(
                        sources = updatedSources,
                        showCustomSourceDialog = false,
                        customSource = "",
                        sourceColor = com.davidbugayov.financeanalyzer.utils.ColorUtils.CASH_COLOR,
                        source = newSource.name
                    )
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

            else -> handleBaseEvent(event, context)
        }
    }

    override fun updateCategoryPositions() {
        // no-op: логика обновления категорий универсальна и реализована в базовом классе
    }

    override fun copyState(
        state: AddTransactionState,
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
        expenseCategories: List<CategoryItem>,
        incomeCategories: List<CategoryItem>,
        sources: List<Source>,
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
        customCategoryIcon: ImageVector,
        availableCategoryIcons: List<ImageVector>
    ): AddTransactionState {
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
            customCategoryIcon = customCategoryIcon,
            availableCategoryIcons = availableCategoryIcons
        )
    }
} 