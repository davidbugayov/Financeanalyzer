package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Calendar
import java.util.Date

/**
 * Use case для фильтрации списка транзакций.
 * Позволяет фильтровать транзакции по периоду и категории.
 * Поддерживает различные типы периодов: все, день, месяц, год и кастомный период.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class FilterTransactionsUseCase {

    /**
     * Фильтрует транзакции по заданным критериям.
     *
     * @param transactions Исходный список транзакций
     * @param periodType Тип периода для фильтрации
     * @param startDate Начальная дата для кастомного периода
     * @param endDate Конечная дата для кастомного периода
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
     * Поддерживает предустановленные периоды (день, месяц, год)
     * и кастомный период с указанием начальной и конечной даты.
     *
     * @param transactions Список транзакций для фильтрации
     * @param periodType Тип периода
     * @param startDate Начальная дата для кастомного периода
     * @param endDate Конечная дата для кастомного периода
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
            PeriodType.DAY -> {
                // Начало текущего дня
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time

                // Конец текущего дня
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = calendar.time

                transactions.filter {
                    (it.date.after(startOfDay) || it.date == startOfDay) &&
                            (it.date.before(endOfDay) || it.date == endOfDay)
                }
            }
            PeriodType.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.time
                transactions.filter { it.date.after(monthAgo) || it.date == monthAgo }
            }
            PeriodType.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                val yearAgo = calendar.time
                transactions.filter { it.date.after(yearAgo) || it.date == yearAgo }
            }
            PeriodType.CUSTOM -> {
                if (startDate != null && endDate != null) {
                    // Устанавливаем начало дня для startDate
                    val startCalendar = Calendar.getInstance()
                    startCalendar.time = startDate
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    startCalendar.set(Calendar.MINUTE, 0)
                    startCalendar.set(Calendar.SECOND, 0)
                    startCalendar.set(Calendar.MILLISECOND, 0)
                    val start = startCalendar.time

                    // Устанавливаем конец дня для endDate
                    val endCalendar = Calendar.getInstance()
                    endCalendar.time = endDate
                    endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                    endCalendar.set(Calendar.MINUTE, 59)
                    endCalendar.set(Calendar.SECOND, 59)
                    endCalendar.set(Calendar.MILLISECOND, 999)
                    val end = endCalendar.time

                    transactions.filter {
                        (it.date.after(start) || it.date == start) &&
                                (it.date.before(end) || it.date == end)
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