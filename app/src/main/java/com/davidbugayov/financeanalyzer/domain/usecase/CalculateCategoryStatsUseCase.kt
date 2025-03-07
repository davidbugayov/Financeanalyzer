package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Date

/**
 * Юзкейс для расчета статистики по категории.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class CalculateCategoryStatsUseCase(
    private val filterTransactionsUseCase: FilterTransactionsUseCase
) {

    /**
     * Рассчитывает статистику по категории для текущего и предыдущего периодов
     * @return Triple<currentTotal, previousTotal, percentChange>
     */
    operator fun invoke(
        transactions: List<Transaction>,
        category: String,
        periodType: PeriodType,
        startDate: Date,
        endDate: Date
    ): Triple<Money, Money, Int?> {
        // Фильтруем транзакции для текущего периода
        val currentPeriodTransactions = filterTransactionsUseCase(
            transactions = transactions,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            category = category
        )
        val currentPeriodTotal = currentPeriodTransactions
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        // Рассчитываем предыдущий период такой же длительности
        val periodDuration = endDate.time - startDate.time
        val previousStartDate = Date(startDate.time - periodDuration)
        val previousEndDate = Date(endDate.time - periodDuration)

        // Фильтруем транзакции для предыдущего периода
        val previousPeriodTransactions = filterTransactionsUseCase(
            transactions = transactions,
            periodType = PeriodType.CUSTOM,
            startDate = previousStartDate,
            endDate = previousEndDate,
            category = category
        )
        val previousPeriodTotal = previousPeriodTransactions
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        // Рассчитываем процентное изменение
        val percentChange = if (!previousPeriodTotal.isZero()) {
            ((currentPeriodTotal.amount.toDouble() - previousPeriodTotal.amount.toDouble()) /
                    previousPeriodTotal.amount.abs().toDouble() * 100).toInt()
        } else null

        return Triple(currentPeriodTotal, previousPeriodTotal, percentChange)
    }
} 