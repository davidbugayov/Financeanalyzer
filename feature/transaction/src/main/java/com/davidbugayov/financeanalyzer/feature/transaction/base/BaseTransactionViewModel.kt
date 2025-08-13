package com.davidbugayov.financeanalyzer.feature.transaction.base

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
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryIconProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiSubcategory
import com.davidbugayov.financeanalyzer.shared.SharedFacade
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import com.davidbugayov.financeanalyzer.utils.kmp.toShared
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.objecthunter.exp4j.ExpressionBuilder
import timber.log.Timber

abstract class BaseTransactionViewModel<S : BaseTransactionState, E : BaseTransactionEvent>(
    protected val categoriesViewModel: CategoriesViewModel,
    protected val sourcePreferences: SourcePreferences,
    protected val walletRepository: WalletRepository,
    private val sharedFacade: SharedFacade,
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
    abstract override fun onEvent(
        event: E,
        context: android.content.Context,
    )

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
            try {
                sharedFacade.updateWalletBalances(
                    walletIdsToUpdate = walletIds,
                    amountForWallets = amount.toShared(),
                    originalTransaction =
                        originalTransaction?.let {
                            it.toShared()
                        },
                )
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении баланса кошельков через SharedFacade")
            }
        }
    }

    /**
     * Определяет список кошельков для сохранения в транзакции
     * @param isExpense Является ли транзакция расходом
     * @param addToWallet Флаг добавления/списания в/из кошельков
     * @param selectedWallets Список выбранных кошельков
     * @return Список ID кошельков или null
     */
    protected fun getWalletIdsForTransaction(
        isExpense: Boolean,
        addToWallet: Boolean,
        selectedWallets: List<String>,
    ): List<String>? {
        return if (addToWallet && selectedWallets.isNotEmpty()) {
            if (isExpense) {
                Timber.d("Сохраняем кошельки для списания: ${selectedWallets.size} шт.")
            } else {
                Timber.d("Сохраняем кошельки для пополнения: ${selectedWallets.size} шт.")
            }
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
     * @param isExpense Является ли транзакция расходом
     * @return Пара (новое значение флага, новый список выбранных кошельков)
     */
    protected fun handleToggleAddToWallet(
        currentAddToWallet: Boolean,
        isExpense: Boolean,
    ): Pair<Boolean, List<String>> {
        val newAddToWallet = !currentAddToWallet

        return if (newAddToWallet) {
            if (isExpense) {
                // Для расходов выбираем первый доступный кошелёк
                val firstWalletId = wallets.firstOrNull()?.id
                if (firstWalletId != null) {
                    Timber.d("Включение кошелька для расхода, выбираем первый кошелёк: $firstWalletId")
                    Pair(true, listOf(firstWalletId))
                } else {
                    Timber.d("Нет доступных кошельков для расхода")
                    Pair(false, emptyList())
                }
            } else {
                // Для доходов автоматически выбираем все кошельки
                val allWalletIds = wallets.map { it.id }
                Timber.d(
                    "Включение кошельков для дохода, автоматический выбор всех ${allWalletIds.size} кошельков",
                )
                Pair(true, allWalletIds)
            }
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

        val updatedWallets =
            if (selected) {
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
                forceExpense = state.forceExpense,
                isExpense = state.isExpense,
                preventAutoSubmit = false,
                selectedExpenseCategory = "",
                selectedIncomeCategory = "",
                customCategory = "",
                sourceColor = 0,
                customSource = "",
                availableCategoryIcons = state.availableCategoryIcons,
                subcategory = "",
                subcategoryError = false,
                showSubcategoryPicker = false,
                showCustomSubcategoryDialog = false,
                customSubcategory = "",
                availableSubcategories = emptyList(),
            )
        }
    }

    // --- Общая обработка событий для транзакций ---
    open fun handleBaseEvent(
        event: BaseTransactionEvent,
        context: android.content.Context,
    ) {
        when (event) {
            is BaseTransactionEvent.SetAmount ->
                _state.update { state ->
                    copyState(state, amount = event.amount)
                }

            is BaseTransactionEvent.SetTitle ->
                _state.update { state ->
                    copyState(state, title = event.title)
                }

            is BaseTransactionEvent.SetCategory -> {
                _state.update { state ->
                    copyState(
                        state,
                        category = event.category,
                        showCategoryPicker = false,
                        subcategory = "",
                    ) // Сбрасываем подкатегорию
                }
                // Загружаем сабкатегории для выбранной категории
                loadSubcategoriesForCurrentCategory()
            }

            is BaseTransactionEvent.SetNote ->
                _state.update { state ->
                    copyState(state, note = event.note)
                }

            is BaseTransactionEvent.SetDate ->
                _state.update { state ->
                    copyState(state, selectedDate = event.date, showDatePicker = false)
                }

            is BaseTransactionEvent.ShowDatePicker ->
                _state.update { state ->
                    copyState(state, showDatePicker = true)
                }

            is BaseTransactionEvent.HideDatePicker ->
                _state.update { state ->
                    copyState(state, showDatePicker = false)
                }

            is BaseTransactionEvent.ShowCategoryPicker ->
                _state.update { state ->
                    copyState(state, showCategoryPicker = true)
                }

            is BaseTransactionEvent.HideCategoryPicker ->
                _state.update { state ->
                    copyState(state, showCategoryPicker = false)
                }

            is BaseTransactionEvent.ShowCustomCategoryDialog ->
                _state.update { state ->
                    copyState(state, showCustomCategoryDialog = true)
                }

            is BaseTransactionEvent.HideCustomCategoryDialog ->
                _state.update { state ->
                    copyState(state, showCustomCategoryDialog = false, customCategory = "")
                }

            is BaseTransactionEvent.ShowCancelConfirmation ->
                _state.update { state ->
                    copyState(state, showCancelConfirmation = true)
                }

            is BaseTransactionEvent.HideCancelConfirmation ->
                _state.update { state ->
                    copyState(state, showCancelConfirmation = false)
                }

            is BaseTransactionEvent.ClearError ->
                _state.update { state ->
                    copyState(state, error = null)
                }

            is BaseTransactionEvent.HideSuccessDialog ->
                _state.update { state ->
                    copyState(state, isSuccess = false)
                }

            is BaseTransactionEvent.ShowSourcePicker ->
                _state.update { state ->
                    copyState(state, showSourcePicker = true)
                }

            is BaseTransactionEvent.HideSourcePicker ->
                _state.update { state ->
                    copyState(state, showSourcePicker = false)
                }

            is BaseTransactionEvent.ShowCustomSourceDialog ->
                _state.update { state ->
                    copyState(state, showCustomSourceDialog = true)
                }

            is BaseTransactionEvent.HideCustomSourceDialog ->
                _state.update { state ->
                    copyState(state, showCustomSourceDialog = false, customSource = "")
                }

            is BaseTransactionEvent.ShowColorPicker ->
                _state.update { state ->
                    copyState(state, showColorPicker = true)
                }

            is BaseTransactionEvent.HideColorPicker ->
                _state.update { state ->
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

            is BaseTransactionEvent.SetCustomSource ->
                _state.update { state ->
                    copyState(state, customSource = event.source)
                }

            is BaseTransactionEvent.AddCustomSource -> { // Обработка в наследнике
            }

            is BaseTransactionEvent.SetSourceColor ->
                _state.update { state ->
                    copyState(state, sourceColor = event.color)
                }

            is BaseTransactionEvent.ShowWalletSelector ->
                _state.update { state ->
                    copyState(state, showWalletSelector = true)
                }

            is BaseTransactionEvent.HideWalletSelector ->
                _state.update { state ->
                    copyState(state, showWalletSelector = false)
                }

            is BaseTransactionEvent.ToggleAddToWallet -> {
                val (newAddToWallet, newSelectedWallets) =
                    handleToggleAddToWallet(
                        _state.value.addToWallet,
                        _state.value.isExpense,
                    )
                _state.update { state ->
                    copyState(
                        state,
                        addToWallet = newAddToWallet,
                        selectedWallets = newSelectedWallets,
                    )
                }
            }

            is BaseTransactionEvent.SelectWallet -> {
                val updated =
                    if (event.selected) {
                        _state.value.selectedWallets + event.walletId
                    } else {
                        _state.value.selectedWallets - event.walletId
                    }
                _state.update { state ->
                    copyState(state, selectedWallets = updated)
                }
            }

            is BaseTransactionEvent.SelectWallets ->
                _state.update { state ->
                    copyState(state, selectedWallets = event.walletIds)
                }

            is BaseTransactionEvent.SetCustomCategory ->
                _state.update { state ->
                    copyState(state, customCategory = event.category)
                }

            is BaseTransactionEvent.AddCustomCategory -> {
                val isExpense = _state.value.isExpense
                val customCategoryIcon = _state.value.customCategoryIcon
                categoriesViewModel.addCustomCategory(event.category, isExpense, customCategoryIcon)
                val updatedCategories =
                    if (isExpense) {
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

            is BaseTransactionEvent.HideDeleteCategoryConfirmDialog ->
                _state.update { state ->
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
                                    val newState =
                                        if (isExpense) {
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

            is BaseTransactionEvent.ToggleTransactionType -> {
                Timber.d(
                    "ТРАНЗАКЦИЯ: Переключение типа транзакции с %b на %b",
                    _state.value.isExpense,
                    !_state.value.isExpense,
                )

                _state.update {
                    copyState(
                        it,
                        isExpense = !it.isExpense,
                        category = "", // Сбрасываем категорию при смене типа
                        subcategory = "", // Сбрасываем сабкатегорию при смене типа
                    )
                }

                // Устанавливаем категорию по умолчанию для нового типа транзакции
                val isExpense = _state.value.isExpense
                val categories =
                    if (isExpense) {
                        _state.value.expenseCategories
                    } else {
                        _state.value.incomeCategories
                    }

                if (categories.isNotEmpty()) {
                    val defaultCategory = categories.first().name
                    _state.update {
                        copyState(it, category = defaultCategory)
                    }
                }

                Timber.d(
                    "ТРАНЗАКЦИЯ: После переключения типа - isExpense=%b, category=%s",
                    _state.value.isExpense,
                    _state.value.category,
                )
            }

            is BaseTransactionEvent.ResetFieldsForNewTransaction -> {
                Timber.d("ТРАНЗАКЦИЯ: Сброс полей суммы и примечания")
                _state.update { state ->
                    copyState(
                        state,
                        amount = "",
                        amountError = false,
                        note = "", // Сбрасываем также поле примечания
                        subcategory = "", // Сбрасываем сабкатегорию
                    )
                }
            }

            is BaseTransactionEvent.HideDeleteSourceConfirmDialog ->
                _state.update { state ->
                    copyState(state, showDeleteSourceConfirmDialog = false, sourceToDelete = null)
                }

            is BaseTransactionEvent.ForceSetIncomeType ->
                _state.update { state ->
                    copyState(state, isExpense = false)
                }

            is BaseTransactionEvent.ForceSetExpenseType ->
                _state.update { state ->
                    copyState(state, isExpense = true)
                }

            is BaseTransactionEvent.SetCustomCategoryIcon -> {
                _state.update { state ->
                    copyState(state, customCategoryIcon = event.icon)
                }
            }

            is BaseTransactionEvent.SetAmountError ->
                _state.update { state ->
                    copyState(state, amountError = event.isError)
                }

            is BaseTransactionEvent.SetExpenseCategory -> {
                _state.update { state ->
                    copyState(
                        state,
                        category = event.category,
                        selectedExpenseCategory = event.category,
                        categoryError = false,
                    )
                }
                // Загружаем сабкатегории для выбранной категории
                loadSubcategoriesForCurrentCategory()
            }

            is BaseTransactionEvent.SetIncomeCategory -> {
                _state.update { state ->
                    copyState(
                        state,
                        category = event.category,
                        selectedIncomeCategory = event.category,
                        categoryError = false,
                    )
                }
                // Загружаем сабкатегории для выбранной категории
                loadSubcategoriesForCurrentCategory()
            }

            // Обработка событий сабкатегорий
            is BaseTransactionEvent.SetSubcategory ->
                _state.update { state ->
                    copyState(state, subcategory = event.subcategory, subcategoryError = false)
                }

            is BaseTransactionEvent.SetCustomSubcategory ->
                _state.update { state ->
                    copyState(state, customSubcategory = event.subcategory)
                }

            is BaseTransactionEvent.AddCustomSubcategory -> {
                // Обработка добавления кастомной сабкатегории
                viewModelScope.launch {
                    val currentCategory = _state.value.category
                    if (currentCategory.isNotBlank()) {
                        val categoryId = getCategoryIdByName(currentCategory, _state.value.isExpense)
                        if (categoryId != null) {
                            sharedFacade.addSubcategory(event.subcategory, categoryId)
                            loadSubcategoriesForCategory(categoryId)
                        }
                    }
                }
                _state.update { state ->
                    copyState(
                        state,
                        showCustomSubcategoryDialog = false,
                        customSubcategory = "",
                        subcategory = event.subcategory,
                    )
                }
            }

            is BaseTransactionEvent.ShowSubcategoryPicker ->
                _state.update { state ->
                    copyState(state, showSubcategoryPicker = true)
                }

            is BaseTransactionEvent.HideSubcategoryPicker ->
                _state.update { state ->
                    copyState(state, showSubcategoryPicker = false)
                }

            is BaseTransactionEvent.ShowCustomSubcategoryDialog ->
                _state.update { state ->
                    copyState(state, showCustomSubcategoryDialog = true)
                }

            is BaseTransactionEvent.HideCustomSubcategoryDialog ->
                _state.update { state ->
                    copyState(state, showCustomSubcategoryDialog = false, customSubcategory = "")
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
                    val sortedCategories =
                        categories.sortedByDescending {
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
                    val firstCategory =
                        if (state.category.isBlank() && sortedCategories.isNotEmpty()) {
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
                    val sortedCategories =
                        categories.sortedByDescending {
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

        // Подписываемся на изменения валюты для обновления AmountField
        viewModelScope.launch {
            CurrencyProvider.getCurrencyFlow().collect { newCurrency ->
                Timber.d("BaseTransactionViewModel: Получено событие изменения валюты на ${newCurrency.name}")
                // Принудительно обновляем состояние, чтобы AmountField перерисовался
                _state.update { state ->
                    Timber.d("BaseTransactionViewModel: Обновляем состояние для перерисовки AmountField")
                    copyState(state)
                }
            }
        }
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
        subcategory: String = state.subcategory,
        subcategoryError: Boolean = state.subcategoryError,
        showSubcategoryPicker: Boolean = state.showSubcategoryPicker,
        showCustomSubcategoryDialog: Boolean = state.showCustomSubcategoryDialog,
        customSubcategory: String = state.customSubcategory,
        availableSubcategories: List<UiSubcategory> = state.availableSubcategories,
    ): S

    // Utility methods

    /**
     * Загружает список источников средств
     */
    protected fun loadSources() {
        viewModelScope.launch {
            try {
                val sources =
                    com.davidbugayov.financeanalyzer.feature.transaction.base.util.getInitialSources(
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

    /**
     * Универсальный парсер арифметических выражений для суммы, возвращает Money
     * @param expr строка с выражением
     * @param currency валюта (по умолчанию RUB)
     */
    protected fun parseMoneyExpression(
        expr: String,
        currency: Currency = Currency.RUB,
    ): Money {
        // Если входная строка пустая, сразу возвращаем 0
        if (expr.isBlank()) {
            Timber.d("parseMoneyExpression: исходное выражение пустое, возвращаем 0")
            return Money(BigDecimal.ZERO, currency)
        }

        // Удаляем все пробелы из строки
        var processedExpr = expr.replace(" ", "")

        // Заменяем запятые на точки для корректной обработки десятичных чисел
        processedExpr = processedExpr.replace(",", ".")

        Timber.d(
            "parseMoneyExpression: исходное выражение: '$expr', после удаления пробелов и замены запятых: '$processedExpr'",
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

        // Проверяем, содержит ли строка только числа и десятичную точку (без операторов)
        val isSimpleNumber = processedExpr.matches(Regex("^-?\\d+(\\.\\d+)?$"))
        if (isSimpleNumber) {
            try {
                val simpleNumber = BigDecimal(processedExpr)
                val result = Money(simpleNumber, currency)
                Timber.d("parseMoneyExpression: обработано как простое число: $result")
                return result
            } catch (e: NumberFormatException) {
                Timber.e(e, "parseMoneyExpression: ошибка при парсинге простого числа: '$processedExpr'")
                // Если не удалось обработать как простое число, вернем 0
                return Money(BigDecimal.ZERO, currency)
            }
        }

        // Проверяем, содержит ли строка операторы и является математическим выражением
        val containsPlus = processedExpr.contains('+')
        val containsMinus =
            processedExpr.indexOf('-', 1) != -1 // Исключаем первый символ (может быть отрицательное число)
        val containsMultiply = processedExpr.contains('*')
        val containsDivide = processedExpr.contains('/')
        val containsOperators = containsPlus || containsMinus || containsMultiply || containsDivide

        if (containsOperators) {
            try {
                // Дополнительная проверка на валидность выражения
                // Удаляем все недопустимые символы, кроме цифр, точек и операторов
                val cleanedExpr = processedExpr.replace(Regex("[^0-9.+\\-*/]"), "")

                if (cleanedExpr != processedExpr) {
                    Timber.d("parseMoneyExpression: выражение содержало недопустимые символы, очищено: '$cleanedExpr'")
                    processedExpr = cleanedExpr
                }

                val resNum = ExpressionBuilder(processedExpr).build().evaluate()
                val result = Money(BigDecimal.valueOf(resNum), currency)
                Timber.d("parseMoneyExpression: успешно вычислено как выражение, результат: $result")
                return result
            } catch (e: Exception) {
                Timber.e(e, "parseMoneyExpression: ошибка вычисления выражения '$processedExpr', возвращаем 0")
                return Money(BigDecimal.ZERO, currency)
            }
        }

        // Если не удалось распознать ни как число, ни как выражение, возвращаем 0
        Timber.e(
            "parseMoneyExpression: не удалось распознать ни как число, ни как выражение: '$processedExpr', возвращаем 0",
        )
        return Money(BigDecimal.ZERO, currency)
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
    fun incrementCategoryUsage(
        categoryName: String,
        isExpense: Boolean,
    ) {
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

            // Триггер достижения за использование категорий
            com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onCategoryUsed(categoryName)

            // Проверяем, использованы ли все возможные категории
            val expenseCategories = categoryUsagePreferences.loadExpenseCategoriesUsage()
            val incomeCategories = categoryUsagePreferences.loadIncomeCategoriesUsage()
            val totalUniqueCategories = (expenseCategories.keys + incomeCategories.keys).distinct().size

            if (totalUniqueCategories >= 10) { // Если использовано 10+ разных категорий
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "all_categories_used",
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
    fun getCategoryUsage(
        categoryName: String,
        isExpense: Boolean,
    ): Int {
        val usage =
            if (isExpense) {
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

    /**
     * Получает ID категории по имени
     * @param categoryName Имя категории
     * @param isExpense Является ли категория расходной
     * @return ID категории или null
     */
    protected fun getCategoryIdByName(
        categoryName: String,
        isExpense: Boolean,
    ): Long? {
        val categories =
            if (isExpense) {
                categoriesViewModel.expenseCategories.value
            } else {
                categoriesViewModel.incomeCategories.value
            }
        return categories.find { it.name == categoryName }?.id
    }

    /**
     * Загружает сабкатегории для выбранной категории
     * @param categoryId ID категории
     */
    protected fun loadSubcategoriesForCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                val subcategories = sharedFacade.getSubcategoriesByCategoryId(categoryId)
                val uiSubcategories =
                    subcategories.map { subcategory ->
                        UiSubcategory(
                            id = subcategory.id,
                            name = subcategory.name,
                            categoryId = subcategory.categoryId,
                        )
                    }
                _state.update { state ->
                    copyState(state, availableSubcategories = uiSubcategories)
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке сабкатегорий для категории $categoryId")
            }
        }
    }

    /**
     * Загружает сабкатегории для текущей выбранной категории
     */
    protected fun loadSubcategoriesForCurrentCategory() {
        val currentCategory = _state.value.category
        if (currentCategory.isNotBlank()) {
            val categoryId = getCategoryIdByName(currentCategory, _state.value.isExpense)
            if (categoryId != null) {
                loadSubcategoriesForCategory(categoryId)
            }
        }
    }

    /**
     * Загружает подкатегорию по ID и возвращает её название
     * @param subcategoryId ID подкатегории
     * @return Название подкатегории или пустая строка, если не найдена
     */
    protected suspend fun loadSubcategoryById(subcategoryId: Long): String {
        return try {
            sharedFacade.getSubcategoryById(subcategoryId)?.name ?: ""
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке подкатегории по ID: %d", subcategoryId)
            ""
        }
    }
}
