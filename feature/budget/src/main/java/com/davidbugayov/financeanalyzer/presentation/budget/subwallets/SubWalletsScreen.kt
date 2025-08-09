package com.davidbugayov.financeanalyzer.presentation.budget.subwallets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.budget.subwallets.components.EmptySubWalletsState
import com.davidbugayov.financeanalyzer.presentation.budget.subwallets.components.ParentWalletCard
import com.davidbugayov.financeanalyzer.presentation.budget.subwallets.components.SubWalletCard
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.davidbugayov.financeanalyzer.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubWalletsScreen(
    parentWalletId: String,
    onNavigateBack: () -> Unit,
    onNavigateToWalletSetup: () -> Unit,
    viewModel: SubWalletsViewModel = koinViewModel { parametersOf(parentWalletId) },
) {
    val state by viewModel.state.collectAsState()
    var shouldRefresh by remember { mutableStateOf(false) }

    // Эффект для обновления после возврата с экрана создания
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            shouldRefresh = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.parentWallet?.name ?: "Подкошельки",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(UiR.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shouldRefresh = true
                        onNavigateToWalletSetup()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription =
                                stringResource(
                                    UiR.string.add_subwallet,
                                ),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Информация о родительском кошельке
            item {
                state.parentWallet?.let { wallet ->
                    ParentWalletCard(
                        wallet = wallet,
                        totalSubWalletAmount = state.totalSubWalletAmount,
                    )
                }
            }

            // Список подкошельков
            if (state.subWallets.isNotEmpty()) {
                item {
                    Text(
                        text = "Подкошельки",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                items(state.subWallets) { subWallet ->
                    SubWalletCard(
                        wallet = subWallet,
                        onEdit = { viewModel.editSubWallet(subWallet.id) },
                        onDelete = { viewModel.deleteSubWallet(subWallet.id) },
                    )
                }
            } else {
                item {
                    EmptySubWalletsState(
                        onAddSubWallet = onNavigateToWalletSetup,
                    )
                }
            }
        }
    }
}