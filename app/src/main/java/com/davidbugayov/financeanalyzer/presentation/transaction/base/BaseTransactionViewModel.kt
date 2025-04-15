package com.davidbugayov.financeanalyzer.presentation.transaction.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.ValidationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber

/**
 * Базовый класс ViewModel для экранов работы с транзакциями.
 * Содержит общую логику для добавления и редактирования транзакций.
 */
abstract class BaseTransactionViewModel(
    application: Application,
    protected val addTransactionUseCase: AddTransactionUseCase,
    protected val updateTransactionUseCase: UpdateTransactionUseCase,
    protected val categoriesViewModel: CategoriesViewModel,
    protected val walletRepository: WalletRepository,
    open protected val txRepository: TransactionRepository
) : AndroidViewModel(application), KoinComponent {

    // Расширение для преобразования строки в Double
    protected fun String.toDouble(): Double {
        return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    // Состояние ViewModel
    protected abstract val _state: MutableStateFlow<out BaseTransactionState>
    abstract val state: StateFlow<out BaseTransactionState>

    // Храним категории, которые были использованы в этой сессии
    protected val usedCategories = mutableSetOf<Pair<String, Boolean>>() // category to isExpense

    /**
     * Флаг автоматического распределения дохода по категориям бюджета.
     * Используется в BudgetScreen при нажатии кнопки "Распределить".
     */
    var autoDistributeIncome: Boolean = false
        protected set(value) {
            field = value
            Timber.d("autoDistributeIncome установлено в $value")
        }

    // Сохраняем ID целевого кошелька отдельно, чтобы иметь возможность восстановить его
    var storedTargetWalletId: String? = null
        protected set

    // Список доступных кошельков с внутренним MutableStateFlow для обновлений
    protected val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: List<Wallet>
        get() = _wallets.value

    /**
     * Метод для возврата к предыдущему экрану
     */
    var navigateBackCallback: (() -> Unit)? = null

    // Make callbacks open for overriding
    open var onIncomeAddedCallback: ((Money) -> Unit)? = null
    open var onExpenseAddedCallback: ((Money) -> Unit)? = null

    /**
     * Инициализирует базовую функциональность ViewModel.
     * Загружает начальные данные, такие как категории, источники и кошельки.
     */
    init {
        // Загружаем список кошельков
        viewModelScope.launch {
            try {
                val walletsList = walletRepository.getAllWallets()
                _wallets.value = walletsList
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
            }
        }
        
        // Загружаем категории и источники
        // Делаем в отдельном потоке, чтобы дочерние классы успели инициализировать свои поля
        viewModelScope.launch {
            loadInitialData()
        }
    }

    /**
     * Загружает категории из CategoriesViewModel.
     */
    protected open fun loadCategories() {
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { categories ->
                updateExpenseCategories(categories)
            }
        }
        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { categories ->
                updateIncomeCategories(categories)
            }
        }
    }

    /**
     * Абстрактные методы для обновления категорий - должны быть реализованы в подклассах
     */
    protected abstract fun updateExpenseCategories(categories: List<Any>)
    protected abstract fun updateIncomeCategories(categories: List<Any>)

    /**
     * Загружает начальные данные для ViewModel.
     */
    protected open fun loadInitialData() {
        loadCategories()
    }

    /**
     * Обновляет позиции всех использованных категорий.
     */
    open fun updateCategoryPositions() {
        viewModelScope.launch {
            usedCategories.forEach { (category, isExpense) ->
                categoriesViewModel.incrementCategoryUsage(category, isExpense)
            }
            // Очищаем список использованных категорий
            usedCategories.clear()
        }
    }

    /**
     * Валидирует введенные данные. Выполняется синхронно.
     * @return true, если данные валидны, иначе false.
     */
    protected fun validateInput(state: BaseTransactionState): Boolean {
        val currentAmount = state.transactionData.amount
        val currentCategory = state.transactionData.category
        var money = Money.zero()

        try {
            money = Money.fromString(currentAmount)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка парсинга суммы: $currentAmount")
            // Ошибка парсинга = невалидная сумма
        }

        val isAmountInvalid = money.isZero() || currentAmount.isBlank()
        val isCategoryInvalid = currentCategory.isBlank()

        val validationError: ValidationError? = when {
            isAmountInvalid && isCategoryInvalid -> ValidationError.General("Введите сумму и выберите категорию") 
            isAmountInvalid -> ValidationError.AmountMissing
            isCategoryInvalid -> ValidationError.CategoryMissing
            else -> null // Все поля валидны
        }

        if (validationError != null) {
            setValidationError(validationError)
            return false
        }

        return true
    }

    /**
     * Устанавливает ошибку валидации.
     */
    protected abstract fun setValidationError(error: ValidationError)

    /**
     * Запрашивает обновление данных.
     */
    protected fun requestDataRefresh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Фактическая реализация будет зависеть от подклассов
                Timber.d("Запрошено обновление данных")
                // Дополнительная логика обновления данных может быть добавлена в подклассах
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении данных")
            }
        }
    }

    /**
     * Обновляет баланс виджета.
     */
    protected fun updateWidget() {
        // Реализация обновления виджета
        Timber.d("Обновление виджета")
    }

    /**
     * Общий обработчик событий для базовых операций.
     */
    abstract fun onEvent(event: BaseTransactionEvent)

    /**
     * Метод для получения репозитория транзакций.
     * Добавлен для BudgetScreen
     */
    open fun getTransactionRepository(): TransactionRepository {
        return txRepository
    }
} 