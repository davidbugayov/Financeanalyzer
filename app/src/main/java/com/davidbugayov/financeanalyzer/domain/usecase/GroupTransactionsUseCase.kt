package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Юзкейс для группировки транзакций по различным периодам.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class GroupTransactionsUseCase {

    /**
     * Группирует транзакции по выбранному типу группировки
     */
    operator fun invoke(
        transactions: List<Transaction>,
        groupingType: GroupingType
    ): Map<String, List<Transaction>> {
        return when (groupingType) {
            GroupingType.DAY -> groupByDay(transactions)
            GroupingType.WEEK -> groupByWeek(transactions)
            GroupingType.MONTH -> groupByMonth(transactions)
        }
    }

    /**
     * Группирует транзакции по дням
     */
    private fun groupByDay(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
        return transactions
            .sortedByDescending { it.date }
            .groupBy { dateFormat.format(it.date).replaceFirstChar { it.uppercase() } }
    }

    /**
     * Группирует транзакции по неделям
     */
    private fun groupByWeek(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val calendar = Calendar.getInstance()
        val result = mutableMapOf<String, MutableList<Transaction>>()
        val sortedTransactions = transactions.sortedByDescending { it.date }

        for (transaction in sortedTransactions) {
            calendar.time = transaction.date
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val firstDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val lastDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)
            val year = calendar.get(Calendar.YEAR)
            val weekKey = "$firstDay - $lastDay $year"

            if (!result.containsKey(weekKey)) {
                result[weekKey] = mutableListOf()
            }
            result[weekKey]?.add(transaction)
        }

        return result
    }

    /**
     * Группирует транзакции по месяцам
     */
    private fun groupByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val format = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        return transactions
            .sortedByDescending { it.date }
            .groupBy { format.format(it.date).replaceFirstChar { it.uppercase() } }
    }
} 