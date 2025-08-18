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
    override suspend fun getAllWallets(): List<SharedWallet> = domainRepo.getAllWallets().map { it.toShared() }

    override suspend fun updateWallet(wallet: SharedWallet) {
        domainRepo.updateWallet(wallet.toDomain())
    }
}

private fun DomainWallet.toShared(): SharedWallet =
    SharedWallet(
        id = this.id,
        name = this.name,
        balance = this.balance,
        limit = this.limit,
    )

private fun SharedWallet.toDomain(): DomainWallet =
    DomainWallet(
        id = this.id,
        name = this.name,
        balance = this.balance,
        limit = this.limit,
        spent = this.balance,
    )
