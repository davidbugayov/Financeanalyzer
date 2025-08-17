package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.preferences.WalletPreferences
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import timber.log.Timber
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Реализация репозитория для работы с кошельками
 */
class WalletRepositoryImpl(
    private val walletPreferences: WalletPreferences,
    private val transactionRepository: TransactionRepository? = null,
) : WalletRepository {
    private val resourceProvider: ResourceProvider
        get() = GlobalContext.get().get()

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

    override suspend fun getWalletsForTransaction(transactionId: String): List<Wallet> {
        try {
            // Сначала получаем все кошельки
            val allWallets = walletPreferences.getWallets()

            // Проверяем, существует ли транзакция
            val transaction = getTransactionForWallets(transactionId)

            if (transaction != null) {
                // Если у транзакции есть walletIds, возвращаем кошельки по этим ID
                val walletIds = transaction.walletIds
                if (walletIds != null && walletIds.isNotEmpty()) {
                    return allWallets.filter { wallet -> walletIds.contains(wallet.id) }
                }

                // Для доходов без явного указания кошельков возвращаем все
                if (!transaction.isExpense) {
                    return allWallets
                }
            }

            // Для расходных транзакций или если транзакция не найдена, возвращаем пустой список
            return emptyList()
        } catch (e: Exception) {
            Timber.e(e, resourceProvider.getString(UiR.string.log_error_getting_wallets, transactionId))
            return emptyList()
        }
    }

    // Вспомогательный метод для получения транзакции из репозитория транзакций
    private suspend fun getTransactionForWallets(transactionId: String): Transaction? {
        if (transactionRepository == null) {
            Timber.d(resourceProvider.getString(UiR.string.log_transaction_repository_not_set))
            return null
        }

        try {
            // В реальной реализации здесь используем репозиторий для получения транзакции
            return transactionRepository.getTransactionById(transactionId)
        } catch (e: Exception) {
            Timber.e(e, resourceProvider.getString(UiR.string.log_error_getting_transaction, transactionId))
            return null
        }
    }

    override suspend fun getSubWallets(parentId: String): List<Wallet> {
        return walletPreferences.getWallets().filter { it.parentWalletId == parentId }
    }
}
