package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.PeerComparison
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

/**
 * UseCase для сравнения финансовых показателей пользователя с другими пользователями схожего дохода.
 * 
 * Пока что использует статистические данные и бенчмарки вместо реальных данных других пользователей
 * для обеспечения приватности.
 */
class CalculatePeerComparisonUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        healthScore: Double
    ): PeerComparison {
        
        if (transactions.isEmpty()) {
            return createDefaultComparison()
        }
        
        // Рассчитываем доход пользователя
        val monthlyIncome = calculateAverageMonthlyIncome(transactions)
        
        // Определяем диапазон дохода для сравнения
        val incomeRange = determineIncomeRange(monthlyIncome)
        
        // Рассчитываем метрики пользователя
        val userSavingsRate = calculateSavingsRate(transactions)
        val userExpenseBreakdown = calculateExpenseBreakdown(transactions)
        
        // Получаем бенчмарки для данного диапазона дохода
        val benchmarks = getIncomeRangeBenchmarks(incomeRange)
        
        // Сравниваем с бенчмарками
        val savingsRateVsPeers = (userSavingsRate - benchmarks.averageSavingsRate) * 100
        val expenseCategoriesVsPeers = compareExpenseCategories(userExpenseBreakdown, benchmarks.averageExpenseBreakdown)
        
        // Рассчитываем перцентиль по здоровью (упрощенно, на основе компонентов)
        val healthScorePercentile = calculateHealthScorePercentile(healthScore, userSavingsRate, benchmarks)
        
        return PeerComparison(
            incomeRange = incomeRange,
            savingsRateVsPeers = savingsRateVsPeers,
            expenseCategoriesVsPeers = expenseCategoriesVsPeers,
            healthScorePercentile = healthScorePercentile,
            peerGroupSize = benchmarks.sampleSize
        )
    }

    /**
     * Создает сравнение по умолчанию при отсутствии данных
     */
    private fun createDefaultComparison(): PeerComparison {
        return PeerComparison(
            incomeRange = "Недостаточно данных",
            savingsRateVsPeers = 0.0,
            expenseCategoriesVsPeers = emptyMap(),
            healthScorePercentile = 50.0,
            peerGroupSize = 0
        )
    }

    /**
     * Рассчитывает среднемесячный доход
     */
    private fun calculateAverageMonthlyIncome(transactions: List<Transaction>): Money {
        val monthlyData = transactions
            .filter { !it.isExpense }
            .groupBy { transaction ->
                val calendar = java.util.Calendar.getInstance()
                calendar.time = transaction.date
                "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
            }
        
        if (monthlyData.isEmpty()) return Money.zero()
        
        val monthlyIncomes = monthlyData.map { (_, monthTransactions) ->
            monthTransactions.sumOf { it.amount.amount }
        }
        
        val averageIncome = monthlyIncomes.fold(BigDecimal.ZERO) { acc, amount -> acc + amount }
            .divide(BigDecimal(monthlyIncomes.size), 2, RoundingMode.HALF_UP)
            
        return Money(averageIncome)
    }

    /**
     * Определяет диапазон дохода пользователя
     */
    private fun determineIncomeRange(monthlyIncome: Money): String {
        val income = monthlyIncome.amount.toInt()
        return when {
            income < 30000 -> "< 30k"
            income < 50000 -> "30-50k" 
            income < 75000 -> "50-75k"
            income < 100000 -> "75-100k"
            income < 150000 -> "100-150k"
            income < 200000 -> "150-200k"
            income < 300000 -> "200-300k"
            else -> "300k+"
        }
    }

    /**
     * Рассчитывает норму сбережений пользователя
     */
    private fun calculateSavingsRate(transactions: List<Transaction>): Double {
        val monthlyData = transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
        }
        
        val monthlySavingsRates = monthlyData.map { (_, monthTransactions) ->
            val income = monthTransactions.filter { !it.isExpense }.sumOf { it.amount.amount }
            val expense = monthTransactions.filter { it.isExpense }.sumOf { it.amount.amount }
            
            if (income > BigDecimal.ZERO) {
                (income - expense).divide(income, 4, RoundingMode.HALF_UP).toDouble()
            } else {
                0.0
            }
        }
        
        return if (monthlySavingsRates.isNotEmpty()) {
            monthlySavingsRates.average()
        } else {
            0.0
        }
    }

    /**
     * Рассчитывает распределение расходов по категориям
     */
    private fun calculateExpenseBreakdown(transactions: List<Transaction>): Map<String, Double> {
        val expenseTransactions = transactions.filter { it.isExpense }
        if (expenseTransactions.isEmpty()) return emptyMap()
        
        val totalExpense = expenseTransactions.sumOf { it.amount.amount }
        if (totalExpense == BigDecimal.ZERO) return emptyMap()
        
        return expenseTransactions
            .groupBy { it.category }
            .mapValues { (_, categoryTransactions) ->
                val categoryTotal = categoryTransactions.sumOf { it.amount.amount }
                categoryTotal.divide(totalExpense, 4, RoundingMode.HALF_UP).toDouble()
            }
    }

    /**
     * Получает бенчмарки для указанного диапазона дохода
     */
    private fun getIncomeRangeBenchmarks(incomeRange: String): IncomeBenchmarks {
        return when (incomeRange) {
            "< 30k" -> IncomeBenchmarks(
                averageSavingsRate = 0.05, // 5%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.25,
                    "Транспорт" to 0.15,
                    "ЖКХ" to 0.20,
                    "Одежда" to 0.08,
                    "Развлечения" to 0.07,
                    "Прочее" to 0.25
                ),
                sampleSize = 1000
            )
            "30-50k" -> IncomeBenchmarks(
                averageSavingsRate = 0.08, // 8%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.22,
                    "Транспорт" to 0.18,
                    "ЖКХ" to 0.18,
                    "Одежда" to 0.10,
                    "Развлечения" to 0.10,
                    "Прочее" to 0.22
                ),
                sampleSize = 1500
            )
            "50-75k" -> IncomeBenchmarks(
                averageSavingsRate = 0.12, // 12%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.20,
                    "Транспорт" to 0.20,
                    "ЖКХ" to 0.15,
                    "Одежда" to 0.12,
                    "Развлечения" to 0.13,
                    "Прочее" to 0.20
                ),
                sampleSize = 2000
            )
            "75-100k" -> IncomeBenchmarks(
                averageSavingsRate = 0.15, // 15%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.18,
                    "Транспорт" to 0.22,
                    "ЖКХ" to 0.12,
                    "Одежда" to 0.13,
                    "Развлечения" to 0.15,
                    "Прочее" to 0.20
                ),
                sampleSize = 1200
            )
            "100-150k" -> IncomeBenchmarks(
                averageSavingsRate = 0.18, // 18%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.16,
                    "Транспорт" to 0.25,
                    "ЖКХ" to 0.10,
                    "Одежда" to 0.15,
                    "Развлечения" to 0.18,
                    "Прочее" to 0.16
                ),
                sampleSize = 800
            )
            "150-200k" -> IncomeBenchmarks(
                averageSavingsRate = 0.22, // 22%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.15,
                    "Транспорт" to 0.28,
                    "ЖКХ" to 0.08,
                    "Одежда" to 0.17,
                    "Развлечения" to 0.20,
                    "Прочее" to 0.12
                ),
                sampleSize = 500
            )
            "200-300k" -> IncomeBenchmarks(
                averageSavingsRate = 0.25, // 25%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.12,
                    "Транспорт" to 0.30,
                    "ЖКХ" to 0.06,
                    "Одежда" to 0.20,
                    "Развлечения" to 0.22,
                    "Прочее" to 0.10
                ),
                sampleSize = 300
            )
            else -> IncomeBenchmarks( // "300k+"
                averageSavingsRate = 0.30, // 30%
                averageExpenseBreakdown = mapOf(
                    "Продукты" to 0.10,
                    "Транспорт" to 0.35,
                    "ЖКХ" to 0.05,
                    "Одежда" to 0.25,
                    "Развлечения" to 0.20,
                    "Прочее" to 0.05
                ),
                sampleSize = 150
            )
        }
    }

    /**
     * Сравнивает распределение расходов пользователя с бенчмарками
     */
    private fun compareExpenseCategories(
        userBreakdown: Map<String, Double>,
        benchmarkBreakdown: Map<String, Double>
    ): Map<String, Double> {
        val comparison = mutableMapOf<String, Double>()
        
        // Сравниваем основные категории
        val mainCategories = setOf("Продукты", "Транспорт", "ЖКХ", "Одежда", "Развлечения")
        
        for (category in mainCategories) {
            val userPercent = userBreakdown[category] ?: 0.0
            val benchmarkPercent = benchmarkBreakdown[category] ?: 0.0
            comparison[category] = (userPercent - benchmarkPercent) * 100 // В процентных пунктах
        }
        
        return comparison
    }

    /**
     * Рассчитывает перцентиль пользователя по финансовому здоровью
     */
    private fun calculateHealthScorePercentile(
        healthScore: Double,
        savingsRate: Double,
        benchmarks: IncomeBenchmarks
    ): Double {
        // Упрощенный расчет перцентиля на основе нормы сбережений и общего скора
        val savingsPerformance = if (benchmarks.averageSavingsRate > 0) {
            savingsRate / benchmarks.averageSavingsRate
        } else {
            1.0
        }
        
        // Нормализуем скор здоровья (предполагаем среднее значение 50)
        val healthPerformance = healthScore / 50.0
        
        // Комбинированная производительность
        val combinedPerformance = (savingsPerformance * 0.6 + healthPerformance * 0.4)
        
        // Преобразуем в перцентиль (с некоторой дисперсией)
        val percentile = when {
            combinedPerformance >= 1.5 -> 90.0 + (combinedPerformance - 1.5) * 20 // 90-100%
            combinedPerformance >= 1.2 -> 75.0 + (combinedPerformance - 1.2) * 50 // 75-90%
            combinedPerformance >= 1.0 -> 50.0 + (combinedPerformance - 1.0) * 125 // 50-75%
            combinedPerformance >= 0.8 -> 25.0 + (combinedPerformance - 0.8) * 125 // 25-50%
            combinedPerformance >= 0.6 -> 10.0 + (combinedPerformance - 0.6) * 75 // 10-25%
            else -> combinedPerformance * 16.67 // 0-10%
        }
        
        return max(1.0, min(99.0, percentile)) // Ограничиваем 1-99%
    }

    /**
     * Бенчмарки для диапазона дохода
     */
    private data class IncomeBenchmarks(
        val averageSavingsRate: Double,
        val averageExpenseBreakdown: Map<String, Double>,
        val sampleSize: Int
    )
} 