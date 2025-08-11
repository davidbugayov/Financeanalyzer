package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.model.filter.PeriodType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus

/**
 * Фильтрация транзакций по периоду/типу.
 */
class FilterTransactionsUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        periodType: PeriodType,
        now: LocalDate,
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null,
        isExpense: Boolean? = null,
    ): List<Transaction> {
        val (start, end) = when (periodType) {
            PeriodType.DAY -> now to now
            PeriodType.WEEK -> now.minus(DatePeriod(days = 6)) to now
            PeriodType.MONTH -> now.minus(DatePeriod(months = 1)).withDay(1) to now
            PeriodType.QUARTER -> now.minus(DatePeriod(months = 3)).withDay(1) to now
            PeriodType.YEAR -> now.minus(DatePeriod(years = 1)).withDay(1) to now
            PeriodType.ALL -> null to null
            PeriodType.CUSTOM -> customStart to customEnd
        }

        return transactions.asSequence()
            .filter { tx -> isExpense == null || tx.isExpense == isExpense }
            .filter { tx ->
                if (start == null || end == null) return@filter true
                tx.date >= start && tx.date <= end
            }
            .toList()
    }
}

private fun LocalDate.withDay(day: Int): LocalDate = LocalDate(year, month, day)


