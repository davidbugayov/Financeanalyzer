package com.davidbugayov.financeanalyzer.widget

import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlin.math.abs

/**
 * Утилита для форматирования сумм в виджетах с сокращениями
 */
object WidgetAmountFormatter {
    /**
     * Форматирует сумму для отображения в виджете с сокращениями
     * @param money Сумма для форматирования
     * @param maxLength Максимальная длина строки
     * @return Отформатированная строка с сокращениями
     */
    fun formatForWidget(
        money: Money,
        maxLength: Int = 8,
    ): String {
        val amount = money.toMajorDouble()
        val absAmount = abs(amount)

        return when {
            absAmount < 1000 -> {
                // Меньше 1000 - показываем как есть
                formatSmallAmount(amount, money.currency.symbol, maxLength)
            }
            absAmount < 1_000_000 -> {
                // От 1000 до 999,999 - показываем как K
                val kAmount = amount / 1000
                formatWithSuffix(kAmount, "K", money.currency.symbol, maxLength)
            }
            absAmount < 1_000_000_000 -> {
                // От 1M до 999M - показываем как M
                val mAmount = amount / 1_000_000
                formatWithSuffix(mAmount, "M", money.currency.symbol, maxLength)
            }
            else -> {
                // Больше 1B - показываем как B
                val bAmount = amount / 1_000_000_000
                formatWithSuffix(bAmount, "B", money.currency.symbol, maxLength)
            }
        }
    }

    /**
     * Форматирует небольшие суммы (меньше 1000)
     */
    private fun formatSmallAmount(
        amount: Double,
        currencySymbol: String,
        maxLength: Int,
    ): String {
        val formatted =
            if (amount % 1 == 0.0) {
                amount.toInt().toString()
            } else {
                String.format(java.util.Locale.ROOT, "%.1f", amount).removeSuffix(".0")
            }

        val result = "$formatted $currencySymbol"
        return if (result.length <= maxLength) result else truncateAmount(amount, currencySymbol, maxLength)
    }

    /**
     * Форматирует суммы с суффиксами (K, M, B)
     */
    private fun formatWithSuffix(
        amount: Double,
        suffix: String,
        currencySymbol: String,
        maxLength: Int,
    ): String {
        val formatted =
            if (amount % 1 == 0.0) {
                amount.toInt().toString()
            } else {
                String.format(java.util.Locale.ROOT, "%.1f", amount).removeSuffix(".0")
            }

        val result = "$formatted$suffix $currencySymbol"
        return if (result.length <= maxLength) result else truncateAmount(amount, currencySymbol, maxLength)
    }

    /**
     * Обрезает сумму, если она не помещается
     */
    private fun truncateAmount(
        amount: Double,
        currencySymbol: String,
        maxLength: Int,
    ): String {
        val availableLength = maxLength - currencySymbol.length - 1 // -1 для пробела

        return when {
            availableLength >= 4 -> {
                // Можем показать число с суффиксом
                val absAmount = abs(amount)
                when {
                    absAmount >= 1_000_000_000 -> "${(amount / 1_000_000_000).toInt()}B $currencySymbol"
                    absAmount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}M $currencySymbol"
                    absAmount >= 1_000 -> "${(amount / 1_000).toInt()}K $currencySymbol"
                    else -> "${amount.toInt()} $currencySymbol"
                }
            }
            availableLength >= 2 -> {
                // Можем показать только число
                "${amount.toInt()}"
            }
            else -> {
                // Показываем только символ валюты
                currencySymbol
            }
        }
    }

    /**
     * Проверяет, нужно ли использовать сокращения для суммы
     */
    fun shouldUseAbbreviation(money: Money): Boolean {
        val amount = abs(money.toMajorDouble())
        return amount >= 1000
    }
}
