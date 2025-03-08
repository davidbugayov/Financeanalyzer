package com.davidbugayov.financeanalyzer.presentation.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.add.components.AddTransactionButton
import com.davidbugayov.financeanalyzer.presentation.add.components.AmountField
import com.davidbugayov.financeanalyzer.presentation.add.components.CategoryField
import com.davidbugayov.financeanalyzer.presentation.add.components.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.DateField
import com.davidbugayov.financeanalyzer.presentation.add.components.NoteField
import com.davidbugayov.financeanalyzer.presentation.add.components.TitleField
import com.davidbugayov.financeanalyzer.presentation.add.components.TransactionTypeSelector
import com.davidbugayov.financeanalyzer.presentation.add.dialogs.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.add.dialogs.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.dialogs.ErrorDialog
import com.davidbugayov.financeanalyzer.presentation.add.dialogs.SuccessDialog
import com.davidbugayov.financeanalyzer.presentation.add.event.AddTransactionEvent
import org.koin.androidx.compose.koinViewModel

/**
 * Экран добавления новой транзакции
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state.collectAsState().value
    var showCancelConfirmation by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.title.isNotBlank() || state.amount.isNotBlank() || state.category.isNotBlank() || state.note.isNotBlank()) {
                            showCancelConfirmation = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Выбор типа транзакции (доход/расход)
                TransactionTypeSelector(
                    isExpense = state.isExpense,
                    onTypeSelected = { isExpense ->
                        viewModel.onEvent(AddTransactionEvent.SetExpenseType(isExpense))
                    }
                )
                
                // Поле для ввода названия
                TitleField(
                    title = state.title,
                    onTitleChange = { title ->
                        viewModel.onEvent(AddTransactionEvent.SetTitle(title))
                    }
                )
                
                // Поле для ввода суммы
                AmountField(
                    amount = state.amount,
                    onAmountChange = { amount ->
                        viewModel.onEvent(AddTransactionEvent.SetAmount(amount))
                    }
                )
                
                // Поле для выбора категории
                CategoryField(
                    category = state.category,
                    onClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCategoryPicker)
                    }
                )

                // Поле для выбора даты
                DateField(
                    date = state.selectedDate,
                    onClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowDatePicker)
                    }
                )
                
                // Поле для ввода примечания
                NoteField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(AddTransactionEvent.SetNote(note))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка добавления транзакции
                AddTransactionButton(
                    enabled = state.title.isNotBlank() && state.amount.isNotBlank() && state.category.isNotBlank() && !state.isLoading,
                    onClick = {
                        val amount = try {
                            state.amount.toDouble()
                        } catch (e: Exception) {
                            0.0
                        }

                        val transaction = Transaction(
                            id = 0, // ID будет сгенерирован базой данных
                            title = state.title,
                            amount = amount,
                            category = state.category,
                            note = state.note,
                            isExpense = state.isExpense,
                            date = state.selectedDate
                        )

                        viewModel.onEvent(AddTransactionEvent.AddTransaction(transaction))
                    }
                )
            }
            
            // Индикатор загрузки
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Диалоги
            if (state.showDatePicker) {
                DatePickerDialog(
                    initialDate = state.selectedDate,
                    onDateSelected = { date ->
                        viewModel.onEvent(AddTransactionEvent.SetDate(date))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideDatePicker)
                    }
                )
            }

            if (state.showCategoryPicker) {
                val categoriesState = if (state.isExpense) {
                    viewModel.expenseCategories.collectAsState().value
                } else {
                    viewModel.incomeCategories.collectAsState().value
                }
                
                CategoryPickerDialog(
                    categories = categoriesState,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCategory(category))
                    },
                    onCustomCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    }
                )
            }

            if (state.showCustomCategoryDialog) {
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { categoryText ->
                        viewModel.onEvent(AddTransactionEvent.SetCustomCategory(categoryText))
                    },
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.AddCustomCategory(state.customCategory))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCustomCategoryDialog)
                    }
                )
            }

            if (state.isSuccess) {
                SuccessDialog(
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.ResetSuccess)
                        onNavigateBack()
                    },
                    onAddAnother = {
                        viewModel.onEvent(AddTransactionEvent.ResetSuccess)
                    }
                )
            }

            if (state.error != null) {
                ErrorDialog(
                    errorMessage = state.error,
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.ResetError)
                    }
                )
            }

            if (showCancelConfirmation) {
                CancelConfirmationDialog(
                    onConfirm = {
                        showCancelConfirmation = false
                        onNavigateBack()
                    },
                    onDismiss = {
                        showCancelConfirmation = false
                    }
                )
            }
        }
    }
}