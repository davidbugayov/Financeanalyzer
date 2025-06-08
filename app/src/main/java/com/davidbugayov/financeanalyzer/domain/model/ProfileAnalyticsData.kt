package com.davidbugayov.financeanalyzer.domain.model // Или другой подходящий пакет в domain

/**
 * Дата-класс для хранения всех аналитических данных, необходимых для экрана профиля.
 */
data class ProfileAnalyticsData(
    val totalIncome: Money,
    val totalExpense: Money,
    val balance: Money,
    val savingsRate: Double,
    val totalTransactions: Int,
    val totalExpenseCategories: Int,
    val totalIncomeCategories: Int,
    val averageExpense: String, // Рассмотреть возможность возвращать Money и форматировать в UI
    val totalSourcesUsed: Int,
    val dateRange: String
) 
