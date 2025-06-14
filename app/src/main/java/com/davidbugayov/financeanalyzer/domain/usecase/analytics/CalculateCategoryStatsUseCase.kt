package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.percentageDifference
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import java.math.BigDecimal
import java.util.Date

/**
 * Юзкейс для расчета статистики по категории.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class CalculateCategoryStatsUseCase(private val transactionRepository: TransactionRepository) {

    /**
     * Рассчитывает статистику по категории для текущего и предыдущего периодов
     * Использует оптимизированный подход для снижения нагрузки на вычисления
     * * @return CategoryStatistics с информацией о категории
     */
    suspend operator fun invoke(
        categoryId: String,
        currentStartDate: Date,
        currentEndDate: Date,
        previousStartDate: Date,
        previousEndDate: Date,
    ): Triple<Money, Money, BigDecimal?> {
        val currentTransactions = transactionRepository.getTransactionsByDateRange(currentStartDate, currentEndDate)
            .filter { it.categoryId == categoryId }
        val previousTransactions = transactionRepository.getTransactionsByDateRange(previousStartDate, previousEndDate)
            .filter { it.categoryId == categoryId }

        val currentTotal = currentTransactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
        val previousTotal = previousTransactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount }

        val difference = if (!previousTotal.isZero()) {
            currentTotal.percentageDifference(previousTotal).toBigDecimal()
        } else {
            null
        }

        return Triple(currentTotal, previousTotal, difference)
    }
}
