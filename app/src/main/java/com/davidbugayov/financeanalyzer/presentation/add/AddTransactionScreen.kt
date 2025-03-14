package com.davidbugayov.financeanalyzer.presentation.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.add.components.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.DateField
import com.davidbugayov.financeanalyzer.presentation.add.components.NoteField
import com.davidbugayov.financeanalyzer.presentation.add.components.TitleField
import com.davidbugayov.financeanalyzer.presentation.add.components.TransactionTypeSelector
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.components.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.ErrorDialog
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
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
    val state by viewModel.state.collectAsState()
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
                            viewModel.onEvent(AddTransactionEvent.ResetFields)
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
                TransactionTypeSelector(
                    isExpense = state.isExpense,
                    onTypeSelected = {
                        viewModel.onEvent(AddTransactionEvent.ToggleTransactionType)
                    }
                )
                
                TitleField(
                    title = state.title,
                    onTitleChange = { title ->
                        viewModel.onEvent(AddTransactionEvent.SetTitle(title))
                    }
                )

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.onEvent(AddTransactionEvent.SetAmount(it)) },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = state.amountError,
                    supportingText = if (state.amountError) {
                        { Text("Введите корректную сумму") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                DateField(
                    date = state.selectedDate,
                    onClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowDatePicker)
                    }
                )

                OutlinedTextField(
                    value = state.category,
                    onValueChange = { viewModel.onEvent(AddTransactionEvent.SetCategory(it)) },
                    label = { Text(stringResource(R.string.category)) },
                    readOnly = true,
                    isError = state.categoryError,
                    supportingText = if (state.categoryError) {
                        { Text("Выберите категорию") }
                    } else null,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.onEvent(AddTransactionEvent.ShowCategoryPicker) }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.select_category))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                NoteField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(AddTransactionEvent.SetNote(note))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.onEvent(AddTransactionEvent.Submit) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add_button))
                }
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
                CategoryPickerDialog(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCategory(category))
                    },
                    onCustomCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    }
                )
            }

            if (state.showCustomCategoryDialog) {
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCustomCategory(category))
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
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                        onNavigateBack()
                    },
                    onAddAnother = {
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                        // Поля уже сброшены в ViewModel при успешном добавлении транзакции
                    }
                )
            }

            state.error?.let { error ->
                ErrorDialog(
                    message = error,
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.ClearError)
                    }
                )
            }

            if (showCancelConfirmation) {
                CancelConfirmationDialog(
                    onConfirm = {
                        showCancelConfirmation = false
                        viewModel.onEvent(AddTransactionEvent.ResetFields)
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