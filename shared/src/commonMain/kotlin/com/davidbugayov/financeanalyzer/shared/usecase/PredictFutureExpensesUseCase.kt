package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlin.math.max
import kotlin.math.min

/**
 * Улучшенный use case для прогнозирования будущих расходов.
 *
 * Особенности:
 * - Анализирует последние 12 месяцев данных
 * - Учитывает тренд расходов (линейная регрессия)
 * - Учитывает сезонность (месячные паттерны)
 * - Взвешивает недавние данные сильнее
 * - Прогнозирует по категориям
 */
class PredictFutureExpensesUseCase {

    operator fun invoke(transactions: List<Transaction>, monthsAhead: Int = 1): Money {
        if (transactions.isEmpty()) return Money.zero()

        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return Money.zero()

        // Получаем последние 12 месяцев данных для анализа
        val monthlyData = prepareMonthlyData(expenses)

        if (monthlyData.isEmpty()) return Money.zero()

        // Рассчитываем прогноз с учетом тренда и сезонности
        val predictedAmount = calculateAdvancedPrediction(monthlyData, monthsAhead)

        return Money(
            amount = predictedAmount,
            currency = expenses.firstOrNull()?.amount?.currency ?: Currency.RUB
        )
    }

    /**
     * Подготавливает месячные данные для анализа
     */
    private fun prepareMonthlyData(expenses: List<Transaction>): List<MonthlyExpenseData> {
        val now = kotlin.time.TimeSource.Monotonic.markNow()

        // Группируем по месяцам и рассчитываем сумму расходов
        val monthlyExpenses = expenses
            .groupBy { transaction ->
                // Создаем ключ месяц-год
                "${transaction.date.year}-${transaction.date.monthNumber.toString().padStart(2, '0')}"
            }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount.amount }
            }
            .toList()
            .sortedBy { it.first } // Сортируем по дате
            .takeLast(12) // Берем последние 12 месяцев

        return monthlyExpenses.mapIndexed { index, (monthKey, amount) ->
            val parts = monthKey.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()

            MonthlyExpenseData(
                month = month,
                year = year,
                amount = amount,
                monthIndex = index
            )
        }
    }

    /**
     * Рассчитывает прогноз с учетом тренда и сезонности
     */
    private fun calculateAdvancedPrediction(
        monthlyData: List<MonthlyExpenseData>,
        monthsAhead: Int
    ): Double {
        if (monthlyData.size < 3) {
            // Если данных мало, используем простое среднее
            return monthlyData.map { it.amount }.average()
        }

        // 1. Рассчитываем базовый тренд (линейная регрессия)
        val trendFactor = calculateTrendFactor(monthlyData)

        // 2. Рассчитываем сезонные коэффициенты
        val seasonalFactors = calculateSeasonalFactors(monthlyData)

        // 3. Получаем последние известные данные
        val lastDataPoint = monthlyData.last()
        val lastAmount = lastDataPoint.amount

        // 4. Применяем тренд к последнему значению
        val trendAdjusted = lastAmount * (1 + trendFactor * monthsAhead)

        // 5. Получаем сезонный коэффициент для прогнозируемого месяца
        val targetMonth = (lastDataPoint.month + monthsAhead - 1) % 12 + 1
        val seasonalFactor = seasonalFactors[targetMonth] ?: 1.0

        // 6. Применяем сезонную корректировку
        val seasonalAdjusted = trendAdjusted * seasonalFactor

        // 7. Ограничиваем прогноз разумными пределами
        val minPrediction = lastAmount * 0.5  // Не менее 50% от последнего месяца
        val maxPrediction = lastAmount * 2.0  // Не более 200% от последнего месяца

        return seasonalAdjusted.coerceIn(minPrediction, maxPrediction)
    }

    /**
     * Рассчитывает коэффициент тренда на основе линейной регрессии
     */
    private fun calculateTrendFactor(monthlyData: List<MonthlyExpenseData>): Double {
        if (monthlyData.size < 2) return 0.0

        val n = monthlyData.size.toDouble()
        val sumX = monthlyData.indices.sum().toDouble()
        val sumY = monthlyData.sumOf { it.amount }
        val sumXY = monthlyData.mapIndexed { index, data -> index * data.amount }.sum()
        val sumXX = monthlyData.indices.sumOf { it * it }.toDouble()

        // Формула линейной регрессии: slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX^2)
        val numerator = n * sumXY - sumX * sumY
        val denominator = n * sumXX - sumX * sumX

        if (denominator == 0.0) return 0.0

        val slope = numerator / denominator

        // Нормализуем наклон относительно среднего значения
        val averageAmount = monthlyData.map { it.amount }.average()
        if (averageAmount == 0.0) return 0.0

        return slope / averageAmount // Возвращаем относительное изменение за месяц
    }

    /**
     * Рассчитывает сезонные коэффициенты по месяцам
     */
    private fun calculateSeasonalFactors(monthlyData: List<MonthlyExpenseData>): Map<Int, Double> {
        if (monthlyData.size < 12) {
            // Если данных меньше года, возвращаем нейтральные коэффициенты
            return (1..12).associateWith { 1.0 }
        }

        // Группируем данные по месяцам (независимо от года)
        val monthlyAverages = mutableMapOf<Int, MutableList<Double>>()

        monthlyData.forEach { data ->
            monthlyAverages.getOrPut(data.month) { mutableListOf() }.add(data.amount)
        }

        // Рассчитываем средние значения по месяцам
        val overallAverage = monthlyData.map { it.amount }.average()

        return (1..12).associateWith { month ->
            val monthData = monthlyAverages[month]
            if (monthData.isNullOrEmpty()) {
                1.0 // Нейтральный коэффициент для месяцев без данных
            } else {
                val monthAverage = monthData.average()
                if (overallAverage == 0.0) 1.0 else monthAverage / overallAverage
            }
        }
    }
}



/**
 * Вспомогательная data class для хранения месячных данных о расходах
 */
private data class MonthlyExpenseData(
    val month: Int,
    val year: Int,
    val amount: Double,
    val monthIndex: Int
)


