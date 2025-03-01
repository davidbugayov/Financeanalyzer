package com.davidbugayov.financeanalyzer.data.model

data class FinanceData(
    val labels: List<String>, // Даты или категории
    val incomes: List<Float>, // Доходы
    val expenses: List<Float> // Расходы
)