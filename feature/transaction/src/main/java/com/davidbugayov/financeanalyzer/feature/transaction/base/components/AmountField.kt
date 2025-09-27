package com.davidbugayov.financeanalyzer.feature.transaction.base.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
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
 * Удаляет символ валюты из форматированной строки (в начале или в конце, с пробелами/неразрывными пробелами).
 */
private fun stripCurrencySymbol(
    formatted: String,
    currency: com.davidbugayov.financeanalyzer.shared.model.Currency,
): String {
    val symbol = currency.symbol
    val escaped = Regex.escape(symbol)
    var s = formatted.replace('\u00A0', ' ') // NBSP -> обычный пробел
    // Удаляем ведущий символ валюты с пробелами
    s = s.replace(Regex("^\\s*$escaped\\s*"), "")
    // Удаляем завершающий символ валюты с пробелами
    s = s.replace(Regex("\\s*$escaped\\s*$"), "")
    return s.trim()
}

/**
 * Поле ввода суммы транзакции с улучшенным дизайном и акцентом
 */
@Composable
fun amountField(
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
        val initialFormatted =
            if (numericAmount != null) {
                Money.fromMajor(
                    numericAmount,
                    currentCurrency,
                ).formatForDisplay()
            } else {
                amount
            }
        val money = Money.fromMajor(numericAmount ?: 0.0, currentCurrency)
        val withoutSymbol =
            if (numericAmount != null) stripCurrencySymbol(initialFormatted, money.currency) else initialFormatted
        val sep = money.currency.decimalSeparator.toString()
        val zeroSuffix = sep + "0".repeat(money.currency.decimalPlaces)
        val initialText =
            if (withoutSymbol.endsWith(zeroSuffix)) withoutSymbol.removeSuffix(zeroSuffix) else withoutSymbol
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }

    // Логируем изменения валюты для отладки и принудительно обновляем TextFieldValue
    LaunchedEffect(currentCurrency) {
        timber.log.Timber.d("AmountField: Валюта изменилась на ${currentCurrency.name}")

        // Принудительно обновляем TextFieldValue при смене валюты
        val numericValue = internalRawAmount.toDoubleOrNull()
        if (numericValue != null) {
            val moneyObject = Money.fromMajor(numericValue, currentCurrency)
            val formattedWithSymbol = moneyObject.formatForDisplay()
            val withoutSymbol = stripCurrencySymbol(formattedWithSymbol, moneyObject.currency)
            val sep = moneyObject.currency.decimalSeparator.toString()
            val zeroSuffix = sep + "0".repeat(moneyObject.currency.decimalPlaces)
            val finalText =
                if (withoutSymbol.endsWith(zeroSuffix)) withoutSymbol.removeSuffix(zeroSuffix) else withoutSymbol

            textFieldValueForDisplay = TextFieldValue(text = finalText, selection = TextRange(finalText.length))
            timber.log.Timber.d("AmountField: Принудительно обновлен TextFieldValue: '$finalText'")
        }
    }

    // Логируем создание Money объекта для отладки
    LaunchedEffect(currentCurrency) {
        val testMoney = Money.fromMajor(100.0, currentCurrency)
        timber.log.Timber.d(
            "AmountField: Тестовый Money объект: ${testMoney.formatForDisplay()}, валюта: ${testMoney.currency.name}",
        )
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
                    // Форматируем число, удаляем символ валюты (в начале или в конце) и отбрасываем .00
                    val moneyObject = Money.fromMajor(numericValue, currentCurrency)
                    val formattedWithSymbol = moneyObject.formatForDisplay()
                    timber.log.Timber.d(
                        "AmountField: Форматирование: " +
                            "numericValue=$numericValue, " +
                            "currency=${currentCurrency.name}, " +
                            "formattedWithSymbol='$formattedWithSymbol'",
                    )

                    val withoutSymbol = stripCurrencySymbol(formattedWithSymbol, moneyObject.currency)
                    val sep = moneyObject.currency.decimalSeparator.toString()
                    val zeroSuffix = sep + "0".repeat(moneyObject.currency.decimalPlaces)
                    val finalText =
                        if (withoutSymbol.endsWith(
                                zeroSuffix,
                            )
                        ) {
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

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Центральное поле суммы с одинаковыми отступами сверху и снизу
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    value = textFieldValueForDisplay,
                    onValueChange = { newTextFieldValue: TextFieldValue ->
                        // Когда пользователь вводит текст, это всегда "сырое" значение.
                        // Удаляем пробелы из введенного текста перед обработкой.
                        val rawTextWithoutSpaces = newTextFieldValue.text.replace(" ", "")

                        // Валидация: разрешаем только числа, точки, запятые и арифметические операторы
                        val validatedRaw =
                            if (rawTextWithoutSpaces.contains(Regex("[+\\-×÷]"))) {
                                // Если есть арифметические операторы, оставляем как есть (не форматируем)
                                rawTextWithoutSpaces
                            } else {
                                // Для простых чисел ограничиваем формат x.xx или x,xx
                                validateMoneyInput(rawTextWithoutSpaces)
                            }

                        // Обновляем internalRawAmount и уведомляем ViewModel сырым значением без пробелов
                        internalRawAmount = validatedRaw
                        onAmountChange(internalRawAmount)

                        // Если поле в фокусе и это не выражение — показываем отформатированный текст с разделителями
                        if (isFocused && !validatedRaw.contains(Regex("[+\\-×÷]"))) {
                            // Форматируем число с разделителями групп, без символа валюты
                            val numeric = validatedRaw.toDoubleOrNull()
                            val formattedForTyping =
                                if (numeric != null) {
                                    val moneyObject = Money.fromMajor(numeric, currentCurrency)
                                    val withSymbol =
                                        moneyObject.formatForDisplay(showCurrency = false, useMinimalDecimals = false)
                                    // Удаляем .00 только визуально при вводе, если нет дробной части в raw
                                    val sep = '.'
                                    if (!validatedRaw.contains('.') && !validatedRaw.contains(',')) {
                                        withSymbol.substringBefore(sep)
                                    } else {
                                        withSymbol
                                    }
                                } else {
                                    validatedRaw
                                }

                            // Сохраняем относительную позицию курсора, учитывая добавленные пробелы как разделители
                            val originalSelection = newTextFieldValue.selection
                            val rawBeforeCaret =
                                validatedRaw.take(originalSelection.start.coerceIn(0, validatedRaw.length))
                            // Строим формат заново для части до каретки, чтобы понять, сколько пробелов будет добавлено
                            val partialNumber = rawBeforeCaret.toDoubleOrNull()
                            val formattedBeforeCaret =
                                if (partialNumber != null) {
                                    val m = Money.fromMajor(partialNumber, currentCurrency)
                                    m.formatForDisplay(showCurrency = false, useMinimalDecimals = false)
                                        .substringBefore('.')
                                } else {
                                    rawBeforeCaret
                                }

                            val addedSpacesBefore =
                                formattedBeforeCaret.count { it == ' ' } - rawBeforeCaret.count { it == ' ' }
                            val newCaret =
                                (originalSelection.start + addedSpacesBefore).coerceIn(0, formattedForTyping.length)

                            textFieldValueForDisplay =
                                TextFieldValue(
                                    text = formattedForTyping,
                                    selection = TextRange(newCaret),
                                )
                        } else if (isFocused) {
                            // Для выражений оставляем как есть, без форматирования, но корректируем позицию
                            val originalSelection = newTextFieldValue.selection
                            textFieldValueForDisplay =
                                TextFieldValue(
                                    text = validatedRaw,
                                    selection =
                                        TextRange(
                                            start = originalSelection.start.coerceIn(0, validatedRaw.length),
                                            end = originalSelection.end.coerceIn(0, validatedRaw.length),
                                        ),
                                )
                        }
                        // Если поле теряет фокус, LaunchedEffect(amount, isFocused) выполнит финальное форматирование.
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle =
                        MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 32.sp,
                        ),
                    singleLine = true,
                    minLines = 1,
                    isError = isError,
                    placeholder = {
                        Text(
                            text = stringResource(UiR.string.amount_placeholder),
                            style =
                                MaterialTheme.typography.headlineMedium.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = 32.sp,
                                ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    },
                    supportingText = {
                        if (isError) {
                            Text(
                                text = stringResource(UiR.string.error_enter_valid_amount),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        Text(
                            text = currentCurrency.symbol,
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            color = accentColor,
                        )
                    },
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            space = 3.dp,
                            alignment = Alignment.CenterHorizontally,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // кнопки операций
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
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = accentColor,
                                    containerColor = Color.Transparent,
                                ),
                            border = null,
                        ) {
                            Text(
                                text = op,
                                style = MaterialTheme.typography.bodySmall,
                                color = accentColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
