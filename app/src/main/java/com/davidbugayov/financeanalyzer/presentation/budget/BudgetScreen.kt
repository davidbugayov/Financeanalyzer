package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.components.WalletAction
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.components.WalletCard
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.NumberTextField
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.math.BigDecimal
import androidx.compose.ui.draw.clip

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

    // Обновляем данные при возвращении на экран
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            // Если текущий экран - BudgetScreen, загружаем данные
            if (destination.route == Screen.Budget.route) {
                viewModel.onEvent(BudgetEvent.LoadCategories)
            }
        }
        
        // Добавляем слушателя
        navController.addOnDestinationChangedListener(listener)
        
        // Загружаем данные при первом входе
        viewModel.onEvent(BudgetEvent.LoadCategories)
        
        // Удаляем слушателя при выходе
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    
    // Подписываемся на события изменения данных транзакций
    LaunchedEffect(Unit) {
        // Получаем репозиторий транзакций через addTransactionViewModel
        addTransactionViewModel.getTransactionRepository().dataChangeEvents.collect { event ->
            // При изменении транзакций, обновляем данные
            when (event) {
                is DataChangeEvent.TransactionChanged -> {
                    Timber.d("BudgetScreen: получено событие изменения транзакции, обновляем данные")
                    viewModel.onEvent(BudgetEvent.LoadCategories)
                }
            }
        }
    }

    // Состояние диалогов
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showDistributeIncomeDialog by remember { mutableStateOf(false) }
    var showSpendFromWalletDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showPeriodSettingsDialog by remember { mutableStateOf(false) }
    var showEditWalletDialog by remember { mutableStateOf(false) }

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

    // Добавляем новый диалог для подтверждения распределения дохода
    var showDistributeConfirmation by remember { mutableStateOf(false) }
    var tempIncomeAmount by remember { mutableStateOf("") }

    // Состояния для диалога редактирования
    var editWalletName by remember { mutableStateOf("") }
    var editWalletLimit by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

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
                            imageVector = Icons.Default.CalendarMonth,
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
                // Улучшенный блок сводки бюджета
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF4F6BED).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF4F6BED),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Расчетный период: ${state.selectedPeriodDuration} дней",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BudgetInfoCard(
                                title = "Общий лимит",
                                value = "${state.totalLimit.amount.toInt()} ₽",
                                containerColor = Color(0xFF4F6BED).copy(alpha = 0.9f),
                                contentColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            BudgetInfoCard(
                                title = "Потрачено",
                                value = "${state.totalSpent.amount.toInt()} ₽",
                                containerColor = Color(0xFF66B1FF).copy(alpha = 0.9f),
                                contentColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            BudgetInfoCard(
                                title = "Баланс",
                                value = "${state.totalWalletBalance.amount.toInt()} ₽",
                                containerColor = Color(0xFF4ECDC4).copy(alpha = 0.9f),
                                contentColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Кнопка добавления кошелька
                        Button(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F6BED).copy(alpha = 0.1f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF4F6BED),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Добавить кошелек",
                                color = Color(0xFF4F6BED),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Добавляем простой заголовок
                Text(
                    text = "Мои кошельки",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Список категорий
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        // Убираем нижний отступ, так как FAB больше нет
                        // .padding(bottom = 80.dp) 
                ) {
                    items(state.categories) { category ->
                        WalletCard(
                            wallet = category,
                            onClick = { onNavigateToTransactions(category.id) }
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
                                    
                                    // Проверяем, есть ли кошельки для распределения
                                    val hasWallets = viewModel.state.value.categories.isNotEmpty()
                                    if (!hasWallets) {
                                        viewModel.onEvent(BudgetEvent.SetError("Нет доступных кошельков для распределения"))
                                        return@Button
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

            // Диалог редактирования кошелька
            if (showEditWalletDialog && selectedWallet != null) {
                val currentSelectedWallet = selectedWallet!! // Гарантированно не null
                
                val editDatePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = currentSelectedWallet.periodStartDate,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            return true // Разрешаем выбирать любую дату
                        }
                    }
                )
                
                EditWalletDialog(
                    walletName = editWalletName,
                    onNameChange = { editWalletName = it },
                    limit = editWalletLimit,
                    onLimitChange = { editWalletLimit = it },
                    selectedDateMillis = editDatePickerState.selectedDateMillis,
                    dateFormatter = dateFormatter,
                    onShowDatePicker = { showDatePickerDialog = true },
                    onDismiss = { showEditWalletDialog = false },
                    onConfirm = {
                        val newLimit = editWalletLimit.toDoubleOrNull()
                        val newStartDate = editDatePickerState.selectedDateMillis ?: currentSelectedWallet.periodStartDate
                        
                        if (editWalletName.isNotBlank() && newLimit != null) {
                            val updatedWallet = currentSelectedWallet.copy(
                                name = editWalletName,
                                limit = Money(newLimit),
                                periodStartDate = newStartDate
                            )
                            viewModel.onEvent(BudgetEvent.UpdateCategory(updatedWallet))
                            showEditWalletDialog = false
                        } else {
                            // TODO: Показать ошибку валидации
                        }
                    }
                )
                
                if (showDatePickerDialog) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePickerDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showDatePickerDialog = false }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePickerDialog = false }) {
                                Text("Отмена")
                            }
                        }
                    ) {
                        DatePicker(state = editDatePickerState)
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetInfoCard(
    title: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun WalletCard(
    wallet: Wallet,
    onClick: () -> Unit
) {
    val percentUsed = if (wallet.limit.amount > BigDecimal.ZERO) {
        ((wallet.spent.amount / wallet.limit.amount) * BigDecimal("100")).toInt().coerceIn(0, 100)
    } else 0

    val progressColor = when {
        percentUsed > 90 -> MaterialTheme.colorScheme.error
        percentUsed > 70 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        percentUsed > 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = Color(0xFF4F6BED), // Можно заменить на wallet.color
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet, // Можно заменить на другую иконку
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = wallet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { /* TODO: show menu */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Опции"
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Потрачено: ${wallet.spent.amount.toInt()} ₽",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Лимит: ${wallet.limit.amount.toInt()} ₽",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentUsed / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$percentUsed% использовано",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF4F6BED),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Кошелёк: ${wallet.balance.amount.toInt()} ₽",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF4F6BED),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EditWalletDialog(
    walletName: String,
    onNameChange: (String) -> Unit,
    limit: String,
    onLimitChange: (String) -> Unit,
    selectedDateMillis: Long?,
    dateFormatter: SimpleDateFormat,
    onShowDatePicker: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать кошелек") },
        text = {
            Column {
                OutlinedTextField(
                    value = walletName,
                    onValueChange = onNameChange,
                    label = { Text("Название кошелька") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                NumberTextField(
                    value = limit,
                    onValueChange = onLimitChange,
                    label = { Text("Лимит расходов") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Поле для выбора даты начала периода
                OutlinedTextField(
                    value = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Выберите дату",
                    onValueChange = {}, // Не редактируется напрямую
                    readOnly = true,
                    label = { Text("Дата начала периода") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Выбрать дату",
                            modifier = Modifier.clickable(onClick = onShowDatePicker)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onShowDatePicker) // Кликабельно всё поле
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
} 