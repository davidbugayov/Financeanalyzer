package com.davidbugayov.financeanalyzer.shared.mapper

import com.davidbugayov.financeanalyzer.shared.model.Wallet
import com.davidbugayov.financeanalyzer.shared.model.WalletType

/**
 * iOS-specific реализация WalletMapper.
 * Использует iOS-specific модели данных.
 */
actual object WalletMapper {

    actual fun toShared(platformWallet: Any): Wallet {
        // iOS implementation will be added when iOS models are defined
        // For now, return a default wallet
        return Wallet(
            id = "ios_default",
            name = "Default Wallet",
            type = WalletType.CARD,
            balance = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
            limit = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
            spent = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
        )
    }

    actual fun toPlatform(sharedWallet: Wallet): Any {
        // iOS implementation will be added when iOS models are defined
        return sharedWallet
    }

    actual fun toSharedList(platformWallets: List<Any>): List<Wallet> {
        return platformWallets.map { toShared(it) }
    }

    actual fun toPlatformList(sharedWallets: List<Wallet>): List<Any> {
        return sharedWallets.map { toPlatform(it) }
    }
}
