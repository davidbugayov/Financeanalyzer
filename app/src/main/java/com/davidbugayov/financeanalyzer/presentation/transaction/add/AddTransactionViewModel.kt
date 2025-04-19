package com.davidbugayov.financeanalyzer.presentation.transaction.add

import android.content.ComponentName
import android.content.Intent
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
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import timber.log.Timber
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.getInitialSources
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.addCustomSource
import com.davidbugayov.financeanalyzer.presentation.transaction.base.util.deleteCustomSource

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    override val categoriesViewModel: CategoriesViewModel,
    override val sourcePreferences: SourcePreferences,
    private val walletRepository: WalletRepository
) : BaseTransactionViewModel<AddTransactionState, BaseTransactionEvent>() {

    // Расширение для преобразования строки в Double
    private fun String.toDouble(): Double {
        return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    // Храним категории, которые были использованы в этой сессии
    protected val usedCategories = mutableSetOf<Pair<String, Boolean>>() // category to isExpense
    
    /**
     * Флаг автоматического распределения дохода по категориям бюджета.
     * Используется в BudgetScreen при нажатии кнопки "Распределить".
     */
    var autoDistributeIncome: Boolean = false
        private set(value) {
            field = value
            Timber.d("autoDistributeIncome установлено в $value")
        }

    // Ссылка на BudgetViewModel для автоматического распределения дохода
    private var budgetViewModel: com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel? = null

    // Сохраняем ID целевого кошелька отдельно, чтобы иметь возможность восстановить его
    var storedTargetWalletId: String? = null
        private set

    // Список доступных кошельков с внутренним MutableStateFlow для обновлений
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    override val wallets: List<Wallet>
        get() = _wallets.value

    /**
     * Метод для возврата к предыдущему экрану (будет вызываться из AddTransactionScreen)
     */
    var navigateBackCallback: (() -> Unit)? = null
     
    // Callback, который будет вызван после успешного добавления дохода
    var onIncomeAddedCallback: ((com.davidbugayov.financeanalyzer.domain.model.Money) -> Unit)? = null
     
    // Callback, который будет вызван после успешного добавления расхода
    var onExpenseAddedCallback: ((com.davidbugayov.financeanalyzer.domain.model.Money) -> Unit)? = null

    /**
     * Устанавливает ID целевого кошелька для добавления дохода
     * и автоматически включает опцию добавления в кошелек
     */
    fun setTargetWalletId(walletId: String) {
        Timber.d("setTargetWalletId вызван с ID: $walletId")
        
        // Сохраняем ID кошелька в отдельном поле для восстановления состояния
        storedTargetWalletId = walletId
        Timber.d("storedTargetWalletId сохранен: $storedTargetWalletId")
        
        _state.update { 
            it.copy(
                targetWalletId = walletId,
                addToWallet = true,
                selectedWallets = listOf(walletId)
            ) 
        }
        // Выводим состояние после обновления для проверки
        Timber.d("После установки targetWalletId: targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}, selectedWallets=${_state.value.selectedWallets}")
        
        // Убрал автоматический вызов ForceSetIncomeType, чтобы не переключать тип транзакции автоматически
    }

    init {
        // Загружаем категории
        loadInitialData()
        
        // Инициализируем список источников
        // initSources() // Called within loadInitialData now

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
     * Загружает начальные данные: категории и источники.
     */
    private fun loadInitialData() {
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
        if (categoriesViewModel == null) {
            Timber.e("categoriesViewModel is null in loadCategories()")
            return
        }
        
        viewModelScope.launch {
            try {
                categoriesViewModel.expenseCategories?.collect { categories ->
                    _state.update { it.copy(expenseCategories = categories) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке категорий расходов")
            }
        }
        
        viewModelScope.launch {
            try {
                categoriesViewModel.incomeCategories?.collect { categories ->
                    _state.update { it.copy(incomeCategories = categories) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке категорий доходов")
            }
        }
    }

    /**
     * Сбрасывает все поля формы до значений по умолчанию
     */
    override fun resetFields() {
        val currentTargetWalletId = _state.value.targetWalletId ?: storedTargetWalletId
        val useStoredWallet = currentTargetWalletId != null
        
        _state.update {
            AddTransactionState(
                // Сохраняем списки категорий и источников
                expenseCategories = it.expenseCategories,
                incomeCategories = it.incomeCategories,
                sources = it.sources,
                // Устанавливаем значения по умолчанию для источника
                source = "Сбер",
                sourceColor = ColorUtils.SBER_COLOR,
                // Всегда устанавливаем тип "Расход" по умолчанию
                isExpense = true,
                // Если это доход и установлена блокировка, устанавливаем категорию дохода
                category = "",
                // Сохраняем настройки кошелька, если есть сохраненное значение
                targetWalletId = if (useStoredWallet) currentTargetWalletId else null,
                addToWallet = if (useStoredWallet) true else false,
                selectedWallets = if (useStoredWallet && currentTargetWalletId != null) 
                                     listOf(currentTargetWalletId) 
                                  else 
                                     emptyList()
            )
        }
    }

    /**
     * Обновляет позиции всех использованных категорий
     */
    override fun updateCategoryPositions() {
        viewModelScope.launch {
            usedCategories.forEach { (category, isExpense) ->
                categoriesViewModel.incrementCategoryUsage(category, isExpense)
            }
            // Очищаем список использованных категорий
            usedCategories.clear()
        }
    }

    /**
     * Сбрасывает состояние ViewModel к значениям по умолчанию.
     * Используется при навигации на экран добавления транзакции из мест,
     * где не требуется предзаполненное или измененное состояние (например, из HomeScreen).
     */
    fun resetToDefaultState() {
        Timber.d("Вызов resetToDefaultState, текущее состояние: targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}, selectedWallets=${_state.value.selectedWallets}, storedTargetWalletId=$storedTargetWalletId, autoDistributeIncome=$autoDistributeIncome")
        
        // Сбрасываем флаг автоматического распределения
        autoDistributeIncome = false
        
        // Сохраняем ID кошелька, если он был установлен ранее
        val currentTargetWalletId = storedTargetWalletId
        val hasWalletData = currentTargetWalletId != null
        
        // Очищаем сохраненный targetWalletId
        storedTargetWalletId = null
        
        // Создаем новое состояние с сохранением необходимых данных
        _state.update { 
            AddTransactionState(
                isExpense = true,
                // Сохраняем категории и источники из текущего состояния
                expenseCategories = it.expenseCategories,
                incomeCategories = it.incomeCategories,
                sources = it.sources,
                // Сбрасываем настройки кошельков при новом переходе
                targetWalletId = null,
                addToWallet = false,
                selectedWallets = emptyList()
            ) 
        }
        
        Timber.d("После resetToDefaultState: targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}, selectedWallets=${_state.value.selectedWallets}, storedTargetWalletId=$storedTargetWalletId")
        
        loadInitialData() // Перезагружаем кошельки и категории
        onIncomeAddedCallback = null // Сбрасываем коллбэк
    }

    /**
     * Настройка ViewModel для добавления дохода с указанной суммой
     * и опционального распределения по кошелькам
     */
    fun setupForIncomeAddition(amount: String, shouldDistribute: Boolean) {
        Timber.d("setupForIncomeAddition: amount=$amount, shouldDistribute=$shouldDistribute")
        Timber.d("Текущее состояние перед setupForIncomeAddition: targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}, selectedWallets=${_state.value.selectedWallets}, isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}")
        
        // Устанавливаем флаг автоматического распределения
        autoDistributeIncome = shouldDistribute
        
        // Сохраняем настройки кошелька
        val currentTargetWalletId = _state.value.targetWalletId
        val currentSelectedWallets = _state.value.selectedWallets
        val currentAddToWallet = _state.value.addToWallet || shouldDistribute // Если включено распределение, включаем добавление в кошельки
        
        // Принудительно устанавливаем тип дохода
        _state.update {
            it.copy(
                isExpense = false,
                forceExpense = false,
                amount = amount,
                // Важно: восстанавливаем сохраненные значения
                targetWalletId = currentTargetWalletId,
                selectedWallets = currentSelectedWallets,
                addToWallet = currentAddToWallet
            )
        }
        
        // Добавляем дополнительный лог для отладки
        Timber.d("=== ВНИМАНИЕ: Принудительно установлен доход в setupForIncomeAddition ===")
        Timber.d("Автоматическое распределение: $shouldDistribute")
        
        // Логируем состояние после установки
        Timber.d("Состояние после setupForIncomeAddition: targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}, selectedWallets=${_state.value.selectedWallets}, isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}")
    }

    /**
     * Настраивает ViewModel для добавления дохода.
     * @param amount Предустановленная сумма дохода (если есть).
     * @param targetWalletId ID кошелька, в который добавляется доход.
     */
    fun setupForIncomeAddition(amount: String, targetWalletId: String, context: android.content.Context) {
        Timber.d("setupForIncomeAddition: amount=$amount, targetWalletId=$targetWalletId")
        if (_state == null) {
            Timber.e("_state is null in setupForIncomeAddition()")
            return
        }
        Timber.d("Текущее состояние перед setupForIncomeAddition: isExpense=${_state.value.isExpense}, targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}")
        // Устанавливаем целевой кошелек для дохода
        onEvent(BaseTransactionEvent.SetTargetWalletId(targetWalletId), context)
        // Включаем добавление в кошелек
        if (!_state.value.addToWallet) {
            onEvent(BaseTransactionEvent.ToggleAddToWallet, context)
        }
        // Принудительно устанавливаем тип "Доход", но без блокировки переключения
        onEvent(BaseTransactionEvent.ForceSetIncomeType, context)
        // Устанавливаем предзаполненную сумму, если есть
        _state.update {
            it.copy(
                amount = amount
            )
        }
        Timber.d("После setupForIncomeAddition: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}, targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}")
    }

    /**
     * Настраивает ViewModel для добавления расхода.
     * @param amount Предустановленная сумма расхода (если есть).
     * @param walletCategory Категория кошелька для списания.
     */
    fun setupForExpenseAddition(amount: String, walletCategory: String, context: android.content.Context) {
        Timber.d("setupForExpenseAddition: amount=$amount, walletCategory=$walletCategory")
        if (_state == null) {
            Timber.e("_state is null in setupForExpenseAddition()")
            return
        }
        Timber.d("Текущее состояние перед setupForExpenseAddition: targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}, isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}")
        
        // Устанавливаем флаг автоматического распределения дохода
        autoDistributeIncome = false
        
        // Выводим лог до вызова ForceSetExpenseType
        Timber.d("ПЕРЕД вызовом ForceSetExpenseType: isExpense=${_state.value.isExpense}")
        
        // Сохраняем текущие настройки кошелька
        val currentTargetWalletId = _state.value.targetWalletId
        val currentSelectedWallets = _state.value.selectedWallets
        val currentAddToWallet = _state.value.addToWallet
        
        // Используем событие ForceSetExpenseType
        onEvent(BaseTransactionEvent.ForceSetExpenseType, context)
        
        // Выводим лог сразу после вызова ForceSetExpenseType
        Timber.d("СРАЗУ ПОСЛЕ вызова ForceSetExpenseType: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}")
        
        // Восстанавливаем настройки кошелька, которые могли быть изменены
        _state.update {
            it.copy(
                amount = amount,
                category = walletCategory, // Используем название кошелька как категорию
                targetWalletId = currentTargetWalletId,
                selectedWallets = currentSelectedWallets,
                addToWallet = currentAddToWallet
            )
        }
        
        // Добавляем отладочный лог
        Timber.d("После setupForExpenseAddition: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}, targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}")
    }

    /**
     * Отправляет транзакцию (добавляет новую или обновляет существующую)
     */
    override fun submitTransaction(context: android.content.Context) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            if (!validateInput()) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            // Always call addTransaction now
            addTransaction(context)
        }
    }

    /**
     * Обрабатывает события экрана добавления транзакции
     */
    override fun onEvent(event: BaseTransactionEvent, context: android.content.Context) {
        when (event) {
            is BaseTransactionEvent.Submit -> submitTransaction(context)
            else -> handleBaseEvent(event, context)
        }
    }

    private fun handleBaseEvent(event: BaseTransactionEvent, context: android.content.Context) {
        when (event) {
            is BaseTransactionEvent.SetAmount -> _state.update { it.copy(amount = event.amount) }
            is BaseTransactionEvent.SetTitle -> _state.update { it.copy(title = event.title) }
            is BaseTransactionEvent.SetCategory -> _state.update { it.copy(category = event.category, showCategoryPicker = false) }
            is BaseTransactionEvent.SetNote -> _state.update { it.copy(note = event.note) }
            is BaseTransactionEvent.SetDate -> _state.update { it.copy(selectedDate = event.date, showDatePicker = false) }
            is BaseTransactionEvent.ToggleTransactionType -> {
                val newIsExpense = !_state.value.isExpense
                val previousSource = _state.value.source
                val previousSourceColor = _state.value.sourceColor
                _state.update { it.copy(isExpense = newIsExpense, category = "") }
                viewModelScope.launch {
                    kotlinx.coroutines.delay(50)
                    _state.update { it.copy(source = previousSource, sourceColor = previousSourceColor) }
                }
            }
            is BaseTransactionEvent.ShowDatePicker -> _state.update { it.copy(showDatePicker = true) }
            is BaseTransactionEvent.HideDatePicker -> _state.update { it.copy(showDatePicker = false) }
            is BaseTransactionEvent.ShowCategoryPicker -> _state.update { it.copy(showCategoryPicker = true) }
            is BaseTransactionEvent.HideCategoryPicker -> _state.update { it.copy(showCategoryPicker = false) }
            is BaseTransactionEvent.ShowCustomCategoryDialog -> _state.update { it.copy(showCustomCategoryDialog = true) }
            is BaseTransactionEvent.HideCustomCategoryDialog -> _state.update { it.copy(showCustomCategoryDialog = false, customCategory = "") }
            is BaseTransactionEvent.ShowCancelConfirmation -> _state.update { it.copy(showCancelConfirmation = true) }
            is BaseTransactionEvent.HideCancelConfirmation -> _state.update { it.copy(showCancelConfirmation = false) }
            is BaseTransactionEvent.ClearError -> _state.update { it.copy(error = null) }
            is BaseTransactionEvent.HideSuccessDialog -> _state.update { it.copy(isSuccess = false) }
            is BaseTransactionEvent.ShowSourcePicker -> _state.update { it.copy(showSourcePicker = true) }
            is BaseTransactionEvent.HideSourcePicker -> _state.update { it.copy(showSourcePicker = false) }
            is BaseTransactionEvent.ShowCustomSourceDialog -> _state.update { it.copy(showCustomSourceDialog = true) }
            is BaseTransactionEvent.HideCustomSourceDialog -> _state.update { it.copy(showCustomSourceDialog = false, customSource = "") }
            is BaseTransactionEvent.ShowColorPicker -> _state.update { it.copy(showColorPicker = true) }
            is BaseTransactionEvent.HideColorPicker -> _state.update { it.copy(showColorPicker = false) }
            is BaseTransactionEvent.SetSource -> {
                val selectedSource = _state.value.sources.find { it.name == event.source }
                _state.update { it.copy(source = event.source, sourceColor = selectedSource?.color ?: it.sourceColor) }
            }
            is BaseTransactionEvent.SetCustomSource -> _state.update { it.copy(customSource = event.source) }
            is BaseTransactionEvent.AddCustomSource -> {
                try {
                    if (event.source.isBlank()) {
                        _state.update { it.copy(error = "Название источника не может быть пустым") }
                        return
                    }
                    val newSource = Source(name = event.source, color = event.color, isCustom = true)
                    val updatedSources = addCustomSource(sourcePreferences, _state.value.sources, newSource)
                    _state.update {
                        it.copy(
                            sources = updatedSources,
                            source = event.source,
                            sourceColor = event.color,
                            showSourcePicker = false,
                            showCustomSourceDialog = false,
                            customSource = ""
                        )
                    }
                    Timber.d("Custom source added directly: ${newSource.name} with color ${newSource.color}")
                } catch (e: Exception) {
                    Timber.e(e, "Error adding custom source: ${e.message}")
                    _state.update { it.copy(error = "Ошибка при добавлении источника: ${e.message}") }
                }
            }
            is BaseTransactionEvent.SetSourceColor -> _state.update { it.copy(sourceColor = event.color) }
            is BaseTransactionEvent.ShowWalletSelector -> _state.update { it.copy(showWalletSelector = true) }
            is BaseTransactionEvent.HideWalletSelector -> _state.update { it.copy(showWalletSelector = false) }
            is BaseTransactionEvent.ToggleAddToWallet -> _state.update { it.copy(addToWallet = !_state.value.addToWallet) }
            is BaseTransactionEvent.SelectWallet -> {
                val updated = if (event.selected) {
                    _state.value.selectedWallets + event.walletId
                } else {
                    _state.value.selectedWallets - event.walletId
                }
                _state.update { it.copy(selectedWallets = updated) }
            }
            is BaseTransactionEvent.SelectWallets -> _state.update { it.copy(selectedWallets = event.walletIds) }
            else -> {}
        }
    }

    /**
     * Проверяет валидность введенных данных перед добавлением транзакции
     */
    protected fun validateInput(): Boolean {
        val currentState = _state.value
        return validateBaseFields(
            amount = currentState.amount,
            category = currentState.category,
            source = currentState.source
        ) { amountError, categoryError, sourceError, errorMsg ->
            _state.update {
                it.copy(
                    amountError = amountError,
                    categoryError = categoryError,
                    sourceError = sourceError,
                    error = errorMsg
                )
            }
        }
    }

    /**
     * Загружает список доступных кошельков
     */
    private fun loadWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем список кошельков из репозитория
                val walletsList = walletRepository.getAllWallets()
                _wallets.value = walletsList
                
                // Если список выбранных кошельков пуст, предварительно выбираем все кошельки
                if (_state.value.selectedWallets.isEmpty()) {
                    val walletIds = walletsList.map { it.id }
                    _state.update { it.copy(selectedWallets = walletIds) }
                }
                
                Timber.d("Загружено ${walletsList.size} кошельков")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
            }
        }
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
                        // Сохраняем категорию, которую пользователь использовал
                        usedCategories.add(Pair(currentState.category, currentState.isExpense))

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
                                    walletRepository = walletRepository,
                                    walletIds = currentState.selectedWallets,
                                    totalAmount = Money(amount),
                                    isExpense = false
                                )
                            }
                        }
                        
                        // Проверяем, нужно ли распределить доход по бюджету или обновить конкретный кошелек
                        if (!currentState.isExpense) {
                            val incomeAmount = Money(amount)
                            
                            // Если есть целевой кошелек или включено автоматическое распределение,
                            // вызываем соответствующий коллбэк
                            if (currentState.targetWalletId != null || autoDistributeIncome) {
                                // Вызываем callback для обновления баланса кошелька или распределения дохода
                                onIncomeAddedCallback?.invoke(incomeAmount)
                                Timber.d("Вызван callback для обновления баланса кошелька: ${currentState.targetWalletId}")
                            }
                            
                            // Или используем BudgetViewModel напрямую, если он доступен и включено автораспределение
                            if (autoDistributeIncome) {
                                budgetViewModel?.let { viewModel ->
                                    viewModel.onEvent(
                                        com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent.DistributeIncome(
                                            incomeAmount
                                        )
                                    )
                                    Timber.d("Доход автоматически распределен: $amount")
                                }
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
     * Добавляет доход в выбранные кошельки, распределяя сумму между ними
     */
    private fun addIncomeToWallets(walletIds: List<String>, totalAmount: Money) {
        if (walletIds.isEmpty()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем список кошельков по их ID
                val walletsList = walletRepository.getWalletsByIds(walletIds)
                
                if (walletsList.isEmpty()) {
                    Timber.e("Не найдено кошельков для добавления дохода")
                    return@launch
                }
                
                // Равномерно распределяем сумму между кошельками
                val amountPerWallet = totalAmount / walletsList.size
                
                // Обновляем каждый кошелек
                for (wallet in walletsList) {
                    // Увеличиваем баланс кошелька
                    val updatedWallet = wallet.copy(
                        balance = wallet.balance.plus(amountPerWallet)
                    )
                    
                    // Сохраняем обновленный кошелек
                    walletRepository.updateWallet(updatedWallet)
                }
                
                Timber.d("Доход $totalAmount добавлен в ${walletsList.size} кошельков")
                
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при добавлении дохода в кошельки")
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
        validateInput()
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
    fun clearSelectedWallets() {
        Timber.d("Очистка списка выбранных кошельков")
        _state.update { it.copy(selectedWallets = emptyList()) }
    }
    
    /**
     * Выбирает все доступные кошельки
     */
    fun selectAllWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем все доступные кошельки
                val allWallets = walletRepository.getAllWallets()
                
                // Собираем их ID
                val allWalletIds = allWallets.map { it.id }
                
                // Включаем флаг добавления в кошельки
                _state.update { 
                    it.copy(
                        selectedWallets = allWalletIds,
                        addToWallet = true
                    ) 
                }
                
                Timber.d("Выбраны все кошельки: ${allWalletIds.size} шт.")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при выборе всех кошельков")
            }
        }
    }

    /**
     * Выбирает все доступные кошельки без показа диалога выбора
     * Используется при автоматическом выборе кошельков из HomeScreen
     */
    fun selectAllWalletsWithoutDialog() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем все доступные кошельки
                val allWallets = walletRepository.getAllWallets()
                
                // Собираем их ID
                val allWalletIds = allWallets.map { it.id }
                
                // Включаем флаг добавления в кошельки и добавляем все кошельки
                // Особенность: НЕ показываем диалог выбора кошельков (showWalletSelector = false)
                _state.update { 
                    it.copy(
                        selectedWallets = allWalletIds,
                        addToWallet = true,
                        showWalletSelector = false
                    ) 
                }
                
                Timber.d("Автоматически выбраны все кошельки без показа диалога: ${allWalletIds.size} шт.")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при автоматическом выборе всех кошельков")
            }
        }
    }

    /**
     * Загружает транзакцию для редактирования
     */
    fun loadTransactionForEditing(transaction: Transaction) {
        Timber.d("Загрузка транзакции для редактирования: $transaction")
        
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
            sourceColor = transaction.sourceColor,
            editMode = true
        ) }
        
        Timber.d("Состояние после загрузки: сумма=${_state.value.amount}, дата=${_state.value.selectedDate}, режим редактирования=${_state.value.editMode}")
    }

    protected override val _state = MutableStateFlow(AddTransactionState())

    override fun addCustomCategory(category: String) {
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

    override fun deleteCategory(category: String) {
        if (category == "Другое") {
            _state.update { it.copy(error = "Категорию \"Другое\" нельзя удалить") }
            return
        }
        viewModelScope.launch {
            if (_state.value.isExpense) {
                categoriesViewModel.deleteExpenseCategory(category)
            } else {
                categoriesViewModel.deleteIncomeCategory(category)
            }
            if (_state.value.category == category) {
                _state.update { it.copy(category = "") }
            }
            com.davidbugayov.financeanalyzer.utils.AnalyticsUtils.logCategoryDeleted(category, _state.value.isExpense)
        }
    }

    override fun addCustomSource(source: String, color: Int) {
        try {
            if (source.isBlank()) {
                _state.update { it.copy(error = "Название источника не может быть пустым") }
                return
            }
            val newSource = com.davidbugayov.financeanalyzer.domain.model.Source(name = source, color = color, isCustom = true)
            val updatedSources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.addCustomSource(sourcePreferences, _state.value.sources, newSource)
            _state.update {
                it.copy(
                    sources = updatedSources,
                    source = source,
                    sourceColor = color,
                    showSourcePicker = false,
                    showCustomSourceDialog = false,
                    customSource = ""
                )
            }
            timber.log.Timber.d("Custom source added: ${newSource.name} with color ${newSource.color}. Updated sources: ${updatedSources.map { it.name }}")
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Error adding custom source: ${e.message}")
            _state.update { it.copy(error = "Ошибка при добавлении источника: ${e.message}") }
        }
    }

    override fun deleteSource(source: String) {
        if (source == "Сбер" || source == "Наличные" || source == "Т-Банк") {
            _state.update { it.copy(error = "Стандартные источники удалить нельзя") }
            return
        }
        val updatedSources = com.davidbugayov.financeanalyzer.presentation.transaction.base.util.deleteCustomSource(sourcePreferences, _state.value.sources, source)
        _state.update { it.copy(sources = updatedSources) }
        if (_state.value.source == source) {
            _state.update {
                it.copy(
                    source = "Сбер",
                    sourceColor = 0xFF21A038.toInt()
                )
            }
        }
        com.davidbugayov.financeanalyzer.utils.AnalyticsUtils.logSourceDeleted(source)
    }
} 