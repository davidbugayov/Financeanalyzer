package com.davidbugayov.financeanalyzer.domain.model

/**
 * Класс, представляющий валюту в приложении.
 * Содержит код валюты, символ и информацию о форматировании.
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val symbolPosition: SymbolPosition = SymbolPosition.AFTER,
    val decimalPlaces: Int = 2,
    val groupingSeparator: Char = ' ',
    val decimalSeparator: Char = '.'
) {

    RUB("RUB", "₽", "Российский рубль", SymbolPosition.AFTER, 2, ' ', ','),
    USD("USD", "$", "Доллар США", SymbolPosition.BEFORE, 2, ',', '.'),
    EUR("EUR", "€", "Евро", SymbolPosition.AFTER, 2, ' ', ','),
    GBP("GBP", "£", "Фунт стерлингов", SymbolPosition.BEFORE, 2, ',', '.'),
    JPY("JPY", "¥", "Японская иена", SymbolPosition.BEFORE, 0, ',', '.'),
    CNY("CNY", "¥", "Китайский юань", SymbolPosition.BEFORE, 2, ',', '.'),
    KZT("KZT", "₸", "Казахстанский тенге", SymbolPosition.AFTER, 2, ' ', ','),
    BYN("BYN", "Br", "Белорусский рубль", SymbolPosition.AFTER, 2, ' ', ','),
    UAH("UAH", "₴", "Украинская гривна", SymbolPosition.AFTER, 2, ' ', ',');

    companion object {

        /**
         * Получает валюту по коду
         * @param code Код валюты
         * @return Валюта или RUB, если валюта не найдена
         */
        fun fromCode(code: String): Currency {
            return values().find { it.code == code } ?: RUB
        }
    }
}

/**
 * Позиция символа валюты относительно суммы
 */
enum class SymbolPosition {

    BEFORE, // Символ перед суммой (например, $100)
    AFTER // Символ после суммы (например, 100₽)
} 
