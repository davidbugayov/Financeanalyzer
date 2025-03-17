package com.davidbugayov.financeanalyzer.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.davidbugayov.financeanalyzer.FinanceActivity
import com.davidbugayov.financeanalyzer.R
import timber.log.Timber
import java.util.Calendar

/**
 * BroadcastReceiver для показа уведомлений о необходимости внести транзакции.
 */
class TransactionReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TRANSACTION_REMINDER_CHANNEL_ID = "transaction_reminder_channel"
        private const val TRANSACTION_REMINDER_NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("TransactionReminderReceiver: onReceive")
        try {
            // Показываем уведомление
            showNotification(context)
            
            // Перепланируем уведомление на следующий день
            rescheduleReminder(context)
        } catch (e: Exception) {
            Timber.e(e, "Error in TransactionReminderReceiver.onReceive")
        }
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
            // Используем NotificationScheduler для планирования следующего уведомления
            val scheduler = NotificationScheduler()
            scheduler.scheduleTransactionReminder(context, 20, 0) // Планируем на 20:00
            Timber.d("Rescheduled reminder for tomorrow at 20:00")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to reschedule reminder due to missing permission")
        } catch (e: Exception) {
            Timber.e(e, "Error rescheduling reminder")
        }
    }
} 