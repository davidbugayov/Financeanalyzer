package com.davidbugayov.financeanalyzer.presentation.budget

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun BudgetTransactionsScreen(
    categoryId: String,
    onNavigateBack: () -> Unit
) {
    // Создаем моковые данные для примера
    val category = remember {
        BudgetCategory(
            name = "Развлечения",
            limit = 10000.0,
            spent = 30471.0,
            id = categoryId
        )
    }

    val transactions = remember {
        listOf(
            Transaction(
                id = "1",
                title = "Развлечения",
                amount = -222.0,
                date = Calendar.getInstance().time,
                categoryId = "entertainment",
                category = "Развлечения",
                isExpense = true,
                source = "Бюджет: Развлечения",
                sourceColor = 0xFF9C27B0.toInt(),
                note = ""
            ),
            Transaction(
                id = "2",
                title = "Рестораны",
                amount = -612.0,
                date = Calendar.getInstance().time,
                categoryId = "restaurants",
                category = "Рестораны",
                isExpense = true,
                source = "Т-Банк",
                sourceColor = 0xFFFFDD2D.toInt(),
                note = "Примечание к транзакции"
            ),
            Transaction(
                id = "3",
                title = "Рестораны",
                amount = -2051.0,
                date = Calendar.getInstance().time,
                categoryId = "restaurants",
                category = "Рестораны",
                isExpense = true,
                source = "Сбер",
                sourceColor = 0xFF21A038.toInt(),
                note = ""
            )
        )
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Все траты", "Прямые траты", "Связанные траты")

    Scaffold(
        topBar = {
            AppTopBar(
                title = category.name,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Budget summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )

                        // Warning icon for over budget
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Бюджет: ${category.limit.toInt()} ₽",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar (red for over budget)
                    LinearProgressIndicator(
                        progress = { 1f }, // Полностью заполнен, так как перерасход
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Категории расходов
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Прямые траты",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "0 ₽",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column {
                            Text(
                                text = "Связанные траты",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${category.spent.toInt()} ₽",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column {
                            Text(
                                text = "Остаток",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "0 ₽",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Red
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Общие расходы
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Общие расходы",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "${category.spent.toInt()} ₽",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "304.7% от бюджета",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            // Transactions list
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(transaction.sourceColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = transaction.category.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Transaction info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${transaction.source} ${dateFormat.format(transaction.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (transaction.note?.isNotBlank() == true) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Amount
        Text(
            text = formatCurrency(transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            fontWeight = FontWeight.Medium
        )
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

// Простая функция для форматирования валюты
private fun formatCurrency(amount: Double): String {
    return "${amount.toInt()} ₽"
} 