package com.davidbugayov.financeanalyzer.shared.model

/**
 * Категория транзакции (KMP).
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val isExpense: Boolean,
    val count: Int = 0,
    val isCustom: Boolean = false,
) {
    companion object {
        fun expense(name: String, isCustom: Boolean = false, count: Int = 0): Category =
            Category(name = name, isExpense = true, count = count, isCustom = isCustom)

        fun income(name: String, isCustom: Boolean = false, count: Int = 0): Category =
            Category(name = name, isExpense = false, count = count, isCustom = isCustom)
    }
}


