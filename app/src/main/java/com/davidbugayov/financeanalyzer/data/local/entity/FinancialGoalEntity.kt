package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal
import java.util.Date

/**
 * Сущность для хранения финансовых целей в базе данных.
 */
@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey
    val id: String,
    
    val name: String,
    
    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,
    
    @ColumnInfo(name = "current_amount")
    val currentAmount: Double,
    
    val deadline: Date?,
    
    val category: String?,
    
    val description: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean
) {
    /**
     * Преобразует сущность в доменную модель.
     * @return Доменная модель финансовой цели.
     */
    fun toDomain(): FinancialGoal {
        return FinancialGoal(
            id = id,
            name = name,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            deadline = deadline,
            category = category,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isCompleted = isCompleted
        )
    }
    
    companion object {
        /**
         * Преобразует доменную модель в сущность.
         * @param domain Доменная модель финансовой цели.
         * @return Сущность финансовой цели.
         */
        fun fromDomain(domain: FinancialGoal): FinancialGoalEntity {
            return FinancialGoalEntity(
                id = domain.id,
                name = domain.name,
                targetAmount = domain.targetAmount,
                currentAmount = domain.currentAmount,
                deadline = domain.deadline,
                category = domain.category,
                description = domain.description,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                isCompleted = domain.isCompleted
            )
        }
    }
} 