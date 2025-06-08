package com.davidbugayov.financeanalyzer.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Конвертер для преобразования типа Date в Long и обратно.
 * Используется Room для хранения дат в базе данных.
 */
class DateConverter {

    /**
     * Преобразует Date в Long для хранения в базе данных
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    /**
     * Преобразует Long из базы данных в Date
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
} 
