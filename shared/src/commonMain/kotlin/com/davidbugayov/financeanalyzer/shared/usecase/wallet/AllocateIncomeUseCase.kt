package com.davidbugayov.financeanalyzer.shared.usecase.wallet

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.repository.WalletRepository

/**
 * Use-case: распределить поступление дохода между всеми доступными кошельками
 * пропорционально их установленным лимитам.
 */
class AllocateIncomeUseCase(
    private val walletRepository: WalletRepository,
) {

    /**
     * Распределяет доход между кошельками пропорционально их лимитам.
     * @param income Сумма дохода для распределения
     * @return true если распределение прошло успешно
     */
    suspend operator fun invoke(income: Money): Boolean {
        return try {
            val wallets = walletRepository.getAllWallets()
            if (wallets.isEmpty()) {
                return true // нет кошельков, распределение не требуется
            }

            val totalLimit = wallets.fold(0.0) { acc, wallet -> acc + wallet.limit.toMajorDouble() }
            if (totalLimit <= 0.0) {
                return true // общий лимит <= 0, распределение пропущено
            }

            wallets.forEach { wallet ->
                val proportion = wallet.limit.toMajorDouble() / totalLimit
                val amountToAdd = income.toMajorDouble() * proportion
                val updatedBalance = wallet.balance.toMajorDouble() + amountToAdd
                val updatedWallet = wallet.copy(
                    balance = Money.fromMajor(updatedBalance, wallet.balance.currency)
                )
                walletRepository.updateWallet(updatedWallet)
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
