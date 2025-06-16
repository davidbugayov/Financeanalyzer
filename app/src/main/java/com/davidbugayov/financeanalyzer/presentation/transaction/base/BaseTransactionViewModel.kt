package com.davidbugayov.financeanalyzer.presentation.transaction.base
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult

import android.app.Application
import android.content.res.Resources
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.data.preferences.SourceUsagePreferences
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryIconProvider
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.objecthunter.exp4j.ExpressionBuilder
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date

abstract class BaseTransactionViewModel<S : BaseTransactionState, E : BaseTransactionEvent>(
    protected val categoriesViewModel: CategoriesViewModel,
    protected val sourcePreferences: SourcePreferences,
    protected val walletRepository: WalletRepository,
    private val updateWalletBalancesUseCase: UpdateWalletBalancesUseCase,
    protected val resources: Resources,
) : ViewModel(), TransactionScreenViewModel<S, E> {

    protected abstract val _state: MutableStateFlow<S>
    override val state: StateFlow<S> get() = _state.asStateFlow()
    override val wallets: List<Wallet> = emptyList()

    /**
     * Список доступных иконок для пользовательских категорий (глобально для всех транзакций)
     */
    protected val availableCategoryIcons: List<ImageVector> =
        CategoryIconProvider.getUniqueIconsForPicker()

    // Add sourceUsagePreferences property
    protected val sourceUsagePreferences: SourceUsagePreferences by lazy {
        SourceUsagePreferences.getInstance(getApplication())
    }

    // Add categoryUsagePreferences property
    protected val categoryUsagePreferences: CategoryUsagePreferences by lazy {
        CategoryUsagePreferences.getInstance(getApplication())
    }

    /**
     * Получает экземпляр Application из CategoriesViewModel
     */
    protected fun getApplication(): Application {
        return (categoriesViewModel as AndroidViewModel).getApplication()
    }

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
    protected suspend fun updateWalletsBalance(
        walletIds: List<String>,
        amount: Money,
        originalTransaction: Transaction?,
    ) {
        if (walletIds.isNotEmpty()) {
            val result = updateWalletBalancesUseCase(
                walletIdsToUpdate = walletIds,
                amountForWallets = amount,
                originalTransaction = originalTransaction,
            )
            if (result is CoreResult.Error) {
                Timber.e(result.exception, "Ошибка при обновлении баланса кошельков через UseCase")
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
        selectedWallets: List<String>,
    ): List<String>? {
        return if (!isExpense && addToWallet && selectedWallets.isNotEmpty()) {
            Timber.d("Сохраняем выбранные кошельки: ${selectedWallets.size} шт.")
            selectedWallets
        } else {
            Timber.d(
                "Не сохраняем кошельки: isExpense=$isExpense, addToWallet=$addToWallet, selectedWallets=${selectedWallets.size}",
            )
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
    protected fun handleToggleAddToWallet(currentAddToWallet: Boolean): Pair<Boolean, List<String>> {
        val newAddToWallet = !currentAddToWallet

        return if (newAddToWallet) {
            // При включении автоматически выбираем все кошельки, если список пуст
            val allWalletIds = wallets.map { it.id }
            Timber.d(
                "Включение кошельков, автоматический выбор всех ${allWalletIds.size} кошельков",
            )
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
        currentSelectedWallets: List<String>,
    ): List<String> {
        Timber.d("SelectWallet событие - walletId=$walletId, selected=$selected")

        val updatedWallets = if (selected) {
            currentSelectedWallets + walletId
        } else {
            currentSelectedWallets - walletId
        }

        Timber.d(
            "Обновление списка выбранных кошельков: было ${currentSelectedWallets.size}, стало ${updatedWallets.size}",
        )

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
                availableCategoryIcons = state.availableCategoryIcons,
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
                            sourceColor = selectedSource.color,
                        )
                    }
                }
            }

            is BaseTransactionEvent.SetCustomSource -> _state.update { state ->
                copyState(state, customSource = event.source)
            }

            is BaseTransactionEvent.AddCustomSource -> { // Обработка в наследнике
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
                            selectedExpenseCategory = event.category,
                        )
                    } else {
                        copyState(
                            it,
                            showCustomCategoryDialog = false,
                            customCategory = "",
                            customCategoryIcon = null,
                            incomeCategories = updatedCategories,
                            category = event.category,
                            selectedIncomeCategory = event.category,
                        )
                    }
                }
            }

            is BaseTransactionEvent.ShowDeleteCategoryConfirmDialog -> {
                _state.update { state ->
                    copyState(
                        state,
                        categoryToDelete = event.category,
                        showDeleteCategoryConfirmDialog = true,
                    )
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
                                        copyState(
                                            state,
                                            category = "",
                                            selectedExpenseCategory = "",
                                        )
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
                    copyState(
                        state,
                        showDeleteCategoryConfirmDialog = false,
                        categoryToDelete = null,
                    )
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
                                            sourceColor = firstSource?.color ?: 0,
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
                    copyState(
                        state,
                        sourceToDelete = event.source,
                        showDeleteSourceConfirmDialog = true,
                    )
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

            is BaseTransactionEvent.SetAmountError -> _state.update { state ->
                copyState(state, amountError = event.isError)
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
                    Timber.d(
                        "Setting default expense category: ${current.expenseCategories.first().name}",
                    )
                    copyState(
                        current,
                        category = current.expenseCategories.first().name,
                        selectedExpenseCategory = current.expenseCategories.first().name,
                    )
                }
            } else if (!current.isExpense && current.incomeCategories.isNotEmpty()) {
                if (!force && current.selectedIncomeCategory.isNotBlank() &&
                    current.incomeCategories.any { it.name == current.selectedIncomeCategory }
                ) {
                    copyState(current, category = current.selectedIncomeCategory)
                } else {
                    Timber.d(
                        "Setting default income category: ${current.incomeCategories.first().name}",
                    )
                    copyState(
                        current,
                        category = current.incomeCategories.first().name,
                        selectedIncomeCategory = current.incomeCategories.first().name,
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
                    // Сортируем категории расходов по частоте использования
                    val sortedCategories = categories.sortedByDescending {
                        getCategoryUsage(it.name, true)
                    }

                    Timber.d(
                        "[CATEGORY_SORT] Категории расходов отсортированы по использованию: %s",
                        sortedCategories.joinToString(", ") {
                            "${it.name}(${getCategoryUsage(
                                it.name,
                                true,
                            )})"
                        },
                    )

                    // Выбираем первую категорию, если категория еще не выбрана
                    val firstCategory = if (state.category.isBlank() && sortedCategories.isNotEmpty()) {
                        sortedCategories.first().name
                    } else {
                        state.category
                    }

                    copyState(
                        state,
                        expenseCategories = sortedCategories,
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
                        availableCategoryIcons = state.availableCategoryIcons,
                    )
                }
            }
        }

        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { categories ->
                _state.update { state ->
                    // Сортируем категории доходов по частоте использования
                    val sortedCategories = categories.sortedByDescending {
                        getCategoryUsage(it.name, false)
                    }

                    Timber.d(
                        "[CATEGORY_SORT] Категории доходов отсортированы по использованию: %s",
                        sortedCategories.joinToString(", ") {
                            "${it.name}(${getCategoryUsage(
                                it.name,
                                false,
                            )})"
                        },
                    )

                    copyState(state, incomeCategories = sortedCategories)
                }
            }
        }

        // Загружаем источники с учетом сортировки по частоте использования
        loadSources()
    }

    // --- Универсальные поля и коллбэки для работы с транзакциями ---
    override fun updateCategoryPositions() {
        viewModelScope.launch {
            // Позиции категорий обновляются при выходе с экрана
            Timber.d("[CATEGORY_SORT] Обновление позиций категорий")
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
                    showWalletSelector = false,
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
        transactionToEdit: Transaction? = state.transactionToEdit,
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
        customCategoryIcon: ImageVector? = state.customCategoryIcon,
    ): S

    // Utility methods

    /**
     * Загружает список источников средств
     */
    protected fun loadSources() {
        viewModelScope.launch {
            try {
                val sources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources(
                    sourcePreferences,
                    resources,
                )
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
                addToWallet = state.addToWallet,
            )
        }
        handleBaseEvent(BaseTransactionEvent.ForceSetExpenseType, context)
    }

    /**
     * Универсальный парсер арифметических выражений для суммы, возвращает Money
     * @param expr строка с выражением
     * @param currency валюта (по умолчанию RUB)
     */
    protected fun parseMoneyExpression(expr: String, currency: Currency = Currency.RUB): Money {
        var processedExpr = expr.replace(",", ".")

        Timber.d(
            "parseMoneyExpression: исходное выражение: '$expr', обработанное: '$processedExpr'",
        )

        // Удаляем "висячий" оператор в конце строки, если он есть
        if (processedExpr.isNotEmpty()) {
            val lastChar = processedExpr.last()
            if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/' ||
                lastChar == '×' || lastChar == '÷'
            ) {
                // Обрабатываем случай, когда оператор в конце
                processedExpr = processedExpr.dropLast(1)
                Timber.d(
                    "parseMoneyExpression: удален висячий оператор, новое выражение: '$processedExpr'",
                )
            }
        }

        // Заменяем символы × и ÷ на * и / для корректного вычисления
        processedExpr = processedExpr.replace("×", "*").replace("÷", "/")

        // Если после обработки строка пустая, или состоит только из точки (например, после "123." -> "123")
        // или некорректна, вернем 0
        if (processedExpr.isBlank() || processedExpr == ".") {
            Timber.d("parseMoneyExpression: выражение пустое или только точка, возвращаем 0")
            return Money(BigDecimal.ZERO, currency)
        }

        return try {
            val resNum = ExpressionBuilder(processedExpr).build().evaluate()
            val result = Money(BigDecimal.valueOf(resNum), currency)
            Timber.d("parseMoneyExpression: успешно вычислено, результат: $result")
            result
        } catch (e: Exception) {
            // Если даже после очистки выражение некорректно, возвращаем 0
            Timber.e(e, "parseMoneyExpression: ошибка вычисления выражения '$processedExpr'")
            Money(BigDecimal.ZERO, currency)
        }
    }

    /**
     * Обновляет позиции источников на основе частоты использования
     */
    override fun updateSourcePositions() {
        viewModelScope.launch {
            // Implementation needed
            // Позиции источников обновляются при выходе с экрана
            Timber.d("[SOURCE_SORT] Обновление позиций источников")
        }
    }

    /**
     * Увеличивает счетчик использования категории
     * @param categoryName Имя категории
     * @param isExpense Является ли категория расходной
     */
    fun incrementCategoryUsage(categoryName: String, isExpense: Boolean) {
        if (categoryName.isBlank()) return

        viewModelScope.launch {
            if (isExpense) {
                categoryUsagePreferences.incrementExpenseCategoryUsage(categoryName)
                Timber.d(
                    "CATEGORY: Увеличен счетчик использования расходной категории: %s",
                    categoryName,
                )
            } else {
                categoryUsagePreferences.incrementIncomeCategoryUsage(categoryName)
                Timber.d(
                    "CATEGORY: Увеличен счетчик использования доходной категории: %s",
                    categoryName,
                )
            }
        }
    }

    /**
     * Возвращает количество использований категории
     * @param categoryName Имя категории
     * @param isExpense Является ли категория расходной
     * @return Количество использований категории
     */
    fun getCategoryUsage(categoryName: String, isExpense: Boolean): Int {
        val usage = if (isExpense) {
            categoryUsagePreferences.loadExpenseCategoriesUsage()[categoryName] ?: 0
        } else {
            categoryUsagePreferences.loadIncomeCategoriesUsage()[categoryName] ?: 0
        }
        Timber.d(
            "CATEGORY: Получено количество использований категории %s: %d",
            categoryName,
            usage,
        )
        return usage
    }

    /**
     * Увеличивает счетчик использования источника
     * @param sourceName Имя источника
     */
    fun incrementSourceUsage(sourceName: String) {
        if (sourceName.isBlank()) return

        viewModelScope.launch {
            sourceUsagePreferences.incrementSourceUsage(sourceName)
            Timber.d("SOURCE: Увеличен счетчик использования источника: %s", sourceName)
        }
    }

    /**
     * Возвращает количество использований источника
     * @param sourceName Имя источника
     * @return Количество использований источника
     */
    fun getSourceUsage(sourceName: String): Int {
        val usageMap = sourceUsagePreferences.getSourceUsage()
        val usage = usageMap[sourceName] ?: 0
        Timber.d("SOURCE: Получено количество использований источника %s: %d", sourceName, usage)
        return usage
    }
}
