package com.davidbugayov.financeanalyzer.presentation

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.data.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: SharedViewModel,
    onTransactionAdded: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Income") }
    var isAmountValid by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Если нужно, добавьте тулбар здесь
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    label = { Text("Дата (MM/DD/YYYY)") },
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
                                        date = convertMillisToDate(it)
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

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newAmount ->
                        if (newAmount.matches(Regex("^[0-9]+(\\.[0-9]+)?$"))) {
                            amount = newAmount
                            isAmountValid = true
                        } else {
                            amount = newAmount
                            isAmountValid = false
                        }
                    },
                    label = { Text("Сумма") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isAmountValid,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        if (!isAmountValid) {
                            Text(
                                text = "Неправильный формат суммы.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    RadioButton(
                        selected = type == "Income",
                        onClick = { type = "Income" }
                    )
                    Text(text = "Доход", modifier = Modifier.padding(8.dp))
                    RadioButton(
                        selected = type == "Expense",
                        onClick = { type = "Expense" }
                    )
                    Text(text = "Расход", modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (date.isNotEmpty() && isAmountValid) {
                            val transaction = Transaction(
                                date = date,
                                description = description,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                type = type
                            )
                            viewModel.addTransaction(transaction)
                            onTransactionAdded()
                        } else {
                            isAmountValid = amount.matches(Regex("^[0-9]+(\\.[0-9]+)?$"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить транзакцию")
                }
            }
        }
    )
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
