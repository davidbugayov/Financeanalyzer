package com.davidbugayov.financeanalyzer.presentation.budget.wallet.model

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet

/**
 * Состояние для экрана транзакций кошелька
 *
 * @property wallet Кошелек, для которого показываются транзакции
 * @property transactions Список транзакций кошелька
 * @property isLoading Флаг загрузки данных
 * @property error Сообщение об ошибке, если есть
 */
data class WalletTransactionsState(
    val wallet: Wallet? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
