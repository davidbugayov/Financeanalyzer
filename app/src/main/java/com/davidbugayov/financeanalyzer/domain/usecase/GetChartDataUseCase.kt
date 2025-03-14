package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.DailyData
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class GetChartDataUseCase {

    fun getExpensesByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(Money.zero()) { acc, transaction ->
                    acc + transaction.amount
                }
            }
    }

    fun getIncomeByCategory(transactions: List<Transaction>): Map<String, Money> {
        return transactions
            .filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(Money.zero()) { acc, transaction ->
                    acc + transaction.amount
                }
            }
    }

    fun getExpensesByDay(days: Int, transactions: List<Transaction>): Map<String, DailyData> {
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val daysInMillis = days * 24 * 60 * 60 * 1000L

        return transactions
            .filter { (currentTime - it.date.time) <= daysInMillis }
            .groupBy { dateFormat.format(it.date) }
            .mapValues { (_, txs) ->
                val dailyExpenses = txs.filter { it.isExpense }
                val dailyIncome = txs.filter { !it.isExpense }

                val categoryBreakdown = dailyExpenses
                    .groupBy { it.category }
                    .mapValues { (_, categoryTransactions) ->
                        categoryTransactions
                            .map { it.amount }
                            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
                    }

                DailyData(
                    income = dailyIncome
                        .map { it.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero(),
                    expense = dailyExpenses
                        .map { it.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero(),
                    categoryBreakdown = categoryBreakdown
                )
            }
            .toSortedMap()
    }
} 