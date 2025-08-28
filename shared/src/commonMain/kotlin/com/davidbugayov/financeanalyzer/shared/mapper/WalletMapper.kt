package com.davidbugayov.financeanalyzer.shared.mapper

import com.davidbugayov.financeanalyzer.shared.model.Wallet
import com.davidbugayov.financeanalyzer.shared.model.WalletType

/**
 * Маппер для конвертации между shared и platform-specific моделями Wallet.
 * Реальные реализации находятся в platform-specific модулях.
 *
 * Этот класс предоставляет интерфейс для маппинга, а конкретные реализации
 * должны быть предоставлены в AndroidMain и iOSMain.
 */
expect object WalletMapper {

    /**
     * Конвертирует platform-specific Wallet в shared Wallet
     */
    fun toShared(platformWallet: Any): Wallet

    /**
     * Конвертирует shared Wallet в platform-specific Wallet
     */
    fun toPlatform(sharedWallet: Wallet): Any

    /**
     * Конвертирует список platform-specific Wallet в список shared Wallet
     */
    fun toSharedList(platformWallets: List<Any>): List<Wallet>

    /**
     * Конвертирует список shared Wallet в список platform-specific Wallet
     */
    fun toPlatformList(sharedWallets: List<Wallet>): List<Any>
}
