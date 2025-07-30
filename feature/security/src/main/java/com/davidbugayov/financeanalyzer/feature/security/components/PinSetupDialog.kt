package com.davidbugayov.financeanalyzer.feature.security.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davidbugayov.financeanalyzer.feature.security.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Диалог для настройки PIN-кода
 * @param onDismiss Обработчик закрытия диалога
 * @param onPinSet Обработчик установки PIN-кода
 * @param title Заголовок диалога
 * @param isChanging Режим изменения PIN-кода (true) или создания нового (false)
 */
@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit,
    title: String = stringResource(R.string.security_pin_setup_title),
    isChanging: Boolean = false,
) {
    var currentPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Pre-load string resources
    val pinMismatchMessage = stringResource(R.string.pin_mismatch)

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text =
                        if (isConfirming) {
                            stringResource(R.string.pin_confirm_new)
                        } else {
                            stringResource(R.string.pin_enter_new)
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Отображение точек PIN-кода
                PinDots(
                    pinLength = if (isConfirming) confirmPin.length else currentPin.length,
                    maxLength = 4,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Отображение ошибки
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Цифровая клавиатура
                PinKeyboard(
                    onNumberClick = { number ->
                        error = null
                        if (isConfirming) {
                            if (confirmPin.length < 4) {
                                confirmPin += number
                            }
                            if (confirmPin.length == 4) {
                                // Проверяем совпадение PIN-кодов
                                if (currentPin == confirmPin) {
                                    onPinSet(currentPin)
                                } else {
                                    error = pinMismatchMessage
                                    confirmPin = ""
                                }
                            }
                        } else {
                            if (currentPin.length < 4) {
                                currentPin += number
                            }
                            if (currentPin.length == 4) {
                                // Переходим к подтверждению
                                isConfirming = true
                            }
                        }
                    },
                    onBackspaceClick = {
                        error = null
                        if (isConfirming) {
                            if (confirmPin.isNotEmpty()) {
                                confirmPin = confirmPin.dropLast(1)
                            } else {
                                // Возвращаемся к вводу первого PIN-кода
                                isConfirming = false
                            }
                        } else {
                            if (currentPin.isNotEmpty()) {
                                currentPin = currentPin.dropLast(1)
                            }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text(stringResource(UiR.string.cancel))
                    }
                }
            }
        }
    }
}

/**
 * Отображение точек для визуализации введенного PIN-кода
 */
@Composable
private fun PinDots(
    pinLength: Int,
    maxLength: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .background(
                            color =
                                if (index < pinLength) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                },
                            shape = CircleShape,
                        ),
            )
        }
    }
}

/**
 * Кнопка цифровой клавиатуры
 */
@Composable
private fun PinKeyboardButton(
    text: String? = null,
    onClick: () -> Unit,
    content: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier =
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = CircleShape,
                )
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text ?: "",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
