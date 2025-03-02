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

    init {
        loadTransactions()
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