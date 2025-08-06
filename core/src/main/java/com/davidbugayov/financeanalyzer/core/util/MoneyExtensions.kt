package com.davidbugayov.financeanalyzer.core.util

import com.davidbugayov.financeanalyzer.core.model.Money
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Extension functions for Money class
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
): String {
    return format(showCurrency, useMinimalDecimals)
}

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
    val symbols = DecimalFormatSymbols(Locale.getDefault())

    // Use space as grouping separator instead of comma
    symbols.groupingSeparator = ' '
    // Use comma as decimal separator instead of period
    symbols.decimalSeparator = ','

    // Check if the amount is a whole number or zero
    val isWholeNumber = amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0
    val isZero = amount.compareTo(BigDecimal.ZERO) == 0

    // Choose pattern based on whether we should show decimals
    val pattern =
        if (useMinimalDecimals && (isWholeNumber || isZero)) {
            "#,##0"
        } else {
            "#,##0.00"
        }

    val formatter = DecimalFormat(pattern, symbols)

    val formattedAmount = formatter.format(amount)
    val result = if (showCurrency) {
        "$formattedAmount ${currency.symbol}"
    } else {
        formattedAmount
    }
    
    return result
}
