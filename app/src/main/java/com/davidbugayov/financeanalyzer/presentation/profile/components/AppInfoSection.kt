package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Update
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
import com.davidbugayov.financeanalyzer.R

/**
 * Компонент для отображения информации о приложении.
 * @param appVersion Версия приложения.
 * @param buildVersion Версия сборки (код версии).
 * @param onLibrariesClick Обработчик нажатия на пункт "Используемые библиотеки".
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun AppInfoSection(
    appVersion: String,
    buildVersion: String,
    onLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.profile_section_padding))
        ) {
            Text(
                text = stringResource(R.string.app_info),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))
            
            // Версия приложения
            AppInfoItem(
                icon = Icons.Default.Info,
                title = stringResource(R.string.app_version),
                value = appVersion
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))
            
            // Версия сборки
            AppInfoItem(
                icon = Icons.Default.Update,
                title = stringResource(R.string.build_version),
                value = buildVersion
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))
            
            // Используемые библиотеки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLibrariesClick)
                    .padding(vertical = dimensionResource(R.dimen.spacing_normal)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                
                Text(
                    text = stringResource(R.string.libraries),
                    style = MaterialTheme.typography.bodyMedium
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