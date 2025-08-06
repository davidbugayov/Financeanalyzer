package com.davidbugayov.financeanalyzer.feature.transaction.base.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider

/**
 * Валидирует ввод денежной суммы, ограничивая формат до x.xx или x,xx
 * @param input Введенный текст
 * @return Валидированный текст
 */
private fun validateMoneyInput(input: String): String {
    if (input.isEmpty()) return input

    // Заменяем запятые на точки для единообразия
    val normalized = input.replace(',', '.')

    // Разрешаем только цифры и одну точку
    val validChars = normalized.filter { it.isDigit() || it == '.' }

    // Проверяем количество точек
    val dotCount = validChars.count { it == '.' }
    if (dotCount > 1) {
        // Если больше одной точки, оставляем только первую
        val firstDotIndex = validChars.indexOf('.')
        return validChars.substring(0, firstDotIndex + 1) +
            validChars.substring(firstDotIndex + 1).replace(".", "")
    }

    // Ограничиваем до 2 знаков после точки
    val dotIndex = validChars.indexOf('.')
    return if (dotIndex != -1 && validChars.length > dotIndex + 3) {
        validChars.substring(0, dotIndex + 3)
    } else {
        validChars
    }
}

/**
 * Поле ввода суммы транзакции с поддержкой арифметических выражений
 */
@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    isError: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val currentCurrency = CurrencyProvider.getCurrencyFlow().collectAsState().value

    // Внутреннее состояние для "сырого" значения, синхронизированное с amount от ViewModel
    var internalRawAmount by remember(currentCurrency) { mutableStateOf(amount) }

    // TextFieldValue для отображения в OutlinedTextField
    var textFieldValueForDisplay by remember(currentCurrency) {
        val numericAmount = amount.toDoubleOrNull()
        val initialText = if (numericAmount != null) Money(numericAmount, currentCurrency).format() else amount
        mutableStateOf(
            TextFieldValue(text = initialText, selection = TextRange(initialText.length)),
        )
    }

    // Логируем изменения валюты для отладки и принудительно обновляем TextFieldValue
    LaunchedEffect(currentCurrency) {
        timber.log.Timber.d("AmountField: Валюта изменилась на ${currentCurrency.name}")

        // Принудительно обновляем TextFieldValue при смене валюты
        val numericValue = internalRawAmount.toDoubleOrNull()
        if (numericValue != null) {
            val moneyObject = Money(numericValue, currentCurrency)
            val formattedWithSymbol = moneyObject.format()
            val withoutSymbol = formattedWithSymbol.substringBeforeLast(" ")
            val sep = moneyObject.currency.decimalSeparator.toString()
            val zeroSuffix = sep + "0".repeat(moneyObject.currency.decimalPlaces)
            val finalText = if (withoutSymbol.endsWith(zeroSuffix)) {
                withoutSymbol.removeSuffix(zeroSuffix)
            } else {
                withoutSymbol
            }

            textFieldValueForDisplay = TextFieldValue(
                text = finalText,
                selection = TextRange(finalText.length)
            )
            timber.log.Timber.d("AmountField: Принудительно обновлен TextFieldValue: '$finalText'")
        }
    }

    // Логируем создание Money объекта для отладки
    LaunchedEffect(currentCurrency) {
        val testMoney = Money(100.0, currentCurrency)
        timber.log.Timber.d("AmountField: Тестовый Money объект: ${testMoney.format()}, валюта: ${testMoney.currency.name}")
    }

    var isFocused by remember { mutableStateOf(false) }

    // Синхронизация внешнего 'amount' (от ViewModel) с 'internalRawAmount'
    // и обновление 'textFieldValueForDisplay' в зависимости от фокуса
    LaunchedEffect(amount, isFocused, currentCurrency) {
        // Если внешний 'amount' изменился, обновляем наше внутреннее "сырое" значение
        if (internalRawAmount != amount) {
            internalRawAmount = amount
        }

        val textToShow =
            if (isFocused) {
                internalRawAmount
            } else {
                val numericValue = internalRawAmount.toDoubleOrNull()
                if (numericValue != null) {
                    // Format number without currency symbol and drop trailing .00
                    val moneyObject = Money(numericValue, currentCurrency)
                    val formattedWithSymbol = moneyObject.format()
                    timber.log.Timber.d("AmountField: Форматирование: numericValue=$numericValue, currency=${currentCurrency.name}, formattedWithSymbol='$formattedWithSymbol'")

                    val withoutSymbol = formattedWithSymbol.substringBeforeLast(" ")
                    val sep = moneyObject.currency.decimalSeparator.toString()
                    val zeroSuffix = sep + "0".repeat(moneyObject.currency.decimalPlaces)
                    val finalText = if (withoutSymbol.endsWith(zeroSuffix)) {
                        withoutSymbol.removeSuffix(zeroSuffix)
                    } else {
                        withoutSymbol
                    }
                    timber.log.Timber.d("AmountField: Финальный текст: '$finalText'")
                    finalText
                } else {
                    internalRawAmount
                }
            }

        // Обновляем textFieldValueForDisplay, только если текст действительно изменился
        if (textFieldValueForDisplay.text != textToShow) {
            textFieldValueForDisplay =
                TextFieldValue(
                    text = textToShow,
                    selection = TextRange(textToShow.length),
                )
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
    ) {
        OutlinedTextField(
            value = textFieldValueForDisplay,
            onValueChange = { newTextFieldValue ->
                // Когда пользователь вводит текст, это всегда "сырое" значение.
                // Удаляем пробелы из введенного текста перед обработкой.
                val rawTextWithoutSpaces = newTextFieldValue.text.replace(" ", "")

                // Валидация: разрешаем только числа, точки, запятые и арифметические операторы
                val validatedText = if (rawTextWithoutSpaces.contains(Regex("[+\\-×÷]"))) {
                    // Если есть арифметические операторы, разрешаем как есть (для выражений)
                    rawTextWithoutSpaces
                } else {
                    // Для простых чисел ограничиваем формат x.xx или x,xx
                    validateMoneyInput(rawTextWithoutSpaces)
                }

                // Обновляем internalRawAmount и вызываем onAmountChange с валидированным текстом.
                internalRawAmount = validatedText
                onAmountChange(internalRawAmount)

                // Если поле в фокусе, немедленно обновляем textFieldValueForDisplay
                // с валидированным текстом для мгновенной обратной связи при вводе.
                if (isFocused) {
                    // Важно сохранить позицию курсора относительно валидированного текста
                    val originalSelection = newTextFieldValue.selection
                    val newSelectionStart =
                        originalSelection.start -
                            newTextFieldValue.text.substring(
                                0,
                                originalSelection.start,
                            ).count { it == ' ' }
                    val newSelectionEnd =
                        originalSelection.end -
                            newTextFieldValue.text.substring(
                                0,
                                originalSelection.end,
                            ).count { it == ' ' }

                    textFieldValueForDisplay =
                        TextFieldValue(
                            text = validatedText,
                            selection =
                                TextRange(
                                    start = newSelectionStart.coerceIn(0, validatedText.length),
                                    end = newSelectionEnd.coerceIn(0, validatedText.length),
                                ),
                        )
                }
                // Если поле теряет фокус, LaunchedEffect(amount, isFocused)
                // позаботится о форматировании и обновлении textFieldValueForDisplay.
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        // При потере фокуса (isFocused стало false),
                        // LaunchedEffect(amount, isFocused) автоматически применит форматирование.
                        // При получении фокуса (isFocused стало true),
                        // LaunchedEffect(amount, isFocused) отобразит "сырое" значение.
                    },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle =
                MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                ),
            singleLine = true,
            minLines = 1,
            isError = isError,
            placeholder = {
                Text(
                    text = "0",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.End,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            },
            supportingText = {
                if (isError) Text("Введите корректную сумму")
            },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Text(
                    text = currentCurrency.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    color = accentColor,
                )
            },
        )
    }
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(
                space = 6.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val opButtons = listOf("+", "-", "×", "÷")
        opButtons.forEach { op ->
            OutlinedButton(
                onClick = {
                    // Работаем с internalRawAmount для добавления операторов
                    val currentTextForOps = internalRawAmount
                    val textWithPotentialSpace =
                        if (currentTextForOps.isNotEmpty() && currentTextForOps.last().isDigit()) {
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
                        textFieldValueForDisplay =
                            TextFieldValue(
                                text = newRawText,
                                selection = TextRange(newRawText.length),
                            )
                    }
                    // Если поле не в фокусе, LaunchedEffect(amount, isFocused)
                    // обновит textFieldValueForDisplay (скорее всего, оставит newRawText, т.к. это выражение).
                },
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
            ) {
                Text(op, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
