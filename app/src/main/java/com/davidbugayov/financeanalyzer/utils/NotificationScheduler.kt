package com.davidbugayov.financeanalyzer.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.davidbugayov.financeanalyzer.R
import timber.log.Timber
import java.util.Calendar

/**
 * Класс для планирования и управления уведомлениями.
 */
object NotificationScheduler {

    private const val TRANSACTION_REMINDER_CHANNEL_ID = "transaction_reminder_channel"
    private const val TRANSACTION_REMINDER_REQUEST_CODE = 1001

    /**
     * Создает канал уведомлений для Android 8.0 (API 26) и выше.
     * @param context Контекст приложения.
     */
    private fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.transaction_reminder_channel_name)
        val description = context.getString(R.string.transaction_reminder_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel =
            NotificationChannel(TRANSACTION_REMINDER_CHANNEL_ID, name, importance).apply {
                this.description = description
            }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Планирует ежедневное уведомление о необходимости внести транзакции.
     * @param context Контекст приложения.
     * @param hour Час для отправки уведомления (0-23).
     * @param minute Минута для отправки уведомления (0-59).
     */
    fun scheduleTransactionReminder(context: Context, hour: Int, minute: Int) {
        // Создаем канал уведомлений
        createNotificationChannel(context)
        
        // Создаем Intent для BroadcastReceiver, который будет показывать уведомление
        val alarmIntent = Intent(context, TransactionReminderReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            TRANSACTION_REMINDER_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Устанавливаем время для уведомления
        val calendar = Calendar.getInstance().apply {
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
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        try {
            // Для Android 6.0 (API 23) и выше используем setExactAndAllowWhileIdle
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent
            )
            Timber.d("Scheduled exact alarm with setExactAndAllowWhileIdle")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to schedule alarm due to missing permission")
            // Используем неточный будильник в случае ошибки
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmPendingIntent
            )
        }
        
        // Для повторяющихся уведомлений нужно будет перепланировать их в BroadcastReceiver
    }

    /**
     * Отменяет запланированные уведомления о транзакциях.
     * @param context Контекст приложения.
     */
    fun cancelTransactionReminder(context: Context) {
        val intent = Intent(context, TransactionReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            TRANSACTION_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Универсальная функция для включения/отключения напоминаний о транзакциях и их перепланирования.
     * @param context Контекст приложения.
     * @param isEnabled Включить или выключить напоминания.
     * @param reminderTime Время напоминания (час, минута). Если null — используется сохранённое в PreferencesManager.
     */
    fun updateTransactionReminder(context: Context, isEnabled: Boolean, reminderTime: Pair<Int, Int>? = null) {
        val preferencesManager = PreferencesManager(context)
        if (isEnabled) {
            val (hour, minute) = reminderTime ?: preferencesManager.getReminderTime()
            preferencesManager.setTransactionReminderEnabled(true)
            preferencesManager.setReminderTime(hour, minute)
            scheduleTransactionReminder(context, hour, minute)
        } else {
            preferencesManager.setTransactionReminderEnabled(false)
            cancelTransactionReminder(context)
        }
    }
} 