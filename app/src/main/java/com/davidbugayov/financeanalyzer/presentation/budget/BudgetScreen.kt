package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.presentation.budget.components.BudgetCategoryCard
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.NumberTextField
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactions: (String) -> Unit,
    viewModel: BudgetViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Состояние диалогов
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showDistributeIncomeDialog by remember { mutableStateOf(false) }
    var showAddFundsDialog by remember { mutableStateOf(false) }
    var showSpendFromWalletDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showPeriodSettingsDialog by remember { mutableStateOf(false) }

    // Выбранные значения для диалогов
    var selectedCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    var selectedFromCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    var selectedToCategory by remember { mutableStateOf<BudgetCategory?>(null) }

    // Значения для полей ввода
    var categoryName by remember { mutableStateOf("") }
    var categoryLimit by remember { mutableStateOf("") }
    var incomeAmount by remember { mutableStateOf("") }
    var walletAmount by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var periodDuration by remember { mutableStateOf(state.selectedPeriodDuration.toString()) }

    // Состояние выпадающего меню категории
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Бюджет",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showPeriodSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Настройки периода"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDistributeIncomeDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Распределить доход") },
                text = { Text("Доход") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Сводка бюджета
                BudgetSummaryCard(
                    totalLimit = state.totalLimit,
                    totalSpent = state.totalSpent,
                    totalWalletBalance = state.totalWalletBalance,
                    periodDuration = state.selectedPeriodDuration,
                    onAddCategoryClick = { showAddCategoryDialog = true }
                )

                // Список категорий
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.categories) { category ->
                        BudgetCategoryCard(
                            category = category,
                            onCategoryClick = onNavigateToTransactions,
                            onMenuClick = {
                                selectedCategory = it
                                categoryMenuExpanded = true
                            }
                        )
                    }
                }
            }

            // Выпадающее меню для категории
            if (categoryMenuExpanded && selectedCategory != null) {
                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Добавить средства") },
                        onClick = {
                            categoryMenuExpanded = false
                            walletAmount = ""
                            showAddFundsDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Потратить") },
                        onClick = {
                            categoryMenuExpanded = false
                            walletAmount = ""
                            showSpendFromWalletDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Перевести в другую категорию") },
                        onClick = {
                            categoryMenuExpanded = false
                            selectedFromCategory = selectedCategory
                            transferAmount = ""
                            showTransferDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Сбросить период") },
                        onClick = {
                            categoryMenuExpanded = false
                            selectedCategory?.let { category ->
                                viewModel.onEvent(BudgetEvent.ResetPeriod(category.id))
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Удалить категорию") },
                        onClick = {
                            categoryMenuExpanded = false
                            selectedCategory?.let { category ->
                                viewModel.onEvent(BudgetEvent.DeleteCategory(category))
                            }
                        }
                    )
                }
            }

            // Диалог добавления категории
            if (showAddCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCategoryDialog = false },
                    title = { Text("Добавить категорию") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = categoryName,
                                onValueChange = { categoryName = it },
                                label = { Text("Название категории") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            NumberTextField(
                                value = categoryLimit,
                                onValueChange = { categoryLimit = it },
                                label = { Text("Лимит расходов") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (categoryName.isNotBlank() && categoryLimit.isNotBlank()) {
                                    val limit = categoryLimit.toDoubleOrNull() ?: 0.0
                                    if (limit > 0) {
                                        viewModel.onEvent(
                                            BudgetEvent.AddCategory(
                                                name = categoryName,
                                                limit = limit
                                            )
                                        )
                                        categoryName = ""
                                        categoryLimit = ""
                                        showAddCategoryDialog = false
                                    }
                                }
                            }
                        ) {
                            Text("Добавить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // Диалог распределения дохода
            if (showDistributeIncomeDialog) {
                AlertDialog(
                    onDismissRequest = { showDistributeIncomeDialog = false },
                    title = { Text("Распределить доход") },
                    text = {
                        Column {
                            Text(
                                text = "Доход будет распределен между кошельками пропорционально лимитам категорий",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NumberTextField(
                                value = incomeAmount,
                                onValueChange = { incomeAmount = it },
                                label = { Text("Сумма дохода") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = incomeAmount.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    viewModel.onEvent(BudgetEvent.DistributeIncome(amount))
                                    incomeAmount = ""
                                    showDistributeIncomeDialog = false
                                }
                            }
                        ) {
                            Text("Распределить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDistributeIncomeDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // Диалог добавления средств в кошелек
            if (showAddFundsDialog && selectedCategory != null) {
                AlertDialog(
                    onDismissRequest = { showAddFundsDialog = false },
                    title = { Text("Добавить средства в кошелек") },
                    text = {
                        Column {
                            Text(
                                text = "Категория: ${selectedCategory!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NumberTextField(
                                value = walletAmount,
                                onValueChange = { walletAmount = it },
                                label = { Text("Сумма") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = walletAmount.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    selectedCategory?.let { category ->
                                        viewModel.onEvent(
                                            BudgetEvent.AddFundsToWallet(
                                                categoryId = category.id,
                                                amount = amount
                                            )
                                        )
                                    }
                                    walletAmount = ""
                                    showAddFundsDialog = false
                                }
                            }
                        ) {
                            Text("Добавить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddFundsDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // Диалог списания средств из кошелька
            if (showSpendFromWalletDialog && selectedCategory != null) {
                AlertDialog(
                    onDismissRequest = { showSpendFromWalletDialog = false },
                    title = { Text("Потратить из кошелька") },
                    text = {
                        Column {
                            Text(
                                text = "Категория: ${selectedCategory!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Баланс кошелька: ${selectedCategory!!.walletBalance.toInt()} ₽",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NumberTextField(
                                value = walletAmount,
                                onValueChange = { walletAmount = it },
                                label = { Text("Сумма") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = walletAmount.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    selectedCategory?.let { category ->
                                        viewModel.onEvent(
                                            BudgetEvent.SpendFromWallet(
                                                categoryId = category.id,
                                                amount = amount
                                            )
                                        )
                                    }
                                    walletAmount = ""
                                    showSpendFromWalletDialog = false
                                }
                            }
                        ) {
                            Text("Потратить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSpendFromWalletDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // Диалог перевода между кошельками
            if (showTransferDialog && selectedFromCategory != null) {
                AlertDialog(
                    onDismissRequest = { showTransferDialog = false },
                    title = { Text("Перевод между кошельками") },
                    text = {
                        Column {
                            Text(
                                text = "Из категории: ${selectedFromCategory!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Баланс: ${selectedFromCategory!!.walletBalance.toInt()} ₽",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "В категорию:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Список категорий для выбора получателя
                            Column {
                                state.categories.forEach { category ->
                                    if (category.id != selectedFromCategory?.id) {
                                        Button(
                                            onClick = { selectedToCategory = category },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(category.name)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Отображаем поле ввода суммы только если выбрана категория-получатель
                            if (selectedToCategory != null) {
                                Text(
                                    text = "Выбрано: ${selectedToCategory!!.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                NumberTextField(
                                    value = transferAmount,
                                    onValueChange = { transferAmount = it },
                                    label = { Text("Сумма перевода") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = transferAmount.toDoubleOrNull() ?: 0.0
                                if (amount > 0 && selectedFromCategory != null && selectedToCategory != null) {
                                    viewModel.onEvent(
                                        BudgetEvent.TransferBetweenWallets(
                                            fromCategoryId = selectedFromCategory!!.id,
                                            toCategoryId = selectedToCategory!!.id,
                                            amount = amount
                                        )
                                    )
                                    transferAmount = ""
                                    selectedToCategory = null
                                    showTransferDialog = false
                                }
                            },
                            enabled = selectedToCategory != null && transferAmount.isNotBlank()
                        ) {
                            Text("Перевести")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showTransferDialog = false
                                selectedToCategory = null
                            }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // Диалог настроек периода
            if (showPeriodSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showPeriodSettingsDialog = false },
                    title = { Text("Настройки периода") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = periodDuration,
                                onValueChange = {
                                    // Принимаем только числовые значения
                                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                        periodDuration = it
                                    }
                                },
                                label = { Text("Длительность периода (дней)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.onEvent(BudgetEvent.ResetAllPeriods)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Сбросить все периоды")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val days = periodDuration.toIntOrNull() ?: 14
                                if (days > 0) {
                                    viewModel.onEvent(BudgetEvent.SetPeriodDuration(days))
                                    showPeriodSettingsDialog = false
                                }
                            }
                        ) {
                            Text("Сохранить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPeriodSettingsDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // Отображение ошибки, если есть
            state.error?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(BudgetEvent.ClearError) },
                    title = { Text("Ошибка") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onEvent(BudgetEvent.ClearError) }) {
                            Text("ОК")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BudgetSummaryCard(
    totalLimit: Double,
    totalSpent: Double,
    totalWalletBalance: Double,
    periodDuration: Int,
    onAddCategoryClick: () -> Unit
) {
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
                    text = "Сводка бюджета",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Button(onClick = onAddCategoryClick) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить категорию")
                    Text("Категория")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Расчетный период: $periodDuration дней",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Общий лимит",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${totalLimit.toInt()} ₽",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Потрачено",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${totalSpent.toInt()} ₽",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (totalSpent > totalLimit) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Баланс",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${totalWalletBalance.toInt()} ₽",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (totalWalletBalance < 0) Color.Red else MaterialTheme.colorScheme
                            .primary
                    )
                }
            }
        }
    }
} 