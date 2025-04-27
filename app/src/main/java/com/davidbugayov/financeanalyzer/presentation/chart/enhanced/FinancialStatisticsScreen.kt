package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar

/**
 * Экран подробной финансовой статистики
 *
 * @param transactions Список транзакций для анализа
 * @param income Общий доход
 * @param expense Общий расход
 * @param period Текстовое описание периода
 * @param onNavigateBack Обработчик нажатия на кнопку "Назад"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialStatisticsScreen(
    transactions: List<Transaction>,
    income: Money,
    expense: Money,
    period: String,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Статистические метрики
    val metrics = remember(transactions) {
        calculateFinancialMetrics(transactions, income, expense)
    }

    // Фоновый градиент
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Финансовая статистика",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Заголовок с периодом
                Text(
                    text = "Статистика за период",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Карточка с основными показателями
                KeyMetricsCard(
                    income = income,
                    expense = expense,
                    savingsRate = metrics.savingsRate,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Карточка со статистикой транзакций
                TransactionsStatisticsCard(
                    metrics = metrics,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Карточка с анализом расходов
                ExpenseAnalysisCard(
                    metrics = metrics,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Карточка с рекомендациями
                RecommendationsCard(
                    metrics = metrics,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Карточка с ключевыми финансовыми показателями
 */
@Composable
private fun KeyMetricsCard(
    income: Money,
    expense: Money,
    savingsRate: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ключевые показатели",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Строка с доходами и расходами
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Доходы
                Column {
                    Text(
                        text = "Доходы",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = income.format(true),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF66BB6A),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Расходы
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Расходы",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = expense.format(true),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Норма сбережений с прогресс-индикатором
            Text(
                text = "Норма сбережений: ${savingsRate.toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val progressColor = when {
                savingsRate >= 30 -> Color(0xFF00C853) // Зеленый
                savingsRate >= 15 -> Color(0xFF66BB6A) // Светло-зеленый
                savingsRate >= 5 -> Color(0xFFFFA726)  // Оранжевый
                else -> Color(0xFFEF5350)              // Красный
            }

            LinearProgressIndicator(
                progress = { (savingsRate.toFloat() / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Статус нормы сбережений
            val status = when {
                savingsRate >= 30 -> "Отлично"
                savingsRate >= 15 -> "Хорошо"
                savingsRate >= 5 -> "Удовлетворительно"
                else -> "Требует внимания"
            }

            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = progressColor,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Карточка со статистикой транзакций
 */
@Composable
private fun TransactionsStatisticsCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Статистика транзакций",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Количество транзакций
            MetricRow(
                title = "Всего транзакций",
                value = "${metrics.totalTransactions}"
            )

            MetricRow(
                title = "Транзакций доходов",
                value = "${metrics.incomeTransactionsCount}"
            )

            MetricRow(
                title = "Транзакций расходов",
                value = "${metrics.expenseTransactionsCount}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Средние значения
            MetricRow(
                title = "Средний доход за транзакцию",
                value = metrics.averageIncomePerTransaction.format(true)
            )

            MetricRow(
                title = "Средний расход за транзакцию",
                value = metrics.averageExpensePerTransaction.format(true)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Максимальные значения
            MetricRow(
                title = "Максимальный доход",
                value = metrics.maxIncome.format(true)
            )

            MetricRow(
                title = "Максимальный расход",
                value = metrics.maxExpense.format(true)
            )
        }
    }
}

/**
 * Карточка с анализом расходов
 */
@Composable
private fun ExpenseAnalysisCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Анализ расходов",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ежедневные расходы
            MetricRow(
                title = "Средний расход в день",
                value = metrics.averageDailyExpense.format(true)
            )

            MetricRow(
                title = "Средний расход в месяц",
                value = metrics.averageMonthlyExpense.format(true)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Основная категория расходов
            if (metrics.topExpenseCategory.isNotEmpty()) {
                Text(
                    text = "Основные категории расходов",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Перечисление топ-3 категорий расходов
                metrics.topExpenseCategories.forEachIndexed { index, (category, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. $category",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = amount.format(true),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка с финансовыми рекомендациями
 */
@Composable
private fun RecommendationsCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Рекомендации",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Генерируем персонализированные рекомендации
            val recommendations = generateRecommendations(metrics)

            recommendations.forEach { recommendation ->
                Text(
                    text = "• $recommendation",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Строка с метрикой (название и значение)
 */
@Composable
private fun MetricRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Класс с метриками финансового анализа
 */
data class FinancialMetrics(
    val totalTransactions: Int,
    val incomeTransactionsCount: Int,
    val expenseTransactionsCount: Int,
    val averageIncomePerTransaction: Money,
    val averageExpensePerTransaction: Money,
    val maxIncome: Money,
    val maxExpense: Money,
    val savingsRate: Double,
    val monthsOfSavings: Double,
    val averageDailyExpense: Money,
    val averageMonthlyExpense: Money,
    val topExpenseCategory: String,
    val topExpenseCategories: List<Pair<String, Money>>,
    val topIncomeCategory: String,
    val mostFrequentExpenseDay: String
)

/**
 * Расчет финансовых метрик на основе транзакций
 */
private fun calculateFinancialMetrics(
    transactions: List<Transaction>,
    totalIncome: Money,
    totalExpense: Money
): FinancialMetrics {
    // Разделяем транзакции на доходы и расходы
    val incomeTransactions = transactions.filter { !it.isExpense }
    val expenseTransactions = transactions.filter { it.isExpense }

    // Считаем количество транзакций
    val incomeCount = incomeTransactions.size
    val expenseCount = expenseTransactions.size

    // Средние значения на транзакцию
    val avgIncomePerTransaction = if (incomeCount > 0)
        totalIncome / incomeCount.toBigDecimal() else Money.zero()

    val avgExpensePerTransaction = if (expenseCount > 0)
        totalExpense / expenseCount.toBigDecimal() else Money.zero()

    // Максимальные значения
    val maxIncome = incomeTransactions.maxByOrNull { it.amount.amount }?.amount ?: Money.zero()
    val maxExpense = expenseTransactions.maxByOrNull { it.amount.amount }?.amount ?: Money.zero()

    // Норма сбережений
    val savingsRate = if (!totalIncome.isZero()) {
        (totalIncome.minus(totalExpense).amount.toDouble() / totalIncome.amount.toDouble()) * 100
    } else 0.0

    // Средние ежедневные и ежемесячные расходы
    val daysInPeriod = 30 // Предполагаем месяц для примера
    val averageDailyExpense = if (daysInPeriod > 0)
        totalExpense / daysInPeriod.toBigDecimal() else Money.zero()

    val averageMonthlyExpense = averageDailyExpense * 30.toBigDecimal()

    // На сколько месяцев хватит сбережений
    val monthsOfSavings = if (!averageMonthlyExpense.isZero()) {
        totalIncome.minus(totalExpense).amount.toDouble() / averageMonthlyExpense.amount.toDouble()
    } else Double.POSITIVE_INFINITY

    // Анализ категорий расходов
    val expensesByCategory = expenseTransactions
        .groupBy { it.category }
        .mapValues { (_, transactions) ->
            transactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
        }

    val topExpenseCategories = expensesByCategory.entries
        .sortedByDescending { it.value.amount }
        .take(3)
        .map { it.key to it.value }

    val topExpenseCategory = topExpenseCategories.firstOrNull()?.first ?: ""

    // Анализ категорий доходов
    val incomesByCategory = incomeTransactions
        .groupBy { it.category }
        .mapValues { (_, transactions) ->
            transactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
        }

    val topIncomeCategory = incomesByCategory.entries
        .maxByOrNull { it.value.amount }?.key ?: ""

    // День недели с наибольшими расходами (для примера используем "Вторник")
    val mostFrequentExpenseDay = "Вторник"

    return FinancialMetrics(
        totalTransactions = transactions.size,
        incomeTransactionsCount = incomeCount,
        expenseTransactionsCount = expenseCount,
        averageIncomePerTransaction = avgIncomePerTransaction,
        averageExpensePerTransaction = avgExpensePerTransaction,
        maxIncome = maxIncome,
        maxExpense = maxExpense,
        savingsRate = savingsRate,
        monthsOfSavings = monthsOfSavings,
        averageDailyExpense = averageDailyExpense,
        averageMonthlyExpense = averageMonthlyExpense,
        topExpenseCategory = topExpenseCategory,
        topExpenseCategories = topExpenseCategories,
        topIncomeCategory = topIncomeCategory,
        mostFrequentExpenseDay = mostFrequentExpenseDay
    )
}

/**
 * Генерирует персонализированные финансовые рекомендации
 */
private fun generateRecommendations(metrics: FinancialMetrics): List<String> {
    val recommendations = mutableListOf<String>()

    // Добавляем рекомендации в зависимости от финансовых показателей
    if (metrics.savingsRate < 10) {
        recommendations.add("Старайтесь сохранять не менее 10-15% дохода. Рассмотрите возможность сокращения необязательных расходов.")
    }

    if (metrics.topExpenseCategory.isNotEmpty()) {
        recommendations.add("Основная категория расходов: ${metrics.topExpenseCategory}. Проанализируйте возможности для оптимизации.")
    }

    if (metrics.monthsOfSavings < 3) {
        recommendations.add("Ваших сбережений хватит менее чем на 3 месяца. Рекомендуется создать резерв на 3-6 месяцев расходов.")
    }

    // Добавим общие рекомендации
    recommendations.add("Отслеживайте свои расходы регулярно и планируйте бюджет на месяц вперед.")
    recommendations.add("Рассмотрите возможности для увеличения дохода через инвестиции или дополнительные источники.")

    return recommendations
} 