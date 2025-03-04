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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Экран добавления новой транзакции.
 * Позволяет пользователю ввести данные о транзакции и сохранить её.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit,
    onTransactionAdded: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    
    // Если транзакция успешно добавлена, возвращаемся назад
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            // Сбрасываем состояние успеха
            viewModel.resetSuccess()
            onTransactionAdded()
        }
    }
    
    // Состояние для полей формы
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(Date()) }
    
    // Состояние для диалогов
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    // Форматтер для отображения даты
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(R.string.add_transaction),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                modifier = Modifier.height(56.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Выбор типа транзакции (доход/расход)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.transaction_type), modifier = Modifier.weight(1f))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isExpense,
                            onClick = { isExpense = true }
                        )
                        Text(
                            text = stringResource(R.string.expense_type),
                            modifier = Modifier.clickable { isExpense = true }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !isExpense,
                            onClick = { isExpense = false }
                        )
                        Text(
                            text = stringResource(R.string.income_type),
                            modifier = Modifier.clickable { isExpense = false }
                        )
                    }
                }
                
                // Поле для ввода названия
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                // Поле для ввода суммы
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = amount.isNotEmpty() && !viewModel.isAmountValid(amount)
                )
                
                // Поле для выбора даты
                OutlinedTextField(
                    value = dateFormatter.format(selectedDate),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.date)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.select_date_button))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { showDatePicker = true }
                )
                
                // Поле для выбора категории
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.category)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryPicker = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.select_category))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { showCategoryPicker = true }
                )
                
                // Поле для ввода примечания
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_optional)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // Сообщение об ошибке
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                            viewModel.addTransaction(transaction)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotEmpty() && viewModel.isAmountValid(amount) && 
                             category.isNotEmpty() && !isLoading
                ) {
                    Text(stringResource(R.string.add_button))
                }
            }
            
            // Индикатор загрузки
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    
    // Диалог выбора даты
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Date(it)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Диалог выбора категории
    if (showCategoryPicker) {
        val categories = if (isExpense) viewModel.expenseCategories else viewModel.incomeCategories
        
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text(stringResource(R.string.select_category_title)) },
            text = {
                Column {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    category = cat
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = category == cat,
                                onClick = {
                                    category = cat
                                    showCategoryPicker = false
                                }
                            )
                            Text(
                                text = cat,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}