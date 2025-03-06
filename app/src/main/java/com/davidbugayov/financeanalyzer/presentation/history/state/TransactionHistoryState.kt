package com.davidbugayov.financeanalyzer.presentation.history.state

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Calendar
import java.util.Date

/**
 * Состояние экрана истории транзакций.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
data class TransactionHistoryState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val groupingType: GroupingType = GroupingType.MONTH,
    val periodType: PeriodType = PeriodType.MONTH,
    val startDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
    val endDate: Date = Date(),
    val categoryStats: Triple<Double, Double, Int?>? = null,
    val showPeriodDialog: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false
) 