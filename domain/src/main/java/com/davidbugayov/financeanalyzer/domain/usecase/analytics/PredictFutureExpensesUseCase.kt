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

    /**
     * Прогноз расходов с базовой устойчивостью:
     * - исключает переводы/возвраты по эвристике
     * - усредняет последние 6 месяцев с усечением выбросов (10-й—90-й перцентили)
     * - опционально масштабирует на months
     */
    operator fun invoke(
        transactions: List<Transaction>,
        months: Int = 1,
    ): Money {
        if (transactions.isEmpty()) return Money.zero()

        // Фильтрация: исключаем явные переводы/возвраты/нулевые
        val cleaned = transactions
            .asSequence()
            .filter { it.isExpense }
            .filter { tx ->
                val label = (tx.category + " " + tx.note + " " + tx.source).lowercase()
                val isTransfer = label.contains("перевод") || label.contains("transfer")
                val isRefund = label.contains("возврат") || label.contains("refund")
                val isZero = tx.amount.amount.compareTo(BigDecimal.ZERO) == 0
                !(isTransfer || isRefund || isZero)
            }
            .toList()

        if (cleaned.isEmpty()) return Money.zero()

        // Группируем по месяцам
        val monthlyExpenses = cleaned
            .groupBy { tx ->
                val cal = Calendar.getInstance()
                cal.time = tx.date
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }
            .mapValues { entry ->
                entry.value.sumOf { it.amount.amount }
            }

        if (monthlyExpenses.isEmpty()) return Money.zero()

        // Берем последние 6 месяцев (если меньше — используем доступные)
        val sorted = monthlyExpenses
            .toList()
            .sortedBy { it.first }
            .takeLast(6)
            .map { it.second }

        // Усечение выбросов по перцентилям
        val trimmed = if (sorted.size >= 3) {
            val values = sorted.map { it }.sorted()
            val p10 = values[(values.lastIndex * 0.10).toInt()]
            val p90 = values[(values.lastIndex * 0.90).toInt()]
            values.map { it.coerceIn(p10, p90) }
        } else sorted

        // Среднее по очищенным значениям
        val sum = trimmed.fold(BigDecimal.ZERO) { acc, x -> acc + x }
        val averageMonthly = sum.divide(BigDecimal(trimmed.size), 2, java.math.RoundingMode.HALF_UP)

        // Умножаем на количество месяцев
        return Money(averageMonthly * BigDecimal(months))
    }
}
