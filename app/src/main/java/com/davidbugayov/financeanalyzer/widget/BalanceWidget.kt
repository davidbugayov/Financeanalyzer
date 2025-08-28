package com.davidbugayov.financeanalyzer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.shared.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import timber.log.Timber

class BalanceWidget : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        // Разблокируем ачивку добавления большого виджета (4x1)
        AchievementTrigger.onMilestoneReached("widget_large_added")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val views = RemoteViews(context.packageName, R.layout.balance_widget_layout)
        // Безопасные дефолты до загрузки
        views.setTextViewText(R.id.widget_title, context.getString(R.string.current_balance))
        views.setTextViewText(R.id.widget_balance, context.getString(R.string.default_amount))
        views.setTextViewText(R.id.widget_income, context.getString(R.string.default_amount))
        views.setTextViewText(R.id.widget_expense, context.getString(R.string.default_amount))
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Устанавливаем начальные значения
        views.setTextViewText(R.id.widget_title, context.getString(R.string.current_balance))
        views.setTextViewText(R.id.widget_balance, context.getString(R.string.default_amount))
        views.setTextViewText(R.id.widget_income, context.getString(R.string.default_amount))
        views.setTextViewText(R.id.widget_expense, context.getString(R.string.default_amount))

        // Добавляем клик для запуска приложения
        val launchAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchAppIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    1,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        }

        // Обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Загружаем данные в фоне
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Загружаем транзакции напрямую из репозитория через Koin, т.к. пустой SharedFacade() вернет emptyList
                val repo = GlobalContext.get().get<ITransactionRepository>()
                val transactions = repo.loadTransactions()

                // Используем те же правила, что и на домашней карточке: конвертация в текущую валюту
                val currentCurrency = CurrencyProvider.getCurrency()

                val income =
                    transactions
                        .filter { !it.isExpense }
                        .fold(Money.zero(currentCurrency)) { acc, t ->
                            val converted = Money.fromMajor(t.amount.toMajorDouble(), currentCurrency)
                            acc + converted
                        }

                val expense =
                    transactions
                        .filter { it.isExpense }
                        .fold(Money.zero(currentCurrency)) { acc, t ->
                            val converted = Money.fromMajor(t.amount.toMajorDouble(), currentCurrency)
                            acc + converted.abs()
                        }

                val balance = income.minus(expense)

                withContext(Dispatchers.Main) {
                    // Обновляем баланс
                    val formattedBalance = WidgetAmountFormatter.formatForWidget(balance, 10)
                    views.setTextViewText(R.id.widget_balance, formattedBalance)
                    // Цвет баланса по знаку
                    val balanceColor = if (balance.isNegative()) com.davidbugayov.financeanalyzer.ui.R.color.expense else com.davidbugayov.financeanalyzer.ui.R.color.income
                    views.setTextColor(R.id.widget_balance, context.getColor(balanceColor))

                    // Обновляем доходы
                    val formattedIncome = WidgetAmountFormatter.formatForWidget(income, 8)
                    views.setTextViewText(R.id.widget_income, formattedIncome)
                    views.setTextColor(
                        R.id.widget_income,
                        context.getColor(com.davidbugayov.financeanalyzer.ui.R.color.income),
                    )

                    // Обновляем расходы
                    val formattedExpense = WidgetAmountFormatter.formatForWidget(expense, 8)
                    views.setTextViewText(R.id.widget_expense, formattedExpense)
                    views.setTextColor(
                        R.id.widget_expense,
                        context.getColor(com.davidbugayov.financeanalyzer.ui.R.color.expense),
                    )

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }

                Timber.d(
                    "Большой виджет обновлен: баланс=${WidgetAmountFormatter.formatForWidget(
                        balance,
                        10,
                    )}, доходы=${WidgetAmountFormatter.formatForWidget(
                        income,
                        8,
                    )}, расходы=${WidgetAmountFormatter.formatForWidget(expense, 8)}",
                )
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении большого виджета")
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.widget_balance, "!")
                    views.setTextViewText(R.id.widget_income, "!")
                    views.setTextViewText(R.id.widget_expense, "!")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            appWidgetIds?.let { ids ->
                onUpdate(context, AppWidgetManager.getInstance(context), ids)
            }
        }
    }
}
