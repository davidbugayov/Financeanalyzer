package com.davidbugayov.financeanalyzer.feature.transaction.base.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.dialogs.SourceColorPickerDialog
import timber.log.Timber

/**
 * Диалог создания пользовательского источника
 */
@Composable
fun CustomSourceDialog(
    sourceName: String,
    color: Int,
    onSourceNameChange: (String) -> Unit,
    onColorClick: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showColorPicker by remember { mutableStateOf(false) }

    // Log when the dialog is opened and closed
    LaunchedEffect(Unit) {
        Timber.d("CustomSourceDialog opened with name='$sourceName', color=$color")
    }

    DisposableEffect(Unit) {
        onDispose {
            Timber.d("CustomSourceDialog closed")
        }
    }

    AlertDialog(
        onDismissRequest = {
            Timber.d("CustomSourceDialog dismissed via outside click")
            onDismiss()
        },
        title = { Text(stringResource(R.string.add_custom_source)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = sourceName,
                    onValueChange = {
                        Timber.d("Source name changed to: '$it'")
                        onSourceNameChange(it)
                    },
                    label = { Text(stringResource(R.string.source_name)) },
                    isError = sourceName.trim().length < 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (sourceName.trim().length < 2) {
                    Text(
                        text = "Название должно содержать минимум 2 символа",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                Timber.d("Color picker clicked, showing color picker dialog")
                                showColorPicker = true
                            }
                            .padding(vertical = 8.dp),
                ) {
                    Text(
                        text = "Выберите цвет:",
                        modifier = Modifier.weight(1f),
                    )

                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { showColorPicker = true },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    Timber.d("Confirm button clicked with sourceName='$sourceName', color=$color")
                    onConfirm()
                },
                enabled = sourceName.trim().length >= 2,
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                Timber.d("Cancel button clicked")
                onDismiss()
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    if (showColorPicker) {
        SourceColorPickerDialog(
            initialColor = color,
            onColorSelected = { selectedColor ->
                Timber.d("Color selected: $selectedColor")
                onColorClick(selectedColor)
                showColorPicker = false
            },
            onDismiss = {
                Timber.d("Color picker dismissed")
                showColorPicker = false
            },
        )
    }
}
