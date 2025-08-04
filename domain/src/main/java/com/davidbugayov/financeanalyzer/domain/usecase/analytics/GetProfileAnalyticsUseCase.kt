package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.domain.model.ProfileAnalytics
import com.davidbugayov.financeanalyzer.domain.model.mapException
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import java.util.Calendar
import java.util.Date

class GetProfileAnalyticsUseCase(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {

    suspend operator fun invoke(currency: Currency): CoreResult<ProfileAnalytics> {
        return try {
            val transactions = transactionRepository.getAllTransactions()
            val wallets = walletRepository.getAllWallets().size

            var totalIncome = Money.zero(currency)
            var totalExpense = Money.zero(currency)
            val incomeCategories = mutableSetOf<String>()
            val expenseCategories = mutableSetOf<String>()
            val sources = mutableSetOf<String>()

            transactions.forEach { transaction ->
                // Конвертируем сумму транзакции в текущую валюту
                val convertedAmount = Money(transaction.amount.amount, currency)

                if (!transaction.isExpense) {
                    totalIncome = totalIncome.plus(convertedAmount)
                    transaction.category.let { incomeCategories.add(it) }
                } else {
                    totalExpense = totalExpense.plus(convertedAmount.abs())
                    transaction.category.let { expenseCategories.add(it) }
                }
                transaction.source.let { sources.add(it) }
            }

            val balance = totalIncome.minus(totalExpense)
            val savingsRate = if (!totalIncome.isZero()) {
                balance.amount.toDouble() / totalIncome.amount.toDouble() * 100.0
            } else {
                0.0
            }

            val averageExpense = if (expenseCategories.isNotEmpty()) {
                totalExpense.div(expenseCategories.size.toDouble())
            } else {
                Money.zero(currency)
            }

            val currentDate = Date()
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -1)
            val yearAgoDate = calendar.time

            val profileAnalytics = ProfileAnalytics(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                balance = balance,
                savingsRate = savingsRate,
                totalTransactions = transactions.size,
                totalExpenseCategories = expenseCategories.size,
                totalIncomeCategories = incomeCategories.size,
                averageExpense = averageExpense,
                totalSourcesUsed = sources.size,
                dateRange = Pair(yearAgoDate, currentDate),
                totalWallets = wallets,
            )

            CoreResult.Success(profileAnalytics)
        } catch (e: Exception) {
            CoreResult.Error(mapException(e))
        }
    }
}
