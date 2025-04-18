package com.davidbugayov.financeanalyzer.presentation.wallet_transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun WalletTransactionsScreen(
    walletId: String,
    onNavigateBack: () -> Unit,
    addTransactionViewModel: AddTransactionViewModel,
    navController: NavController
) {
    // Состояние для диалога действий
    var showActionsDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Wallet Transactions Screen")
            
            // Logic for handling transactions
            // This code will use the correct setup methods
            val isIncome = true // This would be determined by your app logic
            val amount = "100" // Example amount
            val selectedCategory = object { val name = "Salary" } // Example category
            
            if (isIncome) {
                addTransactionViewModel.setupForIncomeAddition(amount, selectedCategory.name)
            } else {
                addTransactionViewModel.setupForExpenseAddition(amount, selectedCategory.name)
            }
            
            // Function to handle onTransactionClick that would show actions dialog
            val onTransactionClick = { transaction: Transaction ->
                selectedTransaction = transaction
                showActionsDialog = true
            }
        }
    }
    
    // Dialog with transaction actions (edit, delete, etc.)
    if (showActionsDialog && selectedTransaction != null) {
        AlertDialog(
            onDismissRequest = { showActionsDialog = false },
            title = { Text("Действия с транзакцией") },
            text = { Text("Выберите действие для транзакции") },
            confirmButton = {
                Button(
                    onClick = {
                        showActionsDialog = false
                        selectedTransaction?.let { transaction ->
                            // Загружаем транзакцию в ViewModel для редактирования
                            addTransactionViewModel.loadTransactionForEditing(transaction)
                            // Переходим на экран редактирования транзакции
                            navController.navigate(Screen.EditTransaction.createRoute(transaction.id))
                            Timber.d("Navigating to edit transaction with ID: ${transaction.id}")
                        }
                    }
                ) {
                    Text("Редактировать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showActionsDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
} 