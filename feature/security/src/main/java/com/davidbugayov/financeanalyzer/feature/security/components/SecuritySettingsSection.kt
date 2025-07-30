package com.davidbugayov.financeanalyzer.feature.security.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.security.R

/**
 * Компонент для отображения настроек безопасности в профиле пользователя.
 * @param isAppLockEnabled Включена ли блокировка приложения
 * @param isBiometricEnabled Включена ли биометрическая аутентификация
 * @param isBiometricAvailable Доступна ли биометрическая аутентификация на устройстве
 * @param onAppLockClick Обработчик нажатия на настройку блокировки приложения
 * @param onBiometricClick Обработчик нажатия на настройку биометрической аутентификации
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun SecuritySettingsSection(
    isAppLockEnabled: Boolean,
    isBiometricEnabled: Boolean,
    isBiometricAvailable: Boolean,
    onAppLockClick: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.security_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp),
        )

        AnimatedVisibility(visible = true, enter = fadeIn()) {
            SecurityActionCard(
                icon = Icons.Default.Lock,
                iconBackground = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.security_app_lock_title),
                subtitle = stringResource(R.string.security_app_lock_description),
                isEnabled = isAppLockEnabled,
                onClick = onAppLockClick,
            )
        }

        if (isBiometricAvailable) {
            AnimatedVisibility(visible = true, enter = fadeIn()) {
                SecurityActionCard(
                    icon = Icons.Default.Fingerprint,
                    iconBackground = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.security_biometric_title),
                    subtitle = stringResource(R.string.security_biometric_description),
                    isEnabled = isBiometricEnabled,
                    onClick = onBiometricClick,
                    enabled = isAppLockEnabled, // Биометрия доступна только при включенной блокировке
                )
            }
        }
    }
}

/**
 * Карточка действия для настроек безопасности
 */
@Composable
private fun SecurityActionCard(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(enabled = enabled) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (enabled) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(
                            color = if (enabled) iconBackground else iconBackground.copy(alpha = 0.6f),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        },
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { if (enabled) onClick() },
                enabled = enabled,
            )
        }
    }
}
