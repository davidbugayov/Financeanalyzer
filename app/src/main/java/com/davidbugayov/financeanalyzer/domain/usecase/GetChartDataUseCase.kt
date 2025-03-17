package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.DailyData
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class GetChartDataUseCase {

    fun getExpensesByCategory(transactions: List<Transaction>): Map<String, Money> {
        val expensesByCategory = transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(0.0) { acc, transaction ->
                    acc + transaction.amount
                }
            }
            
        return expensesByCategory.mapValues { (_, amount) -> Money(amount) }
    }

    fun getIncomeByCategory(transactions: List<Transaction>): Map<String, Money> {
        val incomeByCategory = transactions
            .filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.fold(0.0) { acc, transaction ->
                    acc + transaction.amount
                }
            }
            
        return incomeByCategory.mapValues { (_, amount) -> Money(amount) }
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

                val categoryBreakdownDouble = dailyExpenses
                    .groupBy { it.category }
                    .mapValues { (_, categoryTransactions) ->
                        categoryTransactions
                            .map { it.amount }
                            .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
                    }
                    
                val categoryBreakdown = categoryBreakdownDouble.mapValues { (_, amount) -> Money(amount) }

                val incomeTotal = dailyIncome
                    .map { it.amount }
                    .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
                    
                val expenseTotal = dailyExpenses
                    .map { it.amount }
                    .reduceOrNull { acc, amount -> acc + amount } ?: 0.0

                DailyData(
                    income = Money(incomeTotal),
                    expense = Money(expenseTotal),
                    categoryBreakdown = categoryBreakdown
                )
            }
            .toSortedMap()
    }
} 