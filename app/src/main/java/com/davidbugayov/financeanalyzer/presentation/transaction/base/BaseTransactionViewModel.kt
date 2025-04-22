package com.davidbugayov.financeanalyzer.presentation.transaction.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.ValidateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

abstract class BaseTransactionViewModel<S : BaseTransactionState, E : BaseTransactionEvent>(
    protected val categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel,
    protected val sourcePreferences: com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences,
    protected val walletRepository: WalletRepository,
    private val validateTransactionUseCase: ValidateTransactionUseCase
) : ViewModel(), TransactionScreenViewModel<S, E> {
    protected abstract val _state: MutableStateFlow<S>
    override val state: StateFlow<S> get() = _state.asStateFlow()
    override val wallets: List<Wallet> = emptyList()

    // Вся обработка событий теперь только в наследниках
    abstract override fun onEvent(event: E, context: android.content.Context)
    /**
     * Метод для отправки транзакции, вызываемый из UI.
     * Реализуется в наследниках.
     */
    abstract override fun submitTransaction(context: android.content.Context)

    open fun updateWidget(context: android.content.Context) {
        val widgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val widgetComponent = android.content.ComponentName(context, "com.davidbugayov.financeanalyzer.widget.BalanceWidget")
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)

        if (widgetIds.isNotEmpty()) {
            val intent = android.content.Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.BalanceWidget"))
            intent.action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }

        // Обновляем малый виджет баланса
        val smallWidgetComponent = android.content.ComponentName(context, "com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget")
        val smallWidgetIds = widgetManager.getAppWidgetIds(smallWidgetComponent)

        if (smallWidgetIds.isNotEmpty()) {
            val intent = android.content.Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget"))
            intent.action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, smallWidgetIds)
            context.sendBroadcast(intent)
        }
    }

    /**
     * Универсальный метод для обновления баланса кошельков после транзакции (доход/расход)
     */
    protected suspend fun updateWalletsAfterTransaction(
        walletIds: List<String>,
        totalAmount: Money,
        isExpense: Boolean
    ) {
        if (walletIds.isEmpty()) return
        withContext(Dispatchers.IO) {
            try {
                val walletsList = walletRepository.getWalletsByIds(walletIds)
                if (walletsList.isEmpty()) return@withContext
                val amountPerWallet = totalAmount / walletsList.size
                for (wallet in walletsList) {
                    val updatedWallet = wallet.copy(
                        balance = if (isExpense) wallet.balance.minus(amountPerWallet)
                                   else wallet.balance.plus(amountPerWallet)
                    )
                    walletRepository.updateWallet(updatedWallet)
                }
            } catch (e: Exception) {
                // Можно добавить Timber.e(e) для логирования
            }
        }
    }

    // --- Сброс полей экрана транзакций ---
    override fun resetFields() {
        _state.update { state ->
            copyState(
                state,
                amount = "",
                title = "",
                note = "",
                category = "",
                source = "",
                selectedDate = Date(),
                amountError = false,
                categoryError = false,
                sourceError = false,
                error = null,
                showDatePicker = false,
                showCategoryPicker = false,
                showCustomCategoryDialog = false,
                showCancelConfirmation = false,
                showSourcePicker = false,
                showCustomSourceDialog = false,
                showColorPicker = false,
                isLoading = false,
                isSuccess = false,
                successMessage = "",
                expenseCategories = state.expenseCategories,
                incomeCategories = state.incomeCategories,
                sources = state.sources,
                categoryToDelete = null,
                sourceToDelete = null,
                showDeleteCategoryConfirmDialog = false,
                showDeleteSourceConfirmDialog = false,
                editMode = false,
                transactionToEdit = null,
                addToWallet = false,
                selectedWallets = emptyList(),
                showWalletSelector = false,
                targetWalletId = null,
                forceExpense = false,
                preventAutoSubmit = false,
                selectedExpenseCategory = "",
                selectedIncomeCategory = "",
                customCategory = "",
                sourceColor = 0,
                customSource = "",
                customCategoryIcon = state.customCategoryIcon,
                availableCategoryIcons = state.availableCategoryIcons
            )
        }
    }

    // --- Общая обработка событий для транзакций ---
    open fun handleBaseEvent(event: BaseTransactionEvent, context: android.content.Context) {
        when (event) {
            is BaseTransactionEvent.SetAmount -> _state.update { state -> 
                copyState(state, amount = event.amount) 
            }
            is BaseTransactionEvent.SetTitle -> _state.update { state -> 
                copyState(state, title = event.title) 
            }
            is BaseTransactionEvent.SetCategory -> _state.update { state -> 
                copyState(state, category = event.category, showCategoryPicker = false) 
            }
            is BaseTransactionEvent.SetNote -> _state.update { state -> 
                copyState(state, note = event.note) 
            }
            is BaseTransactionEvent.SetDate -> _state.update { state -> 
                copyState(state, selectedDate = event.date, showDatePicker = false) 
            }
            is BaseTransactionEvent.ShowDatePicker -> _state.update { state -> 
                copyState(state, showDatePicker = true) 
            }
            is BaseTransactionEvent.HideDatePicker -> _state.update { state -> 
                copyState(state, showDatePicker = false) 
            }
            is BaseTransactionEvent.ShowCategoryPicker -> _state.update { state -> 
                copyState(state, showCategoryPicker = true) 
            }
            is BaseTransactionEvent.HideCategoryPicker -> _state.update { state -> 
                copyState(state, showCategoryPicker = false) 
            }
            is BaseTransactionEvent.ShowCustomCategoryDialog -> _state.update { state -> 
                copyState(state, showCustomCategoryDialog = true) 
            }
            is BaseTransactionEvent.HideCustomCategoryDialog -> _state.update { state -> 
                copyState(state, showCustomCategoryDialog = false, customCategory = "") 
            }
            is BaseTransactionEvent.ShowCancelConfirmation -> _state.update { state -> 
                copyState(state, showCancelConfirmation = true) 
            }
            is BaseTransactionEvent.HideCancelConfirmation -> _state.update { state -> 
                copyState(state, showCancelConfirmation = false) 
            }
            is BaseTransactionEvent.ClearError -> _state.update { state -> 
                copyState(state, error = null) 
            }
            is BaseTransactionEvent.HideSuccessDialog -> _state.update { state -> 
                copyState(state, isSuccess = false) 
            }
            is BaseTransactionEvent.ShowSourcePicker -> _state.update { state -> 
                copyState(state, showSourcePicker = true) 
            }
            is BaseTransactionEvent.HideSourcePicker -> _state.update { state -> 
                copyState(state, showSourcePicker = false) 
            }
            is BaseTransactionEvent.ShowCustomSourceDialog -> _state.update { state -> 
                copyState(state, showCustomSourceDialog = true) 
            }
            is BaseTransactionEvent.HideCustomSourceDialog -> _state.update { state -> 
                copyState(state, showCustomSourceDialog = false, customSource = "") 
            }
            is BaseTransactionEvent.ShowColorPicker -> _state.update { state -> 
                copyState(state, showColorPicker = true) 
            }
            is BaseTransactionEvent.HideColorPicker -> _state.update { state -> 
                copyState(state, showColorPicker = false) 
            }
            is BaseTransactionEvent.SetSource -> {
                val selectedSource = _state.value.sources.find { it.name == event.source }
                _state.update { state -> 
                    copyState(state, source = event.source, sourceColor = selectedSource?.color ?: state.sourceColor) 
                }
            }
            is BaseTransactionEvent.SetCustomSource -> _state.update { state -> 
                copyState(state, customSource = event.source) 
            }
            is BaseTransactionEvent.AddCustomSource -> { /* Обработка в наследнике */ }
            is BaseTransactionEvent.SetSourceColor -> _state.update { state -> 
                copyState(state, sourceColor = event.color) 
            }
            is BaseTransactionEvent.ShowWalletSelector -> _state.update { state -> 
                copyState(state, showWalletSelector = true) 
            }
            is BaseTransactionEvent.HideWalletSelector -> _state.update { state -> 
                copyState(state, showWalletSelector = false) 
            }
            is BaseTransactionEvent.ToggleAddToWallet -> _state.update { state -> 
                copyState(state, addToWallet = !state.addToWallet) 
            }
            is BaseTransactionEvent.SelectWallet -> {
                val updated = if (event.selected) {
                    _state.value.selectedWallets + event.walletId
                } else {
                    _state.value.selectedWallets - event.walletId
                }
                _state.update { state -> 
                    copyState(state, selectedWallets = updated) 
                }
            }
            is BaseTransactionEvent.SelectWallets -> _state.update { state -> 
                copyState(state, selectedWallets = event.walletIds) 
            }
            is BaseTransactionEvent.SetCustomCategoryIcon -> {
                // Более безопасная реализация без использования приведения типов
                val state = _state.value

                // Обрабатываем только если состояние поддерживает customCategoryIcon
                if (state is AddTransactionState) {
                    // Создаем новый объект AddTransactionState
                    val newState = state.copy(customCategoryIcon = event.icon)

                    // Обновляем состояние
                    _state.update {
                        // Используем текущее значение _state, которое может быть уже обновлено к этому моменту
                        // Но т.к. только что сделали копию, безопасно использовать ее как новое значение
                        @Suppress("UNCHECKED_CAST")
                        newState as S
                    }
                } else {
                    Timber.w("SetCustomCategoryIcon event received but state is not AddTransactionState")
                }
            }
            is BaseTransactionEvent.SetCustomCategory -> _state.update { state ->
                copyState(state, customCategory = event.category)
            }
            is BaseTransactionEvent.AddCustomCategory -> {
                val icon = if (_state.value is AddTransactionState) (_state.value as AddTransactionState).customCategoryIcon else Icons.Default.MoreHoriz
                val category = event.category
                if (category.isNotBlank()) {
                    categoriesViewModel.addCustomCategory(category, _state.value.isExpense, icon)
                }
                _state.update { state ->
                    copyState(state, showCustomCategoryDialog = false, customCategory = "")
                }
            }

            is BaseTransactionEvent.ShowDeleteCategoryConfirmDialog -> {
                _state.update { state ->
                    copyState(state, categoryToDelete = event.category, showDeleteCategoryConfirmDialog = true)
                }
            }

            is BaseTransactionEvent.HideDeleteCategoryConfirmDialog -> _state.update { state ->
                copyState(state, showDeleteCategoryConfirmDialog = false, categoryToDelete = null)
            }

            is BaseTransactionEvent.DeleteCategory -> {
                val category = event.category
                if (category.isNotBlank()) {
                    val isExpense = _state.value.isExpense

                    // Не даем удалить "Другое" и "Переводы"
                    if (category != "Другое" && category != "Переводы") {
                        if (isExpense) {
                            Timber.d("Deleting expense category: $category")
                            categoriesViewModel.deleteExpenseCategory(category)
                        } else {
                            Timber.d("Deleting income category: $category")
                            categoriesViewModel.deleteIncomeCategory(category)
                        }

                        // Если текущая выбранная категория - это удаляемая категория, сбрасываем ее
                        if (_state.value.category == category) {
                            // Сбрасываем текущую категорию, чтобы пользователь выбрал новую
                            _state.update { state ->
                                val newState = if (isExpense) {
                                    copyState(state, category = "", selectedExpenseCategory = "")
                                } else {
                                    copyState(state, category = "", selectedIncomeCategory = "")
                                }
                                newState
                            }
                            // Устанавливаем первую доступную категорию
                            setDefaultCategoryIfNeeded(true)
                        }
                    } else {
                        Timber.d("Attempted to delete protected category: $category")
                    }

                    // Скрываем диалог подтверждения удаления
                    _state.update { state ->
                        copyState(state, showDeleteCategoryConfirmDialog = false, categoryToDelete = null)
                    }
                }
            }

            is BaseTransactionEvent.DeleteSource -> {
                val sourceName = event.source
                if (sourceName.isNotBlank()) {
                    // Получаем список защищенных источников (можно настроить по вашим требованиям)
                    val protectedSources = listOf("Наличные", "Карта")

                    if (!protectedSources.contains(sourceName)) {
                        Timber.d("Deleting source: $sourceName")

                        // Удаляем источник из списка
                        val updatedSources = _state.value.sources.filter { it.name != sourceName }

                        // Сохраняем обновленный список источников
                        viewModelScope.launch {
                            try {
                                sourcePreferences.saveCustomSources(updatedSources)

                                // Обновляем состояние с новым списком источников
                                _state.update { state ->
                                    copyState(state, sources = updatedSources)
                                }

                                // Если текущий источник - это удаляемый источник, сбрасываем его
                                if (_state.value.source == sourceName) {
                                    val firstSource = updatedSources.firstOrNull()
                                    _state.update { state ->
                                        copyState(
                                            state,
                                            source = firstSource?.name ?: "",
                                            sourceColor = firstSource?.color ?: 0
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error deleting source")
                            }
                        }
                    } else {
                        Timber.d("Attempted to delete protected source: $sourceName")
                    }

                    // Скрываем диалог подтверждения удаления
                    _state.update { state ->
                        copyState(state, showDeleteSourceConfirmDialog = false, sourceToDelete = null)
                    }
                }
            }

            is BaseTransactionEvent.ShowDeleteSourceConfirmDialog -> {
                _state.update { state ->
                    copyState(state, sourceToDelete = event.source, showDeleteSourceConfirmDialog = true)
                }
            }

            is BaseTransactionEvent.HideDeleteSourceConfirmDialog -> _state.update { state ->
                copyState(state, showDeleteSourceConfirmDialog = false, sourceToDelete = null)
            }
            else -> {}
        }
    }

    /**
     * Sets a default category based on transaction type if none is selected
     * @param force If true, force selection of the first category even if one is already selected
     */
    open fun setDefaultCategoryIfNeeded(force: Boolean = false) {
        _state.update { current ->
            if (current.isExpense && current.expenseCategories.isNotEmpty()) {
                // If a category is already selected and in the list - don't change it
                if (!force && current.selectedExpenseCategory.isNotBlank() &&
                    current.expenseCategories.any { it.name == current.selectedExpenseCategory }
                ) {
                    copyState(current, category = current.selectedExpenseCategory)
                } else {
                    Timber.d("Setting default expense category: ${current.expenseCategories.first().name}")
                    copyState(
                        current,
                        category = current.expenseCategories.first().name,
                        selectedExpenseCategory = current.expenseCategories.first().name
                    )
                }
            } else if (!current.isExpense && current.incomeCategories.isNotEmpty()) {
                if (!force && current.selectedIncomeCategory.isNotBlank() &&
                    current.incomeCategories.any { it.name == current.selectedIncomeCategory }
                ) {
                    copyState(current, category = current.selectedIncomeCategory)
                } else {
                    Timber.d("Setting default income category: ${current.incomeCategories.first().name}")
                    copyState(
                        current,
                        category = current.incomeCategories.first().name,
                        selectedIncomeCategory = current.incomeCategories.first().name
                    )
                }
            } else {
                current
            }
        }
    }

    // --- Общая валидация ---
    protected fun validateInput(
        amount: String,
        category: String,
        source: String,
        updateState: (ValidateTransactionUseCase.Result) -> Unit
    ): Boolean {
        val result = validateTransactionUseCase(amount, category, source)
        updateState(result)
        return result.isValid
    }

    // --- Общая инициализация данных (кошельки, категории, источники) ---
    open fun loadInitialData() {
        // Реализация загрузки категорий и источников
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { categories ->
                _state.update { state ->
                    // Выбираем первую категорию, если категория еще не выбрана
                    val firstCategory = if (state.category.isBlank() && categories.isNotEmpty()) {
                        categories.first().name
                    } else {
                        state.category
                    }
                    // Обновляем список категорий, чтобы первая имела флаг wasSelected=true
                    val updatedCategories = if (categories.isNotEmpty()) {
                        categories.mapIndexed { index, categoryItem ->
                            if (index == 0 || categoryItem.name == firstCategory) {
                                categoryItem.copy(wasSelected = true)
                            } else {
                                categoryItem
                            }
                        }
                    } else {
                        categories
                    }
                    copyState(
                        state,
                        expenseCategories = updatedCategories,
                        category = firstCategory,
                        showDatePicker = false,
                        showCategoryPicker = false,
                        showCustomCategoryDialog = false,
                        showCancelConfirmation = false,
                        showSourcePicker = false,
                        showCustomSourceDialog = false,
                        showColorPicker = false,
                        isLoading = false,
                        isSuccess = false,
                        successMessage = "",
                        sourceColor = 0,
                        customSource = "",
                        source = "",
                        addToWallet = false,
                        selectedWallets = emptyList(),
                        showWalletSelector = false,
                        targetWalletId = null,
                        forceExpense = false,
                        sourceError = false,
                        preventAutoSubmit = false,
                        selectedExpenseCategory = "",
                        selectedIncomeCategory = "",
                        customCategory = "",
                        incomeCategories = state.incomeCategories,
                        sources = state.sources,
                        categoryToDelete = null,
                        sourceToDelete = null,
                        showDeleteCategoryConfirmDialog = false,
                        showDeleteSourceConfirmDialog = false,
                        editMode = false,
                        transactionToEdit = null,
                        amountError = false,
                        categoryError = false,
                        note = state.note,
                        selectedDate = state.selectedDate,
                        customCategoryIcon = state.customCategoryIcon,
                        availableCategoryIcons = state.availableCategoryIcons
                    )
                }
            }
        }
        
        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { categories ->
                _state.update { state -> 
                    copyState(state, incomeCategories = categories)
                }
            }
        }
        
        viewModelScope.launch {
            val sources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources(sourcePreferences)
            _state.update { state ->
                val firstSource = sources.firstOrNull()
                copyState(
                    state,
                    sources = sources,
                    source = firstSource?.name ?: "",
                    sourceColor = firstSource?.color ?: 0,
                    showDatePicker = false,
                    showCategoryPicker = false,
                    showCustomCategoryDialog = false,
                    showCancelConfirmation = false,
                    showSourcePicker = false,
                    showCustomSourceDialog = false,
                    showColorPicker = false,
                    isLoading = false,
                    isSuccess = false,
                    successMessage = "",
                    sourceError = false,
                    preventAutoSubmit = false,
                    selectedExpenseCategory = "",
                    selectedIncomeCategory = "",
                    customCategory = "",
                    expenseCategories = state.expenseCategories,
                    incomeCategories = state.incomeCategories,
                    categoryToDelete = null,
                    sourceToDelete = null,
                    showDeleteCategoryConfirmDialog = false,
                    showDeleteSourceConfirmDialog = false,
                    editMode = false,
                    transactionToEdit = null,
                    amountError = false,
                    categoryError = false,
                    note = state.note,
                    selectedDate = state.selectedDate,
                    customCategoryIcon = state.customCategoryIcon,
                    availableCategoryIcons = state.availableCategoryIcons
                )
            }
        }
    }
    
    open fun loadWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletsList = walletRepository.getAllWallets()
                // _wallets.value = walletsList // если нужно хранить локально
                _state.update { state -> 
                    copyState(state, selectedWallets = walletsList.map { it.id })
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
            }
        }
    }

    // --- Универсальные поля и коллбэки для работы с транзакциями ---
    protected val usedCategories = mutableSetOf<Pair<String, Boolean>>()
    open var autoDistributeIncome: Boolean = false
        protected set
    open var onIncomeAddedCallback: ((Money) -> Unit)? = null
    open var onExpenseAddedCallback: ((Money) -> Unit)? = null
    open var storedTargetWalletId: String? = null
        protected set
    open var budgetViewModel: com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel? = null
    open var navigateBackCallback: (() -> Unit)? = null

    // --- Универсальные методы для настройки и сброса состояния ---
    open fun setTargetWalletId(walletId: String) {
        storedTargetWalletId = walletId
        _state.update { state ->
            copyState(
                state,
                targetWalletId = walletId,
                addToWallet = true,
                selectedWallets = listOf(walletId)
            )
        }
    }

    open fun setupForIncomeAddition(amount: String, shouldDistribute: Boolean) {
        autoDistributeIncome = shouldDistribute
        val currentTargetWalletId = _state.value.targetWalletId
        val currentSelectedWallets = _state.value.selectedWallets
        val currentAddToWallet = _state.value.addToWallet || shouldDistribute
        _state.update { state ->
            copyState(
                state,
                isExpense = false,
                forceExpense = false,
                amount = amount,
                targetWalletId = currentTargetWalletId,
                selectedWallets = currentSelectedWallets,
                addToWallet = currentAddToWallet
            )
        }
    }

    open fun setupForIncomeAddition(amount: String, targetWalletId: String, context: android.content.Context) {
        setTargetWalletId(targetWalletId)
        if (!_state.value.addToWallet) {
            handleBaseEvent(BaseTransactionEvent.ToggleAddToWallet, context)
        }
        handleBaseEvent(BaseTransactionEvent.ForceSetIncomeType, context)
        _state.update { state -> 
            copyState(state, amount = amount)
        }
    }

    open fun setupForExpenseAddition(amount: String, walletCategory: String, context: android.content.Context) {
        autoDistributeIncome = false
        val currentTargetWalletId = _state.value.targetWalletId
        val currentSelectedWallets = _state.value.selectedWallets
        val currentAddToWallet = _state.value.addToWallet
        handleBaseEvent(BaseTransactionEvent.ForceSetExpenseType, context)
        _state.update { state ->
            copyState(
                state,
                amount = amount,
                category = walletCategory,
                targetWalletId = currentTargetWalletId,
                selectedWallets = currentSelectedWallets,
                addToWallet = currentAddToWallet
            )
        }
    }

    open fun resetToDefaultState() {
        autoDistributeIncome = false
        storedTargetWalletId = null
        _state.update { state ->
            copyState(
                state,
                isExpense = true,
                targetWalletId = null,
                addToWallet = false,
                selectedWallets = emptyList()
            )
        }
        onIncomeAddedCallback = null
    }

    override fun updateCategoryPositions() {
        viewModelScope.launch {
            usedCategories.forEach { (category, isExpense) ->
                categoriesViewModel.incrementCategoryUsage(category, isExpense)
            }
            usedCategories.clear()
        }
    }

    override fun clearSelectedWallets() {
        _state.update { state ->
            copyState(state, selectedWallets = emptyList())
        }
    }

    override fun selectAllWallets(context: android.content.Context) {
        viewModelScope.launch {
            val allWallets = walletRepository.getAllWallets()
            val allWalletIds = allWallets.map { wallet -> wallet.id }
            _state.update { state ->
                copyState(
                    state,
                    selectedWallets = allWalletIds,
                    addToWallet = true, 
                    showWalletSelector = false
                )
            }
        }
    }

    /**
     * Защищенный метод для обновления состояния, который избегает приведения типов.
     * Наследники должны переопределить этот метод для своих конкретных классов состояния.
     */
    protected abstract fun copyState(state: S, 
        title: String = state.title,
        amount: String = state.amount,
        amountError: Boolean = state.amountError,
        category: String = state.category,
        categoryError: Boolean = state.categoryError,
        note: String = state.note,
                                     selectedDate: Date = state.selectedDate,
        isExpense: Boolean = state.isExpense,
        showDatePicker: Boolean = state.showDatePicker,
        showCategoryPicker: Boolean = state.showCategoryPicker,
        showCustomCategoryDialog: Boolean = state.showCustomCategoryDialog,
        showCancelConfirmation: Boolean = state.showCancelConfirmation,
        customCategory: String = state.customCategory,
        showSourcePicker: Boolean = state.showSourcePicker,
        showCustomSourceDialog: Boolean = state.showCustomSourceDialog,
        customSource: String = state.customSource,
        source: String = state.source,
        sourceColor: Int = state.sourceColor,
        showColorPicker: Boolean = state.showColorPicker,
        isLoading: Boolean = state.isLoading,
        error: String? = state.error,
        isSuccess: Boolean = state.isSuccess,
        successMessage: String = state.successMessage,
        expenseCategories: List<com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem> = state.expenseCategories,
        incomeCategories: List<com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem> = state.incomeCategories,
        sources: List<com.davidbugayov.financeanalyzer.domain.model.Source> = state.sources,
        categoryToDelete: String? = state.categoryToDelete,
        sourceToDelete: String? = state.sourceToDelete,
        showDeleteCategoryConfirmDialog: Boolean = state.showDeleteCategoryConfirmDialog,
        showDeleteSourceConfirmDialog: Boolean = state.showDeleteSourceConfirmDialog,
        editMode: Boolean = state.editMode,
        transactionToEdit: com.davidbugayov.financeanalyzer.domain.model.Transaction? = state.transactionToEdit,
        addToWallet: Boolean = state.addToWallet,
        selectedWallets: List<String> = state.selectedWallets,
        showWalletSelector: Boolean = state.showWalletSelector,
        targetWalletId: String? = state.targetWalletId,
        forceExpense: Boolean = state.forceExpense,
        sourceError: Boolean = state.sourceError,
        preventAutoSubmit: Boolean = state.preventAutoSubmit,
        selectedExpenseCategory: String = state.selectedExpenseCategory,
        selectedIncomeCategory: String = state.selectedIncomeCategory,
        customCategoryIcon: ImageVector = state.customCategoryIcon,
        availableCategoryIcons: List<ImageVector> = state.availableCategoryIcons
    ): S
} 