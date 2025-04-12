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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.components.WalletCard
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.components.WalletAction
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.components.NumberTextField
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onNavigateToTransactions: (String) -> Unit,
    viewModel: BudgetViewModel = koinViewModel(),
    addTransactionViewModel: AddTransactionViewModel = koinViewModel()
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
    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedFromWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedToWallet by remember { mutableStateOf<Wallet?>(null) }

    // Значения для полей ввода
    var categoryName by remember { mutableStateOf("") }
    var categoryLimit by remember { mutableStateOf("") }
    var incomeAmount by remember { mutableStateOf("") }
    var walletAmount by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var periodDuration by remember { mutableStateOf(state.selectedPeriodDuration.toString()) }

    // Состояние выпадающего меню категории
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Добавляем новый диалог для подтверждения распределения дохода
    var showDistributeConfirmation by remember { mutableStateOf(false) }
    var tempIncomeAmount by remember { mutableStateOf("") }

    // Добавляем состояние для меню действий
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Бюджет",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        tempIncomeAmount = "" // Сбрасываем временную сумму
                        showDistributeConfirmation = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить доход"
                        )
                    }
                    IconButton(onClick = { showPeriodSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Настройки периода"
                        )
                    }
                }
            )
        },
        floatingActionButton = { }
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

                // Переносим заголовок "Мои кошельки" и меню сюда
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Добавляем отступы
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Мои кошельки",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    // Меню действий
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Меню действий"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Настройки периода") },
                                onClick = {
                                    expanded = false
                                    showPeriodSettingsDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Сбросить все периоды") },
                                onClick = {
                                    expanded = false
                                    viewModel.onEvent(BudgetEvent.ResetAllPeriods)
                                }
                            )
                        }
                    }
                }

                // Список категорий
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        // Убираем нижний отступ, так как FAB больше нет
                        // .padding(bottom = 80.dp) 
                ) {
                    items(state.categories) { category ->
                        WalletCard(
                            category = category,
                            onWalletClick = onNavigateToTransactions,
                            onMenuClick = { categoryFromMenu, action ->
                                when (action) {
                                    WalletAction.ADD_FUNDS -> {
                                        // Открываем диалог добавления средств
                                        selectedWallet = categoryFromMenu
                                        walletAmount = ""
                                        showAddFundsDialog = true
                                    }
                                    WalletAction.SPEND -> {
                                        // Открываем диалог списания средств
                                        selectedWallet = categoryFromMenu
                                        walletAmount = ""
                                        showSpendFromWalletDialog = true
                                    }
                                    WalletAction.TRANSFER -> {
                                        // Открываем диалог перевода средств
                                        selectedFromWallet = categoryFromMenu
                                        transferAmount = ""
                                        showTransferDialog = true
                                    }
                                    WalletAction.RESET_PERIOD -> {
                                        // Сбрасываем период
                                        viewModel.onEvent(BudgetEvent.ResetPeriod(categoryFromMenu.id))
                                    }
                                    WalletAction.DELETE -> {
                                        // Удаляем категорию
                                        viewModel.onEvent(BudgetEvent.DeleteCategory(categoryFromMenu))
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Диалог добавления новой категории бюджета
            if (showAddCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCategoryDialog = false },
                    title = { Text("Добавить новый кошелек") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = categoryName,
                                onValueChange = { categoryName = it },
                                label = { Text("Название кошелька") },
                                singleLine = true,
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
                                    try {
                                        val limit = categoryLimit.toDouble()
                                        viewModel.onEvent(
                                            BudgetEvent.AddCategory(
                                                name = categoryName,
                                                limit = Money(limit)
                                            )
                                        )
                                        categoryName = ""
                                        categoryLimit = ""
                                        showAddCategoryDialog = false
                                    } catch (e: Exception) {
                                        // Обработка ошибки
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
                                    viewModel.onEvent(BudgetEvent.DistributeIncome(Money(amount)))
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
            if (showAddFundsDialog && selectedWallet != null) {
                AlertDialog(
                    onDismissRequest = { showAddFundsDialog = false },
                    title = { Text("Добавить средства в кошелек") },
                    text = {
                        Column {
                            Text(
                                text = "Категория: ${selectedWallet!!.name}",
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
                                    selectedWallet?.let { category ->
                                        viewModel.onEvent(
                                            BudgetEvent.AddFundsToWallet(
                                                categoryId = category.id,
                                                amount = Money(amount)
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
            if (showSpendFromWalletDialog && selectedWallet != null) {
                AlertDialog(
                    onDismissRequest = { showSpendFromWalletDialog = false },
                    title = { Text("Потратить из кошелька") },
                    text = {
                        Column {
                            Text(
                                text = "Категория: ${selectedWallet!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Баланс кошелька: ${selectedWallet!!.balance} ₽",
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
                                    selectedWallet?.let { category ->
                                        viewModel.onEvent(
                                            BudgetEvent.SpendFromWallet(
                                                categoryId = category.id,
                                                amount = Money(amount)
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
            if (showTransferDialog && selectedFromWallet != null) {
                AlertDialog(
                    onDismissRequest = { showTransferDialog = false },
                    title = { Text("Перевод между кошельками") },
                    text = {
                        Column {
                            Text(
                                text = "Из категории: ${selectedFromWallet!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Баланс: ${selectedFromWallet!!.balance} ₽",
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
                                    if (category.id != selectedFromWallet?.id) {
                                        Button(
                                            onClick = { selectedToWallet = category },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(category.name)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Отображаем поле ввода суммы только если выбрана категория-получатель
                            if (selectedToWallet != null) {
                                Text(
                                    text = "Выбрано: ${selectedToWallet!!.name}",
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
                                if (amount > 0 && selectedFromWallet != null && selectedToWallet != null) {
                                    viewModel.onEvent(
                                        BudgetEvent.TransferBetweenWallets(
                                            fromCategoryId = selectedFromWallet!!.id,
                                            toCategoryId = selectedToWallet!!.id,
                                            amount = Money(amount)
                                        )
                                    )
                                    transferAmount = ""
                                    selectedToWallet = null
                                    showTransferDialog = false
                                }
                            },
                            enabled = selectedToWallet != null && transferAmount.isNotBlank()
                        ) {
                            Text("Перевести")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showTransferDialog = false
                                selectedToWallet = null
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
                            Text(
                                text = "Установите продолжительность периода в днях",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = periodDuration,
                                onValueChange = { 
                                    // Проверяем, что введено число
                                    if (it.isBlank() || it.all { c -> c.isDigit() }) {
                                        periodDuration = it
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Количество дней") },
                                modifier = Modifier.fillMaxWidth()
                            )
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

            // Диалог подтверждения распределения дохода
            if (showDistributeConfirmation) {
                Dialog(
                    onDismissRequest = { showDistributeConfirmation = false }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Заголовок с иконкой
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Распределить доход",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Пояснительный текст
                            Text(
                                text = "Доход будет распределен между кошельками пропорционально их установленным лимитам.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Кнопки действий
                            Button(
                                onClick = {
                                    showDistributeConfirmation = false
                                    
                                    // Настраиваем экран добавления транзакции для дохода
                                    addTransactionViewModel.setupForIncomeAddition(
                                        amount = tempIncomeAmount,
                                        shouldDistribute = true,
                                        lockExpenseSelection = true
                                    )
                                    
                                    // Принудительно устанавливаем режим дохода
                                    addTransactionViewModel.onEvent(AddTransactionEvent.ForceSetIncomeType)
                                    
                                    // Устанавливаем callback для автоматического распределения дохода после добавления
                                    addTransactionViewModel.onIncomeAddedCallback = { amount ->
                                        viewModel.onEvent(BudgetEvent.DistributeIncome(amount))
                                    }
                                    
                                    // Навигация на экран добавления транзакции
                                    navController.navigate(Screen.AddTransaction.route)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Распределить",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            TextButton(
                                onClick = {
                                    showDistributeConfirmation = false
                                    
                                    // Навигация без флага автораспределения
                                    addTransactionViewModel.setupForIncomeAddition(
                                        amount = tempIncomeAmount,
                                        shouldDistribute = false,
                                        lockExpenseSelection = true
                                    )
                                    
                                    // Принудительно устанавливаем режим дохода
                                    addTransactionViewModel.onEvent(AddTransactionEvent.ForceSetIncomeType)
                                    
                                    navController.navigate(Screen.AddTransaction.route)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Добавить без распределения",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetSummaryCard(
    totalLimit: Money,
    totalSpent: Money,
    totalWalletBalance: Money,
    periodDuration: Int,
    onAddCategoryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Сводка бюджета",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Расчетный период: $periodDuration дней",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Карточки с основной информацией
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Общий лимит
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Общий лимит",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalLimit.format(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Потрачено
                val spentColor = if (totalSpent > totalLimit) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = spentColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Потрачено",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (totalSpent > totalLimit) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalSpent.format(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalSpent > totalLimit) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Баланс
                val balanceColor = if (totalWalletBalance < Money.zero()) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = balanceColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Баланс",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (totalWalletBalance < Money.zero())
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalWalletBalance.format(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalWalletBalance < Money.zero())
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка добавления кошелька
            Button(
                onClick = onAddCategoryClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить кошелек"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Добавить кошелек",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
} 