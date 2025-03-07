package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Calendar
import java.util.Date

/**
 * Use case для фильтрации списка транзакций.
 * Позволяет фильтровать транзакции по периоду и категории.
 * Поддерживает различные типы периодов и произвольный диапазон дат.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class FilterTransactionsUseCase {

    /**
     * Фильтрует транзакции по заданным критериям.
     *
     * @param transactions Исходный список транзакций
     * @param periodType Тип периода для фильтрации
     * @param startDate Начальная дата для произвольного периода
     * @param endDate Конечная дата для произвольного периода
     * @param category Категория для фильтрации (null для всех категорий)
     * @return Отфильтрованный список транзакций
     */
    operator fun invoke(
        transactions: List<Transaction>,
        periodType: PeriodType,
        startDate: Date? = null,
        endDate: Date? = null,
        category: String? = null
    ): List<Transaction> {
        val filteredByPeriod = filterByPeriod(transactions, periodType, startDate, endDate)
        return filterByCategory(filteredByPeriod, category)
    }

    /**
     * Фильтрует транзакции по периоду.
     * Поддерживает предустановленные периоды (месяц, квартал, полгода, год)
     * и произвольный период с указанием начальной и конечной даты.
     *
     * @param transactions Список транзакций для фильтрации
     * @param periodType Тип периода
     * @param startDate Начальная дата для произвольного периода
     * @param endDate Конечная дата для произвольного периода
     * @return Отфильтрованный по периоду список транзакций
     */
    private fun filterByPeriod(
        transactions: List<Transaction>,
        periodType: PeriodType,
        startDate: Date? = null,
        endDate: Date? = null
    ): List<Transaction> {
        val calendar = Calendar.getInstance()

        return when (periodType) {
            PeriodType.ALL -> transactions
            PeriodType.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.time
                transactions.filter { it.date.after(monthAgo) || it.date == monthAgo }
            }
            PeriodType.QUARTER -> {
                calendar.add(Calendar.MONTH, -3)
                val quarterAgo = calendar.time
                transactions.filter { it.date.after(quarterAgo) || it.date == quarterAgo }
            }
            PeriodType.HALF_YEAR -> {
                calendar.add(Calendar.MONTH, -6)
                val halfYearAgo = calendar.time
                transactions.filter { it.date.after(halfYearAgo) || it.date == halfYearAgo }
            }
            PeriodType.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                val yearAgo = calendar.time
                transactions.filter { it.date.after(yearAgo) || it.date == yearAgo }
            }
            PeriodType.CUSTOM -> {
                if (startDate != null && endDate != null) {
                    val endCalendar = Calendar.getInstance()
                    endCalendar.time = endDate
                    endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                    endCalendar.set(Calendar.MINUTE, 59)
                    endCalendar.set(Calendar.SECOND, 59)

                    transactions.filter {
                        (it.date.after(startDate) || it.date == startDate) &&
                                (it.date.before(endCalendar.time) || it.date == endCalendar.time)
                    }
                } else {
                    transactions
                }
            }
        }
    }

    /**
     * Фильтрует транзакции по категории.
     * Если категория не указана, возвращает все транзакции.
     *
     * @param transactions Список транзакций для фильтрации
     * @param category Категория для фильтрации
     * @return Отфильтрованный по категории список транзакций
     */
    private fun filterByCategory(
        transactions: List<Transaction>,
        category: String?
    ): List<Transaction> {
        return if (category != null) {
            transactions.filter { it.category == category }
        } else {
            transactions
        }
    }
} 