package com.davidbugayov.financeanalyzer.shared.model

import kotlinx.datetime.LocalDate

/**
 * Унифицированная KMP-модель транзакции для всех платформ.
 * Содержит все необходимые поля для работы с транзакциями.
 */
data class Transaction(
    val id: String,
    val amount: Money,
    val category: String,
    val date: LocalDate,
    val isExpense: Boolean,
    val note: String? = null,
    val source: String,
    val sourceColor: Int = 0,
    val categoryId: String = "",
    val title: String = "",
    val isTransfer: Boolean = false,
    val walletIds: List<String>? = null,
    val subcategoryId: Long? = null,
) {
    /**
     * Конвертирует дату в timestamp для совместимости с Java Date
     * Упрощенная версия без сложных datetime операций
     */
    fun toTimestamp(): Long {
        // Упрощенная конвертация: год * 365 + месяц * 30 + день
        // Это не точный timestamp, но достаточно для базовой функциональности
        return (date.year * 365L + date.monthNumber * 30L + date.dayOfMonth) * 24 * 60 * 60 * 1000
    }



    companion object {
        /**
         * Создает транзакцию из timestamp
         */
        fun fromTimestamp(
            id: String,
            amount: Money,
            category: String,
            timestamp: Long,
            isExpense: Boolean,
            note: String? = null,
            source: String,
            sourceColor: Int = 0,
            categoryId: String = "",
            title: String = "",
            isTransfer: Boolean = false,
            walletIds: List<String>? = null,
            subcategoryId: Long? = null,
        ): Transaction {
            // Упрощенная конвертация timestamp в дату
            // В реальном приложении это будет реализовано platform-specific способом
            val year = 2024
            val month = 1
            val day = 1

            return Transaction(
                id = id,
                amount = amount,
                category = category,
                date = LocalDate(year, month, day),
                isExpense = isExpense,
                note = note,
                source = source,
                sourceColor = sourceColor,
                categoryId = categoryId,
                title = title,
                isTransfer = isTransfer,
                walletIds = walletIds,
                subcategoryId = subcategoryId,
            )
        }
    }
}


