package com.davidbugayov.financeanalyzer.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import timber.log.Timber

/**
 * Класс для планирования и управления уведомлениями.
 */
class NotificationScheduler(
    private val applicationContext: Context,
    private val preferencesManager: PreferencesManager, // Добавляем PreferencesManager как зависимость
) : com.davidbugayov.financeanalyzer.utils.INotificationScheduler {
    private val TRANSACTION_REMINDER_CHANNEL_ID = "transaction_reminder_channel"
    private val TRANSACTION_REMINDER_REQUEST_CODE = 1001

    /**
     * Создает канал уведомлений для Android 8.0 (API 26) и выше.
     */
    private fun createNotificationChannel() { // Убираем context из параметра
        val name = "Напоминания о транзакциях"
        val description = "Канал для уведомлений о необходимости внести транзакции"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel =
            NotificationChannel(TRANSACTION_REMINDER_CHANNEL_ID, name, importance).apply {
                this.description = description
            }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Планирует ежедневное уведомление о необходимости внести транзакции.
     * @param hour Час для отправки уведомления (0-23).
     * @param minute Минута для отправки уведомления (0-59).
     */
    internal fun scheduleTransactionReminder(
        hour: Int,
        minute: Int,
    ) { // Убираем context из параметра
        // Создаем канал уведомлений
        createNotificationChannel()

        // Создаем Intent для BroadcastReceiver, который будет показывать уведомление
        val alarmIntent = Intent(applicationContext, TransactionReminderReceiver::class.java)
        val alarmPendingIntent =
            PendingIntent.getBroadcast(
                applicationContext,
                TRANSACTION_REMINDER_REQUEST_CODE,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        // Устанавливаем время для уведомления
        val calendar =
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)

                // Если указанное время уже прошло сегодня, планируем на завтра
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

        // Получаем AlarmManager и планируем повторяющееся уведомление
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent,
            )
            Timber.d("Scheduled exact alarm with setExactAndAllowWhileIdle")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to schedule alarm due to missing permission")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent,
            )
        }
    }

    /**
     * Отменяет запланированные уведомления о транзакциях.
     */
    private fun cancelTransactionReminder() { // Убираем context из параметра
        val intent = Intent(applicationContext, TransactionReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                applicationContext,
                TRANSACTION_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Универсальная функция для включения/отключения напоминаний о транзакциях и их перепланирования.
     * @param isEnabled Включить или выключить напоминания.
     * @param reminderTime Время напоминания (объект Time). Если null — используется сохранённое в PreferencesManager.
     */
    override fun updateTransactionReminder(
        isEnabled: Boolean,
        reminderTime: Time?,
    ) {
        if (isEnabled) {
            val (h, m) =
                if (reminderTime != null) {
                    Pair(reminderTime.hour, reminderTime.minute)
                } else {
                    preferencesManager.getReminderTime()
                }
            preferencesManager.setTransactionReminderEnabled(true)
            preferencesManager.setReminderTime(h, m)
            scheduleTransactionReminder(h, m)
        } else {
            preferencesManager.setTransactionReminderEnabled(false)
            cancelTransactionReminder()
        }
    }
}
