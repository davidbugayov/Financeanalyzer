package com.davidbugayov.financeanalyzer.presentation.transaction.add

import android.app.Application
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsUiViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources
import com.davidbugayov.financeanalyzer.presentation.transaction.validation.ValidationBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    categoriesViewModel: CategoriesViewModel,
    sourcePreferences: SourcePreferences,
    walletRepository: WalletRepository,
    private val updateWidgetsUseCase: UpdateWidgetsUseCase,
    private val application: Application,
    updateWalletBalancesUseCase: UpdateWalletBalancesUseCase,
    private val achievementsRepository: AchievementsRepository = AchievementsRepository(),
    private val achievementsUiViewModel: AchievementsUiViewModel
) : BaseTransactionViewModel<AddTransactionState, BaseTransactionEvent>(
    categoriesViewModel,
    sourcePreferences,
    walletRepository,
    updateWalletBalancesUseCase,
    application.resources
) {

    override val _state = MutableStateFlow(
        AddTransactionState(
            expenseCategories = categoriesViewModel.expenseCategories.value,
            incomeCategories = categoriesViewModel.incomeCategories.value
        )
    )

    // Расширение для преобразования строки в Double
    private fun String.toDouble(): Double {
        return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    // Список доступных кошельков с внутренним MutableStateFlow для обновлений
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    override val wallets: List<Wallet>
        get() = _wallets.value

    // --- Achievement unlocked flag ---
    private val _achievementUnlocked = MutableStateFlow(false)
    val achievementUnlocked: StateFlow<Boolean> = _achievementUnlocked

    init {
        Timber.d("[VM] AddTransactionViewModel создан: $this, categoriesViewModel: $categoriesViewModel")
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
        _state.update { it.copy(availableCategoryIcons = availableCategoryIcons) }
    }

    /**
     * Инициализирует список источников
     */
    private fun initSources() {
        val sources = getInitialSources(sourcePreferences, application.resources)
        _state.update { it.copy(sources = sources) }
    }

    /**
     * Загружает категории из CategoriesViewModel
     */
    private fun loadCategories() {
        viewModelScope.launch {
            if (_state.value.isExpense) {
                categoriesViewModel.expenseCategories.collect { expenseCategories ->
                    _state.update { state ->
                        state.copy(
                            expenseCategories = expenseCategories,
                            selectedCategory = expenseCategories.firstOrNull()
                        )
                    }
                }
            } else {
                categoriesViewModel.incomeCategories.collect { incomeCategories ->
                    _state.update { state ->
                        state.copy(
                            incomeCategories = incomeCategories,
                            selectedCategory = incomeCategories.firstOrNull()
                        )
                    }
                }
            }
        }
    }

    override fun setDefaultCategoryIfNeeded(force: Boolean) {
        _state.update { current ->
            val filtered = if (current.isExpense) current.expenseCategories else current.incomeCategories
            if (filtered.isNotEmpty()) {
                if (!force && current.selectedCategory != null && filtered.any { it.name == current.selectedCategory.name }) {
                    current
                } else {
                    current.copy(selectedCategory = filtered.first())
                }
            } else {
                current.copy(selectedCategory = null)
            }
        }
    }

    /**
     * Отправляет транзакцию (добавляет новую или обновляет существующую)
     */
    override fun submitTransaction(context: android.content.Context) {
        viewModelScope.launch {
            val currentState = _state.value
            Timber.d("ТРАНЗАКЦИЯ_ДОБ: submitTransaction - Начальное значение currentState.amount: '%s'", currentState.amount)

            _state.update { it.copy(isLoading = true) }

            // 1. Обрабатываем выражение суммы
            val moneyFromExpression = parseMoneyExpression(currentState.amount, selectedCurrency)
            // 2. Для валидации используем строковое представление уже обработанной суммы
            val amountForValidation = moneyFromExpression.amount.toPlainString()
            Timber.d(
                "ТРАНЗАКЦИЯ_ДОБ: submitTransaction - moneyFromExpression: %s, amountForValidation: '%s'",
                moneyFromExpression,
                amountForValidation
            )

            // 3. Вызываем валидацию с обработанной суммой
            val isValid = validateInput(
                walletId = if (currentState.addToWallet && !currentState.isExpense) currentState.selectedWallets.firstOrNull() else null,
                amount = amountForValidation,
                date = currentState.selectedDate,
                categoryId = currentState.category,
                isExpense = currentState.isExpense
            )

            if (!isValid) {
                _state.update { it.copy(isLoading = false) }
                Timber.e("ТРАНЗАКЦИЯ_ДОБ: submitTransaction - Валидация не прошла для суммы: '%s'", amountForValidation)
                return@launch
            }

            // 4. Создаем транзакцию (createTransactionFromState УЖЕ использует parseMoneyExpression, это хорошо)
            // Но важно, чтобы moneyFromExpression, который мы получили выше, был тем же, что используется в createTransactionFromState,
            // или передать его напрямую, если это возможно и более чисто.
            // В данном случае, createTransactionFromState сам вызовет parseMoneyExpression с currentState.amount.
            // Чтобы гарантировать консистентность, лучше бы createTransactionFromState принимал Money.
            // Пока оставим как есть, но это потенциальное место для рефакторинга.
            val transaction = createTransactionFromState(currentState)
            Timber.d("ТРАНЗАКЦИЯ_ДОБ: submitTransaction - Транзакция для сохранения: %s", transaction)

            try {
                val result = addTransactionUseCase(transaction)
                if (result is DomainResult.Success) {
                    if (!transaction.isExpense && transaction.walletIds != null && transaction.walletIds.isNotEmpty()) {
                        updateWalletsBalance(transaction.walletIds, transaction.amount, null)
                    }

                    // Увеличиваем счетчик использования категории
                    if (transaction.category.isNotBlank()) {
                        incrementCategoryUsage(transaction.category, transaction.isExpense)
                        Timber.d(
                            "ТРАНЗАКЦИЯ_ДОБ: Увеличен счетчик использования категории %s (isExpense=%b)",
                            transaction.category, transaction.isExpense
                        )
                    }

                    // Увеличиваем счетчик использования источника
                    if (transaction.source.isNotBlank()) {
                        incrementSourceUsage(transaction.source)
                        Timber.d(
                            "ТРАНЗАКЦИЯ_ДОБ: Увеличен счетчик использования источника %s",
                            transaction.source
                        )
                    }
                    
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            amount = "",
                            note = "",
                            amountError = false,
                            categoryError = false,
                            sourceError = false
                        )
                    }
                    setDefaultCategoryIfNeeded(force = true)
                    updateWidgetsUseCase(application.applicationContext)
                    Timber.d("ТРАНЗАКЦИЯ_ДОБ: Успешно добавлена, ID=%s", transaction.id)

                    // --- [ГЕЙМИФИКАЦИЯ] ---
                    val count = getTransactionRepositoryInstance().getTransactionsCount()
                    if (count == 1) {
                        achievementsRepository.unlockAchievement("first_transaction")
                        _achievementUnlocked.value = true
                        val achievement = achievementsRepository.achievements.value.find { it.id == "first_transaction" }
                        if (achievement != null) {
                            achievementsUiViewModel.onAchievementUnlocked(achievement)
                        }
                    }
                } else if (result is DomainResult.Error) {
                    Timber.e(result.exception, "ТРАНЗАКЦИЯ_ДОБ: Ошибка при добавлении: %s", result.exception.message)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Неизвестная ошибка добавления"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ТРАНЗАКЦИЯ_ДОБ: Исключение при добавлении транзакции: %s", e.message)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Исключение при добавлении"
                    )
                }
            }
        }
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
        // Получаем сумму из выражения через базовый метод
        Timber.d("ТРАНЗАКЦИЯ_ДОБ: createTransactionFromState - currentState.amount перед parse: '%s'", currentState.amount)
        val money = parseMoneyExpression(currentState.amount, selectedCurrency)
        Timber.d("ТРАНЗАКЦИЯ_ДОБ: createTransactionFromState - money после parse: %s", money)

        // Инвертируем сумму, если это расход
        val finalAmount = if (currentState.isExpense) money.copy(amount = money.amount.negate()) else money
        // Проверяем, является ли категория "Переводы"
        val isTransfer = currentState.category == "Переводы"
        // Генерируем UUID для новой транзакции, если id не задан
        val transactionId = currentState.transactionToEdit?.id ?: java.util.UUID.randomUUID().toString()
        Timber.d("Используем ID транзакции: %s (новый: %s)", transactionId, (currentState.transactionToEdit == null))
        // Получаем список ID кошельков для сохранения в транзакции
        val selectedWalletIds = getWalletIdsForTransaction(
            isExpense = currentState.isExpense,
            addToWallet = currentState.addToWallet,
            selectedWallets = currentState.selectedWallets
        )
        // Создаем объект транзакции
        return Transaction(
            id = transactionId,
            amount = finalAmount,
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
        Timber.d("ТРАНЗАКЦИЯ_ДОБ: validateInput - Входящая сумма для валидации: '%s'", amount)
        val validationBuilder = ValidationBuilder()
        // Reset errors
        _state.update {
            it.copy(
                amountError = false,
                categoryError = false,
                sourceError = false
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
                Timber.d("Ошибка: невозможно преобразовать сумму в число $e")
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
                amountError = validationResult.hasAmountError,
                categoryError = validationResult.hasCategoryError,
                sourceError = validationResult.hasSourceError
            )
        }

        Timber.d(
            "ТРАНЗАКЦИЯ_ДОБ: validateInput - Результат: isValid=%b, hasAmountError=%b",
            validationBuilder.build().isValid,
            validationBuilder.build().hasAmountError
        )
        return validationResult.isValid && !dateError
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
                submitTransaction(context)
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
                        sourceColor = newSource.color,
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
        expenseCategories: List<UiCategory>,
        incomeCategories: List<UiCategory>,
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
        availableCategoryIcons: List<ImageVector>,
        customCategoryIcon: ImageVector?
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
            availableCategoryIcons = availableCategoryIcons,
            customCategoryIcon = customCategoryIcon
        )
    }

    /**
     * Сброс флага после показа ачивки
     */
    fun resetAchievementUnlockedFlag() {
        _achievementUnlocked.value = false
    }
} 