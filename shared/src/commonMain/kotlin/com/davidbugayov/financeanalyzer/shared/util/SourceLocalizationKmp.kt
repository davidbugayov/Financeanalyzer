package com.davidbugayov.financeanalyzer.shared.util

/**
 * KMP-локализация имени источника. Возвращает человекочитаемое имя для текущей локали.
 * Здесь мы не имеем доступ к Android ресурсам, поэтому держим минимальный набор на EN/RU/ZH.
 */
object SourceLocalizationKmp {
    fun localize(key: SourceKey, locale: String): String = when (key) {
        SourceKey.CASH -> when {
            locale.startsWith("ru") -> "Наличные"
            locale.startsWith("zh") -> "现金"
            else -> "Cash"
        }
        SourceKey.SBER -> when {
            locale.startsWith("ru") -> "Сбербанк"
            locale.startsWith("zh") -> "俄储银行"
            else -> "Sberbank"
        }
        SourceKey.TINKOFF -> when {
            locale.startsWith("ru") -> "Тинькофф"
            else -> "Tinkoff"
        }
        SourceKey.ALFA -> when {
            locale.startsWith("ru") -> "Альфа-Банк"
            else -> "Alfa-Bank"
        }
        SourceKey.OZON -> "Ozon"
        SourceKey.VTB -> "VTB"
        SourceKey.GAZPROM -> "Gazprombank"
        SourceKey.RAIFFEISEN -> "Raiffeisenbank"
        SourceKey.POCHTA -> when {
            locale.startsWith("ru") -> "Почта Банк"
            locale.startsWith("zh") -> "邮政银行"
            else -> "Post Bank"
        }
        SourceKey.YOOMONEY -> "YuMoney"
        SourceKey.OTHER -> ""
    }
}



