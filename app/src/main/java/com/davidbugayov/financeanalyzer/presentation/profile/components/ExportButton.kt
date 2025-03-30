package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R

/**
 * Кнопка для экспорта данных в CSV с функцией автоматического поделиться.
 *
 * @param onClick Обработчик нажатия кнопки
 * @param isExporting Флаг, указывающий на процесс экспорта
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@Composable
fun ExportButton(
    onClick: () -> Unit,
    isExporting: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = !isExporting
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = dimensionResource(R.dimen.spacing_medium))
                    .size(dimensionResource(R.dimen.icon_size_small)),
                strokeWidth = dimensionResource(R.dimen.stroke_medium)
            )
        } else {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_medium))
            )
        }
        Text(
            text = stringResource(R.string.export_to_csv),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Описание функциональности экспорта данных.
 *
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@Composable
fun ExportDescription(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.export_description) + "\nПри экспорте автоматически откроется диалог для отправки файла.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
} 