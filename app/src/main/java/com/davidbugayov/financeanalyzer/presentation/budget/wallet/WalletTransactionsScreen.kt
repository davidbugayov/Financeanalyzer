package com.davidbugayov.financeanalyzer.presentation.budget.wallet

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.budget.ImportCategoriesDialog
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.model.WalletTransactionsEvent
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalletTransactionsScreen(
    walletId: String,
    onNavigateBack: () -> Unit,
    viewModel: WalletTransactionsViewModel = koinViewModel(),
    addTransactionViewModel: AddTransactionViewModel = koinViewModel(),
    navController: NavController = rememberNavController()
) {
    // Загружаем данные для выбранного кошелька
    LaunchedEffect(walletId) {
        viewModel.onEvent(WalletTransactionsEvent.LoadWallet(walletId))
        viewModel.onEvent(WalletTransactionsEvent.LoadTransactions(walletId))
    }
    
    // Получаем текущее состояние
    val state by viewModel.state.collectAsState()
    
    // Состояние для диалога импорта категорий
    var showImportCategoriesDialog by remember { mutableStateOf(false) }
    
    // Получаем доступ к CategoriesViewModel для импорта категорий
    val categoriesViewModel: CategoriesViewModel = koinViewModel()
    val expenseCategories by categoriesViewModel.expenseCategories.collectAsState()

    // Обработчик нажатия на кнопку "Потратить"
    val navigateToAddTransaction = {
        state.wallet?.let { wallet ->
            // Настроим экран добавления транзакции
            addTransactionViewModel.setupForIncomeAddition(
                amount = "",  // Пустая строка для поля суммы
                shouldDistribute = false  // Не распределяем автоматически
            )
            
            // Принудительно установим тип "Расход" и категорию, соответствующую кошельку
            addTransactionViewModel.onEvent(AddTransactionEvent.ToggleTransactionType) // Переключаем на расход
            addTransactionViewModel.onEvent(AddTransactionEvent.SetCategory(wallet.name))
            
            // Переходим на экран добавления транзакции
            navController.navigate(Screen.AddTransaction.route)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.wallet?.name ?: "Кошелек",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { showImportCategoriesDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Связать категории"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Показываем загрузку, если данные еще не получены
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка...")
                }
            } else if (state.wallet == null) {
                // Показываем сообщение, если кошелек не найден
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Кошелек не найден")
                }
            } else {
                // Сохраняем кошелек в локальную переменную для умного приведения типов
                val wallet = state.wallet!!
                
                // Wallet summary card
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
                                text = wallet.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )

                            // Warning icon for over budget
                            if (wallet.spent.amount > wallet.limit.amount) {
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
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Бюджет: ${wallet.limit.amount.toInt()} ₽",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar
                        val progress = (wallet.spent.amount / wallet.limit.amount).toFloat()
                        val progressColor = if (progress > 1f) Color.Red else MaterialTheme.colorScheme.primary
                        
                        LinearProgressIndicator(
                            progress = { if (progress > 1f) 1f else progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = progressColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Категории расходов
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Траты",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${wallet.spent.amount.toInt()} ₽",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Column {
                                Text(
                                    text = "Остаток",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val remaining = wallet.limit.minus(wallet.spent)
                                val remainingColor = if (remaining.isNegative()) Color.Red else MaterialTheme.colorScheme.onSurface
                                Text(
                                    text = "${remaining.amount.toInt()} ₽",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = remainingColor
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Баланс кошелька
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Баланс кошелька:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${wallet.balance.amount.toInt()} ₽",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Кнопка "Потратить из кошелька"
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = navigateToAddTransaction as () -> Unit,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Потратить из кошелька",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
                
                // Отображение связанных категорий
                if (state.wallet?.linkedCategories?.isNotEmpty() == true) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Связанные категории:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val linkedCategories = state.wallet?.linkedCategories ?: emptyList()
                        Text(
                            text = linkedCategories.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
                
                // Заголовок списка транзакций
                Text(
                    text = "Транзакции",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
                )
                
                // Если транзакций нет, показываем сообщение
                if (state.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет транзакций",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Список транзакций
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = {},
                                showDivider = true
                            )
                        }
                    }
                }
            }
        }
        
        // Диалог связывания категорий
        if (showImportCategoriesDialog) {
            ImportCategoriesDialog(
                onDismiss = { showImportCategoriesDialog = false },
                onImport = { selectedCategories ->
                    // Связываем выбранные категории с кошельком
                    viewModel.onEvent(WalletTransactionsEvent.LinkCategories(selectedCategories))
                    showImportCategoriesDialog = false
                },
                availableCategories = expenseCategories,
                title = "Связать категории",
                subtitle = "Выберите категории расходов, которые будут учитываться в этом кошельке:",
                confirmButtonText = "Связать",
                preselectedCategories = state.wallet?.linkedCategories ?: emptyList()
            )
        }
        
        // Отображение ошибки, если есть
        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(WalletTransactionsEvent.ClearError) },
                title = { Text("Ошибка") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(WalletTransactionsEvent.ClearError) }) {
                        Text("ОК")
                    }
                }
            )
        }
    }
} 