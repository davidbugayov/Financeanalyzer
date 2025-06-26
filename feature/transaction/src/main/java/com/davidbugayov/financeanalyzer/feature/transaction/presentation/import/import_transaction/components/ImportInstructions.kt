package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor

/**
 * Компонент с инструкциями по импорту
 */
@Composable
fun ImportInstructions() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.space_medium)),
        colors = CardDefaults.cardColors(
            containerColor = LocalFriendlyCardBackgroundColor.current,
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_card)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.space_medium)),
        ) {
            Text(
                text = stringResource(R.string.how_to_import),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = stringResource(R.string.import_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = dimensionResource(R.dimen.space_small)),
            )
        }
    }
}
