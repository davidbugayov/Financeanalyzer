package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money

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
    // Внутреннее состояние для "сырого" значения, синхронизированное с amount от ViewModel
    var internalRawAmount by remember { mutableStateOf(amount) }

    // TextFieldValue для отображения в OutlinedTextField
    var textFieldValueForDisplay by remember {
        val numericAmount = amount.toDoubleOrNull()
        val initialText = if (numericAmount != null) Money(numericAmount).format() else amount
        mutableStateOf(
            TextFieldValue(text = initialText, selection = TextRange(initialText.length))
        )
    }

    var isFocused by remember { mutableStateOf(false) }

    // Синхронизация внешнего 'amount' (от ViewModel) с 'internalRawAmount'
    // и обновление 'textFieldValueForDisplay' в зависимости от фокуса
    LaunchedEffect(amount, isFocused) {
        // Если внешний 'amount' изменился, обновляем наше внутреннее "сырое" значение
        if (internalRawAmount != amount) {
            internalRawAmount = amount
        }

        val textToShow = if (isFocused) {
            internalRawAmount // В фокусе показываем "сырое" значение
        } else {
            // Не в фокусе, пытаемся отформатировать "сырое" значение
            val numericValue = internalRawAmount.toDoubleOrNull()
            if (numericValue != null) {
                Money(numericValue).format(showCurrency = false) // Форматируем, если это число, БЕЗ СИМВОЛА ВАЛЮТЫ
            } else {
                internalRawAmount // Иначе (например, выражение "100+5") показываем как есть
            }
        }

        // Обновляем textFieldValueForDisplay, только если текст действительно изменился
        if (textFieldValueForDisplay.text != textToShow) {
            textFieldValueForDisplay = TextFieldValue(
                text = textToShow,
                selection = TextRange(textToShow.length)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp)
    ) {
        OutlinedTextField(
            value = textFieldValueForDisplay,
            onValueChange = { newTextFieldValue ->
                // Когда пользователь вводит текст, это всегда "сырое" значение.
                // Удаляем пробелы из введенного текста перед обработкой.
                val rawTextWithoutSpaces = newTextFieldValue.text.replace(" ", "")

                // Обновляем internalRawAmount и вызываем onAmountChange с текстом без пробелов.
                internalRawAmount = rawTextWithoutSpaces
                onAmountChange(internalRawAmount)

                // Если поле в фокусе, немедленно обновляем textFieldValueForDisplay
                // также текстом без пробелов для мгновенной обратной связи при вводе.
                if (isFocused) {
                    // Важно сохранить позицию курсора относительно текста без пробелов
                    val originalSelection = newTextFieldValue.selection
                    val newSelectionStart = originalSelection.start - newTextFieldValue.text.substring(
                        0,
                        originalSelection.start
                    ).count { it == ' ' }
                    val newSelectionEnd = originalSelection.end - newTextFieldValue.text.substring(
                        0,
                        originalSelection.end
                    ).count { it == ' ' }

                    textFieldValueForDisplay = TextFieldValue(
                        text = rawTextWithoutSpaces,
                        selection = TextRange(
                            start = newSelectionStart.coerceIn(0, rawTextWithoutSpaces.length),
                            end = newSelectionEnd.coerceIn(0, rawTextWithoutSpaces.length)
                        )
                    )
                }
                // Если поле теряет фокус, LaunchedEffect(amount, isFocused)
                // позаботится о форматировании и обновлении textFieldValueForDisplay.
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    // При потере фокуса (isFocused стало false),
                    // LaunchedEffect(amount, isFocused) автоматически применит форматирование.
                    // При получении фокуса (isFocused стало true),
                    // LaunchedEffect(amount, isFocused) отобразит "сырое" значение.
                },
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(
            space = 6.dp,
            alignment = Alignment.CenterHorizontally
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val opButtons = listOf("+", "-", "×", "÷")
        opButtons.forEach { op ->
            OutlinedButton(
                onClick = {
                    // Работаем с internalRawAmount для добавления операторов
                    val currentTextForOps = internalRawAmount
                    val textWithPotentialSpace = if (currentTextForOps.isNotEmpty() && currentTextForOps.last().isDigit()) {
                        "$currentTextForOps "
                    } else {
                        currentTextForOps
                    }
                    val newRawText = "$textWithPotentialSpace$op "

                    internalRawAmount = newRawText // Обновляем внутреннее "сырое" значение
                    onAmountChange(newRawText) // Сообщаем ViewModel

                    // Если поле в фокусе, немедленно обновляем textFieldValueForDisplay
                    // для отображения добавленного оператора.
                    if (isFocused) {
                        textFieldValueForDisplay = TextFieldValue(
                            text = newRawText,
                            selection = TextRange(newRawText.length)
                        )
                    }
                    // Если поле не в фокусе, LaunchedEffect(amount, isFocused)
                    // обновит textFieldValueForDisplay (скорее всего, оставит newRawText, т.к. это выражение).
                },
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) {
                Text(op, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
