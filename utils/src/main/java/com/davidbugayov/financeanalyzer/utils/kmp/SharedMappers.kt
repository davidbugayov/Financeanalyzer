package com.davidbugayov.financeanalyzer.utils.kmp

import com.davidbugayov.financeanalyzer.core.model.Money as CoreMoney
import com.davidbugayov.financeanalyzer.domain.model.Transaction as DomainTransaction
import com.davidbugayov.financeanalyzer.shared.model.Currency as SharedCurrency
import com.davidbugayov.financeanalyzer.shared.model.Money as SharedMoney
import com.davidbugayov.financeanalyzer.shared.model.Transaction as SharedTransaction
import java.time.Instant
import java.time.ZoneId
import kotlinx.datetime.LocalDate

fun DomainTransaction.toShared(): SharedTransaction {
    val localDate = Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate()
    val currency = SharedCurrency.fromCode(this.amount.currency.code)
    val sharedMoney = SharedMoney.fromMajor(this.amount.amount.toDouble(), currency)
    return SharedTransaction(
        id = id,
        amount = sharedMoney,
        category = category,
        date = LocalDate(localDate.year, localDate.monthValue, localDate.dayOfMonth),
        isExpense = isExpense,
        note = note,
        source = source,
    )
}

fun List<DomainTransaction>.toShared(): List<SharedTransaction> = map { it.toShared() }

fun SharedMoney.toCore(): CoreMoney =
    CoreMoney(
        this.toMajorDouble(),
        com.davidbugayov.financeanalyzer.core.model.Currency
            .fromCode(this.currency.code),
    )

fun SharedTransaction.toDomain(): DomainTransaction {
    val dateJava =
        java.util.Date.from(
            java.time.LocalDate
                .of(date.year, date.monthNumber, date.day)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant(),
        )
    val coreCurrency =
        com.davidbugayov.financeanalyzer.core.model.Currency
            .fromCode(this.amount.currency.code)
    val coreMoney = CoreMoney(this.amount.toMajorDouble(), coreCurrency)
    return DomainTransaction(
        id = this.id,
        amount = coreMoney,
        category = this.category,
        date = dateJava,
        isExpense = this.isExpense,
        note = this.note,
        source = this.source,
        sourceColor = 0,
    )
}

fun List<SharedTransaction>.toDomain(): List<DomainTransaction> = map { it.toDomain() }

fun java.util.Date.toLocalDateKmp(): kotlinx.datetime.LocalDate {
    val ld = this.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    return LocalDate(ld.year, ld.monthValue, ld.dayOfMonth)
}

fun com.davidbugayov.financeanalyzer.core.model.Money.toShared(): com.davidbugayov.financeanalyzer.shared.model.Money {
    val currency =
        com.davidbugayov.financeanalyzer.shared.model.Currency
            .fromCode(this.currency.code)
    return com.davidbugayov.financeanalyzer.shared.model.Money
        .fromMajor(this.amount.toDouble(), currency)
}

fun com.davidbugayov.financeanalyzer.core.model.Currency.toShared(): com.davidbugayov.financeanalyzer.shared.model.Currency {
    return com.davidbugayov.financeanalyzer.shared.model.Currency.fromCode(this.code)
}

fun com.davidbugayov.financeanalyzer.shared.model.FinancialRecommendation.toDomain(): com.davidbugayov.financeanalyzer.domain.model.FinancialRecommendation {
    return com.davidbugayov.financeanalyzer.domain.model.FinancialRecommendation(
        title = this.code,
        description = this.code,
        priority =
            when (this.priority) {
                com.davidbugayov.financeanalyzer.shared.model.RecommendationPriority.HIGH -> com.davidbugayov.financeanalyzer.domain.model.RecommendationPriority.HIGH
                com.davidbugayov.financeanalyzer.shared.model.RecommendationPriority.MEDIUM -> com.davidbugayov.financeanalyzer.domain.model.RecommendationPriority.MEDIUM
                com.davidbugayov.financeanalyzer.shared.model.RecommendationPriority.LOW -> com.davidbugayov.financeanalyzer.domain.model.RecommendationPriority.LOW
            },
        category =
            when (this.category) {
                com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory.SAVINGS -> com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory.SAVINGS
                com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory.EXPENSES -> com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory.EXPENSES
                com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory.INCOME -> com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory.INCOME
                com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory.EMERGENCY_FUND -> com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory.EMERGENCY_FUND
                com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory.RETIREMENT -> com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory.RETIREMENT
            },
        potentialImpact = 0.0,
    )
}
