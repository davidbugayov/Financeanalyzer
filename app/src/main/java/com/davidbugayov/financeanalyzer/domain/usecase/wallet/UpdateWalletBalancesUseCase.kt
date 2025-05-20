package com.davidbugayov.financeanalyzer.domain.usecase.wallet

import com.davidbugayov.financeanalyzer.domain.model.AppException.Unknown
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import timber.log.Timber

class UpdateWalletBalancesUseCase(
    private val walletRepository: WalletRepository
) {

    suspend operator fun invoke(
        walletIdsToUpdate: List<String>,
        amountForWallets: Money,
        originalTransaction: Transaction?
    ): Result<Unit> {
        return try {
            // Логика отката изменений для оригинальной транзакции
            if (originalTransaction?.walletIds?.isNotEmpty() == true) {
                val originalAmount = originalTransaction.amount
                val originalWalletIds = originalTransaction.walletIds
                val originalAmountPerWallet = if (originalWalletIds.size > 1) {
                    originalAmount.div(originalWalletIds.size)
                } else {
                    originalAmount
                }
                Timber.d("UpdateWalletBalancesUseCase: Откат для ${'$'}{originalWalletIds.size} оригинальных кошельков, сумма: ${'$'}originalAmountPerWallet")
                val originalWallets = walletRepository.getWalletsByIds(originalWalletIds)
                originalWallets.forEach { wallet ->
                    val updatedWallet = wallet.copy(
                        balance = wallet.balance.minus(originalAmountPerWallet)
                    )
                    Timber.d("UpdateWalletBalancesUseCase: Откат для кошелька ${'$'}{wallet.name}: баланс ${'$'}{wallet.balance} -> ${'$'}{updatedWallet.balance}")
                    walletRepository.updateWallet(updatedWallet)
                }
            }

            // Логика обновления для новых/измененных кошельков
            if (walletIdsToUpdate.isNotEmpty()) {
                val amountPerWallet = if (walletIdsToUpdate.size > 1) {
                    amountForWallets.div(walletIdsToUpdate.size)
                } else {
                    amountForWallets
                }
                Timber.d("UpdateWalletBalancesUseCase: Обновление для ${'$'}{walletIdsToUpdate.size} кошельков, сумма на кошелек: ${'$'}amountPerWallet")
                val walletsToUpdateList = walletRepository.getWalletsByIds(walletIdsToUpdate)
                walletsToUpdateList.forEach { wallet ->
                    val updatedWallet = wallet.copy(
                        balance = wallet.balance.plus(amountPerWallet)
                    )
                    Timber.d("UpdateWalletBalancesUseCase: Обновляем кошелек ${'$'}{wallet.name}: старый баланс=${'$'}{wallet.balance}, новый баланс=${'$'}{updatedWallet.balance}")
                    walletRepository.updateWallet(updatedWallet)
                }
            }
            Timber.d("UpdateWalletBalancesUseCase: Балансы кошельков успешно обновлены")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "UpdateWalletBalancesUseCase: Ошибка при обновлении баланса кошельков: ${e.message}")
            Result.Error(Unknown(e.message, e))
        }
    }
} 