package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R

/**
 * 🔄 Маппер для преобразования финансовых данных в премиум карточки
 * Создает красивые и информативные представления данных
 */
object FinancialDataMapper {

    /**
     * 📊 Создание статистики транзакций
     */
    @Composable
    fun createTransactionStatistics(
        totalTransactions: Int,
        incomeTransactionsCount: Int,
        expenseTransactionsCount: Int,
        averageIncomePerTransaction: String,
        averageExpensePerTransaction: String,
        maxIncome: String,
        maxExpense: String,
        savingsRate: Float,
        monthsOfSavings: Float
    ): List<StatisticItem> {
        return listOf(
            StatisticItem(
                label = "Всего операций",
                value = totalTransactions.toString(),
                description = "За выбранный период",
                icon = Icons.Default.Receipt,
                type = StatisticType.NEUTRAL
            ),
            StatisticItem(
                label = "Операции доходов",
                value = incomeTransactionsCount.toString(),
                description = "${(incomeTransactionsCount.toFloat() / totalTransactions * 100).toInt()}% от общего",
                icon = Icons.Default.TrendingUp,
                type = StatisticType.POSITIVE
            ),
            StatisticItem(
                label = "Операции расходов", 
                value = expenseTransactionsCount.toString(),
                description = "${(expenseTransactionsCount.toFloat() / totalTransactions * 100).toInt()}% от общего",
                icon = Icons.Default.TrendingDown,
                type = StatisticType.NEGATIVE
            ),
            StatisticItem(
                label = "Средний доход",
                value = averageIncomePerTransaction,
                description = "На операцию",
                icon = Icons.Default.AttachMoney,
                type = StatisticType.POSITIVE
            ),
            StatisticItem(
                label = "Средний расход",
                value = averageExpensePerTransaction,
                description = "На операцию", 
                icon = Icons.Default.Money,
                type = StatisticType.NEGATIVE
            ),
            StatisticItem(
                label = "Максимальный доход",
                value = maxIncome,
                description = "Самая крупная сумма",
                icon = Icons.Default.Star,
                type = StatisticType.POSITIVE
            ),
            StatisticItem(
                label = "Максимальный расход",
                value = maxExpense,
                description = "Самая крупная трата",
                icon = Icons.Default.Warning,
                type = if (maxExpense.replace(Regex("[^\\d.]"), "").toFloatOrNull() ?: 0f > 50000f) 
                    StatisticType.WARNING else StatisticType.NEGATIVE
            ),
            StatisticItem(
                label = "Норма сбережений",
                value = "${savingsRate.toInt()}%",
                description = "От общего дохода",
                icon = Icons.Default.Savings,
                type = when {
                    savingsRate >= 20f -> StatisticType.POSITIVE
                    savingsRate >= 10f -> StatisticType.WARNING
                    else -> StatisticType.NEGATIVE
                }
            ),
            StatisticItem(
                label = "Финансовая подушка",
                value = "${monthsOfSavings.toInt()} мес",
                description = "На текущие расходы",
                icon = Icons.Default.Shield,
                type = when {
                    monthsOfSavings >= 6f -> StatisticType.POSITIVE
                    monthsOfSavings >= 3f -> StatisticType.WARNING
                    else -> StatisticType.NEGATIVE
                }
            )
        )
    }

    /**
     * 📈 Создание анализа расходов
     */
    @Composable
    fun createExpenseAnalysis(
        averageDailyExpense: String,
        averageMonthlyExpense: String,
        topIncomeCategory: String,
        topExpenseCategory: String,
        topExpenseCategories: List<Pair<String, String>>,
        mostFrequentExpenseDay: String
    ): List<StatisticItem> {
        val statistics = mutableListOf<StatisticItem>()
        
        statistics.add(
            StatisticItem(
                label = "Ежедневные расходы",
                value = averageDailyExpense,
                description = "В среднем за день",
                icon = Icons.Default.CalendarToday,
                type = StatisticType.NEUTRAL
            )
        )
        
        statistics.add(
            StatisticItem(
                label = "Месячные расходы",
                value = averageMonthlyExpense,
                description = "В среднем за месяц",
                icon = Icons.Default.DateRange,
                type = StatisticType.NEUTRAL
            )
        )
        
        if (topIncomeCategory.isNotEmpty()) {
            statistics.add(
                StatisticItem(
                    label = "Основной доход",
                    value = topIncomeCategory,
                    description = "Главный источник",
                    icon = Icons.Default.AccountBalance,
                    type = StatisticType.POSITIVE
                )
            )
        }
        
        if (topExpenseCategory.isNotEmpty()) {
            statistics.add(
                StatisticItem(
                    label = "Основной расход",
                    value = topExpenseCategory,
                    description = "Больше всего тратите",
                    icon = Icons.Default.PieChart,
                    type = StatisticType.WARNING
                )
            )
        }
        
        if (mostFrequentExpenseDay.isNotEmpty()) {
            statistics.add(
                StatisticItem(
                    label = "Активный день",
                    value = mostFrequentExpenseDay,
                    description = "Чаще всего тратите",
                    icon = Icons.Default.Schedule,
                    type = StatisticType.NEUTRAL
                )
            )
        }
        
        return statistics
    }

    /**
     * 🔍 Создание инсайтов расходов
     */
    @Composable
    fun createExpenseInsights(
        topExpenseCategories: List<Pair<String, String>>,
        savingsRate: Float,
        monthsOfSavings: Float,
        totalTransactions: Int,
        mostFrequentExpenseDay: String
    ): List<InsightItem> {
        val insights = mutableListOf<InsightItem>()
        
        // Анализ категорий расходов
        if (topExpenseCategories.isNotEmpty()) {
            val topCategory = topExpenseCategories.first()
            val totalExpense = topExpenseCategories.sumOf { 
                it.second.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0 
            }
            val percentage = if (totalExpense > 0) {
                (((topCategory.second.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0) / totalExpense) * 100).toInt()
            } else 0
            
            insights.add(
                InsightItem(
                    title = "Концентрация расходов",
                    description = "Категория \"${topCategory.first}\" составляет ${percentage}% ваших расходов. ${
                        when {
                            percentage > 50 -> "Это критически много - стоит диверсифицировать траты"
                            percentage > 30 -> "Довольно высокая концентрация - рекомендуем оптимизировать"
                            else -> "Хорошее распределение расходов по категориям"
                        }
                    }",
                    metric = "${percentage}% в одной категории",
                    icon = Icons.Default.PieChart,
                    importance = when {
                        percentage > 50 -> InsightImportance.HIGH
                        percentage > 30 -> InsightImportance.MEDIUM
                        else -> InsightImportance.LOW
                    }
                )
            )
        }
        
        // Анализ нормы сбережений
        insights.add(
            InsightItem(
                title = "Финансовое здоровье",
                description = when {
                    savingsRate >= 20f -> "Отличная норма сбережений! Вы на правильном пути к финансовой независимости"
                    savingsRate >= 15f -> "Хорошая норма сбережений. Попробуйте довести до 20% для оптимального результата"
                    savingsRate >= 10f -> "Неплохо, но есть потенциал для улучшения. Эксперты рекомендуют откладывать 15-20%"
                    savingsRate >= 5f -> "Низкая норма сбережений. Попробуйте увеличить до 10% как минимум"
                    else -> "Критически низкий уровень сбережений. Необходимо срочно пересмотреть бюджет"
                },
                metric = "${savingsRate.toInt()}% от дохода",
                icon = when {
                    savingsRate >= 15f -> Icons.Default.TrendingUp
                    savingsRate >= 10f -> Icons.Default.Balance
                    else -> Icons.Default.Warning
                },
                importance = when {
                    savingsRate < 5f -> InsightImportance.HIGH
                    savingsRate < 15f -> InsightImportance.MEDIUM
                    else -> InsightImportance.LOW
                }
            )
        )
        
        // Анализ финансовой подушки
        insights.add(
            InsightItem(
                title = "Финансовая защита",
                description = when {
                    monthsOfSavings >= 6f -> "У вас отличная финансовая подушка! Вы защищены от большинства непредвиденных ситуаций"
                    monthsOfSavings >= 3f -> "Хорошая финансовая подушка. Для полной безопасности рекомендуется 6 месяцев расходов"
                    monthsOfSavings >= 1f -> "Минимальная финансовая защита. Стремитесь к 3-6 месяцам расходов в резерве"
                    else -> "Отсутствует финансовая подушка. Это очень рискованно - любая непредвиденная ситуация может стать критической"
                },
                metric = "${monthsOfSavings.toInt()} мес. расходов",
                icon = when {
                    monthsOfSavings >= 3f -> Icons.Default.Shield
                    monthsOfSavings >= 1f -> Icons.Default.Security
                    else -> Icons.Default.Warning
                },
                importance = when {
                    monthsOfSavings < 1f -> InsightImportance.HIGH
                    monthsOfSavings < 3f -> InsightImportance.MEDIUM
                    else -> InsightImportance.LOW
                }
            )
        )
        
        // Анализ количества транзакций
        if (totalTransactions > 100) {
            insights.add(
                InsightItem(
                    title = "Частота трат",
                    description = "У вас много мелких операций ($totalTransactions). Это может указывать на импульсивные покупки. Попробуйте планировать траты заранее и объединять покупки.",
                    metric = "$totalTransactions операций",
                    icon = Icons.Default.ShoppingCart,
                    importance = if (totalTransactions > 200) InsightImportance.MEDIUM else InsightImportance.LOW
                )
            )
        }
        
        // Анализ паттернов трат
        if (mostFrequentExpenseDay.isNotEmpty()) {
            insights.add(
                InsightItem(
                    title = "Паттерн расходов",
                    description = "Вы чаще всего тратите деньги по $mostFrequentExpenseDay. Это может быть связано с зарплатой, выходными или распорядком дня. Понимание этого паттерна поможет лучше планировать бюджет.",
                    metric = "Пик: $mostFrequentExpenseDay",
                    icon = Icons.Default.Timeline,
                    importance = InsightImportance.LOW
                )
            )
        }
        
        return insights
    }

    /**
     * 📋 Создание инсайтов паттернов трат
     */
    @Composable 
    fun createSpendingPatternInsights(
        mostFrequentExpenseDay: String,
        expenseTransactionsCount: Int,
        averageExpensePerTransaction: Float
    ): List<InsightItem> {
        val insights = mutableListOf<InsightItem>()
        
        if (mostFrequentExpenseDay.isNotEmpty()) {
            insights.add(
                InsightItem(
                    title = "Активный день недели",
                    description = "По статистике, $mostFrequentExpenseDay - ваш самый \"дорогой\" день. Планируйте бюджет с учетом этого паттерна.",
                    metric = "Пик: $mostFrequentExpenseDay",
                    icon = Icons.Default.CalendarToday,
                    importance = InsightImportance.LOW
                )
            )
        }
        
        // Анализ размера трат
        when {
            averageExpensePerTransaction < 500f -> {
                insights.add(
                    InsightItem(
                        title = "Мелкие частые траты",
                        description = "Ваши расходы состоят в основном из мелких покупок. Это хорошо для контроля, но следите за их накоплением.",
                        metric = "Средняя трата: ${averageExpensePerTransaction.toInt()}₽",
                        icon = Icons.Default.LocalGroceryStore,
                        importance = InsightImportance.LOW
                    )
                )
            }
            averageExpensePerTransaction > 2000f -> {
                insights.add(
                    InsightItem(
                        title = "Крупные покупки",
                        description = "У вас преобладают крупные траты. Убедитесь, что каждая такая покупка хорошо продумана.",
                        metric = "Средняя трата: ${averageExpensePerTransaction.toInt()}₽",
                        icon = Icons.Default.ShoppingBag,
                        importance = InsightImportance.MEDIUM
                    )
                )
            }
        }
        
        // Анализ частоты трат
        if (expenseTransactionsCount > 150) {
            insights.add(
                InsightItem(
                    title = "Высокая активность",
                    description = "У вас очень много операций расходов ($expenseTransactionsCount). Возможно, стоит оптимизировать процесс покупок.",
                    metric = "$expenseTransactionsCount операций",
                    icon = Icons.Default.Speed,
                    importance = InsightImportance.MEDIUM
                )
            )
        }
        
        return insights
    }
}