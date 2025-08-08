package com.davidbugayov.financeanalyzer.feature.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.ui.R
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import com.davidbugayov.financeanalyzer.utils.Time

/**
 * Компонент для отображения секции настроек в профиле пользователя.
 * @param onThemeClick Обработчик нажатия на настройку темы.
 * @param onLanguageClick Обработчик нажатия на настройку языка.
 * @param onCurrencyClick Обработчик нажатия на настройку валюты.
 * @param onTransactionReminderClick Обработчик нажатия на настройку напоминаний о транзакциях.
 * @param themeMode Текущий режим темы приложения.
 * @param isTransactionReminderEnabled Включены ли напоминания о транзакциях.
 * @param transactionReminderTime Время напоминания о транзакциях (объект Time) или null, если отключено.
 * @param hasNotificationPermission Флаг наличия разрешения на уведомления.
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun SettingsSection(
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onTransactionReminderClick: () -> Unit,
    themeMode: ThemeMode,
    selectedCurrency: Currency,
    isTransactionReminderEnabled: Boolean,
    transactionReminderTime: Time?,
    hasNotificationPermission: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.profile_settings_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp),
        )
        AnimatedVisibility(visible = true, enter = fadeIn()) {
            SettingsActionCard(
                icon =
                    when (themeMode) {
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.SYSTEM -> Icons.Default.Brightness6
                    },
                iconBackground = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_theme_title),
                subtitle =
                    when (themeMode) {
                        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                    },
                onClick = onThemeClick,
            )
        }
        AnimatedVisibility(visible = true, enter = fadeIn()) {
            SettingsActionCard(
                icon = Icons.Default.Language,
                iconBackground = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.settings_language_title),
                subtitle = stringResource(R.string.settings_language_current_value),
                onClick = onLanguageClick,
            )
        }
        AnimatedVisibility(visible = true, enter = fadeIn()) {
            SettingsActionCard(
                icon = Icons.Default.Payments,
                iconBackground = MaterialTheme.colorScheme.tertiary,
                title = stringResource(R.string.profile_currency_title),
                subtitle = getCurrencyDisplayName(selectedCurrency),
                onClick = onCurrencyClick,
            )
        }
        AnimatedVisibility(visible = true, enter = fadeIn()) {
            SettingsActionCard(
                icon = Icons.Default.Timer,
                iconBackground = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.profile_transaction_reminders_title),
                subtitle =
                    if (!hasNotificationPermission) {
                        stringResource(R.string.notification_disabled_description)
                    } else if (isTransactionReminderEnabled && transactionReminderTime != null) {
                        stringResource(
                            R.string.settings_reminder_time_format,
                            transactionReminderTime.hour,
                            transactionReminderTime.minute,
                        )
                    } else {
                        stringResource(R.string.off)
                    },
                subtitleColor = if (!hasNotificationPermission) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onTransactionReminderClick,
            )
        }
    }
}

private fun getCurrencyDisplayName(currency: Currency): String {
    return when (currency) {
        Currency.RUB -> "Российский рубль (₽)"
        Currency.USD -> "Доллар США ($)"
        Currency.EUR -> "Евро (€)"
        Currency.CNY -> "Китайский юань (¥)"
        else -> "${currency.name} (${currency.symbol})"
    }
}

@Composable
fun SettingsActionCard(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 0.dp)
                .background(Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(iconBackground, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
