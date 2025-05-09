package com.davidbugayov.financeanalyzer.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.davidbugayov.financeanalyzer.FinanceActivity
import com.davidbugayov.financeanalyzer.R
import timber.log.Timber

/**
 * BroadcastReceiver для показа уведомлений о необходимости внести транзакции.
 */
class TransactionReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TRANSACTION_REMINDER_CHANNEL_ID = "transaction_reminder_channel"
        private const val TRANSACTION_REMINDER_NOTIFICATION_ID = 1001

        // Action для показа уведомления о необходимости предоставления разрешений
        const val ACTION_SHOW_PERMISSION_NOTIFICATION =
            "com.davidbugayov.financeanalyzer.SHOW_PERMISSION_NOTIFICATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("TransactionReminderReceiver: onReceive, action: ${intent.action}")
        try {
            when (intent.action) {
                ACTION_SHOW_PERMISSION_NOTIFICATION -> {
                    // Показываем уведомление о разрешениях
                    showPermissionNotification(context)
                }

                else -> {
                    // Показываем стандартное уведомление
                    showNotification(context)

                    // Перепланируем уведомление на следующий день
                    rescheduleReminder(context)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in TransactionReminderReceiver.onReceive")
        }
    }

    /**
     * Показывает уведомление о необходимости предоставления разрешений на уведомления.
     * @param context Контекст приложения.
     */
    private fun showPermissionNotification(context: Context) {
        // Создаем Intent для запуска настроек уведомлений с использованием PermissionUtils
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = "package:${context.packageName}".toUri()
            // Используем специальный флаг, чтобы создать новую задачу
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Будем использовать тот же ID для обоих уведомлений
        val pendingIntent = PendingIntent.getActivity(
            context,
            TRANSACTION_REMINDER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Создаем уведомление
        val builder = NotificationCompat.Builder(context, TRANSACTION_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_disabled_title))
            .setContentText(context.getString(R.string.notification_disabled_description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Показываем уведомление
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TRANSACTION_REMINDER_NOTIFICATION_ID, builder.build())

        Timber.d("Permission notification shown")
    }

    /**
     * Показывает уведомление о необходимости внести транзакции.
     * @param context Контекст приложения.
     */
    private fun showNotification(context: Context) {
        // Создаем Intent для запуска приложения при нажатии на уведомление
        val intent = Intent(context, FinanceActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            TRANSACTION_REMINDER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Создаем уведомление
        val builder = NotificationCompat.Builder(context, TRANSACTION_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Убедитесь, что у вас есть такая иконка
            .setContentTitle(context.getString(R.string.transaction_reminder_title))
            .setContentText(context.getString(R.string.transaction_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Показываем уведомление
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TRANSACTION_REMINDER_NOTIFICATION_ID, builder.build())
    }

    /**
     * Перепланирует уведомление на следующий день.
     * @param context Контекст приложения.
     */
    private fun rescheduleReminder(context: Context) {
        try {
            val preferencesManager = PreferencesManager(context)
            val (hour, minute) = preferencesManager.getReminderTime()
            NotificationScheduler.scheduleTransactionReminder(context, hour, minute)
            Timber.d("Rescheduled reminder for tomorrow at %02d:%02d", hour, minute)
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to reschedule reminder due to missing permission")
        } catch (e: Exception) {
            Timber.e(e, "Error rescheduling reminder")
        }
    }
} 