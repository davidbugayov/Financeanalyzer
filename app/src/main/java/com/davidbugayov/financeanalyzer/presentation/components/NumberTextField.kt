package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Компонент для ввода числовых значений (денежных сумм)
 *
 * @param value Текущее значение поля
 * @param onValueChange Обработчик изменения значения
 * @param label Метка поля ввода
 * @param modifier Модификатор для компонента
 * @param allowNegative Разрешить ли отрицательные значения
 */
@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    allowNegative: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Проверяем, является ли введенное значение допустимым числом
            val isValid = newValue.isEmpty() ||
                newValue.toDoubleOrNull() != null ||
                (newValue == "-" && allowNegative) ||
                (
                    newValue.matches(Regex("-?\\d*\\.?\\d*")) && (
                        allowNegative || !newValue.startsWith(
                            "-"
                        )
                        )
                    )

            if (isValid) {
                onValueChange(newValue)
            }
        },
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
} 
