package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R

@Composable
fun TransactionTypeSelector(
    isExpense: Boolean,
    onTypeSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isExpense,
            onClick = { onTypeSelected(true) }
        )
        Text(
            text = stringResource(R.string.expense_type),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 16.dp)
        )

        RadioButton(
            selected = !isExpense,
            onClick = { onTypeSelected(false) }
        )
        Text(
            text = stringResource(R.string.income_type),
            style = MaterialTheme.typography.bodyLarge
        )
    }
} 