package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.R

/**
 * Компонент для отображения списка поддерживаемых банков
 */
@Composable
fun BanksList(onBankClick: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.space_medium)),
    ) {
        Text(
            text = stringResource(R.string.supported_banks_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.space_medium)),
        )

        // Сбербанк
        BankItem(
            name = stringResource(R.string.bank_sberbank),
            color = Color(0xFF1A9F29),
            onClick = { onBankClick("Сбербанк") },
        )

        // Тинькофф
        BankItem(
            name = stringResource(R.string.bank_tinkoff),
            color = Color(0xFFFFDD2D),
            onClick = { onBankClick("Тинькофф") },
        )

        // Альфа-Банк
        BankItem(
            name = stringResource(R.string.bank_alfabank),
            color = Color(0xFFEF3124),
            onClick = { onBankClick("Альфа-Банк") },
        )

        // Озон
        BankItem(
            name = stringResource(R.string.bank_ozon),
            color = Color(0xFF005BFF),
            onClick = { onBankClick("Озон") },
        )

        // CSV
        BankItem(
            name = stringResource(R.string.bank_csv),
            color = Color(0xFF607D8B),
            isCSV = true,
            onClick = { onBankClick("CSV") },
        )
    }
}

/**
 * Элемент списка банков
 */
@Composable
private fun BankItem(
    name: String,
    color: Color,
    isCSV: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.space_small))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_card)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.space_medium)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Цветной круг с иконкой
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.import_bank_icon_size))
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isCSV) Icons.Default.Description else Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(dimensionResource(R.dimen.import_bank_icon_inner_size)),
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.space_medium)))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
