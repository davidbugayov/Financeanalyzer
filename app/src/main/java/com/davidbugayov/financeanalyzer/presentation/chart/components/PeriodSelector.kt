package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * A component that displays a row of period options for filtering transactions.
 *
 * @param periodOptions List of period options to display
 * @param selectedPeriod Currently selected period
 * @param onPeriodSelected Callback when a period is selected, provides the period name and date range
 * @param modifier Optional modifier for styling
 */
@Composable
fun PeriodSelector(
    periodOptions: List<String>,
    selectedPeriod: String,
    onPeriodSelected: (String, LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            periodOptions.forEach { period ->
                val isSelected = period == selectedPeriod
                val (startDate, endDate) = getDateRangeForPeriod(period)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .clickable { onPeriodSelected(period, startDate, endDate) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get date range for a given period.
 *
 * @param period The period name (week, month, quarter, year, all time)
 * @return Pair of start and end dates for the period
 */
private fun getDateRangeForPeriod(period: String): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()

    return when (period.lowercase()) {
        "неделя", "week" -> {
            val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
            Pair(startOfWeek, today)
        }
        "месяц", "month" -> {
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
            Pair(startOfMonth, endOfMonth)
        }
        "квартал", "quarter" -> {
            val currentMonth = today.monthValue
            val quarterStartMonth = when {
                currentMonth <= 3 -> 1
                currentMonth <= 6 -> 4
                currentMonth <= 9 -> 7
                else -> 10
            }
            val startOfQuarter = today.withMonth(quarterStartMonth).withDayOfMonth(1)
            val endOfQuarter = startOfQuarter.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth())
            Pair(startOfQuarter, endOfQuarter)
        }
        "год", "year" -> {
            val startOfYear = today.withDayOfYear(1)
            val endOfYear = today.withDayOfYear(today.lengthOfYear())
            Pair(startOfYear, endOfYear)
        }
        "все время", "all time" -> {
            Pair(today.minusYears(10), today)
        }
        else -> Pair(today.minusDays(30), today)
    }
} 