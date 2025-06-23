package com.davidbugayov.financeanalyzer.feature.profile.model

/**
 * Модель времени для уведомлений.
 * @param hour Час (0-23)
 * @param minute Минута (0-59)
 */
data class Time(val hour: Int, val minute: Int) {

    override fun toString(): String {
        val hourStr = hour.toString().padStart(2, '0')
        val minuteStr = minute.toString().padStart(2, '0')
        return "$hourStr:$minuteStr"
    }
}
