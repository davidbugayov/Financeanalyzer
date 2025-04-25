package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Класс для централизованного управления финансовыми метриками.
 * Всегда отображает реальный баланс по всем видимым транзакциям.
 */
class FinancialMetrics private constructor() : KoinComponent {
    private val repository: ITransactionRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _totalIncome = MutableStateFlow(Money.zero())
    val totalIncome: StateFlow<Money> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(Money.zero())
    val totalExpense: StateFlow<Money> = _totalExpense.asStateFlow()

    private val _balance = MutableStateFlow(Money.zero())
    val balance: StateFlow<Money> = _balance.asStateFlow()

    companion object {
        @Volatile
        private var instance: FinancialMetrics? = null
        fun getInstance(): FinancialMetrics {
            return instance ?: synchronized(this) {
                instance ?: FinancialMetrics().also { instance = it }
            }
        }
    }

    init {
        Timber.d("Инициализация FinancialMetrics (реальный баланс)")
        recalculateStats()
    }

    /**
     * Пересчитывает все метрики по всем видимым транзакциям
     */
    fun recalculateStats() {
        scope.launch {
            try {
                val visibleTransactions = repository.loadTransactions()
                Timber.d("[DIAG] FinancialMetrics транзакций: ${visibleTransactions.size}")
                visibleTransactions.forEach { Timber.d("[DIAG] FM TX: id=${it.id}, amount=${it.amount}, date=${it.date}, isExpense=${it.isExpense}") }
                val income = visibleTransactions.filter { !it.isExpense }
                    .fold(Money.zero()) { acc, t -> acc + t.amount }
                val expense = visibleTransactions.filter { it.isExpense }
                    .fold(Money.zero()) { acc, t -> acc + t.amount }
                val balance = income - expense
                _totalIncome.value = income
                _totalExpense.value = expense
                _balance.value = balance
                Timber.d("Метрики обновлены: доход=${income.formatted()}, расход=${expense.formatted()}, баланс=${balance.formatted()}")
                val expenseIds = visibleTransactions.filter { it.isExpense }.map { it.id }
                Timber.d("[DIAG] FM EXPENSE IDS: ${expenseIds}")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при пересчёте метрик")
            }
        }
    }

    fun getCurrentBalance(): Money = _balance.value
    fun getTotalIncomeAsMoney(): Money = _totalIncome.value
    fun getTotalExpenseAsMoney(): Money = _totalExpense.value
}