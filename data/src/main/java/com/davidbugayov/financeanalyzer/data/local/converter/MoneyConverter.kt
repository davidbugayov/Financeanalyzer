package com.davidbugayov.financeanalyzer.data.local.converter

import androidx.room.TypeConverter
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider
import java.math.BigDecimal

class MoneyConverter {
    @TypeConverter
    fun fromMoney(money: Money?): String? {
        return money?.let {
            "${it.amount.toPlainString()},${it.currency.code}"
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
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse amount: $amountStr")
                CrashLoggerProvider.crashLogger.logException(e)
                return null
            }
        }
    }
} 