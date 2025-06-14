package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.formatted
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
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
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase by inject()
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

                val metrics = calculateBalanceMetricsUseCase(visibleTransactions)
                val income = metrics.income
                val expense = metrics.expense
                val balance = income - expense

                // Обновляем значения в состоянии
                _totalIncome.value = income
                _totalExpense.value = expense
                _balance.value = balance

                // Логгируем результаты для диагностики
                val formattedExpenses = totalExpense.value.formatted
                val formattedIncome = totalIncome.value.formatted
                val formattedBalance = balance.formatted
                Timber.d(
                    "Метрики обновлены: доход=$formattedIncome, расход=$formattedExpenses, баланс=$formattedBalance",
                )
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при пересчёте метрик")
            }
        }
    }

    fun getCurrentBalance(): Money = _balance.value
    fun getTotalIncomeAsMoney(): Money = _totalIncome.value
    fun getTotalExpenseAsMoney(): Money = _totalExpense.value

    /**
     * Инициализация метрик (публичный метод)
     */
    fun initialize(context: Context) {
        Timber.d("Initializing financial metrics")
        // Здесь может быть код для загрузки настроек или предварительных данных
    }

    /**
     * Инициализация метрик (устаревший метод для обратной совместимости)
     */
    fun init(context: Context) {
        initialize(context)
    }
}
