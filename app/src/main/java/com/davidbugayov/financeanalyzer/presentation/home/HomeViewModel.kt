package com.davidbugayov.financeanalyzer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * ViewModel для главного экрана.
 * Отвечает за загрузку и отображение данных о транзакциях.
 */
class HomeViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> get() = _transactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error
    
    // Текущий выбранный фильтр для главного экрана
    private val _currentFilter = MutableStateFlow(TransactionFilter.MONTH)
    val currentFilter: StateFlow<TransactionFilter> get() = _currentFilter

    init {
        loadTransactions()
    }
    
    /**
     * Устанавливает текущий фильтр для отображения транзакций
     */
    fun setFilter(filter: TransactionFilter) {
        _currentFilter.value = filter
    }

    /**
     * Загружает транзакции из репозитория
     */
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = loadTransactionsUseCase()
                _transactions.value = result
                
                // Если транзакций нет, генерируем тестовые данные
                if (result.isEmpty()) {
                    generateAndSaveTestData()
                }
            } catch (e: Exception) {
                _error.value = "Ошибка при загрузке транзакций: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Генерирует и сохраняет тестовые данные
     */
    private fun generateAndSaveTestData() {
        viewModelScope.launch {
            try {
                val testTransactions = TestDataGenerator.generateTransactions(20)
                
                // Сохраняем каждую транзакцию
                testTransactions.forEach { transaction ->
                    addTransactionUseCase(transaction)
                }
                
                // Загружаем сохраненные транзакции
                _transactions.value = loadTransactionsUseCase()
            } catch (e: Exception) {
                _error.value = "Ошибка при генерации тестовых данных: ${e.message}"
            }
        }
    }

    /**
     * Возвращает последние транзакции
     * @param count Количество транзакций для отображения
     * @return Список последних транзакций
     */
    fun getRecentTransactions(count: Int = 5): List<Transaction> {
        return _transactions.value.sortedByDescending { it.date }.take(count)
    }
    
    /**
     * Возвращает транзакции за последний месяц
     * @return Список транзакций за последний месяц
     */
    fun getLastMonthTransactions(): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val monthAgo = calendar.time
        
        return _transactions.value
            .filter { it.date.after(monthAgo) || it.date == monthAgo }
            .sortedByDescending { it.date }
    }
    
    /**
     * Возвращает транзакции за сегодня
     * @return Список транзакций за сегодня
     */
    fun getTodayTransactions(): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        return _transactions.value
            .filter { 
                val transactionDate = Calendar.getInstance().apply { time = it.date }
                val today = Calendar.getInstance()
                
                transactionDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                transactionDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            .sortedByDescending { it.date }
    }
    
    /**
     * Возвращает транзакции за последнюю неделю
     * @return Список транзакций за последнюю неделю
     */
    fun getLastWeekTransactions(): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        val weekAgo = calendar.time
        
        return _transactions.value
            .filter { it.date.after(weekAgo) || it.date == weekAgo }
            .sortedByDescending { it.date }
    }
    
    /**
     * Возвращает отфильтрованные транзакции в соответствии с текущим фильтром
     */
    fun getFilteredTransactions(): List<Transaction> {
        return when (_currentFilter.value) {
            TransactionFilter.TODAY -> getTodayTransactions()
            TransactionFilter.WEEK -> getLastWeekTransactions()
            TransactionFilter.MONTH -> getLastMonthTransactions()
        }
    }

    /**
     * Возвращает общую сумму доходов
     * @return Сумма всех доходов
     */
    fun getTotalIncome(): Double {
        return _transactions.value.filter { !it.isExpense }.sumOf { it.amount }
    }

    /**
     * Возвращает общую сумму расходов
     * @return Сумма всех расходов
     */
    fun getTotalExpense(): Double {
        return _transactions.value.filter { it.isExpense }.sumOf { it.amount }
    }

    /**
     * Возвращает текущий баланс
     * @return Разница между доходами и расходами
     */
    fun getCurrentBalance(): Double {
        return getTotalIncome() - getTotalExpense()
    }
}

/**
 * Перечисление для фильтров транзакций
 */
enum class TransactionFilter {
    TODAY, WEEK, MONTH
} 