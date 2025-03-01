package com.davidbugayov.financeanalyzer.presentation

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.davidbugayov.financeanalyzer.data.model.FinanceData
import com.davidbugayov.financeanalyzer.data.model.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceChartScreen(viewModel: SharedViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val financeData = calculateFinanceData(transactions)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
             CenterAlignedTopAppBar(
                 title = { Text("Финансовый анализ") },
             )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (financeData != null) {
                BarChartView(financeData = financeData)
            } else {
                Text("Нет данных")
            }
        }
    }
}

private fun calculateFinanceData(transactions: List<Transaction>): FinanceData? {
    if (transactions.isEmpty()) return null

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val labels = transactions.map { dateFormat.format(it.date) }.distinct()
    
    val incomes = labels.map { date ->
        transactions.filter { dateFormat.format(it.date) == date && !it.isExpense }
            .sumOf { it.amount }.toFloat()
    }
    
    val expenses = labels.map { date ->
        transactions.filter { dateFormat.format(it.date) == date && it.isExpense }
            .sumOf { it.amount }.toFloat()
    }

    return FinanceData(labels, incomes, expenses)
}

@Composable
private fun BarChartView(financeData: FinanceData) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                animateY(1000)
                setBackgroundColor(Color.WHITE)
                legend.textColor = Color.BLACK
            }
        },
        update = { chart ->
            val incomes = financeData.incomes.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }
            val expenses = financeData.expenses.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            val incomeDataSet = BarDataSet(incomes, "Доходы").apply {
                color = Color.GREEN
                valueTextColor = Color.BLACK
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f", value)
                    }
                }
            }

            val expenseDataSet = BarDataSet(expenses, "Расходы").apply {
                color = Color.RED
                valueTextColor = Color.BLACK
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f", value)
                    }
                }
            }

            val barData = BarData(incomeDataSet, expenseDataSet)
            barData.barWidth = 0.3f

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return financeData.labels[value.toInt()]
                }
            }
            chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            chart.xAxis.spaceMin = 0.5f
            chart.xAxis.spaceMax = 0.5f

            chart.data = barData
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
