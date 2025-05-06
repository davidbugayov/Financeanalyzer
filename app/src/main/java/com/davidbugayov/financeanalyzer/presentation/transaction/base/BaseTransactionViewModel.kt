package com.davidbugayov.financeanalyzer.presentation.transaction.base

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryIconProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryProvider
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

abstract class BaseTransactionViewModel<S : BaseTransactionState, E : BaseTransactionEvent>(
    protected val categoriesViewModel: CategoriesViewModel,
    protected val sourcePreferences: SourcePreferences,
    protected val walletRepository: WalletRepository
) : ViewModel(), TransactionScreenViewModel<S, E> {

    protected abstract val _state: MutableStateFlow<S>
    override val state: StateFlow<S> get() = _state.asStateFlow()
    override val wallets: List<Wallet> = emptyList()

    /**
     * Список доступных иконок для пользовательских категорий (глобально для всех транзакций)
     */
    protected val availableCategoryIcons: List<ImageVector> =
        CategoryProvider.defaultCategories.map { meta -> CategoryIconProvider.getIconByName(meta.iconName) }

    // Вся обработка событий теперь только в наследниках
    abstract override fun onEvent(event: E, context: android.content.Context)
    /**
     * Метод для отправки транзакции, вызываемый из UI.
     * Реализуется в наследниках.
     */
    abstract override fun submitTransaction(context: android.content.Context)

    /**
     * Обновляет балансы кошельков после редактирования доходной транзакции
     * @param walletIds Список ID кошельков для обновления
     * @param amount Новая сумма дохода
     * @param originalTransaction Исходная транзакция до редактирования
     */
    protected fun updateWalletsBalance(walletIds: List<String>, amount: Money, originalTransaction: com.davidbugayov.financeanalyzer.domain.model.Transaction?) {
        viewModelScope.launch {
            try {
                // Если кошельков несколько, делим сумму равномерно между ними
                val amountPerWallet = if (walletIds.size > 1) {
                    amount.div(walletIds.size)
                } else {
                    amount
                }
                
                Timber.d("Обновление баланса кошельков: ${walletIds.size} кошельков, сумма на кошелек: $amountPerWallet")
                
                // Получаем кошельки по ID
                val walletsList = walletRepository.getWalletsByIds(walletIds)
                
                // Если есть исходная транзакция и у нее были кошельки, сначала откатываем изменения
                if (originalTransaction != null && originalTransaction.walletIds != null && originalTransaction.walletIds.isNotEmpty()) {
                    val originalAmount = originalTransaction.amount
                    val originalWalletIds = originalTransaction.walletIds
                    val originalAmountPerWallet = if (originalWalletIds.size > 1) {
                        originalAmount.div(originalWalletIds.size)
                    } else {
                        originalAmount
                    }
                    
                    Timber.d("Откат изменений для ${originalWalletIds.size} оригинальных кошельков, сумма: $originalAmountPerWallet")
                    
                    // Получаем исходные кошельки
                    val originalWallets = walletRepository.getWalletsByIds(originalWalletIds)
                    
                    // Откатываем изменения в исходных кошельках
                    originalWallets.forEach { wallet ->
                        val updatedWallet = wallet.copy(
                            balance = wallet.balance.minus(originalAmountPerWallet)
                        )
                        Timber.d("Откат для кошелька ${wallet.name}: баланс ${wallet.balance} -> ${updatedWallet.balance}")
                        walletRepository.updateWallet(updatedWallet)
                    }
                }
                
                // Обновляем баланс каждого кошелька с новыми данными
                walletsList.forEach { wallet ->
                    val updatedWallet = wallet.copy(
                        balance = wallet.balance.plus(amountPerWallet)
                    )
                    Timber.d("Обновляем кошелек ${wallet.name}: старый баланс=${wallet.balance}, новый баланс=${updatedWallet.balance}")
                    walletRepository.updateWallet(updatedWallet)
                }
                
                Timber.d("Балансы кошельков успешно обновлены")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении баланса кошельков: ${e.message}")
            }
        }
    }
    
    /**
     * Определяет список кошельков для сохранения в транзакции
     * @param isExpense Является ли транзакция расходом
     * @param addToWallet Флаг добавления в кошельки
     * @param selectedWallets Список выбранных кошельков
     * @return Список ID кошельков или null
     */
    protected fun getWalletIdsForTransaction(
        isExpense: Boolean,
        addToWallet: Boolean,
        selectedWallets: List<String>
    ): List<String>? {
        return if (!isExpense && addToWallet && selectedWallets.isNotEmpty()) {
            Timber.d("Сохраняем выбранные кошельки: ${selectedWallets.size} шт.")
            selectedWallets
        } else {
            Timber.d("Не сохраняем кошельки: isExpense=$isExpense, addToWallet=$addToWallet, selectedWallets=${selectedWallets.size}")
            null
        }
    }
    
    /**
     * Загружает кошельки
     */
    open fun loadWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = walletRepository.getAllWallets()
                Timber.d("Загружено ${result.size} кошельков")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
            }
        }
    }
    
    /**
     * Обрабатывает событие переключения флага "Добавить в кошельки"
     * @param currentAddToWallet Текущее значение флага
     * @return Пара (новое значение флага, новый список выбранных кошельков)
     */
    protected fun handleToggleAddToWallet(
        currentAddToWallet: Boolean
    ): Pair<Boolean, List<String>> {
        val newAddToWallet = !currentAddToWallet
        
        return if (newAddToWallet) {
            // При включении автоматически выбираем все кошельки, если список пуст
            val allWalletIds = wallets.map { it.id }
            Timber.d("Включение кошельков, автоматический выбор всех ${allWalletIds.size} кошельков")
            Pair(true, allWalletIds)
        } else {
            // При отключении очищаем список
            Timber.d("Отключение кошельков")
            Pair(false, emptyList())
        }
    }
    
    /**
     * Обрабатывает событие выбора кошелька
     * @param walletId ID кошелька
     * @param selected Выбран или отменен выбор
     * @param currentSelectedWallets Текущие выбранные кошельки
     * @return Новый список выбранных кошельков
     */
    protected fun handleSelectWallet(
        walletId: String,
        selected: Boolean,
        currentSelectedWallets: List<String>
    ): List<String> {
        Timber.d("SelectWallet событие - walletId=$walletId, selected=$selected")
        
        val updatedWallets = if (selected) {
            currentSelectedWallets + walletId
        } else {
            currentSelectedWallets - walletId
        }
        
        Timber.d("Обновление списка выбранных кошельков: было ${currentSelectedWallets.size}, стало ${updatedWallets.size}")
        
        return updatedWallets
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
                if (selectedSource != null) {
                    _state.update { state ->
                        copyState(
                            state,
                            source = selectedSource.name,
                            sourceColor = selectedSource.color
                        )
                    }
                }
            }

            is BaseTransactionEvent.SetCustomSource -> _state.update { state ->
                copyState(state, customSource = event.source)
            }

            is BaseTransactionEvent.AddCustomSource -> { /* Обработка в наследнике */
            }

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

            is BaseTransactionEvent.SetCustomCategory -> _state.update { state ->
                copyState(state, customCategory = event.category)
            }

            is BaseTransactionEvent.AddCustomCategory -> {
                val isExpense = _state.value.isExpense
                val customCategoryIcon = _state.value.customCategoryIcon
                categoriesViewModel.addCustomCategory(event.category, isExpense, customCategoryIcon)
                val updatedCategories = if (isExpense) {
                    categoriesViewModel.expenseCategories.value
                } else {
                    categoriesViewModel.incomeCategories.value
                }
                _state.update {
                    if (isExpense) {
                        copyState(
                            it,
                            showCustomCategoryDialog = false,
                            customCategory = "",
                            customCategoryIcon = null,
                            expenseCategories = updatedCategories,
                            category = event.category,
                            selectedExpenseCategory = event.category
                        )
                    } else {
                        copyState(
                            it,
                            showCustomCategoryDialog = false,
                            customCategory = "",
                            customCategoryIcon = null,
                            incomeCategories = updatedCategories,
                            category = event.category,
                            selectedIncomeCategory = event.category
                        )
                    }
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
                        viewModelScope.launch {
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
                            }
                        }
                    } else {
                        Timber.d("Attempted to delete protected category: $category")
                    }
                }
                // Скрываем диалог подтверждения удаления
                _state.update { state ->
                    copyState(state, showDeleteCategoryConfirmDialog = false, categoryToDelete = null)
                }
            }

            is BaseTransactionEvent.DeleteSource -> {
                val sourceName = event.source
                if (sourceName.isNotBlank()) {
                    // Получаем список защищенных источников
                    val protectedSources = listOf("Наличные", "Карта")
                
                    if (!protectedSources.contains(sourceName)) {
                        Timber.d("Deleting source: $sourceName")
                    
                        // Удаляем источник
                        viewModelScope.launch {
                            try {
                                // Удаляем источник из предпочтений
                                sourcePreferences.deleteSource(sourceName)
                                
                                // Перезагружаем список источников
                                loadSources()
                                
                                // Если текущий источник - это удаляемый источник, сбрасываем его
                                if (_state.value.source == sourceName) {
                                    val firstSource = _state.value.sources.firstOrNull()
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
                }
                // Скрываем диалог подтверждения удаления
                _state.update { state ->
                    copyState(state, showDeleteSourceConfirmDialog = false, sourceToDelete = null)
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

            is BaseTransactionEvent.ForceSetIncomeType -> _state.update { state ->
                copyState(state, isExpense = false)
            }

            is BaseTransactionEvent.ForceSetExpenseType -> _state.update { state ->
                copyState(state, isExpense = true)
            }

            is BaseTransactionEvent.SetCustomCategoryIcon -> {
                _state.update { state ->
                    copyState(state, customCategoryIcon = event.icon)
                }
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
                                categoryItem.copy()
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
                    availableCategoryIcons = state.availableCategoryIcons
                )
            }
        }
    }

    // --- Универсальные поля и коллбэки для работы с транзакциями ---
    override fun updateCategoryPositions() {
        viewModelScope.launch {
            // Implementation needed
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
    protected abstract fun copyState(
        state: S,
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
        expenseCategories: List<com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory> = state.expenseCategories,
        incomeCategories: List<com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory> = state.incomeCategories,
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
        availableCategoryIcons: List<ImageVector> = state.availableCategoryIcons,
        customCategoryIcon: ImageVector? = state.customCategoryIcon
    ): S

    // Utility methods
    
    /**
     * Загружает список источников средств
     */
    protected fun loadSources() {
        viewModelScope.launch {
            try {
                val sources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources(sourcePreferences)
                _state.update { state ->
                    copyState(state, sources = sources)
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке источников: ${e.message}")
            }
        }
    }

    // --- Универсальные методы для настройки и сброса состояния ---
    open fun setupForExpenseAddition(amount: String, walletCategory: String, context: android.content.Context) {
        _state.update { state ->
            copyState(
                state,
                amount = amount,
                category = walletCategory,
                targetWalletId = state.targetWalletId,
                selectedWallets = state.selectedWallets,
                addToWallet = state.addToWallet
            )
        }
        handleBaseEvent(BaseTransactionEvent.ForceSetExpenseType, context)
    }
} 