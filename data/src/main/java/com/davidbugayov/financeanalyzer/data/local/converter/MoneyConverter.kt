package com.davidbugayov.financeanalyzer.data.local.converter

import androidx.room.TypeConverter
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import java.math.BigDecimal
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

class MoneyConverter {
    @TypeConverter
    fun fromMoney(money: Money?): String? {
        return money?.let {
            require(it.amount.scale() <= it.currency.decimalPlaces) {
                "Amount scale (${it.amount.scale()}) exceeds currency decimal places (${it.currency.decimalPlaces})"
            }
            "${it.amount},${it.currency.code}"
        }
    }

    @TypeConverter
    fun toMoney(value: String?): Money? {
        return value?.let {
            require(it.isNotBlank()) { "Money string cannot be blank" }
            val parts = it.split(",")
            require(parts.isNotEmpty()) { "Invalid money format: empty string" }

            val amountStr = parts[0]
            require(amountStr.matches(Regex("""^-?\d+(\.\d+)?$"""))) {
                "Invalid amount format: $amountStr"
            }

            val currency = if (parts.size > 1) {
                val currencyCode = parts[1]
                require(currencyCode.isNotBlank()) { "Currency code cannot be blank" }
                Currency.fromCode(currencyCode)
            } else {
                Currency.RUB
            }

            try {
                Money(BigDecimal(amountStr), currency)
            } catch (e: NumberFormatException) {
                Timber.e(e, "Failed to parse amount: $amountStr")
                CrashLoggerProvider.crashLogger.logException(e)
                throw IllegalArgumentException("Invalid amount format: $amountStr", e)
            }
        }
    }
} 