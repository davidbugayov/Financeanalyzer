package com.davidbugayov.financeanalyzer.presentation.home.event

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter

/**
 * События для экрана Home.
 * Следует принципу открытости/закрытости (OCP) из SOLID.
 */
sealed class HomeEvent {
    data class SetFilter(
        val filter: TransactionFilter,
    ) : HomeEvent()

    data object LoadTransactions : HomeEvent()

    data object GenerateTestData : HomeEvent()

    data class SetShowGroupSummary(
        val show: Boolean,
    ) : HomeEvent()

    data class ShowDeleteConfirmDialog(
        val transaction: Transaction,
    ) : HomeEvent()

    data object HideDeleteConfirmDialog : HomeEvent()

    data class DeleteTransaction(
        val transaction: Transaction,
    ) : HomeEvent()

    data class ChangeNotifications(
        val enabled: Boolean,
    ) : HomeEvent()

    data object NavigateToChart : HomeEvent()

    data object NavigateToProfile : HomeEvent()

    data object NavigateToHistory : HomeEvent()

    data object NavigateToAddTransaction : HomeEvent()

    data object NavigateToDebts : HomeEvent()

    data class EditTransaction(
        val transaction: Transaction,
    ) : HomeEvent()
}
