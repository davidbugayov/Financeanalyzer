package com.davidbugayov.financeanalyzer.presentation.budget.wallet.model

/**
 * События для экрана транзакций кошелька
 */
sealed class WalletTransactionsEvent {
    /**
     * Загрузить информацию о кошельке
     */
    data class LoadWallet(
        val walletId: String,
    ) : WalletTransactionsEvent()

    /**
     * Загрузить транзакции для кошелька
     */
    data class LoadTransactions(
        val walletId: String,
    ) : WalletTransactionsEvent()

    /**
     * Связать выбранные категории с кошельком
     */
    data class LinkCategories(
        val categories: List<String>,
    ) : WalletTransactionsEvent()

    /**
     * Очистить сообщение об ошибке
     */
    data object ClearError : WalletTransactionsEvent()
}
