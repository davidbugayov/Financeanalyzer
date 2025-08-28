package com.davidbugayov.financeanalyzer.shared.mapper

import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * iOS-specific реализация TransactionMapper.
 * Использует iOS-specific модели данных.
 */
actual object TransactionMapper {

    actual fun toShared(platformTransaction: Any): Transaction {
        // iOS implementation will be added when iOS models are defined
        // For now, return a default transaction
        return Transaction(
            id = "ios_default",
            amount = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
            category = "Default",
            date = kotlinx.datetime.LocalDate(2024, 1, 1),
            isExpense = false,
            source = "iOS",
        )
    }

    actual fun toPlatform(sharedTransaction: Transaction): Any {
        // iOS implementation will be added when iOS models are defined
        return sharedTransaction
    }

    actual fun toSharedList(platformTransactions: List<Any>): List<Transaction> {
        return platformTransactions.map { toShared(it) }
    }

    actual fun toPlatformList(sharedTransactions: List<Transaction>): List<Any> {
        return sharedTransactions.map { toPlatform(it) }
    }
}
