package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import java.math.BigDecimal

class UpdateWalletBalancesUseCase {
    operator fun invoke(transactions: List<Transaction>): Map<String, Money> {
        val walletBalances = mutableMapOf<String, Money>()

        transactions.forEach { transaction ->
            val walletId = transaction.source
            val currentBalance = walletBalances[walletId] ?: Money.zero()
            
            val newBalance = if (transaction.isExpense) {
                currentBalance - transaction.amount
            } else {
                currentBalance + transaction.amount
            }
            
            walletBalances[walletId] = newBalance
        }

        return walletBalances
    }
}


