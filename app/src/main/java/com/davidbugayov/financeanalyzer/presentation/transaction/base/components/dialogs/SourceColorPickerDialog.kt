package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseChartPalette

/**
 * Диалог выбора цвета источника
 */
@Composable
fun SourceColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_color)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.source_color_picker_padding))
            ) {
                // Горизонтальный скроллируемый ряд цветов
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.source_color_picker_item_spacing)
                    )
                ) {
                    items(ExpenseChartPalette) { composeColor ->
                        val argbColor = composeColor.toArgb()
                        ColorItem(
                            color = argbColor,
                            isSelected = argbColor == initialColor,
                            onClick = { onColorSelected(argbColor) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun ColorItem(color: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.source_color_picker_item_padding))
            .size(dimensionResource(R.dimen.source_color_picker_item_size))
            .clip(CircleShape)
            .background(Color(color))
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.source_color_picker_item_size))
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }
    }
} 
