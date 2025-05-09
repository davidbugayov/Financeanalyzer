package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor

/**
 * Компонент для отображения информации о приложении.
 * @param appVersion Версия приложения.
 * @param buildVersion Версия сборки (код версии).
 * @param onNavigateToLibraries Обработчик нажатия на кнопку "Используемые библиотеки".
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun AppInfoSection(
    appVersion: String,
    buildVersion: String,
    onNavigateToLibraries: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = stringResource(R.string.app_info),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            // Версия приложения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.spacing_small))
            ) {
                Text(
                    text = stringResource(R.string.app_version),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = appVersion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Версия сборки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.spacing_small))
            ) {
                Text(
                    text = stringResource(R.string.build_version),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = buildVersion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            // Кнопка для перехода к списку использованных библиотек
            androidx.compose.material3.TextButton(
                onClick = onNavigateToLibraries,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.libraries),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Элемент информации о приложении.
 * @param icon Иконка элемента.
 * @param title Заголовок элемента.
 * @param value Значение элемента.
 */
@Composable
private fun AppInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 