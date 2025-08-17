package com.davidbugayov.financeanalyzer.shared.model

/**
 * KMP-совместимая валюта.
 */
enum class Currency(val code: String, val symbol: String, val fractionDigits: Int) {
    RUB("RUB", "₽", 2),
    USD("USD", "$", 2),
    EUR("EUR", "€", 2),
    GBP("GBP", "£", 2),
    JPY("JPY", "¥", 0),
    CNY("CNY", "¥", 2),
    KZT("KZT", "₸", 2),
    BYN("BYN", "Br", 2),
    ;

    companion object {
        fun fromCode(code: String): Currency = entries.find { it.code == code } ?: RUB
    }
    
    // Для обратной совместимости
    val decimalPlaces: Int get() = fractionDigits
    val decimalSeparator: Char get() = '.'
    val symbolPosition: SymbolPosition get() = SymbolPosition.AFTER
    
    // Методы для обратной совместимости
    fun format(amount: Double): String = "$symbol${amount}"
}

enum class SymbolPosition {
    BEFORE,
    AFTER,
}

