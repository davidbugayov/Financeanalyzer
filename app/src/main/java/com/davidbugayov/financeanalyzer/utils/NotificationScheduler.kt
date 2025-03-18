package com.davidbugayov.financeanalyzer.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.davidbugayov.financeanalyzer.R
import timber.log.Timber
import java.util.Calendar

/**
 * Класс для планирования и управления уведомлениями.
 */
class NotificationScheduler {

    companion object {
        private const val TRANSACTION_REMINDER_CHANNEL_ID = "transaction_reminder_channel"
        private const val TRANSACTION_REMINDER_REQUEST_CODE = 1001
    }

    /**
     * Проверяет, есть ли у приложения разрешение на использование точных будильников.
     * @param context Контекст приложения.
     * @return true, если разрешение предоставлено, иначе false.
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // До Android 12 (API 31) разрешение не требуется
        }
    }

    /**
     * Возвращает Intent для открытия настроек разрешений для точных будильников.
     * @param context Контекст приложения.
     * @return Intent для открытия настроек.
     */
    fun getExactAlarmSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    /**
     * Создает канал уведомлений для Android 8.0 (API 26) и выше.
     * @param context Контекст приложения.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.transaction_reminder_channel_name)
            val description = context.getString(R.string.transaction_reminder_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(TRANSACTION_REMINDER_CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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
        
        // Проверяем, есть ли у нас разрешение на использование точных будильников
        val canScheduleExactAlarms = canScheduleExactAlarms(context)
        
        try {
            if (canScheduleExactAlarms) {
                // Для Android 6.0 (API 23) и выше используем setExactAndAllowWhileIdle
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        alarmPendingIntent
                    )
                    Timber.d("Scheduled exact alarm with setExactAndAllowWhileIdle")
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        alarmPendingIntent
                    )
                    Timber.d("Scheduled exact alarm with setExact")
                }
            } else {
                // Если нет разрешения, используем неточный будильник
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    alarmPendingIntent
                )
                Timber.d("Scheduled inexact alarm with set")
            }
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
} 