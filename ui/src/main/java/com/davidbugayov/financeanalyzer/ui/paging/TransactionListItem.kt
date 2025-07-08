package com.davidbugayov.financeanalyzer.ui.paging

import com.davidbugayov.financeanalyzer.domain.model.Transaction

sealed interface TransactionListItem {
    data class Header(val title: String) : TransactionListItem

    data class Item(val transaction: Transaction) : TransactionListItem
}
