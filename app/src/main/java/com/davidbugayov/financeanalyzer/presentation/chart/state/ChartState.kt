package com.davidbugayov.financeanalyzer.presentation.chart.state

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Состояние экрана с графиками.
 * Содержит все необходимые данные для отображения финансовой статистики.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @property transactions Список всех транзакций
 * @property isLoading Флаг загрузки данных
 * @property error Текст ошибки (null если ошибок нет)
 * @property expensesByCategory Карта расходов по категориям
 * @property incomeByCategory Карта доходов по категориям
 * @property transactionsByMonth Карта транзакций по месяцам
 * @property expensesByDay Карта расходов по дням
 */
data class ChartState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val expensesByCategory: Map<String, Money> = emptyMap(),
    val incomeByCategory: Map<String, Money> = emptyMap(),
    val transactionsByMonth: Map<String, ChartMonthlyData> = emptyMap(),
    val expensesByDay: Map<String, ChartMonthlyData> = emptyMap()
)

/**
 * Данные о транзакциях за период (месяц или день).
 * Используется для отображения статистики в графиках.
 *
 * @property totalIncome Общая сумма доходов за период
 * @property totalExpense Общая сумма расходов за период
 * @property categoryBreakdown Распределение сумм по категориям
 */
data class ChartMonthlyData(
    val totalIncome: Money,
    val totalExpense: Money,
    val categoryBreakdown: Map<String, Money>
) 