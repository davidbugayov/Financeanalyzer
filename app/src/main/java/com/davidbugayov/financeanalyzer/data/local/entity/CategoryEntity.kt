package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность категории для базы данных Room
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val name: String,
    val isExpense: Boolean,
    val color: Int,
    val icon: String
) 