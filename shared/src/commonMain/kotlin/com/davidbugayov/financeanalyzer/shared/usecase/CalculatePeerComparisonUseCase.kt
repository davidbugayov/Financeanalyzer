package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.PeerComparison
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import java.math.BigDecimal

class CalculatePeerComparisonUseCase {
    operator fun invoke(transactions: List<Transaction>, healthScore: Double): PeerComparison {
        if (transactions.isEmpty()) return PeerComparison()

        val monthlyIncome = calculateAverageMonthlyIncome(transactions)
        val incomeRange = determineIncomeRange(monthlyIncome)
        val userSavingsRate = calculateSavingsRate(transactions)
        val userExpenseBreakdown = calculateExpenseBreakdown(transactions)
        val benchmarks = getIncomeRangeBenchmarks(incomeRange)

        val savingsRateVsPeers = (userSavingsRate - benchmarks.averageSavingsRate) * 100
        val expenseCategoriesVsPeers = compareExpenseCategories(userExpenseBreakdown, benchmarks.averageExpenseBreakdown)
        val healthScorePercentile = calculateHealthScorePercentile(healthScore, userSavingsRate, benchmarks)

        return PeerComparison(
            incomeRange = incomeRange,
            savingsRateVsPeers = savingsRateVsPeers,
            expenseCategoriesVsPeers = expenseCategoriesVsPeers,
            healthScorePercentile = healthScorePercentile,
            peerGroupSize = benchmarks.sampleSize,
        )
    }

    private fun calculateAverageMonthlyIncome(transactions: List<Transaction>): Money {
        val monthly = transactions.filter { !it.isExpense }
            .groupBy { t -> "${t.date.year}-${t.date.month}" }
            .mapValues { (_, txs) -> txs.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) } }
        if (monthly.isEmpty()) return Money.zero()
        val avg = monthly.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }.divide(BigDecimal.valueOf(monthly.size.toDouble()), 10, java.math.RoundingMode.HALF_EVEN)
        return Money(avg)
    }

    private fun determineIncomeRange(monthlyIncome: Money): String = when {
        monthlyIncome.amount < BigDecimal.valueOf(30_000_00.0) -> "< 30k"
        monthlyIncome.amount < BigDecimal.valueOf(50_000_00.0) -> "30-50k"
        monthlyIncome.amount < BigDecimal.valueOf(75_000_00.0) -> "50-75k"
        monthlyIncome.amount < BigDecimal.valueOf(100_000_00.0) -> "75-100k"
        monthlyIncome.amount < BigDecimal.valueOf(150_000_00.0) -> "100-150k"
        monthlyIncome.amount < BigDecimal.valueOf(200_000_00.0) -> "150-200k"
        monthlyIncome.amount < BigDecimal.valueOf(300_000_00.0) -> "200-300k"
        else -> "300k+"
    }

    private fun calculateSavingsRate(transactions: List<Transaction>): Double {
        val byMonth = transactions.groupBy { t -> "${t.date.year}-${t.date.month}" }
        val rates = byMonth.values.map { monthTxs ->
            val income = monthTxs.filter { !it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }.toDouble()
            val expense = monthTxs.filter { it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount.abs()) }.toDouble()
            if (income > 0.0) (income - expense) / income else 0.0
        }
        return if (rates.isNotEmpty()) rates.average() else 0.0
    }

    private fun calculateExpenseBreakdown(transactions: List<Transaction>): Map<String, Double> {
        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return emptyMap()
        val total = expenses.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount.abs()) }.toDouble()
        if (total == 0.0) return emptyMap()
        return expenses.groupBy { it.category }
            .mapValues { (_, txs) -> txs.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount.abs()) }.toDouble() / total }
    }

    private fun getIncomeRangeBenchmarks(incomeRange: String): IncomeBenchmarks {
        // Локализованные названия категорий снаружи недоступны в commonMain, используем стандартные ключи
        val base = mapOf(
            "products" to 0.22,
            "transport" to 0.18,
            "utilities" to 0.16,
            "clothing" to 0.10,
            "entertainment" to 0.12,
            "other" to 0.22,
        )
        return when (incomeRange) {
            "< 30k" -> IncomeBenchmarks(0.05, base, 1000)
            "30-50k" -> IncomeBenchmarks(0.08, base, 1500)
            "50-75k" -> IncomeBenchmarks(0.12, base, 2000)
            "75-100k" -> IncomeBenchmarks(0.15, base, 1200)
            "100-150k" -> IncomeBenchmarks(0.18, base, 800)
            "150-200k" -> IncomeBenchmarks(0.22, base, 500)
            "200-300k" -> IncomeBenchmarks(0.25, base, 300)
            else -> IncomeBenchmarks(0.30, base, 150)
        }
    }

    private fun compareExpenseCategories(user: Map<String, Double>, bench: Map<String, Double>): Map<String, Double> {
        val keys = (user.keys + bench.keys).toSet()
        return keys.associateWith { k -> (user[k] ?: 0.0 - (bench[k] ?: 0.0)) * 100 }
    }

    private fun calculateHealthScorePercentile(healthScore: Double, savingsRate: Double, b: IncomeBenchmarks): Double {
        val savingsPerformance = if (b.averageSavingsRate > 0) savingsRate / b.averageSavingsRate else 1.0
        val healthPerformance = healthScore / 50.0
        val combined = (savingsPerformance * 0.6 + healthPerformance * 0.4)
        val percentile = when {
            combined >= 1.5 -> 90.0 + (combined - 1.5) * 20
            combined >= 1.2 -> 75.0 + (combined - 1.2) * 50
            combined >= 1.0 -> 50.0 + (combined - 1.0) * 125
            combined >= 0.8 -> 25.0 + (combined - 0.8) * 125
            combined >= 0.6 -> 10.0 + (combined - 0.6) * 75
            else -> combined * 16.67
        }
        return kotlin.math.max(1.0, kotlin.math.min(99.0, percentile))
    }

    private data class IncomeBenchmarks(
        val averageSavingsRate: Double,
        val averageExpenseBreakdown: Map<String, Double>,
        val sampleSize: Int,
    )
}


