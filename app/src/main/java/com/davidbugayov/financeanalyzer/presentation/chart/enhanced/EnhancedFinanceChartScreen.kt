package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.LineChartTypeSelector
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.LineChartPoint
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.LineChartDisplayMode
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartScreenState
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.components.EmptyContent
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartContract
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartItemData
import androidx.compose.ui.graphics.toArgb
import java.math.BigDecimal
import com.davidbugayov.financeanalyzer.utils.DateUtils
import com.davidbugayov.financeanalyzer.presentation.util.UiUtils
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.EnhancedSummaryCard
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.FinancialHealthMetricsCard
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.EnhancedCategoryPieChart
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.EnhancedLineChart

/**
 * Улучшенный экран с финансовыми графиками.
 * Поддерживает свайп между разными типами визуализации и обновленный дизайн.
 *
 * @param viewModel ViewModel для управления состоянием экрана
 * @param onNavigateBack Колбэк для навигации назад
 * @param onNavigateToTransactions Опциональный колбэк для навигации к списку транзакций с фильтрами
 * @param onNavigateToStatistics Опциональный колбэк для навигации к экрану финансовой статистики
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Suppress("DEPRECATION") // Suppressing deprecation warnings for accompanist/pager components
@Composable
fun EnhancedFinanceChartScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTransactions: ((String, Date, Date) -> Unit)? = null,
    onNavigateToStatistics: ((List<Transaction>, Money, Money, String) -> Unit)? = null
) {
    // Собираем состояние из ViewModel
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Add the missing variable
    var shouldScrollToSummaryCard by remember { mutableStateOf(false) }

    // Состояния для улучшенной интерактивности
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Состояние для горизонтального свайпа между типами графиков
    val pagerState = rememberPagerState(initialPage = 0)

    // Состояние для управления типом отображаемого графика (доходы, расходы, оба)
    var lineChartDisplayMode by remember { mutableStateOf(LineChartDisplayMode.BOTH) }

    // Логируем открытие экрана
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "enhanced_finance_chart",
            screenClass = "EnhancedFinanceChartScreen"
        )
        
        // Загружаем данные при каждом открытии экрана
        Timber.d("EnhancedFinanceChartScreen: Принудительно обновляем данные при открытии экрана")
        viewModel.handleIntent(ChartIntent.LoadTransactions)
        
        Timber.d("EnhancedFinanceChartScreen: Всего загружено транзакций: ${state.transactions.size}")
        Timber.d("EnhancedFinanceChartScreen: Даты транзакций: ${state.transactions.map { DateUtils.formatDate(it.date) }.distinct().joinToString()}")
    }
    
    // Следим за изменениями периода
    LaunchedEffect(state.periodType, state.startDate, state.endDate) {
        val periodText = UiUtils.formatPeriod(state.periodType, state.startDate, state.endDate)
        Timber.d("Период изменен на: $periodText")
        Timber.d("Текущие даты: ${DateUtils.formatDate(state.startDate)} - ${DateUtils.formatDate(state.endDate)}")
        Timber.d("Кол-во транзакций после изменения периода: ${state.transactions.size}")
        
        // Для целей отладки - показываем мин. и макс. даты транзакций
        if (state.transactions.isNotEmpty()) {
            val minDate = state.transactions.minByOrNull { it.date }?.date
            val maxDate = state.transactions.maxByOrNull { it.date }?.date
            if (minDate != null && maxDate != null) {
                Timber.d("Диапазон дат транзакций: ${DateUtils.formatDate(minDate)} - ${DateUtils.formatDate(maxDate)}")
            }
        }
    }

    // Настраиваем фоновый градиент
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.charts_title),
                showBackButton = true,
                onBackClick = {
                    onNavigateBack()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundGradient)
        ) {
            // Показываем индикатор загрузки, если данные загружаются
            if (state.isLoading) {
                Timber.d("Отображаем индикатор загрузки")
                CenteredLoadingIndicator(message = stringResource(R.string.loading_data))
            } else if (state.error != null) {
                Timber.d("Отображаем ошибку: ${state.error}")
                // Показываем сообщение об ошибке
                ErrorContent(
                    error = state.error ?: stringResource(R.string.unknown_error),
                    onRetry = { viewModel.handleIntent(ChartIntent.LoadTransactions) }
                )
            } else if (state.transactions.isEmpty()) {
                Timber.d("Отображаем пустое состояние")
                // Показываем сообщение о пустом состоянии
                EmptyContent(
                    message = "Нет данных для отображения",
                    onActionClick = { viewModel.handleIntent(ChartIntent.LoadTransactions) }
                )
            } else {
                Timber.d("Отображаем содержимое графиков")
                // Основной контент с графиками
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Карточка с общим балансом и периодом
                    EnhancedSummaryCard(
                        income = state.income ?: Money.zero(),
                        expense = (state.expense ?: Money.zero()).abs(),
                        startDate = state.startDate,
                        endDate = state.endDate,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    // Табы для переключения между типами графиков
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(0)
                                }
                            },
                            text = { Text("Категории") }
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                            },
                            text = { Text("Динамика") }
                        )
                        Tab(
                            selected = pagerState.currentPage == 2,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                            },
                            text = { Text("Анализ") }
                        )
                    }

                    // Горизонтальный пейджер для свайпа между графиками
                    HorizontalPager(
                        state = pagerState,
                        count = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Пирографик по категориям
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxSize()
                                        .padding(vertical = 16.dp)
                                ) {
                                    // Получаем данные категорий в зависимости от выбранного режима
                                    val categoryData = if (state.showExpenses) {
                                        val data = getExpensesByCategory(state.transactions, state.startDate, state.endDate)
                                        Timber.d("Данные по расходам за период ${DateUtils.formatDate(state.startDate)} - ${DateUtils.formatDate(state.endDate)}: ${data.size} категорий, сумма: ${data.values.sumOf { it.amount.toDouble() }}")
                                        Timber.d("Список категорий расходов: ${data.keys.joinToString()}")
                                        data
                                    } else {
                                        val data = getIncomeByCategory(state.transactions, state.startDate, state.endDate) 
                                        Timber.d("Данные по доходам за период ${DateUtils.formatDate(state.startDate)} - ${DateUtils.formatDate(state.endDate)}: ${data.size} категорий, сумма: ${data.values.sumOf { it.amount.toDouble() }}")
                                        Timber.d("Список категорий доходов: ${data.keys.joinToString()}")
                                        data
                                    }

                                    // Проверяем, нет ли данных для отображения
                                    if (categoryData.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(250.dp)
                                                .padding(top = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(R.string.enhanced_chart_no_data),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    } else {
                                        // Улучшенный пирограф категорий
                                        CategoryPieChartAdapter(
                                            data = categoryData,
                                            isIncome = !state.showExpenses,
                                            onCategorySelected = { category ->
                                                selectedCategory = category?.name
                                                category?.name?.let { categoryName ->
                                                    onNavigateToTransactions?.invoke(categoryName, state.startDate, state.endDate)
                                                }
                                            },
                                            modifier = Modifier.padding(top = 16.dp),
                                            showExpenses = state.showExpenses,
                                            onShowExpensesChange = { showExpenses ->
                                                viewModel.handleIntent(ChartIntent.ToggleExpenseView(showExpenses))
                                            }
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // Линейный график динамики
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    // Линейный график
                                    LineChartTypeSelector(
                                        selectedMode = lineChartDisplayMode,
                                        onModeSelected = { lineChartDisplayMode = it }
                                    )

                                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                                    val periodText = "${dateFormat.format(state.startDate)} - ${dateFormat.format(state.endDate)}"

                                    // Создаем данные для графика заранее для логирования
                                    val incomeData = createLineChartData(
                                        transactions = state.transactions,
                                        isIncome = true,
                                        startDate = state.startDate,
                                        endDate = state.endDate
                                    )
                                    val expenseData = createLineChartData(
                                        transactions = state.transactions,
                                        isIncome = false,
                                        startDate = state.startDate,
                                        endDate = state.endDate
                                    )
                                    
                                    // Логируем созданные данные
                                    Timber.d("Линейный график - данные для доходов: ${incomeData.size} точек")
                                    if (incomeData.isNotEmpty()) {
                                        Timber.d("Доходы: от ${DateUtils.formatDate(incomeData.first().date)} до ${DateUtils.formatDate(incomeData.last().date)}")
                                        Timber.d("Доходы: значения от ${incomeData.minOf { it.value.amount }} до ${incomeData.maxOf { it.value.amount }}")
                                    }
                                    
                                    Timber.d("Линейный график - данные для расходов: ${expenseData.size} точек")
                                    if (expenseData.isNotEmpty()) {
                                        Timber.d("Расходы: от ${DateUtils.formatDate(expenseData.first().date)} до ${DateUtils.formatDate(expenseData.last().date)}")
                                        Timber.d("Расходы: значения от ${expenseData.minOf { it.value.amount }} до ${expenseData.maxOf { it.value.amount }}")
                                    }

                                    val showIncome = lineChartDisplayMode == LineChartDisplayMode.INCOME || 
                                                    lineChartDisplayMode == LineChartDisplayMode.BOTH
                                    val showExpense = lineChartDisplayMode == LineChartDisplayMode.EXPENSE || 
                                                    lineChartDisplayMode == LineChartDisplayMode.BOTH
                                    
                                    Timber.d("Линейный график - настройки отображения: showIncome=$showIncome, showExpense=$showExpense")
                                                    
                                    EnhancedLineChart(
                                        incomeData = incomeData,
                                        expenseData = expenseData,
                                        showIncome = showIncome,
                                        showExpense = showExpense,
                                        title = "Динамика финансов",
                                        period = periodText,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            2 -> {
                                // Аналитика и инсайты
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                ) {
                                    // Карточка "Полная статистика"
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                            .clickable {
                                                // Переход на экран подробной статистики
                                                onNavigateToStatistics?.invoke(
                                                    state.transactions,
                                                    state.income ?: Money.zero(),
                                                    state.expense ?: Money.zero(),
                                                    getPeriodText(state)
                                                )
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Подробная финансовая статистика",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Text(
                                                    text = "Изучите ваши финансовые показатели",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Filled.Analytics,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // Оставляем остальной контент этой вкладки
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Скоро здесь появится подробная аналитика и финансовые инсайты",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 32.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Карточка с метриками финансового здоровья
                    FinancialHealthMetricsCard(
                        savingsRate = getSavingsRate(state),
                        averageDailyExpense = getAverageDailyExpense(state),
                        monthsOfSavings = getMonthsOfSavings(state),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    // Если нужно прокручивать к карточке
                    if (shouldScrollToSummaryCard) {
                        LaunchedEffect(true) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                                shouldScrollToSummaryCard = false
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Получение данных о расходах по категориям
 */
private fun getExpensesByCategory(
    transactions: List<Transaction>,
    startDate: Date,
    endDate: Date
): Map<String, Money> {
    // Логируем диапазон дат
    Timber.d("getExpensesByCategory: фильтрация с ${DateUtils.formatDate(startDate)} по ${DateUtils.formatDate(endDate)}")
    
    // Показываем все существующие даты до фильтрации
    val allDates = transactions.filter { it.isExpense }.map { DateUtils.formatDate(it.date) }.distinct()
    Timber.d("getExpensesByCategory: имеющиеся даты расходных транзакций: ${allDates.joinToString()}")
    
    // Дополнительно логируем метки времени для сравнения
    Timber.d("getExpensesByCategory: startDate=${startDate.time}, endDate=${endDate.time}")
    
    // Вместо прямого сравнения дат используем усеченные даты без времени
    val filteredByDate = transactions.filter { 
        it.isExpense && 
        DateUtils.truncateToDay(it.date).time >= DateUtils.truncateToDay(startDate).time && 
        DateUtils.truncateToDay(it.date).time <= DateUtils.truncateToDay(endDate).time 
    }
    
    // Логируем результаты фильтрации
    Timber.d("getExpensesByCategory: найдено ${filteredByDate.size} расходных транзакций в указанном периоде")
    
    // Если транзакции найдены, показываем их даты
    if (filteredByDate.isNotEmpty()) {
        val filteredDates = filteredByDate.map { DateUtils.formatDate(it.date) }.distinct()
        Timber.d("getExpensesByCategory: даты найденных транзакций: ${filteredDates.joinToString()}")
    }
    
    return filteredByDate
        .groupBy { it.category }
        .mapValues { (_, transactions) ->
            transactions.map { it.amount.abs() }.reduceOrNull { acc, money ->
                acc + money
            } ?: Money.zero()
        }
}

/**
 * Получение данных о доходах по категориям
 */
private fun getIncomeByCategory(
    transactions: List<Transaction>,
    startDate: Date,
    endDate: Date
): Map<String, Money> {
    // Логируем диапазон дат
    Timber.d("getIncomeByCategory: фильтрация с ${DateUtils.formatDate(startDate)} по ${DateUtils.formatDate(endDate)}")
    
    // Показываем все существующие даты до фильтрации
    val allDates = transactions.filter { !it.isExpense }.map { DateUtils.formatDate(it.date) }.distinct()
    Timber.d("getIncomeByCategory: имеющиеся даты доходных транзакций: ${allDates.joinToString()}")
    
    // Дополнительно логируем метки времени для сравнения
    Timber.d("getIncomeByCategory: startDate=${startDate.time}, endDate=${endDate.time}")
    
    // Вместо прямого сравнения дат используем усеченные даты без времени
    val filteredByDate = transactions.filter { 
        !it.isExpense && 
        DateUtils.truncateToDay(it.date).time >= DateUtils.truncateToDay(startDate).time && 
        DateUtils.truncateToDay(it.date).time <= DateUtils.truncateToDay(endDate).time 
    }
    
    // Логируем результаты фильтрации
    Timber.d("getIncomeByCategory: найдено ${filteredByDate.size} доходных транзакций в указанном периоде")
    
    // Если транзакции найдены, показываем их даты
    if (filteredByDate.isNotEmpty()) {
        val filteredDates = filteredByDate.map { DateUtils.formatDate(it.date) }.distinct()
        Timber.d("getIncomeByCategory: даты найденных транзакций: ${filteredDates.joinToString()}")
    }
    
    return filteredByDate
        .groupBy { it.category }
        .mapValues { (_, transactions) ->
            transactions.map { it.amount }.reduceOrNull { acc, money ->
                acc + money
            } ?: Money.zero()
        }
}

/**
 * Получение форматированного текста периода
 */
private fun getPeriodText(state: ChartScreenState): String {
    return com.davidbugayov.financeanalyzer.presentation.util.UiUtils.formatPeriod(
        state.periodType,
        state.startDate,
        state.endDate
    )
}

/**
 * Расчет процента сбережений
 */
private fun getSavingsRate(state: ChartScreenState): Double {
    val income = state.income ?: Money.zero()
    // Если нет дохода, возвращаем 0
    if (income.isZero()) return 0.0

    // Расчет процента сбережений
    val expense = (state.expense ?: Money.zero()).abs()
    val savings = income.minus(expense)
    return if (!income.isZero()) {
        savings.percentageOf(income)
    } else {
        0.0
    }
}

/**
 * Расчет среднедневных расходов
 */
private fun getAverageDailyExpense(state: ChartScreenState): Money {
    // Получаем количество дней в периоде
    val expense = (state.expense ?: Money.zero()).abs()
    val days = (state.endDate.time - state.startDate.time) / (24 * 60 * 60 * 1000)
    val daysInPeriod = if (days <= 0) 1 else days

    // Расчет средних ежедневных расходов
    return expense.div(daysInPeriod.toBigDecimal())
}

/**
 * Расчет количества месяцев, на которые хватит средств при текущих расходах
 */
private fun getMonthsOfSavings(state: ChartScreenState): Double {
    val averageDailyExpense = getAverageDailyExpense(state)
    val income = state.income ?: Money.zero()
    val expense = (state.expense ?: Money.zero()).abs()

    // Если нет расходов или средние расходы нулевые, возвращаем бесконечность
    if (averageDailyExpense.isZero()) return Double.POSITIVE_INFINITY

    // Получаем среднемесячные расходы (умножаем на 30 дней)
    val monthlyExpense = averageDailyExpense.times(30.toBigDecimal())

    // Расчет месяцев сбережений
    return if (!monthlyExpense.isZero()) {
        (income.minus(expense).amount.toDouble() / monthlyExpense.amount.toDouble())
    } else {
        Double.POSITIVE_INFINITY
    }
}

/**
 * Создает данные для линейного графика на основе транзакций
 */
private fun createLineChartData(
    transactions: List<Transaction>,
    isIncome: Boolean,
    startDate: Date,
    endDate: Date
): List<LineChartPoint> {
    // Логируем начало процесса создания данных
    Timber.d("createLineChartData: создаем данные для ${if(isIncome) "доходов" else "расходов"}, всего транзакций: ${transactions.size}")
    
    // Фильтруем транзакции по типу (доход/расход)
    val filteredTransactions = transactions.filter {
        (isIncome && !it.isExpense) || (!isIncome && it.isExpense)
    }

    // Логируем количество отфильтрованных транзакций
    Timber.d("createLineChartData: после фильтрации типа осталось транзакций: ${filteredTransactions.size}")
    
    // Если нет транзакций, возвращаем пустой список
    if (filteredTransactions.isEmpty()) {
        Timber.d("createLineChartData: нет ${if(isIncome) "доходных" else "расходных"} транзакций, возвращаем пустой список")
        return emptyList()
    }

    // Показываем все даты транзакций перед группировкой
    val allDates = filteredTransactions.map { DateUtils.formatDate(it.date) }.distinct()
    Timber.d("createLineChartData: даты ${if(isIncome) "доходных" else "расходных"} транзакций: ${allDates.joinToString()}")
    
    // Группируем транзакции по дате
    val aggregatedData = filteredTransactions
        .groupBy {
            // Убираем время из даты, оставляем только день
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        }
        .mapValues { (date, transactions) ->
            // Суммируем все транзакции за один день - исправляем логику для доходов
            Timber.d("Агрегирование на дату ${DateUtils.formatDate(date)}: ${transactions.size} транзакций")
            
            transactions.fold(Money.zero()) { acc, transaction ->
                val value = if (isIncome) {
                    // Для доходов берем значение как есть
                    transaction.amount
                } else {
                    // Для расходов берем абсолютное значение
                    transaction.amount.abs()
                }
                Timber.d("  Транзакция: категория=${transaction.category}, источник=${transaction.source}, сумма=${value.formatted()}")
                acc + value
            }
        }
        
    // Логируем количество дней после группировки
    Timber.d("createLineChartData: после группировки по дням получено ${aggregatedData.size} точек данных")
    
    // Проверяем, что у агрегированных данных корректные значения
    val aggrSums = aggregatedData.values.sumOf { it.amount.toDouble() }
    Timber.d("createLineChartData: сумма значений в агрегированных данных: $aggrSums")

    // Сортируем точки по дате
    return aggregatedData.entries
        .sortedBy { it.key }
        .map { (date, value) ->
            LineChartPoint(
                date = date,
                value = value
            )
        }
}

/**
 * Адаптер для вызова EnhancedCategoryPieChart с правильными параметрами
 */
@Composable
private fun CategoryPieChartAdapter(
    data: Map<String, Money>,
    isIncome: Boolean,
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier,
    showExpenses: Boolean = !isIncome,
    onShowExpensesChange: (Boolean) -> Unit = {}
) {
    // Сохраняем текущее значение типа отображения
    var currentShowExpenses by remember(showExpenses) { mutableStateOf(showExpenses) }
    
    // Логируем размер данных перед началом обработки
    Timber.d("CategoryPieChartAdapter: получено ${data.size} категорий, общая сумма: ${data.values.sumOf { it.amount.toDouble() }}")
    
    // Проверяем, есть ли нулевые суммы
    val hasZeroAmounts = data.any { it.value.isZero() }
    Timber.d("CategoryPieChartAdapter: есть нулевые суммы: $hasZeroAmounts")
    
    // Получаем нужное количество цветов для диаграммы
    val colors = com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils.PieChartUtils
        .getCategoryColors(data.size, !currentShowExpenses)
    
    // Отфильтровываем категории с нулевыми суммами
    val filteredData = data.filter { !it.value.isZero() }
    Timber.d("CategoryPieChartAdapter: после фильтрации нулевых сумм осталось ${filteredData.size} категорий")
    
    // Convert Map<String, Money> to List<PieChartData>
    val pieChartDataList = filteredData.entries.mapIndexed { index, entry ->
        val categoryName = entry.key
        val moneyValue = entry.value // Keep Money object
        // Create a simple Category object with ID based on index
        val category = if (!currentShowExpenses) {
            Category.income(name = categoryName)
        } else {
            Category.expense(name = categoryName)
        }
        
        // Используем цвет из полученной палитры вместо одного цвета для всех категорий
        val color = colors.getOrElse(index) { 
            // Если вдруг индекс вышел за пределы, генерируем новый цвет
            if (!currentShowExpenses) {
                Color(0xFF66BB6A + (index * 1000)) // Разные оттенки зеленого для доходов
            } else {
                Color(0xFFEF5350 + (index * 1000)) // Разные оттенки красного для расходов
            }
        }
        
        PieChartItemData(
            id = index.toString(),
            name = categoryName,
            money = moneyValue, // Pass Money object
            percentage = 0f, // Will be calculated later
            color = color,
            category = category,
            transactions = emptyList() // Add empty transactions list
        )
    }
    
    // Логируем размер списка до подсчета процентов
    Timber.d("CategoryPieChartAdapter: после конвертации получено ${pieChartDataList.size} элементов")
    
    // Calculate total and percentages
    if (pieChartDataList.isNotEmpty()) {
        val totalMoney = Money(pieChartDataList.sumOf { it.money.amount }) // Sum BigDecimal from Money
        Timber.d("CategoryPieChartAdapter: общая сумма для расчета процентов: ${totalMoney.formatted()}")
        
        // Create the final list with percentages
        val finalDataList = pieChartDataList.map { item ->
            val percentage = if (!totalMoney.isZero()) {
                 // Calculate percentage using BigDecimal for precision
                 (item.money.amount.toDouble() / totalMoney.amount.toDouble() * 100.0).toFloat()
             } else 0f
            item.copy(percentage = percentage) 
        }
        
        Timber.d("CategoryPieChartAdapter: финальный список содержит ${finalDataList.size} элементов с процентами")
        
        // Create the enhanced pie chart with the data
        EnhancedCategoryPieChart(
            items = finalDataList,
            selectedIndex = null,
            onSectorClick = { item ->
                if (item != null) {
                    onCategorySelected(item.category)
                } else {
                    onCategorySelected(null)
                }
            },
            modifier = modifier,
            showExpenses = currentShowExpenses,
            onShowExpensesChange = { newShowExpenses ->
                currentShowExpenses = newShowExpenses
                onShowExpensesChange(newShowExpenses)
            }
        )
    } else {
        Timber.d("CategoryPieChartAdapter: список элементов пуст, график не отображается")
        
        // Показываем сообщение "Нет доступных данных" если список пуст
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.enhanced_chart_no_data),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}