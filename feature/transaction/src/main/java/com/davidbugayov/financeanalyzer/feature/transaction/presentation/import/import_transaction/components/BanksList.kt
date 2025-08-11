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
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Компонент для отображения списка поддерживаемых банков
 */
@Composable
fun BanksList(onBankClick: (String) -> Unit = {}) {
    val sberName = stringResource(UiR.string.bank_sberbank)
    val tinkoffName = stringResource(UiR.string.bank_tinkoff)
    val alfaName = stringResource(UiR.string.bank_alfabank)
    val ozonName = stringResource(UiR.string.bank_ozon)
    val csvName = stringResource(UiR.string.bank_csv)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(UiR.dimen.space_medium)),
    ) {
        // Сбербанк
        BankItem(
            name = sberName,
            color = colorResource(id = UiR.color.bank_sberbank),
            onClick = { onBankClick(sberName) },
        )

        // Тинькофф
        BankItem(
            name = tinkoffName,
            color = colorResource(id = UiR.color.bank_tinkoff),
            onClick = { onBankClick(tinkoffName) },
        )

        // Альфа-Банк
        BankItem(
            name = alfaName,
            color = colorResource(id = UiR.color.bank_alfabank),
            onClick = { onBankClick(alfaName) },
        )

        // Озон
        BankItem(
            name = ozonName,
            color = colorResource(id = UiR.color.bank_ozon),
            onClick = { onBankClick(ozonName) },
        )

        // CSV
        BankItem(
            name = csvName,
            color = colorResource(id = UiR.color.bank_csv),
            isCSV = true,
            onClick = { onBankClick(csvName) },
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
        shape = RoundedCornerShape(dimensionResource(UiR.dimen.radius_card)),
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
