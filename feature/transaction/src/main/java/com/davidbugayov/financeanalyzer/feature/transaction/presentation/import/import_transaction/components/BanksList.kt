package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Компонент для отображения списка поддерживаемых банков
 */
@Composable
fun BanksList(onBankClick: (String) -> Unit = {}) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.space_medium)),
    ) {
        // Сбербанк
        BankItem(
            name = stringResource(UiR.string.bank_sberbank),
            color = colorResource(id = R.color.bank_sberbank),
            onClick = { onBankClick("Сбербанк") },
        )

        // Тинькофф
        BankItem(
            name = stringResource(UiR.string.bank_tinkoff),
            color = colorResource(id = R.color.bank_tinkoff),
            onClick = { onBankClick("Тинькофф") },
        )

        // Альфа-Банк
        BankItem(
            name = stringResource(UiR.string.bank_alfabank),
            color = colorResource(id = R.color.bank_alfabank),
            onClick = { onBankClick("Альфа-Банк") },
        )

        // Озон
        BankItem(
            name = stringResource(R.string.bank_ozon),
            color = colorResource(id = R.color.bank_ozon),
            onClick = { onBankClick("Озон") },
        )

        // CSV
        BankItem(
            name = stringResource(R.string.bank_csv),
            color = colorResource(id = R.color.bank_csv),
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.5.dp, color = color.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_card)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Цветной круг с иконкой
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(color),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isCSV) Icons.Default.Description else Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
