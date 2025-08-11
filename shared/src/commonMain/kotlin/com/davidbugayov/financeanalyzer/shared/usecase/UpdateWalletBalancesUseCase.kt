package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.model.Wallet
import com.davidbugayov.financeanalyzer.shared.repository.WalletRepository

/**
 * Пересчитывает балансы кошельков по списку транзакций.
 */
class UpdateWalletBalancesUseCase(
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(transactions: List<Transaction>) {
        val wallets = walletRepository.getAllWallets()
        val walletsByName = wallets.associateBy { it.name }
        val deltas = mutableMapOf<String, Long>()

        transactions.forEach { tx ->
            val key = tx.source
            val sign = if (tx.isExpense) -1 else 1
            deltas[key] = (deltas[key] ?: 0L) + sign * tx.amount.minor
        }

        for ((name, delta) in deltas) {
            val w = walletsByName[name] ?: continue
            val updated = w.copy(balance = Money(w.balance.minor + delta, w.balance.currency))
            walletRepository.updateWallet(updated)
        }
    }
}


