package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider

/**
 * Простой список с группировкой и «аккордеоном».
 * @param groups Map<key, List<Transaction>> – уже сгруппированные транзакции (ключ отображается как title)
 */
@Composable
fun groupedTransactionList(
    groups: Map<String, List<Transaction>>, // ключ -> список
    categoriesViewModel: CategoriesViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
) {
    // Состояние свёрнутости по ключу группы (в памяти экрана)
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    val currentCurrency = CurrencyProvider.getCurrencyFlow().collectAsState().value

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        // Сортируем ключи по убыванию (предположим, ключ содержит дату/текст)
        val sortedKeys = groups.keys.sortedDescending()

        sortedKeys.forEach { key ->
            val list = groups[key].orEmpty()
            val total: Money =
                list.fold(Money.zero(currentCurrency)) { acc, tx ->
                    val convertedAmount = Money.fromMajor(tx.amount.toMajorDouble(), currentCurrency)
                    acc + convertedAmount
                }
            val expanded = expandedMap.getOrPut(key) { true }

            // Header
            item(key + "_header") {
                // Формируем заголовок группы. Для недельного ключа вида YYYY-Www показываем диапазон дат недели
                val displayKey: String = run {
                    val weekRegex = Regex("^(\\d{4})-W(\\d{1,2})$")
                    val match = weekRegex.matchEntire(key)
                    if (match != null) {
                        val (yearStr, weekStr) = match.destructured
                        val year = yearStr.toInt()
                        val week = weekStr.toInt()
                        val weekFields = WeekFields.ISO
                        val base = LocalDate.of(year, 1, 4)
                        val start = base
                            .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week.toLong())
                            .with(weekFields.dayOfWeek(), 1)
                            .with(IsoFields.WEEK_BASED_YEAR, year.toLong())
                        val end = start.plusDays(6)
                        val pattern = androidx.compose.ui.res.stringResource(UiR.string.date_pattern_short)
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val locale = context.resources.configuration.locales[0]
                        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
                        "${start.format(formatter)} - ${end.format(formatter)}"
                    } else key
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { expandedMap[key] = !expanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val tintColor =
                        if (total.isNegative()) {
                            colorResource(id = UiR.color.expense_primary)
                        } else {
                            colorResource(id = UiR.color.income_primary)
                        }

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text(
                        text = displayKey,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = total.formatForDisplay(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tintColor,
                    )
                }
            }

            if (expanded) {
                items(list) { tx ->
                    transactionItem(
                        transaction = tx,
                        categoriesViewModel = categoriesViewModel,
                        onClick = { onTransactionClick(tx) },
                        onTransactionLongClick = { /* long tap убран */ },
                        animated = false,
                        animationDelay = 0L,
                    )
                }
            }
        }
        item("fab_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
