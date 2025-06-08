package com.davidbugayov.financeanalyzer.domain.model

/**
 * Перечисление для типов транзакций
 */
enum class TransactionType {
    INCOME,
    EXPENSE;

    companion object {
        /**
         * Преобразует флаг isExpense в TransactionType
         */
        fun fromExpenseFlag(isExpense: Boolean): TransactionType {
            return if (isExpense) EXPENSE else INCOME
        }
    }

    /**
     * Возвращает флаг isExpense на основе типа транзакции
     */
    fun isExpense(): Boolean {
        return this == EXPENSE
    }
} 
