package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Date
import kotlin.math.abs

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
        categories: List<String>,
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
            categories = categories
        )
        val currentPeriodTotalDouble = currentPeriodTransactions
            .map { it.amount }
            .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
        
        val currentPeriodTotal = Money(currentPeriodTotalDouble)

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
            categories = categories
        )
        val previousPeriodTotalDouble = previousPeriodTransactions
            .map { it.amount }
            .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
            
        val previousPeriodTotal = Money(previousPeriodTotalDouble)

        // Рассчитываем процентное изменение
        val percentChange = if (previousPeriodTotalDouble != 0.0) {
            ((currentPeriodTotalDouble - previousPeriodTotalDouble) /
                    abs(previousPeriodTotalDouble) * 100).toInt()
        } else null

        return Triple(currentPeriodTotal, previousPeriodTotal, percentChange)
    }
} 