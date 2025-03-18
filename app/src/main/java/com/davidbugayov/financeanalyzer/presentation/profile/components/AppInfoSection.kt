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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "О приложении",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Версия приложения
            AppInfoItem(
                icon = Icons.Default.Info,
                title = "Версия приложения",
                value = appVersion
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Версия сборки
            AppInfoItem(
                icon = Icons.Default.Update,
                title = "Версия кода",
                value = buildVersion
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Используемые библиотеки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLibrariesClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Используемые библиотеки",
                    style = MaterialTheme.typography.bodyLarge
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 