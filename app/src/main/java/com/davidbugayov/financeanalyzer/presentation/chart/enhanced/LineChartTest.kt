package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.components.ToggleButtonGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Тестовый экран для отображения линейного графика с примерными данными
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineChartTestScreen() {
    // Создаем тестовые данные для графика
    val incomeData = generateTestData(isIncome = true)
    val expenseData = generateTestData(isIncome = false)

    // Состояние для типа отображаемых данных
    var displayMode by remember { mutableStateOf(LineChartDisplayMode.BOTH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тест линейного графика") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Селектор типа данных
            TestLineChartTypeSelector(
                selectedMode = displayMode,
                onModeSelected = { displayMode = it }
            )

            // Линейный график
            EnhancedLineChart(
                incomeData = incomeData,
                expenseData = expenseData,
                showIncome = displayMode == LineChartDisplayMode.INCOME ||
                        displayMode == LineChartDisplayMode.BOTH,
                showExpense = displayMode == LineChartDisplayMode.EXPENSE ||
                        displayMode == LineChartDisplayMode.BOTH,
                title = "Тестовый график",
                period = "Последние 30 дней",
                onPointSelected = { point ->
                    // При выборе точки можно что-то сделать
                }
            )
        }
    }
}

/**
 * Селектор режима отображения для линейного графика
 */
@Composable
fun TestLineChartTypeSelector(
    selectedMode: LineChartDisplayMode,
    onModeSelected: (LineChartDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        ToggleButtonGroup(
            options = LineChartDisplayMode.values().map { it.title },
            selectedOptionIndex = selectedMode.ordinal,
            onOptionSelected = { index -> onModeSelected(LineChartDisplayMode.values()[index]) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Генерирует тестовые данные для графика
 */
private fun generateTestData(isIncome: Boolean): List<LineChartPoint> {
    val calendar = Calendar.getInstance()
    val format = SimpleDateFormat("dd.MM", Locale("ru"))

    // Устанавливаем дату на 30 дней назад
    calendar.add(Calendar.DAY_OF_MONTH, -30)

    // Создаем список точек
    val data = mutableListOf<LineChartPoint>()

    // Базовая сумма для точек
    val baseValue = if (isIncome) 5000.0 else 3000.0

    // Генерируем точки на каждый день
    repeat(30) { day ->
        // Добавляем один день к календарю
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val date = calendar.time

        // Генерируем случайное значение для точки с некоторой вариацией
        val variation = (Math.random() - 0.5) * baseValue * 0.5
        val value = baseValue + variation

        // Для наглядности добавляем тренд
        val trendFactor = day * (if (isIncome) 50.0 else 30.0)

        data.add(
            LineChartPoint(
                date = date,
                value = Money(value + trendFactor),
                label = format.format(date)
            )
        )
    }

    return data
}

@Preview(showBackground = true)
@Composable
fun LineChartTestScreenPreview() {
    LineChartTestScreen()
} 