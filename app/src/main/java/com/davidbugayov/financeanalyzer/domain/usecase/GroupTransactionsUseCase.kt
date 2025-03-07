package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Use case для группировки транзакций по временным периодам.
 * Поддерживает группировку по дням, неделям и месяцам.
 * Использует локализованные форматы дат на русском языке.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class GroupTransactionsUseCase {

    /**
     * Группирует транзакции по выбранному типу группировки.
     * Сортирует транзакции по убыванию даты внутри каждой группы.
     *
     * @param transactions Список транзакций для группировки
     * @param groupingType Тип группировки (день, неделя, месяц)
     * @return Карта, где ключ - название периода, значение - список транзакций
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
     * Группирует транзакции по дням.
     * Формат даты: "DD MMMM YYYY" (например, "1 января 2024")
     *
     * @param transactions Список транзакций
     * @return Карта сгруппированных по дням транзакций
     */
    private fun groupByDay(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
        return transactions
            .sortedByDescending { it.date }
            .groupBy { dateFormat.format(it.date).replaceFirstChar { it.uppercase() } }
    }

    /**
     * Группирует транзакции по неделям.
     * Формат периода: "DD.MM - DD.MM YYYY" (например, "01.01 - 07.01 2024")
     * Неделя начинается с понедельника.
     *
     * @param transactions Список транзакций
     * @return Карта сгруппированных по неделям транзакций
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
     * Группирует транзакции по месяцам.
     * Формат периода: "MMMM YYYY" (например, "Январь 2024")
     *
     * @param transactions Список транзакций
     * @return Карта сгруппированных по месяцам транзакций
     */
    private fun groupByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val format = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        return transactions
            .sortedByDescending { it.date }
            .groupBy { format.format(it.date).replaceFirstChar { it.uppercase() } }
    }
} 