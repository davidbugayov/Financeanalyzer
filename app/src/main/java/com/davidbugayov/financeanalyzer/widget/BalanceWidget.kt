package com.davidbugayov.financeanalyzer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.LoadTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Виджет для отображения текущего баланса, доходов и расходов.
 * Обновляется каждые 30 минут или при запуске приложения.
 * Визуально соответствует компоненту BalanceCard из приложения.
 * При нажатии на виджет открывается приложение.
 *
 * @property loadTransactionsUseCase UseCase для загрузки транзакций
 * @property scope Корутин скоуп для асинхронных операций
 */
class BalanceWidget : AppWidgetProvider(), KoinComponent {

    private val loadTransactionsUseCase: LoadTransactionsUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

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
        val views = RemoteViews(context.packageName, R.layout.balance_widget_layout)

        // Создаем Intent для запуска главной активности приложения
        val launchAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchAppIntent != null) {
            launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Создаем PendingIntent для запуска приложения
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Устанавливаем обработчик нажатия на весь виджет
            views.setOnClickPendingIntent(R.id.widget_balance, pendingIntent)

            // Также добавляем обработчик на весь контейнер для лучшей отзывчивости
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        }

        // Загружаем данные о транзакциях в фоновом потоке
        scope.launch {
            loadTransactionsUseCase().fold(
                onSuccess = { transactions ->
                    // Рассчитываем баланс, доходы и расходы
                    val currency = if (transactions.isNotEmpty()) transactions.first().amount.currency else Money.zero().currency

                    val income = transactions
                        .filter { transaction -> !transaction.isExpense }
                        .fold(Money.zero(currency)) { acc, transaction -> acc + transaction.amount }

                    val expense = transactions
                        .filter { transaction -> transaction.isExpense }
                        .fold(Money.zero(currency)) { acc, transaction -> acc + transaction.amount }

                    val balance = income - expense

                    // Обновляем UI виджета
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.widget_balance, balance.formatForDisplay())
                        views.setTextViewText(R.id.widget_income, income.formatForDisplay())
                        views.setTextViewText(R.id.widget_expense, expense.formatForDisplay())

                        // Устанавливаем цвет в зависимости от значения баланса
                        val color = if (balance.isPositive()) {
                            context.getColor(R.color.income)
                        } else {
                            context.getColor(R.color.expense)
                        }
                        views.setTextColor(R.id.widget_balance, color)

                        // Обновляем виджет
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                },
                onFailure = { exception: Throwable ->
                    Timber.e(exception, "Failed to load transactions for widget")
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.widget_balance, "?")
                        views.setTextViewText(R.id.widget_income, "?")
                        views.setTextViewText(R.id.widget_expense, "?")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            )
        }
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
                ComponentName(context, BalanceWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
} 