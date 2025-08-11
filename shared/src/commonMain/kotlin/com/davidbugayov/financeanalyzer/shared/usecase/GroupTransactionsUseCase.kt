package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate

/**
 * Группирует транзакции по ключу (день/месяц/год/категория), возвращая Map ключ -> список транзакций.
 */
class GroupTransactionsUseCase {
    enum class KeyType { DAY, MONTH, YEAR, CATEGORY, SOURCE }

    operator fun invoke(
        transactions: List<Transaction>,
        keyType: KeyType,
    ): Map<String, List<Transaction>> {
        return transactions.groupBy { tx ->
            when (keyType) {
                KeyType.DAY -> formatDate(tx.date)
                KeyType.MONTH -> "${tx.date.year}-${tx.date.month}"
                KeyType.YEAR -> tx.date.year.toString()
                KeyType.CATEGORY -> tx.category
                KeyType.SOURCE -> tx.source
            }
        }
    }

    private fun formatDate(d: LocalDate): String {
        val mm = d.monthNumber.toString().padStart(2, '0')
        val dd = d.dayOfMonth.toString().padStart(2, '0')
        return "${d.year}-$mm-$dd"
    }
}


