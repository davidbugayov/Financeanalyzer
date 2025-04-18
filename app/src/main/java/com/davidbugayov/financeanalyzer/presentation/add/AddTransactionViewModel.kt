package com.davidbugayov.financeanalyzer.presentation.add

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
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
import java.util.Locale
import com.davidbugayov.financeanalyzer.domain.model.Currency

/**
 * ViewModel для экрана добавления транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
class AddTransactionViewModel(
    application: Application,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val categoriesViewModel: CategoriesViewModel,
    private val sourcePreferences: SourcePreferences,
    private val walletRepository: WalletRepository
) : AndroidViewModel(application), KoinComponent {

    // Расширение для преобразования строки в Double
    private fun String.toDouble(): Double {
        return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    // Храним категории, которые были использованы в этой сессии
    private val usedCategories = mutableSetOf<Pair<String, Boolean>>() // category to isExpense
    
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
    val wallets: List<Wallet>
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
    fun setupForIncomeAddition(amount: String, targetWalletId: String) {
        Timber.d("setupForIncomeAddition: amount=$amount, targetWalletId=$targetWalletId")
        Timber.d("Текущее состояние перед setupForIncomeAddition: isExpense=${_state.value.isExpense}, targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}")
        
        // Устанавливаем целевой кошелек для дохода
        onEvent(AddTransactionEvent.SetTargetWalletId(targetWalletId))
        
        // Включаем добавление в кошелек
        if (!_state.value.addToWallet) {
            onEvent(AddTransactionEvent.ToggleAddToWallet)
        }
        
        // Принудительно устанавливаем тип "Доход", но без блокировки переключения
        onEvent(AddTransactionEvent.ForceSetIncomeType)
        
        // Устанавливаем предзаполненную сумму, если есть
        _state.update {
            it.copy(
                amount = amount
            )
        }
        
        // Добавляем отладочный лог
        Timber.d("После setupForIncomeAddition: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}, targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}")
    }

    /**
     * Настраивает ViewModel для добавления расхода.
     * @param amount Предустановленная сумма расхода (если есть).
     * @param walletCategory Категория кошелька для списания.
     */
    fun setupForExpenseAddition(amount: String, walletCategory: String) {
        Timber.d("setupForExpenseAddition: amount=$amount, walletCategory=$walletCategory")
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
        onEvent(AddTransactionEvent.ForceSetExpenseType)
        
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
            is AddTransactionEvent.ForceSetIncomeType -> {
                val currentTargetWalletId = _state.value.targetWalletId
                val currentSelectedWallets = _state.value.selectedWallets
                val currentAddToWallet = _state.value.addToWallet
                
                Timber.d("ForceSetIncomeType начало обработки: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}")
                Timber.d("Forced income type selection with wallet settings: targetWalletId=$currentTargetWalletId, addToWallet=$currentAddToWallet, selectedWallets=$currentSelectedWallets")
                
                _state.update {
                    it.copy(
                        isExpense = false,
                        forceExpense = false, // Выключаем принудительный расход
                        // Сохраняем настройки кошелька при принудительном переключении на доход
                        targetWalletId = currentTargetWalletId,
                        selectedWallets = currentSelectedWallets,
                        addToWallet = currentAddToWallet
                    )
                }
                
                Timber.d("ForceSetIncomeType после обработки: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}, targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}")
            }
            
            is AddTransactionEvent.ForceSetExpenseType -> {
                val currentTargetWalletId = _state.value.targetWalletId
                val currentSelectedWallets = _state.value.selectedWallets
                val currentAddToWallet = _state.value.addToWallet
                
                Timber.d("ForceSetExpenseType начало обработки: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}")
                Timber.d("Forced expense type selection: targetWalletId=$currentTargetWalletId, selectedWallets=$currentSelectedWallets, addToWallet=$currentAddToWallet")
                
                _state.update {
                    it.copy(
                        isExpense = true,
                        forceExpense = true, // Включаем принудительный расход
                        // Сохраняем настройки кошелька при принудительном переключении на расход
                        targetWalletId = currentTargetWalletId,
                        selectedWallets = currentSelectedWallets,
                        addToWallet = currentAddToWallet
                    )
                }
                
                Timber.d("ForceSetExpenseType после обработки: isExpense=${_state.value.isExpense}, forceExpense=${_state.value.forceExpense}, targetWalletId=${_state.value.targetWalletId}, addToWallet=${_state.value.addToWallet}")
            }
            
            is AddTransactionEvent.SetTargetWalletId -> {
                _state.update { 
                    it.copy(
                        targetWalletId = event.walletId
                    ) 
                }
                Timber.d("Установлен targetWalletId: ${event.walletId}")
            }
            
            // Обработка событий для работы с кошельками
            is AddTransactionEvent.ToggleAddToWallet -> {
                // Переключаем состояние добавления в кошелек
                val newAddToWallet = !_state.value.addToWallet
                
                // Сохраняем текущие кошельки для предотвращения потери данных
                val currentSelectedWallets = _state.value.selectedWallets.toMutableList()
                
                // Если includeToWallet стал true и у нас есть targetWalletId, но его нет в списке,
                // добавляем его в список выбранных кошельков
                val targetWalletId = _state.value.targetWalletId
                if (newAddToWallet && targetWalletId != null && !currentSelectedWallets.contains(targetWalletId)) {
                    Timber.d("ToggleAddToWallet: добавляем targetWalletId=$targetWalletId в список выбранных кошельков")
                    currentSelectedWallets.add(targetWalletId)
                }
                
                // Обновляем состояние
                _state.update { it.copy(addToWallet = newAddToWallet, selectedWallets = currentSelectedWallets) }
                
                // Если включено добавление в кошельки, и список выбранных кошельков пуст,
                // только тогда открываем диалог выбора кошельков
                if (newAddToWallet && _state.value.selectedWallets.isEmpty()) {
                    Timber.d("ToggleAddToWallet: открываем диалог выбора кошельков, так как selectedWallets пуст")
                    onEvent(AddTransactionEvent.ShowWalletSelector)
                } else {
                    Timber.d("ToggleAddToWallet: не открываем диалог, так как selectedWallets не пуст: ${_state.value.selectedWallets.size} шт.")
                }
            }
            
            is AddTransactionEvent.ShowWalletSelector -> {
                _state.update { it.copy(showWalletSelector = true) }
                // Здесь можно загрузить список кошельков, если он еще не загружен
                loadWallets()
            }
            
            is AddTransactionEvent.HideWalletSelector -> {
                _state.update { it.copy(showWalletSelector = false) }
            }
            
            is AddTransactionEvent.SelectWallet -> {
                val currentSelected = _state.value.selectedWallets.toMutableList()
                
                if (event.selected) {
                    // Добавляем кошелек, если его еще нет в списке
                    if (!currentSelected.contains(event.walletId)) {
                        currentSelected.add(event.walletId)
                    }
                } else {
                    // Удаляем кошелек из списка
                    currentSelected.remove(event.walletId)
                }
                
                _state.update { it.copy(selectedWallets = currentSelected) }
            }
            
            is AddTransactionEvent.SelectWallets -> {
                _state.update { it.copy(selectedWallets = event.walletIds) }
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
    private fun addTransaction() {
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
                        updateWidget()

                        // Запрашиваем обновление данных
                        requestDataRefresh()

                        // Обновляем категории
                        updateCategoryPositions()
                        
                        // Если это доход и установлен флаг добавления в кошелек
                        if (!currentState.isExpense && currentState.addToWallet) {
                            // Распределяем доход по выбранным кошелькам
                            if (currentState.selectedWallets.isNotEmpty()) {
                                addIncomeToWallets(
                                    walletIds = currentState.selectedWallets,
                                    totalAmount = Money(amount)
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
                        _state.update { it.copy(error = "ID транзакции не указан", isLoading = false) }
                    }
                    return@launch
                }

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
                            error = e.message ?: "Непредвиденная ошибка при обновлении",
                            isSuccess = false,
                            isLoading = false
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
     * Валидирует сумму транзакции
     */
    private fun validateAmount() {
        try {
            val money = Money.fromString(_state.value.amount)
            val hasInvalidAmount = money.isZero() || _state.value.amount.isBlank()
            if (hasInvalidAmount) {
                _state.update { it.copy(amountError = true) }
            } else {
                _state.update { it.copy(amountError = false) }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при парсинге суммы: ${_state.value.amount}")
            _state.update { it.copy(amountError = true) }
        }
    }

    // Выбранная валюта (рубль по умолчанию)
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
        validateAmount()
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
     * Загружает транзакцию для редактирования по ID
     */
    fun loadTransactionForEdit(transactionId: String) {
        viewModelScope.launch {
            try {
                Timber.d("Загрузка транзакции для редактирования с ID: $transactionId")
                
                // Загружаем транзакцию из репозитория
                val transaction = getTransactionRepositoryInstance().getTransactionById(transactionId)
                
                if (transaction != null) {
                    Timber.d("Транзакция найдена, загружаем для редактирования")
                    loadTransactionForEditing(transaction)
                } else {
                    Timber.e("Транзакция с ID $transactionId не найдена")
                    _state.update { 
                        it.copy(
                            error = "Транзакция не найдена",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке транзакции с ID $transactionId")
                _state.update { 
                    it.copy(
                        error = "Ошибка загрузки: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    /**
     * Загружает транзакцию для редактирования и затем осуществляет навигацию на экран редактирования
     * @param transaction Транзакция для редактирования
     * @param onNavigateToEdit Функция для навигации на экран редактирования, принимающая ID транзакции
     */
    fun loadTransactionAndNavigateToEdit(transaction: Transaction, onNavigateToEdit: (String) -> Unit) {
        // Логируем действие
        Timber.d("Загрузка транзакции для редактирования и навигация: ${transaction.id}")
        
        try {
            // Загружаем транзакцию в текущее состояние
            loadTransactionForEditing(transaction)
            
            // После загрузки переходим на экран редактирования с ID транзакции
            onNavigateToEdit(transaction.id)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при подготовке транзакции к редактированию: ${e.message}")
            _state.update { 
                it.copy(
                    error = "Ошибка при подготовке к редактированию: ${e.message}"
                ) 
            }
        }
    }
} 