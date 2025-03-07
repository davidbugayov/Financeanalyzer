package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R

/**
 * Компонент для отображения индикатора загрузки.
 * Центрирует CircularProgressIndicator в доступном пространстве.
 *
 * @param modifier Модификатор для настройки внешнего вида
 * @param height Фиксированная высота компонента в dp (0 для автоматической высоты)
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier.fillMaxSize(),
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

/**
 * Компонент для отображения ошибки с возможностью повторить действие.
 * Отображает текст ошибки и кнопку для повторного выполнения действия.
 *
 * @param error Текст ошибки (если null, отображается общая ошибка)
 * @param onRetry Callback, вызываемый при нажатии на кнопку повтора
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun ErrorContent(
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error ?: stringResource(R.string.unknown_error),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * Компонент для отображения пустого состояния.
 * Отображает текст по центру экрана серым цветом.
 *
 * @param text Текст для отображения
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun EmptyContent(
    text: String,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Gray
        )
    }
} 