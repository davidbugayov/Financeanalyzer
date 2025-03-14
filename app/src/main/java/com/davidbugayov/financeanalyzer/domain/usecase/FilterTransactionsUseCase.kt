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
        val filteredByPeriod = if (periodType == PeriodType.CUSTOM && startDate != null && endDate != null) {
            filterByDateRange(transactions, startDate, endDate)
        } else {
            filterByPeriod(transactions, periodType)
        }
        return filterByCategory(filteredByPeriod, category)
    }

    /**
     * Фильтрует транзакции по периоду.
     * Поддерживает предустановленные периоды (день, месяц, год)
     * и кастомный период с указанием начальной и конечной даты.
     *
     * @param transactions Список транзакций для фильтрации
     * @param periodType Тип периода
     * @return Отфильтрованный по периоду список транзакций
     */
    fun filterByPeriod(transactions: List<Transaction>, periodType: PeriodType): List<Transaction> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time

        val startDate = calendar.apply {
            when (periodType) {
                PeriodType.ALL -> add(Calendar.YEAR, -10)
                PeriodType.DAY -> add(Calendar.DAY_OF_MONTH, -1)
                PeriodType.WEEK -> add(Calendar.WEEK_OF_YEAR, -1)
                PeriodType.MONTH -> add(Calendar.MONTH, -1)
                PeriodType.QUARTER -> add(Calendar.MONTH, -3)
                PeriodType.YEAR -> add(Calendar.YEAR, -1)
                PeriodType.CUSTOM -> Unit // Не меняем даты для пользовательского периода
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return transactions.filter { transaction ->
            transaction.date >= startDate && transaction.date <= endDate
        }
    }

    /**
     * Фильтрует транзакции по дате.
     *
     * @param transactions Список транзакций для фильтрации
     * @param startDate Начальная дата для фильтрации
     * @param endDate Конечная дата для фильтрации
     * @return Отфильтрованный по дате список транзакций
     */
    fun filterByDateRange(transactions: List<Transaction>, startDate: Date, endDate: Date): List<Transaction> {
        return transactions.filter { transaction ->
            transaction.date >= startDate && transaction.date <= endDate
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