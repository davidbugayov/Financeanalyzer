package com.davidbugayov.financeanalyzer.presentation.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val categories = if (isExpense) {
            listOf(
                CategoryItem("Продукты", Icons.Default.ShoppingCart),
                CategoryItem("Транспорт", Icons.Default.DirectionsCar),
                CategoryItem("Развлечения", Icons.Default.Movie),
                CategoryItem("Рестораны", Icons.Default.Restaurant),
                CategoryItem("Здоровье", Icons.Default.LocalHospital),
                CategoryItem("Одежда", Icons.Default.Checkroom),
                CategoryItem("Жилье", Icons.Default.Home),
                CategoryItem("Связь", Icons.Default.Phone),
                CategoryItem("Образование", Icons.Default.School),
                CategoryItem("Прочее", Icons.Default.MoreHoriz),
                CategoryItem("Другое", Icons.Default.Add)
            )
        } else {
            listOf(
                CategoryItem("Зарплата", Icons.Default.Payments),
                CategoryItem("Фриланс", Icons.Default.Computer),
                CategoryItem("Подарки", Icons.Default.CardGiftcard),
                CategoryItem("Проценты", Icons.Default.TrendingUp),
                CategoryItem("Аренда", Icons.Default.HomeWork),
                CategoryItem("Прочее", Icons.Default.MoreHoriz),
                CategoryItem("Другое", Icons.Default.Add)
            )
        }

        var showCustomCategoryDialog by remember { mutableStateOf(false) }
        var customCategory by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text(stringResource(R.string.select_category_title)) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { categoryItem ->
                        CategoryCard(
                            categoryItem = categoryItem,
                            isSelected = category == categoryItem.name,
                            onClick = {
                                if (categoryItem.name == "Другое") {
                                    showCustomCategoryDialog = true
                                } else {
                                    category = categoryItem.name
                                    showCategoryPicker = false
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )

        // Диалог для ввода пользовательской категории
        if (showCustomCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCustomCategoryDialog = false },
                title = { Text("Добавить категорию") },
                text = {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Название категории") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (customCategory.isNotBlank()) {
                                category = customCategory
                                showCustomCategoryDialog = false
                                showCategoryPicker = false
                            }
                        },
                        enabled = customCategory.isNotBlank()
                    ) {
                        Text("Добавить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomCategoryDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

data class CategoryItem(
    val name: String,
    val icon: ImageVector
)

@Composable
fun CategoryCard(
    categoryItem: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = categoryItem.icon,
                contentDescription = categoryItem.name,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = categoryItem.name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}