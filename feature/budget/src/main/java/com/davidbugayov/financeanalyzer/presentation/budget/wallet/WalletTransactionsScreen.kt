package com.davidbugayov.financeanalyzer.presentation.budget.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.budget.ImportCategoriesDialog
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.components.WalletSummaryCard
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.model.WalletTransactionsEvent
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.ui.R
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import kotlin.experimental.ExperimentalTypeInference
import org.koin.androidx.compose.koinViewModel
import com.davidbugayov.financeanalyzer.ui.R as UiR

@OptIn(ExperimentalTypeInference::class)
@Composable
fun WalletTransactionsScreen(
    walletId: String,
    viewModel: WalletTransactionsViewModel = koinViewModel(),
) {
    LocalContext.current
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
    val categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.PersistentCategoriesViewModel =
        koinViewModel()
    val expenseCategories by categoriesViewModel.expenseCategories.collectAsState()

    // Обработчик нажатия на кнопку "Потратить"
    val navigateToAddTransaction: () -> Unit = {
        state.wallet?.let { wallet ->
            viewModel.onNavigateToAddTransaction(wallet.name)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.wallet?.name ?: stringResource(UiR.string.wallet_not_found),
                showBackButton = true,
                onBackClick = viewModel::onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { showImportCategoriesDialog = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(UiR.string.link_categories),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Показываем загрузку, если данные еще не получены
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(UiR.string.loading_data))
                }
            } else if (state.wallet == null) {
                // Показываем сообщение, если кошелек не найден
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(UiR.string.wallet_not_found))
                }
            } else {
                // Сохраняем кошелек в локальную переменную для умного приведения типов
                val wallet = state.wallet!!

                // Wallet summary card
                WalletSummaryCard(
                    wallet = wallet,
                    onSpendClick = navigateToAddTransaction,
                    onAddFundsClick = null, // Можно добавить позже
                    onManageClick = null, // Можно добавить позже
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                )

                // Отображение связанных категорий
                if (state.wallet?.linkedCategories?.isNotEmpty() == true) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = stringResource(UiR.string.linked_categories),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val linkedCategories = state.wallet?.linkedCategories ?: emptyList()
                        Text(
                            text = linkedCategories.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }

                // Заголовок списка транзакций
                Text(
                    text = stringResource(UiR.string.transactions_section),
                    style = MaterialTheme.typography.titleMedium,
                    modifier =
                        Modifier.padding(
                            start = 16.dp,
                            top = 24.dp,
                            end = 16.dp,
                            bottom = 8.dp,
                        ),
                )

                // Если транзакций нет, показываем сообщение
                if (state.transactions.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(UiR.string.no_transactions_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    // Список транзакций
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                categoriesViewModel = categoriesViewModel,
                                onClick = {},
                                onTransactionLongClick = { /* TODO: Handle long click */ },
                            )
                        }
                    }
                }
            }
        }

        // Диалог связывания категорий
        if (showImportCategoriesDialog) {
            ImportCategoriesDialog(
                availableCategories = expenseCategories,
                onDismiss = { showImportCategoriesDialog = false },
                onImport = { selectedCategories ->
                    // Связываем выбранные категории
                },
                title = stringResource(UiR.string.import_categories_title),
                subtitle = stringResource(UiR.string.import_categories_subtitle),
                confirmButtonText = stringResource(UiR.string.import_categories_confirm),
                preselectedCategories = state.wallet?.linkedCategories ?: emptyList(),
            )
        }

        // Отображение ошибки, если есть
        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(WalletTransactionsEvent.ClearError) },
                title = { Text(stringResource(UiR.string.error_title)) },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(WalletTransactionsEvent.ClearError) }) {
                        Text(stringResource(UiR.string.ok_button))
                    }
                },
            )
        }
    }
}