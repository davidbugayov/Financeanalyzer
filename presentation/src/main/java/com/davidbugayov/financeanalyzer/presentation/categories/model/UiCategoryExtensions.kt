package com.davidbugayov.financeanalyzer.presentation.categories.model

fun UiCategory.isDefaultExpenseCategory(): Boolean = isExpense && !isCustom

fun UiCategory.isDefaultIncomeCategory(): Boolean = !isExpense && !isCustom
