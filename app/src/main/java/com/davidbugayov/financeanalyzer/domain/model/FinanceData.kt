package com.davidbugayov.financeanalyzer.domain.model

/**
 * Модель данных для финансовых графиков.
 * Содержит метки, доходы и расходы для отображения на графиках.
 */
data class FinanceData(
    val labels: List<String>, // Даты или категории
    val incomes: List<Float>, // Доходы
    val expenses: List<Float> // Расходы
) 