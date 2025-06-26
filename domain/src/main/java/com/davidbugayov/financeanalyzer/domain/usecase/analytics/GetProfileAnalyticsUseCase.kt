package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.ProfileAnalytics
import com.davidbugayov.financeanalyzer.domain.model.mapException
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import java.util.Calendar
import java.util.Date
import timber.log.Timber
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult

class GetProfileAnalyticsUseCase(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {

    suspend operator fun invoke(): CoreResult<ProfileAnalytics> {
        return try {
            val transactions = transactionRepository.getAllTransactions()
            val wallets = walletRepository.getAllWallets().size

            var totalIncome = Money.zero()
            var totalExpense = Money.zero()
            val incomeCategories = mutableSetOf<String>()
            val expenseCategories = mutableSetOf<String>()
            val sources = mutableSetOf<String>()

            transactions.forEach { transaction ->
                if (transaction.amount.isPositive()) {
                    totalIncome = totalIncome.plus(transaction.amount)
                    transaction.category?.let { incomeCategories.add(it) }
                } else {
                    totalExpense = totalExpense.plus(transaction.amount.abs())
                    transaction.category?.let { expenseCategories.add(it) }
                }
                transaction.source?.let { sources.add(it) }
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
                Money.zero()
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
            Timber.e(e, "Ошибка при получении аналитики профиля")
            CoreResult.Error(mapException(e))
        }
    }
} 