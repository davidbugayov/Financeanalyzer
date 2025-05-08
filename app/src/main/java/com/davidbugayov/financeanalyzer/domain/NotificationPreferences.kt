package com.davidbugayov.financeanalyzer.domain

import kotlinx.coroutines.flow.flowOf

class NotificationPreferences(
    val enabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0
) {
    // TODO: Реализовать методы и свойства по необходимости

    fun getNotificationPreferences() = flowOf(this)
    fun updateNotificationEnabled() {}
    fun updateReminderTime() {}
} 