package com.davidbugayov.financeanalyzer.presentation.chart.detail.model

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.CategoryStats
import com.davidbugayov.financeanalyzer.domain.model.FinancialHealthMetrics
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Модель данных для финансовых метрик и аналитики.
 * Используется для отображения расширенной статистики на экране FinancialStatistics.
 */
data class FinancialMetrics(
    // Норма сбережений (в процентах)
    val savingsRate: Float = 0f,
    // Статистика по категориям расходов
    val expenseCategories: List<CategoryStats> = emptyList(),
    // Статистика по категориям доходов
    val incomeCategories: List<CategoryStats> = emptyList(),
    // Общее количество транзакций
    val transactionCount: Int = 0,
    // Средний ежедневный расход
    val averageDailyExpense: Money = Money.zero(),
    // Средний месячный расход
    val averageMonthlyExpense: Money = Money.zero(),
    // Категория с наибольшими расходами
    val largestExpenseCategory: String = "",
    // Категория с наибольшими доходами
    val largestIncomeCategory: String = "",
    // Количество дней в анализируемом периоде
    val dayCount: Int = 1,
    // Топ категории расходов (для UI)
    val topExpenseCategories: List<Pair<String, Money>> = emptyList(),
    // Топ категория расходов (для UI)
    val topExpenseCategory: String = "",
    // Топ категория доходов (для UI)
    val topIncomeCategory: String = "",
    // Самый частый день расходов
    val mostFrequentExpenseDay: String = "",
    // Количество месяцев, на которые хватит сбережений
    val monthsOfSavings: Float = 0f,
    // Количество транзакций доходов
    val incomeTransactionsCount: Int = 0,
    // Количество транзакций расходов
    val expenseTransactionsCount: Int = 0,
    // Средний доход на транзакцию
    val averageIncomePerTransaction: Money = Money.zero(),
    // Средний расход на транзакцию
    val averageExpensePerTransaction: Money = Money.zero(),
    // Максимальный доход
    val maxIncome: Money = Money.zero(),
    // Максимальный расход
    val maxExpense: Money = Money.zero(),
    // Общее количество транзакций
    val totalTransactions: Int = 0,
    // Продвинутые метрики финансового здоровья
    val healthMetrics: FinancialHealthMetrics? = null,
)

// Метод для форматирования процентов
fun Float.setScale(scale: Int): Float = BigDecimal(this.toDouble()).setScale(scale, RoundingMode.HALF_UP).toFloat()
