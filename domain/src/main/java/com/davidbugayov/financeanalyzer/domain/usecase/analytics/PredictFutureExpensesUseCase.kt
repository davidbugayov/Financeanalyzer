package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.util.Calendar

/**
 * UseCase для предсказания будущих расходов на основе исторических данных.
 *
 * @return Предсказанная сумма расходов.
 */
class PredictFutureExpensesUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        months: Int = 1,
    ): Money {
        if (transactions.isEmpty()) return Money.zero()

        // Группируем расходы по месяцам
        val monthlyExpenses = transactions
            .filter { it.isExpense }
            .groupBy { tx ->
                val cal = Calendar.getInstance()
                cal.time = tx.date
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }
            .mapValues { entry ->
                entry.value.sumOf { it.amount.amount }
            }

        if (monthlyExpenses.isEmpty()) return Money.zero()

        // Рассчитываем средний месячный расход
        val averageMonthly = monthlyExpenses.values
            .fold(BigDecimal.ZERO) { acc, amount -> acc + amount }
            .divide(BigDecimal(monthlyExpenses.size), 2, java.math.RoundingMode.HALF_UP)

        // Умножаем на количество месяцев
        return Money(averageMonthly * BigDecimal(months))
    }
}
