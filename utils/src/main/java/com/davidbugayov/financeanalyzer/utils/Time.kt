package com.davidbugayov.financeanalyzer.utils

import java.util.Locale

/**
 * Простая модель локального времени (часы-минуты) для напоминаний.
 */
data class Time(val hour: Int, val minute: Int) {
    override fun toString(): String = java.lang.String.format(Locale.ROOT, "%02d:%02d", hour, minute)
}
