package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Компонент для отображения пустого состояния.
 * Используется, когда нет данных для отображения.
 */
@Composable
fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_transactions),
            color = Color.Gray,
        )
    }
} 