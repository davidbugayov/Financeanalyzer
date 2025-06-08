package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.utils.DateTimeUtils.getDefaultEndDate
import com.davidbugayov.financeanalyzer.utils.DateTimeUtils.getDefaultStartDate
import kotlinx.datetime.LocalDate

class GetCategoriesWithAmountUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        isIncome: Boolean,
        startDate: LocalDate = getDefaultStartDate(),
        endDate: LocalDate = getDefaultEndDate()
    ): List<Pair<Category, Double>> {
        val transactions = transactionRepository.getTransactionsByDateRange(startDate, endDate)

        // Filter by transaction type (income or expense)
        val filteredTransactions = if (isIncome) {
            transactions.filter { it.amount.isPositive() }
        } else {
            transactions.filter { it.amount.isNegative() }
        }

        // Group by category and sum amounts
        return filteredTransactions
            .filter { transaction -> true }
            .groupBy { transaction -> transaction.category }
            .map { (category, categoryTransactions) ->
                val totalAmount = categoryTransactions.fold(Money.zero()) { acc, transaction ->
                    acc + transaction.amount.abs()
                }.amount.toDouble()

                // Create category using factory methods based on transaction type
                val categoryObject = if (isIncome) {
                    Category.income(name = category)
                } else {
                    Category.expense(name = category)
                }

                Pair(categoryObject, totalAmount)
            }
            .sortedByDescending { (_, amount) -> amount }
    }
} 
