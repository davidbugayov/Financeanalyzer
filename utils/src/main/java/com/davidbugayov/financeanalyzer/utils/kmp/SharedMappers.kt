package com.davidbugayov.financeanalyzer.utils.kmp

import com.davidbugayov.financeanalyzer.domain.model.CategoryStats as DomainCategoryStats
import com.davidbugayov.financeanalyzer.domain.model.FinancialRecommendation as DomainFinancialRecommendation
import com.davidbugayov.financeanalyzer.domain.model.RecommendationCategory as DomainRecommendationCategory
import com.davidbugayov.financeanalyzer.domain.model.RecommendationPriority as DomainRecommendationPriority
import com.davidbugayov.financeanalyzer.domain.model.Transaction as DomainTransaction
import com.davidbugayov.financeanalyzer.shared.model.CategoryStats as SharedCategoryStats
import com.davidbugayov.financeanalyzer.shared.model.Currency as SharedCurrency
import com.davidbugayov.financeanalyzer.shared.model.FinancialRecommendation as SharedFinancialRecommendation
import com.davidbugayov.financeanalyzer.shared.model.Money as SharedMoney
import com.davidbugayov.financeanalyzer.shared.model.RecommendationCategory as SharedRecommendationCategory
import com.davidbugayov.financeanalyzer.shared.model.RecommendationPriority as SharedRecommendationPriority
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

@Suppress("DEPRECATION")
fun SharedTransaction.toDomain(): DomainTransaction {
    val instant =
        java.time.LocalDateTime
            .of(date.year, date.monthNumber, date.dayOfMonth, 0, 0)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
    val dateJava = java.util.Date.from(instant)
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

fun SharedFinancialRecommendation.toDomain(): DomainFinancialRecommendation {
    return DomainFinancialRecommendation(
        title = this.code,
        description = this.code,
        priority =
            when (this.priority) {
                SharedRecommendationPriority.HIGH -> DomainRecommendationPriority.HIGH
                SharedRecommendationPriority.MEDIUM -> DomainRecommendationPriority.MEDIUM
                SharedRecommendationPriority.LOW -> DomainRecommendationPriority.LOW
            },
        category =
            when (this.category) {
                SharedRecommendationCategory.SAVINGS -> DomainRecommendationCategory.SAVINGS
                SharedRecommendationCategory.EXPENSES -> DomainRecommendationCategory.EXPENSES
                SharedRecommendationCategory.INCOME -> DomainRecommendationCategory.INCOME
                SharedRecommendationCategory.EMERGENCY_FUND -> DomainRecommendationCategory.EMERGENCY_FUND
                SharedRecommendationCategory.RETIREMENT -> DomainRecommendationCategory.RETIREMENT
            },
        potentialImpact = 0.0,
    )
}

fun SharedCategoryStats.toDomain(): DomainCategoryStats {
    return DomainCategoryStats(
        category = this.category,
        amount = this.amount,
        percentage = java.math.BigDecimal.valueOf(this.percentage),
        count = this.count,
        isExpense = this.isExpense,
    )
}
