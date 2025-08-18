package com.davidbugayov.financeanalyzer.utils.kmp

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
    val sharedMoney = SharedMoney.fromMajor(this.amount.toMajorDouble(), currency)
    return SharedTransaction(
        id = id,
        amount = sharedMoney,
        category = category,
        date = LocalDate(localDate.year, localDate.monthValue, localDate.dayOfMonth),
        isExpense = isExpense,
        note = note,
        source = source,
        subcategoryId = subcategoryId,
    )
}

fun List<DomainTransaction>.toShared(): List<SharedTransaction> = map { it.toShared() }

/**
 * Конвертирует SharedTransaction в DomainTransaction.
 *
 * @return DomainTransaction с преобразованными данными.
 */
fun SharedTransaction.toCore(): DomainTransaction = toDomain()

/**
 * Конвертирует список SharedTransaction в список DomainTransaction.
 *
 * @return Список DomainTransaction с преобразованными данными.
 */
fun List<SharedTransaction>.toCore(): List<DomainTransaction> = map { it.toCore() }

fun SharedTransaction.toDomain(): DomainTransaction {
    val dateJava =
        java.util.Date.from(
            java.time.LocalDate
                .of(date.year, date.monthNumber, date.day)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant(),
        )
    return DomainTransaction(
        id = this.id,
        amount = this.amount,
        category = this.category,
        date = dateJava,
        isExpense = this.isExpense,
        note = this.note,
        source = this.source,
        sourceColor = 0,
        subcategoryId = this.subcategoryId,
    )
}

fun List<SharedTransaction>.toDomain(): List<DomainTransaction> = map { it.toDomain() }

fun java.util.Date.toLocalDateKmp(): kotlinx.datetime.LocalDate {
    val ld = this.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    return LocalDate(ld.year, ld.monthValue, ld.dayOfMonth)
}

/**
 * Расширение для BigDecimal для установки масштаба.
 *
 * @param scale Количество знаков после запятой.
 * @param roundingMode Режим округления.
 * @return BigDecimal с установленным масштабом.
 */
fun java.math.BigDecimal.setScale(
    scale: Int,
    roundingMode: java.math.RoundingMode,
): java.math.BigDecimal {
    return this.setScale(scale, roundingMode)
}

/**
 * Расширение для Double для установки масштаба.
 *
 * @param scale Количество знаков после запятой.
 * @param roundingMode Режим округления.
 * @return BigDecimal с установленным масштабом.
 */
fun Double.setScale(
    scale: Int,
    roundingMode: java.math.RoundingMode,
): java.math.BigDecimal {
    return java.math.BigDecimal.valueOf(this).setScale(scale, roundingMode)
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

fun com.davidbugayov.financeanalyzer.shared.model.CategoryStats.toDomain(): com.davidbugayov.financeanalyzer.domain.model.CategoryStats {
    return com.davidbugayov.financeanalyzer.domain.model.CategoryStats(
        category = this.category,
        amount = this.amount,
        percentage = java.math.BigDecimal.valueOf(this.percentage),
        count = this.count,
        isExpense = this.isExpense,
    )
}
