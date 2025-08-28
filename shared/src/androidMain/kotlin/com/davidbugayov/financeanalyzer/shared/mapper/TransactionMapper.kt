package com.davidbugayov.financeanalyzer.shared.mapper

import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Android-specific реализация TransactionMapper.
 * Пока использует заглушки для избежания циклических зависимостей.
 */
actual object TransactionMapper {

    actual fun toShared(platformTransaction: Any): Transaction {
        // Заглушка - будет реализована после решения циклических зависимостей
        return Transaction(
            id = "stub_${System.currentTimeMillis()}",
            amount = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
            category = "Stub",
            date = kotlinx.datetime.LocalDate(2024, 1, 1),
            isExpense = false,
            source = "Stub",
        )
    }

    actual fun toPlatform(sharedTransaction: Transaction): Any {
        // Заглушка - будет реализована после решения циклических зависимостей
        return sharedTransaction
    }

    actual fun toSharedList(platformTransactions: List<Any>): List<Transaction> {
        return platformTransactions.map { toShared(it) }
    }

    actual fun toPlatformList(sharedTransactions: List<Transaction>): List<Any> {
        return sharedTransactions.map { toPlatform(it) }
    }
}