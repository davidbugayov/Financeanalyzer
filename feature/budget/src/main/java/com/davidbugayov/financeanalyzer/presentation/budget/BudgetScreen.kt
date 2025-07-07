package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetState
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.NumberTextField
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

import timber.log.Timber
import androidx.compose.material3.Surface
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.GoalProgressUseCase
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Добавляем наблюдение за жизненным циклом, чтобы обновлять список кошельков при возврате на экран
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Перезагружаем кошельки каждый раз, когда экран становится видимым
                viewModel.onEvent(BudgetEvent.LoadCategories)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                onBackClick = viewModel::onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        // Триггер ачивки - работа с доходами/бюджетом
                        Timber.d("🏆 Триггер ачивки: Пользователь работает с доходами")
                        tempIncomeAmount = "" // Сбрасываем временную сумму
                        showDistributeConfirmation = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить доход",
                        )
                    }
                    IconButton(onClick = { showPeriodSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Настройки периода",
                        )
                    }
                },
            )
        },
        floatingActionButton = { },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Список кошельков с закреплённым заголовком
                LazyColumn(
                    modifier = Modifier
                        .weight(1f),
                ) {
                    // Сводка бюджета (sticky)
                    stickyHeader {
                        BudgetSummaryHeader(state, onAddWalletClick = { viewModel.onNavigateToWalletSetup() })
                    }

                    // Заголовок списка (sticky)
                    stickyHeader {
                        Surface(
                            tonalElevation = 4.dp,
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            Text(
                                text = "Мои кошельки",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                    items(state.categories) { category ->
                        WalletCard(
                            wallet = category,
                            onClick = {
                                if (category.id.isNotEmpty()) {
                                    viewModel.onNavigateToTransactions(category.id)
                                } else {
                                    Timber.w(
                                        "ID кошелька пуст, навигация невозможна: %s",
                                        category,
                                    )
                                }
                            },
                            onSubWalletsClick = {
                                viewModel.onNavigateToSubWallets(category.id)
                            },
                            goalProgressUseCase = viewModel.goalProgressUseCase,
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
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            NumberTextField(
                                value = categoryLimit,
                                onValueChange = { categoryLimit = it },
                                label = "Лимит расходов",
                                modifier = Modifier.fillMaxWidth(),
                                allowDecimal = true,
                                isError = false,
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (categoryName.isNotBlank() && categoryLimit.isNotBlank()) {
                                    try {
                                        val limit = categoryLimit.toBigDecimal()
                                        viewModel.onEvent(
                                            BudgetEvent.AddCategory(
                                                name = categoryName,
                                                limit = Money(limit),
                                            ),
                                        )
                                        categoryName = ""
                                        categoryLimit = ""
                                        showAddCategoryDialog = false
                                    } catch (_: Exception) {
                                        // Обработка ошибки
                                    }
                                }
                            },
                            enabled = categoryName.isNotBlank() && categoryLimit.isNotBlank(),
                        ) {
                            Text("Добавить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text("Отмена")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NumberTextField(
                                value = incomeAmount,
                                onValueChange = { incomeAmount = it },
                                label = "Сумма дохода",
                                modifier = Modifier.fillMaxWidth(),
                                allowDecimal = true,
                                isError = false,
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
                            },
                            enabled = incomeAmount.toBigDecimalOrNull() != null && incomeAmount.toBigDecimalOrNull()!! > BigDecimal.ZERO,
                        ) {
                            Text("Распределить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDistributeIncomeDialog = false }) {
                            Text("Отмена")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
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
                                fontWeight = FontWeight.Medium,
                            )

                            Text(
                                text = "Баланс кошелька: ${selectedWallet!!.balance.formatForDisplay(
                                    showCurrency = true,
                                    useMinimalDecimals = true,
                                )}",
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NumberTextField(
                                value = walletAmount,
                                onValueChange = { walletAmount = it },
                                label = "Сумма",
                                modifier = Modifier.fillMaxWidth(),
                                allowDecimal = true,
                                isError = false,
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = walletAmount.toDoubleOrNull() ?: 0.0
                                // Ensure selectedWallet is not null again for safety, though checked in outer if
                                selectedWallet?.let { sw ->
                                    if (amount > 0 && walletAmount.toBigDecimalOrNull()?.let { it <= sw.balance.amount } == true) {
                                        viewModel.onEvent(
                                            BudgetEvent.SpendFromWallet(
                                                sw.id, // Corrected: pass ID
                                                Money(amount),
                                            ),
                                        )
                                        walletAmount = ""
                                        showSpendFromWalletDialog = false
                                    }
                                }
                            },
                            enabled = selectedWallet?.let { sw ->
                                walletAmount.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO && it <= sw.balance.amount } == true
                            } == true,
                        ) {
                            Text("Потратить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSpendFromWalletDialog = false }) {
                            Text("Отмена")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
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
                                fontWeight = FontWeight.Medium,
                            )

                            Text(
                                text = "Баланс: ${selectedFromWallet!!.balance.formatForDisplay(
                                    showCurrency = true,
                                    useMinimalDecimals = true,
                                )}",
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "В категорию:",
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            // Список категорий для выбора получателя
                            Column {
                                state.categories.forEach { category ->
                                    if (category.id != selectedFromWallet?.id) {
                                        Button(
                                            onClick = { selectedToWallet = category },
                                            modifier = Modifier.fillMaxWidth(),
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
                                    fontWeight = FontWeight.Medium,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                NumberTextField(
                                    value = transferAmount,
                                    onValueChange = { transferAmount = it },
                                    label = "Сумма перевода",
                                    modifier = Modifier.fillMaxWidth(),
                                    allowDecimal = true,
                                    isError = false,
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = transferAmount.toDoubleOrNull() ?: 0.0
                                // Ensure selectedFromWallet and selectedToWallet are not null again for safety
                                if (selectedFromWallet != null && selectedToWallet != null) {
                                    if (amount > 0 && transferAmount.toBigDecimalOrNull()?.let { it <= selectedFromWallet!!.balance.amount } == true) {
                                        viewModel.onEvent(
                                            BudgetEvent.TransferBetweenWallets(
                                                selectedFromWallet!!.id, // Corrected: pass ID
                                                selectedToWallet!!.id, // Corrected: pass ID
                                                Money(amount),
                                            ),
                                        )
                                        transferAmount = ""
                                        selectedToWallet = null
                                        showTransferDialog = false
                                    }
                                }
                            },
                            enabled = selectedFromWallet?.let { sfw ->
                                selectedToWallet != null && transferAmount.toBigDecimalOrNull()
                                    ?.let { it > BigDecimal.ZERO && it <= sfw.balance.amount } == true
                            } == true,
                        ) {
                            Text("Перевести")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showTransferDialog = false
                                selectedToWallet = null
                            },
                        ) {
                            Text("Отмена")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
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
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = periodDuration,
                                onValueChange = { // Проверяем, что введено число
                                    if (it.isBlank() || it.all { c -> c.isDigit() }) {
                                        periodDuration = it
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                ),
                                label = { Text("Количество дней") },
                                modifier = Modifier.fillMaxWidth(),
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
                            },
                            enabled = periodDuration.toIntOrNull() != null && periodDuration.toInt() > 0,
                        ) {
                            Text("Сохранить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPeriodSettingsDialog = false }) {
                            Text("Отмена")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
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
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            }

            // Диалог подтверждения распределения дохода
            if (showDistributeConfirmation) {
                Dialog(
                    onDismissRequest = { showDistributeConfirmation = false },
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Заголовок с иконкой
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Распределить доход",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Пояснительный текст
                            Text(
                                text = "Доход будет распределен между кошельками пропорционально их установленным лимитам.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Кнопки действий
                            Button(
                                onClick = {
                                    showDistributeConfirmation = false

                                    // Проверяем, есть ли кошельки для распределения
                                    val hasWallets = viewModel.state.value.categories.isNotEmpty()
                                    if (!hasWallets) {
                                        viewModel.onEvent(
                                            BudgetEvent.SetError(
                                                "Нет доступных кошельков для распределения",
                                            ),
                                        )
                                        return@Button
                                    }

                                    // Навигация на экран добавления транзакции
                                    viewModel.onNavigateToTransactions(state.categories.first().id)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    text = "Распределить",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = {
                                    showDistributeConfirmation = false

                                    viewModel.onNavigateToTransactions(state.categories.first().id)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Добавить без распределения",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    },
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
                                periodStartDate = newStartDate,
                            )
                            viewModel.onEvent(BudgetEvent.UpdateCategory(updatedWallet))
                            showEditWalletDialog = false
                        } else {
                            // TODO: Показать ошибку валидации
                        }
                    },
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
                        },
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun WalletCard(
    wallet: Wallet,
    onClick: () -> Unit,
    onSubWalletsClick: (() -> Unit)? = null,
    goalProgressUseCase: GoalProgressUseCase? = null,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = remember(wallet.color, wallet.name, isDarkTheme) {
        wallet.color?.takeIf { it != 0 }?.let { Color(it) }
            ?: ColorUtils.getSourceColorByName(wallet.name)
            ?: surfaceVariantColor
    }
    val contentColor = contentColorFor(backgroundColor)

    val isGoal = wallet.type.name == "GOAL"
    val percentUsed = if (isGoal && goalProgressUseCase != null) {
        goalProgressUseCase.invoke(wallet)
    } else if (wallet.limit.amount > BigDecimal.ZERO) {
        (
            wallet.spent.amount.divide(wallet.limit.amount, 4, RoundingMode.HALF_EVEN)
                .multiply(BigDecimal(100))
            )
            .setScale(0, RoundingMode.FLOOR)
            .toInt().coerceIn(0, 100)
    } else {
        0
    }

    val progressIndicatorColor = when {
        isGoal && percentUsed >= 100 -> MaterialTheme.colorScheme.primary
        isGoal -> lerpColor(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.primary, percentUsed / 100f)
        percentUsed > 90 -> MaterialTheme.colorScheme.error
        percentUsed > 70 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        percentUsed > 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val progressTrackColor = progressIndicatorColor.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = contentColor.copy(alpha = 0.2f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = wallet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                }
                IconButton(onClick = { /* TODO: show menu */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Опции",
                        tint = contentColor,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Потрачено: ${wallet.spent.formatForDisplay(
                        showCurrency = true,
                        useMinimalDecimals = true,
                    )}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                )
                Text(
                    text = "Лимит: ${wallet.limit.formatForDisplay(
                        showCurrency = true,
                        useMinimalDecimals = true,
                    )}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentUsed / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressIndicatorColor,
                trackColor = progressTrackColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isGoal) "$percentUsed% к цели" else "$percentUsed% использовано",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Кошелёк: ${wallet.balance.formatForDisplay(
                            showCurrency = true,
                            useMinimalDecimals = true,
                        )}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        ),
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                    )
                }

                onSubWalletsClick?.let { onSubWallets ->
                    OutlinedButton(
                        onClick = onSubWallets,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = contentColor,
                        ),
                        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = "Подкошельки",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
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
    onConfirm: () -> Unit,
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
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                NumberTextField(
                    value = limit,
                    onValueChange = onLimitChange,
                    label = "Лимит расходов",
                    modifier = Modifier.fillMaxWidth(),
                    allowDecimal = true,
                    isError = false,
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
                            modifier = Modifier.clickable(onClick = onShowDatePicker),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onShowDatePicker), // Кликабельно всё поле
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun BudgetSummaryHeader(state: BudgetState, onAddWalletClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = "Сводка бюджета",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Расчетный период: ${state.selectedPeriodDuration} дней",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BudgetInfoCard(
                    title = "Общий лимит",
                    value = state.totalLimit.formatForDisplay(
                        showCurrency = true,
                        useMinimalDecimals = true,
                    ),
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                BudgetInfoCard(
                    title = "Потрачено",
                    value = state.totalSpent.formatForDisplay(
                        showCurrency = true,
                        useMinimalDecimals = true,
                    ),
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                BudgetInfoCard(
                    title = "Баланс",
                    value = state.totalWalletBalance.formatForDisplay(
                        showCurrency = true,
                        useMinimalDecimals = true,
                    ),
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f),
                )
            }

            // Кнопка добавления кошелька
            Button(
                onClick = onAddWalletClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Добавить кошелек",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = lerp(start.red, end.red, fraction),
        green = lerp(start.green, end.green, fraction),
        blue = lerp(start.blue, end.blue, fraction),
        alpha = lerp(start.alpha, end.alpha, fraction),
    )
}

fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
