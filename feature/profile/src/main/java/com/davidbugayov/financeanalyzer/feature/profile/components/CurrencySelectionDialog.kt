package com.davidbugayov.financeanalyzer.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.model.Currency

@Composable
fun CurrencySelectionDialog(
    currentCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выберите валюту",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp, bottom = 0.dp),
            ) {
                Currency.entries
                    .filter { it in listOf(Currency.RUB, Currency.USD, Currency.EUR, Currency.CNY) }
                    .forEach { currency ->
                        CurrencyItem(
                            currency = currency,
                            isSelected = currency == selectedCurrency,
                            onClick = { selectedCurrency = currency },
                        )
                        if (currency != Currency.CNY) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCurrencySelected(selectedCurrency)
                    onDismiss()
                },
                colors =
                    androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        properties =
            androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
            ),
    )
}

@Composable
private fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable { onClick() }
                .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CurrencyIcon(currency = currency)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = getCurrencyName(currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                )
                Text(
                    text = "${currency.code} • ${currency.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f),
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Выбрано",
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp),
            )
        }
    }
}

@Composable
private fun CurrencyIcon(currency: Currency) {
    val iconColor =
        when (currency) {
            Currency.RUB -> Color(0xFF0066CC).copy(alpha = 0.8f)
            Currency.USD -> Color(0xFF00AA00).copy(alpha = 0.8f)
            Currency.EUR -> Color(0xFF0033CC).copy(alpha = 0.8f)
            Currency.CNY -> Color(0xFFCC0000).copy(alpha = 0.8f)
            else -> Color(0xFF666666).copy(alpha = 0.8f)
        }

    Box(
        modifier =
            Modifier
                .size(40.dp)
                .background(iconColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = currency.symbol,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun getCurrencyName(currency: Currency): String {
    return when (currency) {
        Currency.RUB -> "Российский рубль"
        Currency.USD -> "Доллар США"
        Currency.EUR -> "Евро"
        Currency.CNY -> "Китайский юань"
        else -> currency.name
    }
}
