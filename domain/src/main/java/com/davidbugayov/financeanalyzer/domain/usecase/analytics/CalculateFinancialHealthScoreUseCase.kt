package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.HealthScoreBreakdown
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * UseCase для расчета коэффициента финансового здоровья.
 * Вычисляет комплексный показатель на основе:
 * - Нормы сбережений (25 баллов)
 * - Стабильности доходов (25 баллов)
 * - Контроля расходов (25 баллов)
 * - Разнообразия финансовых инструментов (25 баллов)
 */
class CalculateFinancialHealthScoreUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        periodMonths: Int = 6 // Период для анализа в месяцах
    ): Pair<Double, HealthScoreBreakdown> {

        if (transactions.isEmpty()) {
            return 0.0 to HealthScoreBreakdown()
        }

        // Группируем транзакции по месяцам
        val monthlyData = groupTransactionsByMonth(transactions)

        // Рассчитываем компоненты коэффициента
        val savingsScore = calculateSavingsRateScore(monthlyData)
        val stabilityScore = calculateIncomeStabilityScore(monthlyData)
        val controlScore = calculateExpenseControlScore(monthlyData)
        val diversificationScore = calculateDiversificationScore(transactions)

        val totalScore = savingsScore + stabilityScore + controlScore + diversificationScore

        val breakdown = HealthScoreBreakdown(
            savingsRateScore = savingsScore,
            incomeStabilityScore = stabilityScore,
            expenseControlScore = controlScore,
            diversificationScore = diversificationScore
        )

        return totalScore to breakdown
    }

    /**
     * Группирует транзакции по месяцам
     */
    private fun groupTransactionsByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        return transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
        }
    }

    /**
     * Рассчитывает баллы за норму сбережений (0-25)
     * Оценивает соотношение сбережений к доходам
     */
    private fun calculateSavingsRateScore(monthlyData: Map<String, List<Transaction>>): Double {
        if (monthlyData.isEmpty()) return 0.0

        val monthlySavingsRates = monthlyData.map { (_, transactions) ->
            val income = transactions.filter { !it.isExpense }.sumOf { it.amount.amount }
            val expense = transactions.filter { it.isExpense }.sumOf { it.amount.amount }

            if (income > BigDecimal.ZERO) {
                val savingsRate = (income - expense).divide(income, 4, RoundingMode.HALF_UP).toDouble()
                max(0.0, savingsRate) // Не менее 0%
            } else {
                0.0
            }
        }

        val averageSavingsRate = monthlySavingsRates.average()

        // Шкала оценки нормы сбережений:
        // 0-5% = 0-5 баллов
        // 5-10% = 5-10 баллов
        // 10-20% = 10-20 баллов
        // 20%+ = 20-25 баллов
        return when {
            averageSavingsRate >= 0.20 -> 25.0
            averageSavingsRate >= 0.10 -> 10.0 + (averageSavingsRate - 0.10) * 150.0 // 15 баллов за каждые 10%
            averageSavingsRate >= 0.05 -> 5.0 + (averageSavingsRate - 0.05) * 100.0 // 5 баллов за каждые 5%
            else -> averageSavingsRate * 100.0 // 1 балл за каждый 1%
        }
    }

    /**
     * Рассчитывает баллы за стабильность доходов (0-25)
     * Оценивает постоянство доходов от месяца к месяцу
     */
    private fun calculateIncomeStabilityScore(monthlyData: Map<String, List<Transaction>>): Double {
        if (monthlyData.size < 2) return 12.5 // Средний балл при недостаточных данных

        val monthlyIncomes = monthlyData.map { (_, transactions) ->
            transactions.filter { !it.isExpense }.sumOf { it.amount.amount }.toDouble()
        }

        if (monthlyIncomes.isEmpty() || monthlyIncomes.all { it == 0.0 }) return 0.0

        val averageIncome = monthlyIncomes.average()
        if (averageIncome == 0.0) return 0.0

        // Рассчитываем коэффициент вариации (стандартное отклонение / среднее)
        val variance = monthlyIncomes.map { (it - averageIncome) * (it - averageIncome) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        val coefficientOfVariation = standardDeviation / averageIncome

        // Чем ниже коэффициент вариации, тем стабильнее доходы
        // CV < 0.1 (10%) = отличная стабильность (20-25 баллов)
        // CV 0.1-0.2 = хорошая стабильность (15-20 баллов)
        // CV 0.2-0.4 = средняя стабильность (10-15 баллов)
        // CV > 0.4 = низкая стабильность (0-10 баллов)
        return when {
            coefficientOfVariation <= 0.1 -> 20.0 + (0.1 - coefficientOfVariation) * 50.0
            coefficientOfVariation <= 0.2 -> 15.0 + (0.2 - coefficientOfVariation) * 50.0
            coefficientOfVariation <= 0.4 -> 10.0 + (0.4 - coefficientOfVariation) * 25.0
            else -> max(0.0, 10.0 - (coefficientOfVariation - 0.4) * 20.0)
        }
    }

    /**
     * Рассчитывает баллы за контроль расходов (0-25)
     * Оценивает постоянство и предсказуемость расходов
     */
    private fun calculateExpenseControlScore(monthlyData: Map<String, List<Transaction>>): Double {
        if (monthlyData.size < 2) return 12.5 // Средний балл при недостаточных данных

        val monthlyExpenses = monthlyData.map { (_, transactions) ->
            transactions.filter { it.isExpense }.sumOf { it.amount.amount }.toDouble()
        }

        if (monthlyExpenses.isEmpty() || monthlyExpenses.all { it == 0.0 }) return 25.0 // Максимум, если нет расходов

        val averageExpense = monthlyExpenses.average()
        if (averageExpense == 0.0) return 25.0

        // Рассчитываем коэффициент вариации расходов
        val variance = monthlyExpenses.map { (it - averageExpense) * (it - averageExpense) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        val coefficientOfVariation = standardDeviation / averageExpense

        // Дополнительно проверяем наличие экстремальных выбросов
        val maxExpense = monthlyExpenses.maxOrNull() ?: 0.0
        val minExpense = monthlyExpenses.minOrNull() ?: 0.0
        val extremeRatio = if (minExpense > 0) maxExpense / minExpense else 10.0

        // Базовый балл за стабильность
        val stabilityScore = when {
            coefficientOfVariation <= 0.15 -> 15.0
            coefficientOfVariation <= 0.25 -> 12.0
            coefficientOfVariation <= 0.4 -> 8.0
            else -> 4.0
        }

        // Штраф за экстремальные выбросы
        val extremePenalty = when {
            extremeRatio <= 2.0 -> 0.0
            extremeRatio <= 3.0 -> 2.0
            extremeRatio <= 5.0 -> 5.0
            else -> 10.0
        }

        return max(0.0, stabilityScore + 10.0 - extremePenalty) // 10 базовых баллов + балл за стабильность - штрафы
    }

    /**
     * Рассчитывает баллы за разнообразие финансовых инструментов (0-25)
     * Оценивает количество различных источников дохода и категорий расходов
     */
    private fun calculateDiversificationScore(transactions: List<Transaction>): Double {
        val incomeSources = transactions.filter { !it.isExpense }.map { it.source }.distinct().size
        val incomeCategories = transactions.filter { !it.isExpense }.map { it.category }.distinct().size
        val expenseCategories = transactions.filter { it.isExpense }.map { it.category }.distinct().size

        // Баллы за разнообразие источников дохода (до 10 баллов)
        val incomeSourceScore = min(10.0, incomeSources * 2.5) // 2.5 балла за каждый источник, максимум 4

        // Баллы за разнообразие категорий доходов (до 5 баллов)
        val incomeCategoryScore = min(5.0, incomeCategories * 1.25) // 1.25 балла за категорию, максимум 4

        // Баллы за структурированность расходов (до 10 баллов)
        val expenseCategoryScore = when {
            expenseCategories >= 8 -> 10.0  // 8+ категорий = отлично
            expenseCategories >= 6 -> 8.0   // 6-7 категорий = хорошо
            expenseCategories >= 4 -> 6.0   // 4-5 категорий = средне
            expenseCategories >= 2 -> 3.0   // 2-3 категории = плохо
            else -> 1.0                     // 1 категория = очень плохо
        }

        return incomeSourceScore + incomeCategoryScore + expenseCategoryScore
    }
}
