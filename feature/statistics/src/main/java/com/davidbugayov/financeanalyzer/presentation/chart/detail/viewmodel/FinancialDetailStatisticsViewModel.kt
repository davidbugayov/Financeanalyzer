package com.davidbugayov.financeanalyzer.presentation.chart.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateCategoryStatsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateEnhancedFinancialMetricsUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.detail.model.FinancialMetrics
import com.davidbugayov.financeanalyzer.presentation.chart.detail.state.FinancialDetailStatisticsContract
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import com.davidbugayov.financeanalyzer.utils.DateUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для экрана подробной финансовой статистики.
 * Следует принципам MVI (Model-View-Intent).
 *
 * @param startDate Начальная дата периода анализа
 * @param endDate Конечная дата периода анализа
 * @param transactionRepository Репозиторий транзакций
 * @param calculateCategoryStatsUseCase UseCase для расчета статистики по категориям
 */
class FinancialDetailStatisticsViewModel(
    private val startDate: Long,
    private val endDate: Long,
    private val transactionRepository: TransactionRepository,
    private val calculateCategoryStatsUseCase: CalculateCategoryStatsUseCase,
    private val calculateEnhancedFinancialMetricsUseCase: CalculateEnhancedFinancialMetricsUseCase? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(FinancialDetailStatisticsContract.State())
    val state: StateFlow<FinancialDetailStatisticsContract.State> = _state.asStateFlow()

    private val _metrics = MutableStateFlow(FinancialMetrics())
    val metrics: StateFlow<FinancialMetrics> = _metrics.asStateFlow()

    private val _effect = MutableSharedFlow<FinancialDetailStatisticsContract.Effect>()
    val effect: SharedFlow<FinancialDetailStatisticsContract.Effect> = _effect.asSharedFlow()

    init {
        // Подписываемся на изменения валюты
        viewModelScope.launch {
            CurrencyProvider.getCurrencyFlow().collect { newCurrency ->
                // Пересчитываем данные с новой валютой
                if (_state.value.transactions.isNotEmpty()) {
                    val transactions = _state.value.transactions
                    val (income, expense) = calculateIncomeAndExpense(transactions)

                    _state.value =
                        _state.value.copy(
                            income = income,
                            expense = expense,
                        )

                    // Пересчитываем расширенные метрики
                    calculateFinancialMetrics(transactions, income, expense)
                }
            }
        }
    }

    /**
     * Обработка интентов (событий) от UI
     */
    fun handleIntent(intent: FinancialDetailStatisticsContract.Intent) {
        when (intent) {
            is FinancialDetailStatisticsContract.Intent.LoadData -> loadData()
            is FinancialDetailStatisticsContract.Intent.ToggleIncludeTransfers -> {
                _state.value = _state.value.copy(includeTransfers = intent.include)
                // TODO: применить к фильтрации транзакций в loadData / calculate
            }
            is FinancialDetailStatisticsContract.Intent.ToggleIncludeRefunds -> {
                _state.value = _state.value.copy(includeRefunds = intent.include)
                // TODO: применить к фильтрации транзакций в loadData / calculate
            }
        }
    }

    /**
     * Загрузка данных для анализа
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                // Форматируем период для отображения
                val startDateObj = Date(startDate)
                val endDateObj = Date(endDate)
                val formattedPeriod = DateUtils.formatDateRange(startDateObj, endDateObj)

                // Загружаем транзакции за указанный период
                val transactions = transactionRepository.getTransactionsForPeriod(startDateObj, endDateObj)

                // Рассчитываем базовые метрики
                val (income, expense) = calculateIncomeAndExpense(transactions)

                // Рассчитываем расширенные метрики сразу
                calculateFinancialMetrics(transactions, income, expense)

                // Обновляем состояние
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        transactions = transactions,
                        income = income,
                        expense = expense,
                        period = formattedPeriod,
                    )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка при загрузке данных",
                    )
                viewModelScope.launch {
                    _effect.emit(
                        FinancialDetailStatisticsContract.Effect.ShowError("Ошибка загрузки данных: ${e.message}"),
                    )
                }
            }
        }
    }

    /**
     * Расчет доходов и расходов из списка транзакций
     */
    private fun calculateIncomeAndExpense(transactions: List<Transaction>): Pair<Money, Money> {
        var totalIncome = BigDecimal.ZERO
        var totalExpense = BigDecimal.ZERO

        for (transaction in transactions) {
            if (!transaction.isExpense) {
                totalIncome = totalIncome.add(transaction.amount.amount)
            } else {
                totalExpense = totalExpense.add(transaction.amount.amount.abs())
            }
        }

        val currentCurrency = CurrencyProvider.getCurrency()
        return Pair(
            Money(totalIncome, currentCurrency),
            Money(totalExpense, currentCurrency),
        )
    }

    /**
     * Расчет расширенных финансовых метрик
     */
    private suspend fun calculateFinancialMetrics(
        transactions: List<Transaction>,
        income: Money,
        expense: Money,
    ) {
        // Рассчитываем норму сбережений (если доход > 0)
        val savingsRate =
            if (income.amount > BigDecimal.ZERO) {
                val balance = income.amount.subtract(expense.amount)
                val rate = balance.divide(income.amount, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal(100)).toFloat()
                rate.coerceIn(-100f, 100f)
            } else {
                0f
            }

        // Рассчитываем статистику по категориям
        val (categoryStats, _, _) = calculateCategoryStatsUseCase(transactions)

        // Разделяем статистику на доходы и расходы
        val expenseCategories = categoryStats.filter { it.isExpense }
        val incomeCategories = categoryStats.filter { !it.isExpense }

        // Рассчитываем среднедневной и среднемесячный расход
        val dayCount = ((endDate - startDate) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
        val currentCurrency = CurrencyProvider.getCurrency()
        val averageDailyExpense =
            if (dayCount > 0) {
                Money(expense.amount.divide(BigDecimal(dayCount), 2, RoundingMode.HALF_EVEN), currentCurrency)
            } else {
                Money.zero(currentCurrency)
            }

        val averageMonthlyExpense = Money(averageDailyExpense.amount.multiply(BigDecimal(30)), currentCurrency)

        // Находим основные категории
        val topExpenseCategory = expenseCategories.maxByOrNull { it.amount.amount }?.category ?: ""
        val topIncomeCategory = incomeCategories.maxByOrNull { it.amount.amount }?.category ?: ""

        // Создаем топ категории расходов для UI
        val topExpenseCategories =
            expenseCategories
                .sortedByDescending { it.amount.amount }
                .take(3)
                .map { Pair(it.category, it.amount) }

        // Рассчитываем количество месяцев, на которые хватит сбережений
        val monthsOfSavings =
            if (averageMonthlyExpense.amount > BigDecimal.ZERO) {
                val savings = income.amount.subtract(expense.amount)
                (savings.divide(averageMonthlyExpense.amount, 0, RoundingMode.FLOOR)).toInt()
            } else {
                0
            }

        // Рассчитываем статистику по транзакциям
        val incomeTransactionsCount = transactions.count { !it.isExpense }
        val expenseTransactionsCount = transactions.count { it.isExpense }

        // Средний доход и расход на транзакцию
        val averageIncomePerTransaction =
            if (incomeTransactionsCount > 0) {
                Money(
                    income.amount.divide(BigDecimal(incomeTransactionsCount), 2, RoundingMode.HALF_EVEN),
                    currentCurrency,
                )
            } else {
                Money.zero(currentCurrency)
            }

        val averageExpensePerTransaction =
            if (expenseTransactionsCount > 0) {
                Money(
                    expense.amount.divide(BigDecimal(expenseTransactionsCount), 2, RoundingMode.HALF_EVEN),
                    currentCurrency,
                )
            } else {
                Money.zero(currentCurrency)
            }

        // Максимальный доход и расход
        val maxIncome =
            transactions
                .filter { !it.isExpense }
                .maxByOrNull { it.amount.amount }
                ?.amount ?: Money.zero(CurrencyProvider.getCurrency())

        val maxExpense =
            transactions
                .filter { it.isExpense }
                .maxByOrNull { it.amount.amount.abs() }
                ?.amount?.let { Money(it.amount.abs(), CurrencyProvider.getCurrency()) }
                ?: Money.zero(CurrencyProvider.getCurrency())

        // Определяем самый частый день расходов
        val dayOfWeekMap =
            transactions
                .filter { it.isExpense }
                .groupBy { getDayOfWeekName(it.date) }
                .mapValues { it.value.size }

        val mostFrequentExpenseDay =
            dayOfWeekMap.entries
                .maxByOrNull { it.value }
                ?.key ?: ""

        // Рассчитываем продвинутые метрики финансового здоровья (если UseCase доступен)
        val healthMetrics = calculateEnhancedFinancialMetricsUseCase?.invoke(transactions)

        // Обновляем метрики
        Timber.d("ViewModel: Сохраняем метрики - savingsRate=$savingsRate, monthsOfSavings=$monthsOfSavings")

        _metrics.value =
            FinancialMetrics(
                savingsRate = savingsRate,
                expenseCategories = expenseCategories,
                incomeCategories = incomeCategories,
                transactionCount = transactions.size,
                totalTransactions = transactions.size,
                averageDailyExpense = averageDailyExpense,
                averageMonthlyExpense = averageMonthlyExpense,
                largestExpenseCategory = topExpenseCategory,
                largestIncomeCategory = topIncomeCategory,
                dayCount = dayCount,
                topExpenseCategories = topExpenseCategories,
                topExpenseCategory = topExpenseCategory,
                topIncomeCategory = topIncomeCategory,
                mostFrequentExpenseDay = mostFrequentExpenseDay,
                monthsOfSavings = monthsOfSavings.toFloat(),
                incomeTransactionsCount = incomeTransactionsCount,
                expenseTransactionsCount = expenseTransactionsCount,
                averageIncomePerTransaction = averageIncomePerTransaction,
                averageExpensePerTransaction = averageExpensePerTransaction,
                maxIncome = maxIncome,
                maxExpense = maxExpense,
                healthMetrics = healthMetrics,
            )
    }

    /**
     * Получает название дня недели по дате
     */
    private fun getDayOfWeekName(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Понедельник"
            Calendar.TUESDAY -> "Вторник"
            Calendar.WEDNESDAY -> "Среда"
            Calendar.THURSDAY -> "Четверг"
            Calendar.FRIDAY -> "Пятница"
            Calendar.SATURDAY -> "Суббота"
            Calendar.SUNDAY -> "Воскресенье"
            else -> ""
        }
    }
}
