package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

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
            transaction.date.weekOfYearKey()
        }
    }

    /**
     * Оценивает регулярность трат (0.0-1.0)
     * Высокий балл = стабильные еженедельные расходы
     */
    private fun calculateRegularityScore(weeklyData: Map<String, List<Transaction>>): Double {
        if (weeklyData.size < 4) return 0.5 // Средний балл при недостаточных данных
        
        val weeklyTotals = weeklyData.values.map { transactions ->
            transactions.sumOf { it.amount.toMajorDouble() }
        }
        
        if (weeklyTotals.isEmpty()) return 1.0
        
        val averageWeeklyExpense = weeklyTotals.average()
        if (averageWeeklyExpense == 0.0) return 1.0
        
        // Рассчитываем коэффициент вариации
        val variance = weeklyTotals.map { (it - averageWeeklyExpense) * (it - averageWeeklyExpense) }.average()
        val standardDeviation = sqrt(variance)
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
        
        val amounts = transactions.map { it.amount.toMajorDouble() }
        val averageExpense = amounts.average()
        val medianExpense = amounts.sorted().let {
            if (it.size % 2 == 0) {
                (it[it.size / 2 - 1] + it[it.size / 2]) / 2
            } else {
                it[it.size / 2]
            }
        }
        
        // Определяем порог для импульсивных покупок (3x медиана)
        val impulsiveThreshold = medianExpense * 3
        
        // Находим импульсивные покупки
        val impulsivePurchases = amounts.filter { it > impulsiveThreshold }
        
        if (impulsivePurchases.isEmpty()) return 1.0
        
        // Рассчитываем штрафы
        val impulsiveRatio = impulsivePurchases.size.toDouble() / amounts.size
        val impulsiveAmountRatio = if (amounts.sum() > 0) {
            impulsivePurchases.sum() / amounts.sum()
        } else {
            0.0
        }
        
        // Комбинированный штраф: количество + сумма
        val penalty = impulsiveRatio * 0.5 + impulsiveAmountRatio * 0.5
        
        return max(0.0, 1.0 - penalty * 2.0)
    }

    /**
     * Оценивает соблюдение бюджета по категориям (0.0-1.0)
     * Проверяет равномерность распределения расходов
     */
    private fun calculateBudgetAdherenceScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        
        val categoryTotals = transactions.groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount.toMajorDouble() }
            }
        
        if (categoryTotals.isEmpty()) return 1.0
        
        val totalExpenses = categoryTotals.values.sum()
        if (totalExpenses == 0.0) return 1.0
        
        // Рассчитываем доли категорий
        val categoryRatios = categoryTotals.values.map { it / totalExpenses }.sortedDescending()
        
        // Индекс Херфиндаля-Хиршмана для концентрации
        val hhi = categoryRatios.sumOf { it * it }
        
        // Количество доминирующих категорий (>40% от общих расходов)
        val dominantCategories = categoryRatios.count { it > 0.4 }
        
        // Штрафы за концентрацию
        val concentrationPenalty = when {
            hhi <= 0.2 -> 0.0 // Хорошая диверсификация
            hhi <= 0.3 -> 0.1 // Умеренная концентрация
            hhi <= 0.5 -> 0.3 // Высокая концентрация
            else -> 0.5 // Очень высокая концентрация
        }
        
        // Штраф за доминирование одной категории
        val dominancePenalty = dominantCategories * 0.2
        
        return max(0.0, 1.0 - concentrationPenalty - dominancePenalty)
    }

    /**
     * Оценивает планирование крупных покупок (0.0-1.0)
     * Проверяет, не группируются ли крупные траты в одном периоде
     */
    private fun calculatePlanningScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        
        val amounts = transactions.map { it.amount.toMajorDouble() }
        val medianAmount = amounts.sorted().let {
            if (it.size % 2 == 0) {
                (it[it.size / 2 - 1] + it[it.size / 2]) / 2
            } else {
                it[it.size / 2]
            }
        }
        
        // Порог для крупных покупок (5x медиана)
        val largePurchaseThreshold = medianAmount * 5
        
        // Находим крупные покупки
        val largePurchases = transactions.filter { 
            it.amount.toMajorDouble() > largePurchaseThreshold 
        }
        
        if (largePurchases.isEmpty()) return 1.0
        
        // Группируем крупные покупки по неделям
        val largePurchasesByWeek = largePurchases.groupBy { it.date.weekOfYearKey() }
        
        // Считаем недели с несколькими крупными покупками
        val weeksWithMultipleLargePurchases = largePurchasesByWeek.values.count { it.size > 1 }
        
        // Коэффициент кластеризации
        val clusteringRatio = if (largePurchasesByWeek.isNotEmpty()) {
            weeksWithMultipleLargePurchases.toDouble() / largePurchasesByWeek.size
        } else {
            0.0
        }
        
        // Чем меньше кластеризация, тем лучше планирование
        return max(0.0, 1.0 - clusteringRatio)
    }
}

/**
 * Расширение для получения ключа недели года
 */
private fun kotlinx.datetime.LocalDate.weekOfYearKey(): String {
    // Приблизительный расчет: используем ISO неделю, считая от первого дня года
    val first = kotlinx.datetime.LocalDate(this.year, 1, 1)
    var current = first
    var week = 1
    
    while (current <= this) {
        current = current + DatePeriod(days = 7)
        week++
    }
    
    return "${this.year}-W$week"
}


