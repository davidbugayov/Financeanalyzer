package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateField(
    date: Date,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    val formattedDate = dateFormat.format(date)

    OutlinedTextField(
        value = formattedDate,
        onValueChange = { },
        readOnly = true,
        label = { Text(stringResource(R.string.date)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.select_date_button)
            )
        },
        modifier = modifier
            .clickable(onClick = onClick)
    )
} 