package com.davidbugayov.financeanalyzer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.util.formatTransactionAmount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Компактный виджет для отображения текущего баланса.
 * Размер 1x1, отображает только сумму баланса.
 * При нажатии на виджет открывается приложение.
 * Обновляется каждые 30 минут или при запуске приложения.
 *
 * @property loadTransactionsUseCase UseCase для загрузки транзакций
 * @property scope Корутин скоуп для асинхронных операций с SupervisorJob для обработки ошибок
 */
class SmallBalanceWidget : AppWidgetProvider(), KoinComponent {

    private val loadTransactionsUseCase: LoadTransactionsUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Обновляем каждый экземпляр виджета
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * Обновляет виджет с актуальными данными о балансе
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Создаем RemoteViews для обновления виджета
        val views = RemoteViews(context.packageName, R.layout.small_balance_widget_layout)
        
        // Создаем Intent для запуска главной активности приложения
        val launchAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchAppIntent != null) {
            launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            // Создаем PendingIntent для запуска приложения
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    context,
                    1, // Используем другой requestCode, чтобы не конфликтовать с основным виджетом
                    launchAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    context,
                    1,
                    launchAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            
            // Устанавливаем обработчик нажатия на весь виджет
            views.setOnClickPendingIntent(R.id.small_widget_container, pendingIntent)
        }

        // Загружаем данные о транзакциях в фоновом потоке
        scope.launch {
            loadTransactionsUseCase().fold(
                onSuccess = { transactions: List<Transaction> ->
                    val income = transactions
                        .filter { transaction -> !transaction.isExpense }
                        .map { transaction -> transaction.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

                    val expenses = transactions
                        .filter { transaction -> transaction.isExpense }
                        .map { transaction -> transaction.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
                        
                    val balance = income - expenses

                    // Обновляем UI виджета
                    withContext(Dispatchers.Main) {
                        // Форматируем баланс для компактного отображения
                        val formattedBalance = formatTransactionAmount(balance)

                        // Устанавливаем значение в виджет
                        views.setTextViewText(R.id.small_widget_balance, formattedBalance)

                        // Устанавливаем цвет баланса в зависимости от значения
                        val balanceColor = if (balance >= Money.zero()) {
                            0xFF4CAF50.toInt() // Green
                        } else {
                            0xFFF44336.toInt() // Red
                        }
                        views.setTextColor(R.id.small_widget_balance, balanceColor)

                        // Обновляем виджет
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                },
                onFailure = { exception: Throwable ->
                    Timber.e(exception, "Failed to load transactions for widget")
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.small_widget_balance, "?")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            )
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        scope.cancel()
    }
    
    /**
     * Вызывается при получении широковещательного сообщения
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Если это запрос на обновление, обновляем данные виджета
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SmallBalanceWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
} 