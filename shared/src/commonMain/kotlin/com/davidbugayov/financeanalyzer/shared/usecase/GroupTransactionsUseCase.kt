package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate

/**
 * Группирует транзакции по ключу (день/месяц/год/категория), возвращая Map ключ -> список транзакций.
 */
class GroupTransactionsUseCase {
    enum class KeyType { DAY, WEEK, MONTH, YEAR, CATEGORY, SOURCE }

    operator fun invoke(
        transactions: List<Transaction>,
        keyType: KeyType,
    ): Map<String, List<Transaction>> {
        return transactions.groupBy { tx ->
            when (keyType) {
                KeyType.DAY -> formatDate(tx.date)
                KeyType.WEEK -> formatWeek(tx.date)
                KeyType.MONTH -> "${tx.date.year}-${(tx.date.month.ordinal + 1).toString().padStart(2, '0')}"
                KeyType.YEAR -> tx.date.year.toString()
                KeyType.CATEGORY -> tx.category
                KeyType.SOURCE -> tx.source
            }
        }
    }

    private fun formatDate(d: LocalDate): String {
        val mm = (d.month.ordinal + 1).toString().padStart(2, '0')
        val dd = d.day.toString().padStart(2, '0')
        return "${d.year}-$mm-$dd"
    }

    private fun formatWeek(d: LocalDate): String {
        // Простая реализация: считаем неделю от начала года
        val dayOfYear = d.dayOfYear
        val week = ((dayOfYear - 1) / 7) + 1
        return "${d.year}-W${week.toString().padStart(2, '0')}"
    }
}


