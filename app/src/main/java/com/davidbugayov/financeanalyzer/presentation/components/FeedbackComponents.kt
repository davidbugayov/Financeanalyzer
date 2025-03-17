package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Типы уведомлений для обратной связи
 */
enum class FeedbackType {

    SUCCESS, ERROR, WARNING, INFO
}

/**
 * Компонент для отображения уведомлений с анимацией
 */
@Composable
fun FeedbackMessage(
    message: String,
    type: FeedbackType,
    visible: Boolean,
    onDismiss: () -> Unit,
    duration: Long = 3000L,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = when (type) {
                FeedbackType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                FeedbackType.ERROR -> MaterialTheme.colorScheme.errorContainer
                FeedbackType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                FeedbackType.INFO -> MaterialTheme.colorScheme.secondaryContainer
            },
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                color = when (type) {
                    FeedbackType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                    FeedbackType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                    FeedbackType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                    FeedbackType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }

        LaunchedEffect(visible) {
            if (visible) {
                delay(duration)
                onDismiss()
            }
        }
    }
}

/**
 * Компонент для отображения кнопки с эффектом нажатия
 */
@Composable
fun PressEffectButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/**
 * Расширение для добавления эффекта нажатия к любому компоненту
 */
fun Modifier.clickableWithPressEffect(
    onClick: () -> Unit,
    onPressChanged: (Boolean) -> Unit
) = this.clickable(
    onClick = {
        onPressChanged(true)
        onClick()
        onPressChanged(false)
    }
) 