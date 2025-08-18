package com.davidbugayov.financeanalyzer.shared.util

/**
 * KMP-логика нормализации источников.
 * Возвращает "ключ" источника по произвольной строке.
 */
enum class SourceKey {
    CASH,
    SBER,
    TINKOFF,
    ALFA,
    OZON,
    VTB,
    GAZPROM,
    RAIFFEISEN,
    POCHTA,
    YOOMONEY,
    OTHER,
}

object SourceResolver {
    /**
     * Преобразует произвольное имя источника в стандартный ключ [SourceKey].
     */
    fun resolve(raw: String): SourceKey {
        val name = raw.trim().lowercase()
        return when {
            name.contains("налич") || name.contains("cash") -> SourceKey.CASH
            name.contains("сбер") || name.contains("sber") -> SourceKey.SBER
            name.contains("тинькофф") || name.contains("tinkoff") || name.contains("t-банк") || name.contains("t-bank") -> SourceKey.TINKOFF
            name.contains("альфа") || name.contains("alfa") -> SourceKey.ALFA
            name.contains("озон") || name.contains("ozon") -> SourceKey.OZON
            name.contains("втб") || name.contains("vtb") -> SourceKey.VTB
            name.contains("газпромбанк") || name.contains("gazprombank") -> SourceKey.GAZPROM
            name.contains("райффайзен") || name.contains("raiffeisen") -> SourceKey.RAIFFEISEN
            name.contains("почта банк") || name.contains("post bank") || name.contains("postbank") -> SourceKey.POCHTA
            name.contains("юмани") || name.contains("yoomoney") || name.contains("yumoney") -> SourceKey.YOOMONEY
            else -> SourceKey.OTHER
        }
    }
}



