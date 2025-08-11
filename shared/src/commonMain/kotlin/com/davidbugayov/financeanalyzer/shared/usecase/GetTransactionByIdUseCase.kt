package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Возвращает транзакцию по id из переданного списка (или null).
 */
class GetTransactionByIdUseCase {
    operator fun invoke(transactions: List<Transaction>, id: String): Transaction? =
        transactions.firstOrNull { it.id == id }
}


