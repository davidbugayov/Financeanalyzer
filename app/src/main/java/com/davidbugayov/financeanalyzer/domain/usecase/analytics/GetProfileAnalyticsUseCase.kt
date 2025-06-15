package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.AppProfileAnalytics
import com.davidbugayov.financeanalyzer.domain.model.ProfileAnalytics
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.util.Result
import java.util.Date
import java.util.Calendar

class GetProfileAnalyticsUseCase(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {

    suspend operator fun invoke(): Result<AppProfileAnalytics> {
        return try {
            // Получаем данные из репозиториев
            val transactions = transactionRepository.getAllTransactions()
            val wallets = walletRepository.getAllWallets().size

            // Вычисляем финансовые показатели
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

            // Вычисляем баланс и процент сбережений
            val balance = totalIncome.minus(totalExpense)
            val savingsRate = if (!totalIncome.isZero()) {
                balance.amount.toDouble() / totalIncome.amount.toDouble() * 100.0
            } else {
                0.0
            }

            // Вычисляем среднюю сумму расходов
            val averageExpense = if (expenseCategories.isNotEmpty()) {
                totalExpense.div(expenseCategories.size.toDouble())
            } else {
                Money.zero()
            }

            // Вычисляем диапазон дат (последний год)
            val currentDate = Date()
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -1)
            val yearAgoDate = calendar.time

            // Создаем доменную модель ProfileAnalytics
            val domainAnalytics = ProfileAnalytics(
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

            // Конвертируем в AppProfileAnalytics для совместимости
            val appAnalytics = AppProfileAnalytics.fromDomainModel(domainAnalytics)

            Result.Success(appAnalytics)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
