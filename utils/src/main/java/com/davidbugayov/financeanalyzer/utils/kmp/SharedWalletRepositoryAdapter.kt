package com.davidbugayov.financeanalyzer.utils.kmp

import com.davidbugayov.financeanalyzer.domain.model.Wallet as DomainWallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository as DomainWalletRepository
import com.davidbugayov.financeanalyzer.shared.model.Wallet as SharedWallet
import com.davidbugayov.financeanalyzer.shared.repository.WalletRepository as SharedWalletRepository

/**
 * Адаптер domain WalletRepository под KMP SharedWalletRepository.
 */
class SharedWalletRepositoryAdapter(
    private val domainRepo: DomainWalletRepository,
) : SharedWalletRepository {
    override suspend fun getAllWallets(): List<SharedWallet> {
        return domainRepo.getAllWallets().map { it.toShared() }
    }

    override suspend fun updateWallet(wallet: SharedWallet) {
        domainRepo.updateWallet(wallet.toDomain())
    }
}

private fun DomainWallet.toShared(): SharedWallet {
    return SharedWallet(
        id = this.id,
        name = this.name,
        balance = this.balance.toSharedMoney(),
        limit = this.limit.toSharedMoney(),
    )
}

private fun com.davidbugayov.financeanalyzer.core.model.Money.toSharedMoney(): com.davidbugayov.financeanalyzer.shared.model.Money {
    val currency = com.davidbugayov.financeanalyzer.shared.model.Currency.fromCode(this.currency.code)
    return com.davidbugayov.financeanalyzer.shared.model.Money.fromMajor(this.amount.toDouble(), currency)
}

private fun SharedWallet.toDomain(): DomainWallet {
    return DomainWallet(
        id = this.id,
        name = this.name,
        balance = this.balance.toCore(),
        limit = this.limit.toCore(),
        spent = this.balance.toCore(), // заглушка для spent
    )
}
