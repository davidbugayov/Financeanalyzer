package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel для экрана с графиками.
 * Отвечает за подготовку данных для отображения на графиках.
 */
class ChartViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> get() = _transactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    /**
     * Загружает транзакции из репозитория
     */
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _transactions.value = loadTransactionsUseCase()
            } catch (e: Exception) {
                _error.value = "Ошибка при загрузке транзакций: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Возвращает данные для графика расходов по категориям
     * @return Карта категорий и сумм расходов
     */
    fun getExpensesByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    /**
     * Возвращает данные для графика доходов по категориям
     * @return Карта категорий и сумм доходов
     */
    fun getIncomeByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    /**
     * Возвращает данные для графика транзакций по месяцам
     * @return Карта месяцев и сумм транзакций
     */
    fun getTransactionsByMonth(): Map<String, Double> {
        val dateFormat = SimpleDateFormat("MM.yyyy", Locale.getDefault())
        return _transactions.value
            .groupBy { dateFormat.format(it.date) }
            .mapValues { (_, transactions) -> 
                transactions.sumOf { if (it.isExpense) -it.amount else it.amount }
            }
            .toSortedMap()
    }

    /**
     * Возвращает данные для графика расходов по дням
     * @param days Количество дней для отображения
     * @return Карта дней и сумм расходов
     */
    fun getExpensesByDay(days: Int = 7): Map<String, Double> {
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val daysInMillis = days * 24 * 60 * 60 * 1000L
        
        return _transactions.value
            .filter { it.isExpense && (currentTime - it.date.time) <= daysInMillis }
            .groupBy { dateFormat.format(it.date) }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toSortedMap()
    }

    init {
        loadTransactions()
    }
} 