package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность источника средств для базы данных Room
 */
@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey
    val name: String,
    val color: Int,
    val icon: String,
    val balance: String
) 