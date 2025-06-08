package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.davidbugayov.financeanalyzer.R

/**
 * Компонент для отображения индикатора загрузки с сообщением.
 * Показывает CircularProgressIndicator и текстовое сообщение в центре экрана.
 *
 * @param message Текстовое сообщение для отображения рядом с индикатором
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun CenteredLoadingIndicator(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal)),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
