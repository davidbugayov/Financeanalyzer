package com.davidbugayov.financeanalyzer.shared.mapper

import com.davidbugayov.financeanalyzer.shared.model.Wallet
import com.davidbugayov.financeanalyzer.shared.model.WalletType

/**
 * Android-specific реализация WalletMapper.
 * Пока использует заглушки для избежания циклических зависимостей.
 */
actual object WalletMapper {

    actual fun toShared(platformWallet: Any): Wallet {
        // Заглушка - будет реализована после решения циклических зависимостей
        return Wallet(
            id = "stub_${System.currentTimeMillis()}",
            name = "Stub Wallet",
            type = WalletType.CARD,
            balance = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
            limit = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
            spent = com.davidbugayov.financeanalyzer.shared.model.Money.zero(),
        )
    }

    actual fun toPlatform(sharedWallet: Wallet): Any {
        // Заглушка - будет реализована после решения циклических зависимостей
        return sharedWallet
    }

    actual fun toSharedList(platformWallets: List<Any>): List<Wallet> {
        return platformWallets.map { toShared(it) }
    }

    actual fun toPlatformList(sharedWallets: List<Wallet>): List<Any> {
        return sharedWallets.map { toPlatform(it) }
    }
}