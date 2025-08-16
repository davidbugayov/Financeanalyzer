package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.HealthScoreBreakdown
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * UseCase для расчета коэффициента финансового здоровья.
 * Вычисляет комплексный показатель на основе:
 * - Нормы сбережений (25 баллов)
 * - Стабильности доходов (25 баллов)
 * - Контроля расходов (25 баллов)
 * - Разнообразия финансовых инструментов (25 баллов)
 */
class CalculateFinancialHealthScoreUseCase {

    /**
     * Рассчитывает итоговый балл и декомпозицию.
     *
     * @param transactions Список транзакций.
     * @param periodMonths Период анализа в месяцах (резерв на будущее, пока не используется явно).
     */
    operator fun invoke(
        transactions: List<Transaction>,
        periodMonths: Int = 6,
    ): Pair<Double, HealthScoreBreakdown> {
        if (transactions.isEmpty()) return 0.0 to HealthScoreBreakdown()

        val monthlyData = groupByMonth(transactions)

        val savingsScore = calculateSavingsRateScore(monthlyData)
        val stabilityScore = calculateIncomeStabilityScore(monthlyData)
        val controlScore = calculateExpenseControlScore(monthlyData)
        val diversificationScore = calculateDiversificationScore(transactions)

        val total = savingsScore + stabilityScore + controlScore + diversificationScore
        val breakdown = HealthScoreBreakdown(
            savingsRateScore = savingsScore,
            incomeStabilityScore = stabilityScore,
            expenseControlScore = controlScore,
            diversificationScore = diversificationScore,
        )
        return total to breakdown
    }

    /**
     * Группирует транзакции по месяцам
     */
    private fun groupByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        return transactions.groupBy { tx ->
            val d: LocalDate = tx.date
            "${d.year}-${d.month}"
        }
    }

    /**
     * Рассчитывает баллы за норму сбережений (0-25)
     * Оценивает соотношение сбережений к доходам
     */
    private fun calculateSavingsRateScore(monthly: Map<String, List<Transaction>>): Double {
        if (monthly.isEmpty()) return 0.0

        val monthlyRates = monthly.map { (_, list) ->
            val income = list.filter { !it.isExpense }.sumOf { it.amount.toMajorDouble() }
            val expense = list.filter { it.isExpense }.sumOf { it.amount.toMajorDouble() }
            if (income > 0.0) max(0.0, (income - expense) / income) else 0.0
        }
        val avg = if (monthlyRates.isNotEmpty()) monthlyRates.average() else 0.0
        
        // Шкала оценки нормы сбережений:
        // 0-5% = 0-5 баллов
        // 5-10% = 5-10 баллов
        // 10-20% = 10-20 баллов
        // 20%+ = 20-25 баллов
        return when {
            avg >= 0.20 -> 25.0
            avg >= 0.10 -> 10.0 + (avg - 0.10) * 150.0 // 15 баллов за каждые 10%
            avg >= 0.05 -> 5.0 + (avg - 0.05) * 100.0 // 5 баллов за каждые 5%
            else -> avg * 100.0 // 1 балл за каждый 1%
        }
    }

    /**
     * Рассчитывает баллы за стабильность доходов (0-25)
     * Оценивает постоянство доходов от месяца к месяцу
     */
    private fun calculateIncomeStabilityScore(monthly: Map<String, List<Transaction>>): Double {
        if (monthly.size < 2) return 12.5
        
        val incomes = monthly.map { (_, list) ->
            list.filter { !it.isExpense }.sumOf { it.amount.toMajorDouble() }
        }
        
        if (incomes.isEmpty() || incomes.all { it == 0.0 }) return 0.0
        
        val avg = incomes.average()
        if (avg == 0.0) return 0.0
        
        val variance = incomes.map { (it - avg) * (it - avg) }.average()
        val std = sqrt(variance)
        val cv = std / avg // Коэффициент вариации
        
        // Шкала оценки стабильности доходов:
        // CV <= 0.1 (очень стабильно) = 20-25 баллов
        // CV <= 0.2 (стабильно) = 15-20 баллов
        // CV <= 0.4 (умеренно стабильно) = 10-15 баллов
        // CV > 0.4 (нестабильно) = 0-10 баллов
        return when {
            cv <= 0.1 -> 20.0 + (0.1 - cv) * 50.0
            cv <= 0.2 -> 15.0 + (0.2 - cv) * 50.0
            cv <= 0.4 -> 10.0 + (0.4 - cv) * 25.0
            else -> max(0.0, 10.0 - (cv - 0.4) * 20.0)
        }
    }

    /**
     * Рассчитывает баллы за контроль расходов (0-25)
     * Оценивает стабильность и предсказуемость расходов
     */
    private fun calculateExpenseControlScore(monthly: Map<String, List<Transaction>>): Double {
        if (monthly.size < 2) return 12.5
        
        val expenses = monthly.map { (_, list) ->
            list.filter { it.isExpense }.sumOf { it.amount.toMajorDouble() }
        }
        
        if (expenses.isEmpty() || expenses.all { it == 0.0 }) return 25.0
        
        val avg = expenses.average()
        if (avg == 0.0) return 25.0
        
        val variance = expenses.map { (it - avg) * (it - avg) }.average()
        val std = sqrt(variance)
        val cv = std / avg // Коэффициент вариации
        
        val maxExpense = expenses.maxOrNull() ?: 0.0
        val minExpense = expenses.minOrNull() ?: 0.0
        val expenseRange = if (avg > 0.0) (maxExpense - minExpense) / avg else 0.0
        
        // Комбинированная оценка: стабильность + отсутствие резких скачков
        val stabilityScore = when {
            cv <= 0.15 -> 15.0 // Очень стабильные расходы
            cv <= 0.25 -> 12.0 // Стабильные расходы
            cv <= 0.4 -> 8.0 // Умеренно стабильные
            else -> max(0.0, 5.0 - (cv - 0.4) * 10.0) // Нестабильные
        }
        
        val controlScore = when {
            expenseRange <= 0.5 -> 10.0 // Хороший контроль
            expenseRange <= 1.0 -> 7.0 // Умеренный контроль
            expenseRange <= 2.0 -> 4.0 // Слабый контроль
            else -> max(0.0, 2.0 - (expenseRange - 2.0) * 1.0) // Плохой контроль
        }
        
        return stabilityScore + controlScore
    }

    /**
     * Рассчитывает баллы за разнообразие финансовых инструментов (0-25)
     * Оценивает использование различных категорий и источников
     */
    private fun calculateDiversificationScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 0.0
        
        val uniqueCategories = transactions.map { it.category }.distinct().size
        val uniqueSources = transactions.map { it.source }.distinct().size
        
        // Оценка разнообразия категорий (0-15 баллов)
        val categoryScore = when {
            uniqueCategories >= 10 -> 15.0
            uniqueCategories >= 7 -> 12.0
            uniqueCategories >= 5 -> 9.0
            uniqueCategories >= 3 -> 6.0
            else -> uniqueCategories * 2.0
        }
        
        // Оценка разнообразия источников (0-10 баллов)
        val sourceScore = when {
            uniqueSources >= 5 -> 10.0
            uniqueSources >= 3 -> 7.0
            uniqueSources >= 2 -> 4.0
            else -> uniqueSources * 2.0
        }
        
        return categoryScore + sourceScore
    }
}


