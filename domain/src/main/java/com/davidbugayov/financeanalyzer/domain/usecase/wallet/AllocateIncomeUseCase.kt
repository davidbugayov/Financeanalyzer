package com.davidbugayov.financeanalyzer.domain.usecase.wallet

import com.davidbugayov.financeanalyzer.core.model.AppException
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import timber.log.Timber
import java.math.BigDecimal

/**
 * Use-case: распределить поступление дохода между всеми доступными кошельками
 * пропорционально их установленным лимитам (`Wallet.limit`).
 *
 * Логику ранее выполнял **BudgetViewModel.distributeIncome**. Переносим её
 * в слой домена, чтобы её можно было переиспользовать из разных слоёв (UI, WorkManager, тесты).
 *
 * – Если кошельков нет или общий лимит == 0, возвращает `Result.success(Unit)` без изменений.
 * – Балансы обновляются атомарно через `walletRepository.updateWallet`.
 */
class AllocateIncomeUseCase(
    private val walletRepository: WalletRepository,
) {

    suspend operator fun invoke(income: Money): Result<Unit> {
        return try {
            val wallets = walletRepository.getAllWallets()
            if (wallets.isEmpty()) {
                Timber.d("AllocateIncomeUseCase: нет кошельков, распределение не требуется")
                return Result.success(Unit)
            }

            val totalLimit = wallets.fold(Money.zero(income.currency)) { acc, wallet -> acc.plus(wallet.limit) }
            if (totalLimit.amount <= BigDecimal.ZERO) {
                Timber.d("AllocateIncomeUseCase: общий лимит <= 0, распределение пропущено")
                return Result.success(Unit)
            }

            wallets.forEach { wallet ->
                val proportion = wallet.limit.amount.divide(totalLimit.amount, 10, java.math.RoundingMode.HALF_EVEN)
                val amountToAdd = Money(income.amount.multiply(proportion), income.currency)
                val updatedWallet = wallet.copy(balance = wallet.balance.plus(amountToAdd))
                walletRepository.updateWallet(updatedWallet)
            }

            Timber.d("AllocateIncomeUseCase: доход ${income.amount} распределён по ${wallets.size} кошелькам")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "AllocateIncomeUseCase: ошибка распределения дохода: ${e.message}")
            Result.error(AppException.Unknown(e.message, e))
        }
    }
} 