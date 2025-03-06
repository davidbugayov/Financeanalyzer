package com.davidbugayov.financeanalyzer.presentation.home.event

import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter

/**
 * События для экрана Home.
 * Следует принципу открытости/закрытости (OCP) из SOLID.
 */
sealed class HomeEvent {

    data class SetFilter(val filter: TransactionFilter) : HomeEvent()
    data object LoadTransactions : HomeEvent()
    data object GenerateTestData : HomeEvent()
    data class SetShowGroupSummary(val show: Boolean) : HomeEvent()
} 