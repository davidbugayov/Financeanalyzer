package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Локализует отображаемое имя источника (банк/кошелёк) по сырым названиям из данных.
 */
object SourceLocalization {
    private val keys: Map<String, List<String>> =
        mapOf(
            "sber" to listOf("sber", "sberbank", "сбер", "сбербанк"),
            "tinkoff" to listOf("tinkoff", "t-bank", "tbank", "тинькофф", "т-банк"),
            "alfa" to listOf("alfa", "alfabank", "alfa-bank", "альфа", "альфа-банк", "alpha bank"),
            "ozon" to listOf("ozon", "ozon bank", "озон"),
            "vtb" to listOf("vtb", "втб"),
            // Дополнительные популярные источники
            "raiffeisen" to listOf("raiffeisen", "райф", "райффайзен", "райфайзен", "райффайзенбанк", "raif"),
            "postbank" to listOf("post bank", "postbank", "pochta bank", "почта", "почта банк"),
            "yoomoney" to
                listOf(
                    "yoomoney",
                    "yumoney",
                    "yu money",
                    "yandex money",
                    "yandex.money",
                    "юмани",
                    "юmoney",
                    "ю деньги",
                    "яндекс деньги",
                    "яндекс.деньги",
                ),
            "cash" to listOf("cash", "наличные", "нал", "налик"),
            "gazprombank" to listOf("gazprombank", "gazprom", "газпромбанк", "гпб"),
        )

    fun displayName(
        context: Context,
        rawName: String,
    ): String {
        val lower = rawName.trim().lowercase()
        val key =
            keys.entries.firstOrNull { entry ->
                entry.value.any { syn -> syn.lowercase() == lower }
            }?.key

        return when (key) {
            "sber" -> context.getString(UiR.string.transaction_source_sber)
            "tinkoff" -> context.getString(UiR.string.bank_tinkoff)
            "alfa" -> context.getString(UiR.string.bank_alfa_short)
            "ozon" -> context.getString(UiR.string.bank_ozon)
            "vtb" -> context.getString(UiR.string.bank_vtb)
            "raiffeisen" -> context.getString(UiR.string.bank_raiffeisen)
            "postbank" -> context.getString(UiR.string.bank_post_bank)
            "yoomoney" -> context.getString(UiR.string.bank_yoomoney)
            "cash" -> context.getString(UiR.string.bank_cash)
            "gazprombank" -> context.getString(UiR.string.bank_gazprombank)
            else -> rawName
        }
    }
}
