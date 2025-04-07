package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory

/**
 * Entity для хранения бюджетных категорий в базе данных Room.
 * Представляет таблицу budget_categories в базе данных.
 */
@Entity(tableName = "budget_categories")
data class BudgetCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val limit: Double,
    val spent: Double,
    @ColumnInfo(name = "wallet_balance") val walletBalance: Double = 0.0,
    @ColumnInfo(name = "period_duration") val periodDuration: Int = 14,
    @ColumnInfo(name = "period_start_date") val periodStartDate: Long = System.currentTimeMillis()
) {
    /**
     * Преобразует Entity в доменную модель
     */
    fun toDomain(): BudgetCategory = BudgetCategory(
        id = id,
        name = name,
        limit = limit,
        spent = spent,
        walletBalance = walletBalance,
        periodDuration = periodDuration,
        periodStartDate = periodStartDate
    )

    companion object {
        /**
         * Преобразует доменную модель в Entity
         */
        fun fromDomain(domain: BudgetCategory): BudgetCategoryEntity = BudgetCategoryEntity(
            id = domain.id,
            name = domain.name,
            limit = domain.limit,
            spent = domain.spent,
            walletBalance = domain.walletBalance,
            periodDuration = domain.periodDuration,
            periodStartDate = domain.periodStartDate
        )
    }
} 