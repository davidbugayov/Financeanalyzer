package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import timber.log.Timber

/**
 * Поле ввода суммы транзакции с поддержкой арифметических выражений
 */
@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    isError: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Timber.d("AmountField: Composing with amount = '%s'", amount)
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = amount, selection = TextRange(amount.length)))
    }

    // Синхронизируем textFieldValue с amount, если amount изменился извне
    LaunchedEffect(amount) {
        // Обновляем textFieldValue только если текст в нем отличается от нового amount,
        // чтобы не перезаписывать ввод пользователя без необходимости и не терять позицию курсора.
        if (textFieldValue.text != amount) {
            Timber.d("AmountField: amount prop changed to '%s', updating textFieldValue from '%s'", amount, textFieldValue.text)
            textFieldValue = TextFieldValue(text = amount, selection = TextRange(amount.length))
        }
    }

    LocalFocusManager.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    rememberCoroutineScope()
    // Состояние фокуса для OutlinedTextField
    val isTextFieldFocused = remember { mutableStateOf(false) }

    // Корневой Column для AmountField, к нему применяется bringIntoViewRequester
    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Новая логика: onAmountChange вызывается с текстом как есть из поля,
                // а ViewModel решает, что с ним делать (фильтровать, парсить и т.д.)
                // Фильтрацию на только цифры и операторы лучше оставить в AmountField, 
                // чтобы пользователь не мог ввести буквы.
                val filteredText = newValue.text.replace(Regex("[^0-9,.+\\-*/×÷]"), "")

                textFieldValue = TextFieldValue(
                    text = filteredText, // Показываем отфильтрованный текст
                    selection = newValue.selection // Сохраняем позицию курсора пользователя
                )
                onAmountChange(filteredText) // Передаем отфильтрованный текст в ViewModel
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            singleLine = true,
            isError = isError,
            supportingText = {
                if (isError) Text("Введите корректное выражение или сумму")
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isTextFieldFocused.value = focusState.isFocused
                },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = accentColor
            ),
            placeholder = {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val opButtons = listOf("+", "-", "×", "÷")
            opButtons.forEach { op ->
                OutlinedButton(
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        val opChar = when (op) {
                            "×" -> "*"
                            "÷" -> "/"
                            else -> op
                        }
                        if (textFieldValue.text.isNotEmpty() && textFieldValue.text.last().isDigit()) {
                            val newText = textFieldValue.text + opChar
                            textFieldValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                            onAmountChange(newText)
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                ) {
                    Text(op, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // Запускаем bringIntoView, когда текстовое поле получает фокус
    LaunchedEffect(isTextFieldFocused.value) {
        if (isTextFieldFocused.value) {
            bringIntoViewRequester.bringIntoView()
        }
    }
}
