package com.davidbugayov.financeanalyzer.presentation.add

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import androidx.compose.ui.platform.LocalLayoutDirection

/**
 * Экран добавления новой транзакции.
 * Позволяет пользователю ввести данные о транзакции и сохранить их.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onTransactionAdded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val datePickerState = rememberDatePickerState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    val categories = if (isExpense) viewModel.expenseCategories else viewModel.incomeCategories
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Добавить транзакцию") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                modifier = Modifier.height(48.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateLeftPadding(layoutDirection),
                    end = innerPadding.calculateRightPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Дата
                OutlinedTextField(
                    value = dateFormat.format(selectedDate),
                    onValueChange = { },
                    label = { Text("Дата") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Выбрать дату")
                        }
                    }
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDatePicker = false
                                    datePickerState.selectedDateMillis?.let {
                                        selectedDate = Date(it)
                                    }
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Отмена")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Сумма
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newAmount ->
                        if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = newAmount
                        }
                    },
                    label = { Text("Сумма") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = !viewModel.isAmountValid(amount)
                )

                // Категория
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryDialog = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Выбрать категорию")
                        }
                    }
                )

                // Тип транзакции
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !isExpense,
                        onClick = { 
                            isExpense = false
                            category = "" // Сбрасываем категорию при смене типа
                        }
                    )
                    Text("Доход")
                    RadioButton(
                        selected = isExpense,
                        onClick = { 
                            isExpense = true
                            category = "" // Сбрасываем категорию при смене типа
                        }
                    )
                    Text("Расход")
                }

                // Заметка
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Сообщение об ошибке
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Кнопка добавления
                Button(
                    onClick = {
                        if (title.isNotEmpty() && viewModel.isAmountValid(amount) && category.isNotEmpty()) {
                            val transaction = Transaction(
                                title = title,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                category = category,
                                isExpense = isExpense,
                                date = selectedDate,
                                note = note.takeIf { it.isNotEmpty() }
                            )
                            viewModel.addTransaction(transaction, onTransactionAdded)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotEmpty() && viewModel.isAmountValid(amount) && 
                             category.isNotEmpty() && !isLoading
                ) {
                    Text("Добавить")
                }
            }
            
            // Диалог выбора категории
            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = { Text("Выберите категорию") },
                    text = {
                        Column {
                            categories.forEach { categoryName ->
                                TextButton(
                                    onClick = {
                                        category = categoryName
                                        showCategoryDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(categoryName)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }
            
            // Индикатор загрузки
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}