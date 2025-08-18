package com.davidbugayov.financeanalyzer.core.util

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.SymbolPosition
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Extension functions for shared Money class
 */

/**
 * Formats money for display with options for showing currency and using minimal decimals
 *
 * @param showCurrency Whether to show the currency symbol
 * @param useMinimalDecimals Whether to show minimal decimals (omit trailing zeros)
 * @return Formatted string representation of the money amount
 */
fun Money.formatForDisplay(
    showCurrency: Boolean = true,
    useMinimalDecimals: Boolean = false,
): String = format(showCurrency, useMinimalDecimals)

/**
 * Formats money with options for showing currency and using minimal decimals
 *
 * @param showCurrency Whether to show the currency symbol
 * @param useMinimalDecimals Whether to show minimal decimals (omit trailing zeros)
 * @return Formatted string representation of the money amount
 */
fun Money.format(
    showCurrency: Boolean = true,
    useMinimalDecimals: Boolean = false,
): String {
    val symbols =
        DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }

    val majorAmount = toMajorDouble()
    val isWholeNumber = majorAmount % 1 == 0.0
    val isZero = majorAmount == 0.0

    val pattern =
        if (useMinimalDecimals && (isWholeNumber || isZero)) {
            "#,##0"
        } else {
            if (currency.fractionDigits == 0) "#,##0" else "#,##0.00"
        }

    val formatter = DecimalFormat(pattern, symbols)
    val formattedAmount = formatter.format(majorAmount)

    return if (showCurrency) {
        if (currency.symbolPosition == SymbolPosition.BEFORE) {
            "${currency.symbol}$formattedAmount"
        } else {
            "$formattedAmount ${currency.symbol}"
        }
    } else {
        formattedAmount
    }
}
