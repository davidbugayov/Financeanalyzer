package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseChartPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_color)) },
        text = {
            Column {
                FlowRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            dimensionResource(R.dimen.spacing_medium),
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            dimensionResource(R.dimen.spacing_medium),
                        ),
                ) {
                    ExpenseChartPalette.forEach { composeColor ->
                        val argbColor = composeColor.toArgb()
                        Box(
                            modifier =
                                Modifier
                                    .size(dimensionResource(R.dimen.color_picker_item_size))
                                    .clip(CircleShape)
                                    .background(composeColor)
                                    .border(
                                        width = dimensionResource(R.dimen.border_width_medium),
                                        color =
                                            if (argbColor == initialColor) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.Transparent
                                            },
                                        shape = CircleShape,
                                    )
                                    .clickable { onColorSelected(argbColor) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(UiR.string.close))
            }
        },
    )
}
