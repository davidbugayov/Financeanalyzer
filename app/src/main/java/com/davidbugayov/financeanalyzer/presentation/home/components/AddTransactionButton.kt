// Этот файл будет содержать компонент кнопки добавления транзакции
// с использованием новых цветов
package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import kotlinx.coroutines.launch

/**
 * Анимированная кнопка добавления новой транзакции.
 * При нажатии на кнопку выполняется анимация уменьшения и увеличения.
 *
 * @param onClick Действие при нажатии на кнопку.
 * @param modifier Модификатор для установки размеров и других параметров.
 * @param iconSize Размер иконки (по умолчанию 32dp).
 */
@Composable
fun AddTransactionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }

    // Анимация при нажатии
    LaunchedEffect(isPressed) {
        if (isPressed) {
            launch {
                scale.animateTo(
                    targetValue = 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        } else {
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    // Используем Surface с IconButton вместо FilledIconButton
    Surface(
        modifier = modifier.scale(scale.value),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        IconButton(
            onClick = onClick,
            interactionSource = interactionSource
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_transaction),
                modifier = Modifier.size(iconSize)
            )
        }
    }
} 