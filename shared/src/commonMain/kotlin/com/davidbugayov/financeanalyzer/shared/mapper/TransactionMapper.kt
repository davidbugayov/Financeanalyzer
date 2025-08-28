package com.davidbugayov.financeanalyzer.shared.mapper

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Маппер для конвертации между shared и platform-specific моделями Transaction.
 * Реальные реализации находятся в platform-specific модулях.
 *
 * Этот класс предоставляет интерфейс для маппинга, а конкретные реализации
 * должны быть предоставлены в AndroidMain и iOSMain.
 */
expect object TransactionMapper {

    /**
     * Конвертирует platform-specific Transaction в shared Transaction
     */
    fun toShared(platformTransaction: Any): Transaction

    /**
     * Конвертирует shared Transaction в platform-specific Transaction
     */
    fun toPlatform(sharedTransaction: Transaction): Any

    /**
     * Конвертирует список platform-specific Transaction в список shared Transaction
     */
    fun toSharedList(platformTransactions: List<Any>): List<Transaction>

    /**
     * Конвертирует список shared Transaction в список platform-specific Transaction
     */
    fun toPlatformList(sharedTransactions: List<Transaction>): List<Any>
}
