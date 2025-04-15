package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.preferences.WalletPreferences
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository

/**
 * Реализация репозитория для работы с кошельками
 */
class WalletRepositoryImpl(
    private val walletPreferences: WalletPreferences
) : WalletRepository {

    override suspend fun getAllWallets(): List<Wallet> {
        return walletPreferences.getWallets()
    }

    override suspend fun getWalletById(id: String): Wallet? {
        return walletPreferences.getWallets()
            .find { it.id == id }
    }

    override suspend fun addWallet(wallet: Wallet) {
        walletPreferences.addWallet(wallet)
    }

    override suspend fun updateWallet(wallet: Wallet) {
        walletPreferences.updateWallet(wallet)
    }

    override suspend fun deleteWallet(wallet: Wallet) {
        walletPreferences.removeWallet(wallet.id)
    }

    override suspend fun deleteWalletById(id: String) {
        walletPreferences.removeWallet(id)
    }

    override suspend fun deleteAllWallets() {
        walletPreferences.saveWallets(emptyList())
    }

    override suspend fun updateSpentAmount(id: String, spent: Money) {
        val wallet = getWalletById(id) ?: return
        val updatedWallet = wallet.copy(spent = spent)
        walletPreferences.updateWallet(updatedWallet)
    }

    override suspend fun hasWallets(): Boolean {
        return walletPreferences.getWallets().isNotEmpty()
    }

    override suspend fun getWalletsByIds(ids: List<String>): List<Wallet> {
        if (ids.isEmpty()) return emptyList()
        
        return walletPreferences.getWallets()
            .filter { ids.contains(it.id) }
    }

} 