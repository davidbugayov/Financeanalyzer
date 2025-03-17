package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Компонент для отображения индикатора загрузки.
 * Центрирует CircularProgressIndicator в доступном пространстве.
 *
 * @param modifier Модификатор для настройки внешнего вида
 * @param height Фиксированная высота компонента в dp (0 для автоматической высоты)
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    height: Int = 0
) {
    Box(
        modifier = if (height > 0) {
            modifier.height(height.dp)
        } else {
            modifier
        },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
} 