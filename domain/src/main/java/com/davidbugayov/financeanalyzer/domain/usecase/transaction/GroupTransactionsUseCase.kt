package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.filter.GroupingType
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
    operator fun invoke(transactions: List<Transaction>, groupingType: GroupingType): Map<String, List<Transaction>> {
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
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ru"))
        val groupedTransactions = transactions
            .sortedByDescending { it.date }
            .groupBy { dateFormat.format(it.date).replaceFirstChar { it.uppercase() } }

        // Сортируем группы по убыванию даты
        return groupedTransactions.toList()
            .sortedByDescending { (key, _) ->
                // Парсим дату из ключа
                val parts = key.split(" ")
                val day = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val monthName = parts.getOrNull(1) ?: ""
                val month = getMonthNumber(monthName)
                val year = parts.getOrNull(2)?.toIntOrNull() ?: 0

                // Создаем числовое представление для сортировки (год*10000 + месяц*100 + день)
                year * 10000 + month * 100 + day
            }
            .toMap()
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
            val firstDay = SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru")).format(
                calendar.time,
            )
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val lastDay = SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru")).format(
                calendar.time,
            )
            val year = calendar.get(Calendar.YEAR)
            val weekKey = "$firstDay - $lastDay $year"

            if (!result.containsKey(weekKey)) {
                result[weekKey] = mutableListOf()
            }
            result[weekKey]?.add(transaction)
        }

        // Сортируем группы по убыванию даты
        return result.toList()
            .sortedByDescending { (key, _) -> // Извлекаем год из ключа
                val year = key.split(" ").lastOrNull()?.toIntOrNull() ?: 0
                // Извлекаем дату из конца периода (последний день недели)
                val lastDate = key.split(" ").firstOrNull()?.split("-")?.lastOrNull()?.trim() ?: ""
                val dateParts = lastDate.split(".")
                val month = dateParts.getOrNull(1)?.toIntOrNull() ?: 0
                val day = dateParts.getOrNull(0)?.toIntOrNull() ?: 0

                // Создаем числовое представление для сортировки (год*10000 + месяц*100 + день)
                year * 10000 + month * 100 + day
            }
            .toMap()
    }

    /**
     * Группирует транзакции по месяцам.
     * Формат периода: "MMMM YYYY" (например, "Январь 2024")
     *
     * @param transactions Список транзакций
     * @return Карта сгруппированных по месяцам транзакций
     */
    private fun groupByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val format = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("ru"))
        val groupedTransactions = transactions
            .sortedByDescending { it.date }
            .groupBy { format.format(it.date).replaceFirstChar { it.uppercase() } }

        // Сортируем группы (ключи) по убыванию даты
        return groupedTransactions.toList()
            .sortedByDescending { (key, _) ->
                // Разбиваем ключ на месяц и год
                val parts = key.split(" ")
                val year = parts.lastOrNull()?.toIntOrNull() ?: 0
                val month = getMonthNumber(parts.firstOrNull() ?: "")
                // Создаем числовое представление для сортировки (год*100 + месяц)
                year * 100 + month
            }
            .toMap()
    }

    /**
     * Преобразует название месяца на русском языке в числовой формат (1-12)
     */
    private fun getMonthNumber(monthName: String): Int {
        return when (monthName.lowercase()) {
                    "январь" -> 1
        "февраль" -> 2
        "март" -> 3
        "апрель" -> 4
        "май" -> 5
        "июнь" -> 6
        "июль" -> 7
        "август" -> 8
        "сентябрь" -> 9
        "октябрь" -> 10
        "ноябрь" -> 11
        "декабрь" -> 12
            else -> 0
        }
    }
} 