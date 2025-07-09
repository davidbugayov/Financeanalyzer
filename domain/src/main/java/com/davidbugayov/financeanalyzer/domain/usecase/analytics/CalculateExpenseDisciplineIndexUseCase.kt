package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * UseCase для расчета индекса расходной дисциплины.
 * Измеряет, насколько последовательно пользователь контролирует свои расходы.
 * 
 * Индекс рассчитывается на основе:
 * - Регулярности трат (30%)
 * - Отсутствия импульсивных покупок (25%)
 * - Соблюдения бюджета по категориям (25%)
 * - Планирования крупных покупок (20%)
 */
class CalculateExpenseDisciplineIndexUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        periodMonths: Int = 6
    ): Double {
        
        if (transactions.isEmpty()) return 0.0
        
        val expenseTransactions = transactions.filter { it.isExpense }
        if (expenseTransactions.isEmpty()) return 100.0 // Максимум, если нет расходов
        
        // Группируем по неделям для более детального анализа
        val weeklyData = groupTransactionsByWeek(expenseTransactions)
        
        // Рассчитываем компоненты индекса
        val regularityScore = calculateRegularityScore(weeklyData) * 0.30
        val impulsiveScore = calculateImpulsiveControlScore(expenseTransactions) * 0.25
        val budgetScore = calculateBudgetAdherenceScore(expenseTransactions) * 0.25
        val planningScore = calculatePlanningScore(expenseTransactions) * 0.20
        
        val totalScore = (regularityScore + impulsiveScore + budgetScore + planningScore) * 100
        
        return max(0.0, min(100.0, totalScore))
    }

    /**
     * Группирует транзакции по неделям
     */
    private fun groupTransactionsByWeek(transactions: List<Transaction>): Map<String, List<Transaction>> {
        return transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            val year = calendar.get(java.util.Calendar.YEAR)
            val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
            "$year-W$week"
        }
    }

    /**
     * Оценивает регулярность трат (0.0-1.0)
     * Высокий балл = стабильные еженедельные расходы
     */
    private fun calculateRegularityScore(weeklyData: Map<String, List<Transaction>>): Double {
        if (weeklyData.size < 4) return 0.5 // Средний балл при недостаточных данных
        
        val weeklyTotals = weeklyData.map { (_, transactions) ->
            transactions.sumOf { it.amount.amount }.toDouble()
        }
        
        if (weeklyTotals.isEmpty()) return 1.0
        
        val averageWeeklyExpense = weeklyTotals.average()
        if (averageWeeklyExpense == 0.0) return 1.0
        
        // Рассчитываем коэффициент вариации
        val variance = weeklyTotals.map { (it - averageWeeklyExpense) * (it - averageWeeklyExpense) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        val coefficientOfVariation = standardDeviation / averageWeeklyExpense
        
        // Чем ниже вариация, тем выше регулярность
        return when {
            coefficientOfVariation <= 0.2 -> 1.0 // Очень регулярные расходы
            coefficientOfVariation <= 0.4 -> 0.8 // Регулярные расходы
            coefficientOfVariation <= 0.6 -> 0.6 // Средняя регулярность
            coefficientOfVariation <= 0.8 -> 0.4 // Низкая регулярность
            else -> 0.2 // Очень нерегулярные расходы
        }
    }

    /**
     * Оценивает контроль импульсивных покупок (0.0-1.0)
     * Ищет аномально крупные разовые траты
     */
    private fun calculateImpulsiveControlScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        
        val amounts = transactions.map { it.amount.amount.toDouble() }
        val averageExpense = amounts.average()
        val medianExpense = amounts.sorted().let {
            if (it.size % 2 == 0) {
                (it[it.size / 2 - 1] + it[it.size / 2]) / 2
            } else {
                it[it.size / 2]
            }
        }
        
        // Находим потенциально импульсивные покупки (больше чем 3 медианы)
        val impulsiveThreshold = medianExpense * 3
        val impulsivePurchases = amounts.filter { it > impulsiveThreshold }
        
        // Рассчитываем долю импульсивных покупок
        val impulsiveRatio = impulsivePurchases.size.toDouble() / amounts.size
        
        // Учитываем также размер импульсивных покупок относительно общих трат
        val impulsiveAmount = impulsivePurchases.sum()
        val totalAmount = amounts.sum()
        val impulsiveAmountRatio = if (totalAmount > 0) impulsiveAmount / totalAmount else 0.0
        
        // Комбинированный штраф за импульсивность
        val impulsivePenalty = impulsiveRatio * 0.5 + impulsiveAmountRatio * 0.5
        
        return max(0.0, 1.0 - impulsivePenalty * 2.0) // Максимальный штраф 100%
    }

    /**
     * Оценивает соблюдение бюджета по категориям (0.0-1.0)
     * Анализирует распределение трат между категориями
     */
    private fun calculateBudgetAdherenceScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        
        // Группируем по категориям
        val categoryExpenses = transactions.groupBy { it.category }
            .mapValues { (_, categoryTransactions) ->
                categoryTransactions.sumOf { it.amount.amount }.toDouble()
            }
        
        if (categoryExpenses.isEmpty()) return 1.0
        
        val totalExpenses = categoryExpenses.values.sum()
        if (totalExpenses == 0.0) return 1.0
        
        // Анализируем концентрацию расходов
        val categoryRatios = categoryExpenses.values.map { it / totalExpenses }.sorted().reversed()
        
        // Рассчитываем индекс Херфиндаля-Хиршмана для концентрации
        val herfindahlIndex = categoryRatios.map { it * it }.sum()
        
        // Проверяем наличие доминирующих категорий (плохо для дисциплины)
        val dominantCategoriesCount = categoryRatios.count { it > 0.4 } // Категории больше 40%
        
        // Хорошая дисциплина = равномерное распределение расходов
        val concentrationPenalty = when {
            herfindahlIndex <= 0.2 -> 0.0 // Отличное распределение
            herfindahlIndex <= 0.3 -> 0.1 // Хорошее распределение
            herfindahlIndex <= 0.5 -> 0.3 // Среднее распределение
            else -> 0.5 // Плохое распределение
        }
        
        val dominancePenalty = dominantCategoriesCount * 0.2 // 20% штрафа за каждую доминирующую категорию
        
        return max(0.0, 1.0 - concentrationPenalty - dominancePenalty)
    }

    /**
     * Оценивает планирование крупных покупок (0.0-1.0)
     * Анализирует распределение крупных трат во времени
     */
    private fun calculatePlanningScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        
        val amounts = transactions.map { it.amount.amount.toDouble() }
        val medianExpense = amounts.sorted().let {
            if (it.size % 2 == 0) {
                (it[it.size / 2 - 1] + it[it.size / 2]) / 2
            } else {
                it[it.size / 2]
            }
        }
        
        // Определяем крупные покупки (больше 5 медианных трат)
        val largePurchaseThreshold = medianExpense * 5
        val largePurchases = transactions.filter { it.amount.amount.toDouble() > largePurchaseThreshold }
        
        if (largePurchases.isEmpty()) return 1.0 // Нет крупных покупок = хорошее планирование
        
        // Группируем крупные покупки по неделям
        val largePurchasesByWeek = groupTransactionsByWeek(largePurchases)
        
        // Проверяем, есть ли недели с множественными крупными покупками (плохое планирование)
        val weeksWithMultipleLarge = largePurchasesByWeek.values.count { it.size > 1 }
        val totalLargePurchaseWeeks = largePurchasesByWeek.size
        
        // Рассчитываем равномерность распределения крупных покупок
        val clusteringRatio = if (totalLargePurchaseWeeks > 0) {
            weeksWithMultipleLarge.toDouble() / totalLargePurchaseWeeks
        } else {
            0.0
        }
        
        // Хорошее планирование = крупные покупки распределены равномерно
        return max(0.0, 1.0 - clusteringRatio)
    }
} 