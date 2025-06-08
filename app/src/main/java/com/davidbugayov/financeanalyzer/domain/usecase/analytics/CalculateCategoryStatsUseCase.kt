package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.FilterTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.math.BigDecimal
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
     * Использует оптимизированный подход для снижения нагрузки на вычисления
     * * @return Triple<currentTotal, previousTotal, percentChange>
     */
    operator fun invoke(
        transactions: List<Transaction>,
        categories: List<String>,
        periodType: PeriodType,
        startDate: Date,
        endDate: Date
    ): Triple<Money, Money, BigDecimal?> {
        // Если список пуст или нет выбранных категорий, быстро возвращаем нулевые значения
        if (transactions.isEmpty() || categories.isEmpty()) {
            return Triple(Money.zero(), Money.zero(), null)
        }

        // Используем предварительную фильтрацию для снижения нагрузки на filterTransactionsUseCase
        // Это особенно важно при больших наборах данных
        val relevantTransactions = transactions.filter {
            it.date in startDate..endDate && (categories.isEmpty() || it.category in categories)
        }

        if (relevantTransactions.isEmpty()) {
            return Triple(Money.zero(), Money.zero(), null)
        }

        // Фильтруем транзакции для текущего периода с оптимизированным набором данных
        val currentPeriodTransactions = filterTransactionsUseCase(
            transactions = relevantTransactions,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            categories = categories
        )

        // Оптимизированный расчет суммы с одним проходом
        val currentPeriodTotal = currentPeriodTransactions.fold(Money.zero()) { acc, transaction ->
            acc + transaction.amount
        }

        // Рассчитываем предыдущий период такой же длительности
        val periodDuration = endDate.time - startDate.time
        val previousStartDate = Date(startDate.time - periodDuration)
        val previousEndDate = Date(endDate.time - periodDuration)

        // Предварительно фильтруем транзакции для предыдущего периода
        val relevantPreviousTransactions = transactions.filter {
            it.date in previousStartDate..previousEndDate && (categories.isEmpty() || it.category in categories)
        }

        // Фильтруем транзакции для предыдущего периода с оптимизированным набором данных
        val previousPeriodTransactions = if (relevantPreviousTransactions.isNotEmpty()) {
            filterTransactionsUseCase(
                transactions = relevantPreviousTransactions,
                periodType = PeriodType.CUSTOM,
                startDate = previousStartDate,
                endDate = previousEndDate,
                categories = categories
            )
        } else {
            emptyList()
        }

        // Оптимизированный расчет суммы с одним проходом
        val previousPeriodTotal = previousPeriodTransactions.fold(Money.zero()) { acc, transaction ->
            acc + transaction.amount
        }

        // Рассчитываем процентное изменение только если оба периода имеют данные
        val percentChange = if (!previousPeriodTotal.isZero()) {
            currentPeriodTotal.percentageDifference(previousPeriodTotal)
        } else {
            null
        }

        return Triple(currentPeriodTotal, previousPeriodTotal, percentChange)
    }
} 
