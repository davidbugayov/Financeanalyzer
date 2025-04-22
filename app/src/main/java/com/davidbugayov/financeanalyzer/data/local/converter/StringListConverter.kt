package com.davidbugayov.financeanalyzer.data.local.converter

import androidx.room.TypeConverter
import timber.log.Timber

/**
 * Конвертер для преобразования списка строк в строку и обратно.
 * Используется для хранения списка walletIds в базе данных.
 */
class StringListConverter {
    private val SEPARATOR = ","
    
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(SEPARATOR)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            try {
                value.split(SEPARATOR).filter { it.isNotBlank() }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при преобразовании строки в список: $value")
                emptyList()
            }
        }
    }
} 