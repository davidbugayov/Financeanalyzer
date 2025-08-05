package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity для хранения подкатегорий в базе данных Room.
 * Представляет таблицу subcategories в базе данных.
 */
@Entity(
    tableName = "subcategories",
    indices = [Index(value = ["categoryId"])],
)
data class SubcategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "categoryId") val categoryId: Long,
    val count: Int = 0,
    val isCustom: Boolean = false,
) 