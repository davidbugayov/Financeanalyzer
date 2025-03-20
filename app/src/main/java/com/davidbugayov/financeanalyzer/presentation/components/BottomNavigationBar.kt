package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R

/**
 * Улучшенная нижняя навигационная панель с анимацией и поддержкой произвольного набора пунктов
 *
 * @param modifier модификатор для управления позиционированием
 * @param visible видима ли панель
 * @param onAddClick обработчик нажатия на кнопку "Добавить"
 * @param onChartClick обработчик нажатия на кнопку "Графики"
 * @param onHistoryClick обработчик нажатия на кнопку "История"
 */
@Composable
fun AnimatedBottomNavigationBar(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onAddClick: () -> Unit = {},
    onChartClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            tonalElevation = 8.dp
        ) {
            // Высота всего контейнера с учетом отступов
            val containerHeight = 132.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(containerHeight)
            ) {
                // Размещаем кнопки в Row с равными отступами
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Используем Box с весом для левой кнопки
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        NavButton(
                            icon = Icons.Default.BarChart,
                            text = stringResource(R.string.charts),
                            onClick = onChartClick,
                            isMain = false
                        )
                    }

                    // Используем Box с весом для центральной кнопки
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        NavButton(
                            icon = Icons.Default.Add,
                            text = stringResource(R.string.add_button),
                            onClick = onAddClick,
                            isMain = true
                        )
                    }

                    // Используем Box с весом для правой кнопки
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        NavButton(
                            icon = Icons.Default.History,
                            text = stringResource(R.string.history),
                            onClick = onHistoryClick,
                            isMain = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Кнопка навигации для нижней панели
 *
 * @param icon иконка кнопки
 * @param text текст под кнопкой
 * @param onClick обработчик нажатия
 * @param isMain является ли кнопка главной (будет выделена)
 */
@Composable
private fun NavButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isMain: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isMain) {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(30.dp)
                )
            }
        } else {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(30.dp)
                )
            }
        }


        Text(
            text = text,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}