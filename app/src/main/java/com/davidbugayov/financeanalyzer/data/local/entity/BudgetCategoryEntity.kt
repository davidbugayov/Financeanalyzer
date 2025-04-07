package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory

/**
 * Entity класс для бюджетных категорий в Room
 */
@Entity(tableName = "budget_categories")
data class BudgetCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "[limit]")
    val limit: Double,
    val spent: Double,
    @ColumnInfo(name = "wallet_balance")
    val wallet_balance: Double = 0.0,
    @ColumnInfo(name = "period_duration")
    val period_duration: Int = 14,
    @ColumnInfo(name = "period_start_date")
    val period_start_date: Long = System.currentTimeMillis()
) {
    /**
     * Преобразует Entity в доменную модель
     */
    fun toDomain(): BudgetCategory {
        return BudgetCategory(
            id = id,
            name = name,
            limit = limit,
            spent = spent,
            walletBalance = wallet_balance,
            periodDuration = period_duration,
            periodStartDate = period_start_date
        )
    }

    companion object {
        /**
         * Преобразует доменную модель в Entity
         */
        fun fromDomain(budgetCategory: BudgetCategory): BudgetCategoryEntity {
            return BudgetCategoryEntity(
                id = budgetCategory.id,
                name = budgetCategory.name,
                limit = budgetCategory.limit,
                spent = budgetCategory.spent,
                wallet_balance = budgetCategory.walletBalance,
                period_duration = budgetCategory.periodDuration,
                period_start_date = budgetCategory.periodStartDate
            )
        }
    }
} 