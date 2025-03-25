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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.profile_section_padding))
        ) {
            Text(
                text = stringResource(R.string.profile_settings_title),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))
            
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

            HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))
            
            // Настройка языка
            SettingsItem(
                icon = Icons.Default.Language,
                title = "Язык",
                subtitle = "Русский",
                onClick = onLanguageClick
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))
            
            // Настройка валюты
            SettingsItem(
                icon = Icons.Default.Payments,
                title = stringResource(R.string.profile_currency_title),
                subtitle = "Рубль (₽)",
                onClick = onCurrencyClick
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))
            
            // Настройка напоминаний о транзакциях
            SettingsItem(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.profile_transaction_reminders_title),
                subtitle = if (isTransactionReminderEnabled && transactionReminderTime != null) {
                    val (hour, minute) = transactionReminderTime
                    "Ежедневно в ${hour}:${String.format("%02d", minute)}"
                } else {
                    stringResource(R.string.off)
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
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = dimensionResource(R.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small)),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
} 