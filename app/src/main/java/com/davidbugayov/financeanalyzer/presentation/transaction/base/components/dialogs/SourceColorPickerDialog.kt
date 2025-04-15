package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Диалог выбора цвета источника
 */
@Composable
fun SourceColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        0xFFE57373.toInt(), // Red
        0xFFF06292.toInt(), // Pink
        0xFFBA68C8.toInt(), // Purple
        0xFF9575CD.toInt(), // Deep Purple
        0xFF7986CB.toInt(), // Indigo
        0xFF64B5F6.toInt(), // Blue
        0xFF4FC3F7.toInt(), // Light Blue
        0xFF4DD0E1.toInt(), // Cyan
        0xFF4DB6AC.toInt(), // Teal
        0xFF81C784.toInt(), // Green
        0xFFAED581.toInt(), // Light Green
        0xFFFFD54F.toInt(), // Amber
        0xFFFFB74D.toInt(), // Orange
        0xFFA1887F.toInt()  // Brown
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет") },
        text = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Grid of colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.take(7).forEach { color ->
                        ColorItem(
                            color = color,
                            isSelected = color == initialColor,
                            onClick = { onColorSelected(color) }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.takeLast(7).forEach { color ->
                        ColorItem(
                            color = color,
                            isSelected = color == initialColor,
                            onClick = { onColorSelected(color) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun ColorItem(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color))
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }
    }
} 