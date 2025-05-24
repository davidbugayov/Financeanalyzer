package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
    // Используем TextFieldValue для контроля положения курсора
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = amount,
                selection = TextRange(amount.length) // Курсор в конце
            )
        )
    }

    // Синхронизируем внешнее значение с внутренним состоянием
    LaunchedEffect(amount) {
        if (amount != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = amount,
                selection = TextRange(amount.length) // Всегда ставим курсор в конец при обновлении извне
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onAmountChange(newValue.text)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = accentColor
            ),
            singleLine = true,
            minLines = 1,
            isError = isError,
            placeholder = {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            supportingText = {
                if (isError) Text("Введите корректную сумму")
            },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Text(
                    text = "₽",
                    style = MaterialTheme.typography.titleLarge,
                    color = accentColor
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val opButtons = listOf("+", "-", "×", "÷")
        opButtons.forEach { op ->
            OutlinedButton(
                onClick = {
                    // Добавляем оператор и обновляем значение
                    val newText = amount + op
                    onAmountChange(newText)
                    // Обновляем textFieldValue с курсором в конце
                    textFieldValue = TextFieldValue(
                        text = newText,
                        selection = TextRange(newText.length)
                    )
                },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) {
                Text(op, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
