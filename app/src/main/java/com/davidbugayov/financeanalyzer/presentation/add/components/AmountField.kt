package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Поле ввода суммы транзакции с форматированием в виде "xxx xxx xxx,xx"
 */
@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    isError: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    // Используем TextFieldValue для контроля над позицией курсора
    var textFieldValue by remember { 
        mutableStateOf(
            TextFieldValue(
                text = formatAmount(amount),
                selection = TextRange(formatAmount(amount).length)
            )
        ) 
    }
    
    // Обновляем форматированный текст при изменении входного значения извне
    LaunchedEffect(amount) {
        val formattedText = formatAmount(amount)
        if (formattedText != textFieldValue.text) {
            // Сохраняем текущую позицию курсора относительно конца строки
            val distanceFromEnd = textFieldValue.text.length - textFieldValue.selection.end
            val newCursorPos = formattedText.length - distanceFromEnd
            
            textFieldValue = TextFieldValue(
                text = formattedText,
                selection = TextRange(newCursorPos.coerceIn(0, formattedText.length))
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.spacing_normal)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = dimensionResource(R.dimen.card_elevation).div(2)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            Text(
                text = stringResource(R.string.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        // Разрешаем запятую и точку, но преобразуем их в единый формат
                        val digitsOnly = newValue.text.replace(Regex("[^0-9.,]"), "")

                        // Заменяем и запятую, и точку на точку для внутреннего представления
                        val normalized = digitsOnly.replace(',', '.')

                        // Проверяем, что у нас не более одного десятичного разделителя
                        val pointCount = normalized.count { it == '.' }
                        if (pointCount <= 1) {
                            // Сохраняем текущую позицию курсора относительно конца строки
                            val distanceFromEnd = newValue.text.length - newValue.selection.end
                            
                            // Форматируем текст
                            val formattedText = formatAmount(normalized)
                            
                            // Вычисляем новую позицию курсора
                            val newCursorPos = formattedText.length - distanceFromEnd
                            
                            // Обновляем TextFieldValue с новым текстом и позицией курсора
                            textFieldValue = TextFieldValue(
                                text = formattedText,
                                selection = TextRange(newCursorPos.coerceIn(0, formattedText.length))
                            )
                            
                            // Передаем нормализованное значение наверх
                            onAmountChange(normalized)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Введите корректную сумму") }
                    } else null,
                    modifier = Modifier.weight(1f),
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

                Text(
                    text = "₽",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_medium))
                )
            }
        }
    }
}

/**
 * Форматирует строку с суммой в формат "xxx xxx xxx,xx"
 */
private fun formatAmount(input: String): String {
    if (input.isBlank()) return ""
    
    // Разделяем на целую и дробную части
    val parts = input.split('.')
    val integerPart = parts[0].replace(Regex("[^0-9]"), "")
    val decimalPart = if (parts.size > 1) parts[1].replace(Regex("[^0-9]"), "") else ""
    
    // Форматируем целую часть с разделителями групп
    val formattedInteger = if (integerPart.isNotEmpty()) {
        integerPart.reversed().chunked(3).joinToString(" ").reversed()
    } else {
        "0"
    }
    
    // Добавляем дробную часть, если она есть
    return if (decimalPart.isNotEmpty()) {
        "$formattedInteger,${decimalPart.take(2)}"
    } else if (input.contains('.')) {
        "$formattedInteger,"
    } else {
        formattedInteger
    }
}

/**
 * Преобразует отформатированную строку в объект Money
 */
fun parseFormattedAmount(formattedAmount: String, currency: Currency = Currency.RUB): Money {
    if (formattedAmount.isBlank()) {
        return Money.zero(currency)
    }
    
    // Удаляем все пробелы, символы валюты и заменяем запятую на точку
    val normalized = formattedAmount
        .replace("₽", "")
        .replace("$", "")
        .replace("€", "")
        .replace(" ", "")
        .replace(',', '.')
        .trim()
    
    return try {
        if (normalized.matches(Regex("""^\d+(\.\d+)?$"""))) {
            Money(normalized, currency)
        } else {
            Money.zero(currency)
        }
    } catch (e: NumberFormatException) {
        Money.zero(currency)
    }
} 