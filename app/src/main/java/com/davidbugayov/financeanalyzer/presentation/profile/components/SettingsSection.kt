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
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode

/**
 * Компонент для отображения секции настроек в профиле пользователя.
 * @param onThemeClick Обработчик нажатия на настройку темы.
 * @param onLanguageClick Обработчик нажатия на настройку языка.
 * @param onCurrencyClick Обработчик нажатия на настройку валюты.
 * @param onTransactionReminderClick Обработчик нажатия на настройку напоминаний о транзакциях.
 * @param themeMode Текущий режим темы приложения.
 * @param isTransactionReminderEnabled Включены ли напоминания о транзакциях.
 * @param transactionReminderTime Время напоминания о транзакциях (часы и минуты) или null, если отключено.
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun SettingsSection(
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onTransactionReminderClick: () -> Unit,
    themeMode: ThemeMode,
    isTransactionReminderEnabled: Boolean,
    transactionReminderTime: Pair<Int, Int>?,
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
                text = "Настройки",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Настройка темы
            SettingsItem(
                icon = when(themeMode) {
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                    ThemeMode.SYSTEM -> Icons.Default.Brightness6
                },
                title = "Тема",
                subtitle = when(themeMode) {
                    ThemeMode.LIGHT -> "Светлая"
                    ThemeMode.DARK -> "Темная"
                    ThemeMode.SYSTEM -> "Системная"
                },
                onClick = onThemeClick
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Настройка языка
            SettingsItem(
                icon = Icons.Default.Language,
                title = "Язык",
                subtitle = "Русский",
                onClick = onLanguageClick
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Настройка валюты
            SettingsItem(
                icon = Icons.Default.Payments,
                title = "Валюта по умолчанию",
                subtitle = "Рубль (₽)",
                onClick = onCurrencyClick
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Настройка напоминаний о транзакциях
            SettingsItem(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.transaction_reminders),
                subtitle = if (isTransactionReminderEnabled && transactionReminderTime != null) {
                    val (hour, minute) = transactionReminderTime
                    "Ежедневно в ${hour}:${String.format("%02d", minute)}"
                } else {
                    "Отключено"
                },
                onClick = onTransactionReminderClick
            )
            
        }
    }
}

/**
 * Элемент настройки.
 * @param icon Иконка настройки.
 * @param title Заголовок настройки.
 * @param subtitle Подзаголовок настройки (опционально).
 * @param onClick Обработчик нажатия на настройку.
 * @param trailingContent Содержимое в конце элемента (опционально).
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        trailingContent?.invoke()
    }
} 