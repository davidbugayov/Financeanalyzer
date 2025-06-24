package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    allowNegative: Boolean = false,
    allowDecimal: Boolean = true,
    maxLength: Int = Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                val isValidNumber = if (allowDecimal) {
                    if (allowNegative) {
                        newValue.matches(Regex("^-?\\d*\\.?\\d*$"))
                    } else {
                        newValue.matches(Regex("^\\d*\\.?\\d*$"))
                    }
                } else {
                    if (allowNegative) {
                        newValue.matches(Regex("^-?\\d*$"))
                    } else {
                        newValue.matches(Regex("^\\d*$"))
                    }
                }

                if (isValidNumber || newValue.isEmpty()) {
                    onValueChange(newValue)
                }
            }
        },
        label = { Text(text = label) },
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(text = errorMessage) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
} 