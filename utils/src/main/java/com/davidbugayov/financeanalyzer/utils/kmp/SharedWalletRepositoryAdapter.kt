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

    override suspend fun getWalletById(id: String): SharedWallet? = domainRepo.getWalletById(id)?.toShared()

    override suspend fun createWallet(wallet: SharedWallet): String {
        domainRepo.addWallet(wallet.toDomain())
        return wallet.id
    }

    override suspend fun updateWallet(wallet: SharedWallet) {
        domainRepo.updateWallet(wallet.toDomain())
    }

    override suspend fun deleteWallet(id: String) {
        domainRepo.deleteWalletById(id)
    }

    override suspend fun getWalletsByType(
        type: com.davidbugayov.financeanalyzer.shared.model.WalletType,
    ): List<SharedWallet> {
        // Для простоты возвращаем все кошельки - можно реализовать фильтрацию позже
        return getAllWallets()
    }

    override suspend fun updateWalletBalance(
        walletId: String,
        newBalance: com.davidbugayov.financeanalyzer.shared.model.Money,
    ) {
        // Обновляем баланс кошелька через updateWallet
        val existingWallet = domainRepo.getWalletById(walletId)
        if (existingWallet != null) {
            val updatedWallet = existingWallet.copy(balance = newBalance)
            domainRepo.updateWallet(updatedWallet)
        }
    }

    override suspend fun clearAllWallets() {
        domainRepo.deleteAllWallets()
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
