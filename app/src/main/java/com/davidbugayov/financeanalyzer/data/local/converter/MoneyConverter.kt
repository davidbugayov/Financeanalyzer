package com.davidbugayov.financeanalyzer.data.local.converter

import androidx.room.TypeConverter
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Money
import java.math.BigDecimal

class MoneyConverter {
    @TypeConverter
    fun fromMoney(money: Money?): String? {
        return money?.let { "${it.amount},${it.currency.code}" }
    }

    @TypeConverter
    fun toMoney(value: String?): Money? {
        return value?.let {
            val parts = it.split(",")
            val amountStr = parts[0]
            val currency = if (parts.size > 1) {
                Currency.fromCode(parts[1]) ?: Currency.RUB
            } else {
                Currency.RUB
            }
            Money(BigDecimal(amountStr), currency)
        }
    }
} 