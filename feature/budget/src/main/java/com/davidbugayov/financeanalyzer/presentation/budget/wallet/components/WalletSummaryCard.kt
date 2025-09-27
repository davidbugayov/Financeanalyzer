package com.davidbugayov.financeanalyzer.presentation.budget.wallet.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.shared.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import kotlin.math.min

/**
 * Современная карточка сводной информации о кошельке с улучшенным дизайном
 *
 * @param wallet Кошелек для отображения
 * @param modifier Модификатор
 * @param onSpendClick Обработчик нажатия на кнопку "Потратить"
 * @param onAddFundsClick Обработчик нажатия на кнопку "Пополнить"
 * @param onManageClick Обработчик нажатия на кнопку управления
 */
@Composable
fun WalletSummaryCard(
    wallet: Wallet,
    modifier: Modifier = Modifier,
    onSpendClick: () -> Unit,
    onAddFundsClick: (() -> Unit)? = null,
    onManageClick: (() -> Unit)? = null,
) {
    // Вычисляем прогресс бюджета
    val rawProgress =
        if (wallet.limit.amount > 0.0) {
            val progress = (wallet.spent.amount.toDouble() / wallet.limit.amount.toDouble()).toFloat()
            // Логируем значения для отладки
            timber.log.Timber.d(
                "Wallet: ${wallet.name}, Spent: ${wallet.spent.amount}, Limit: ${wallet.limit.amount}, Progress: $progress",
            )

            // Триггер достижения бюджета только для значимых порогов
            // Проверяем ачивку "Экономный" если потратили менее 80% и есть значимые траты
            if (progress > 0.1f && progress < 0.8f && wallet.spent.amount > 0.0) {
                timber.log.Timber.d(
                    "🏆 Триггер budget_saver: прогресс $progress < 0.8, потрачено: ${wallet.spent.amount}",
                )
                AchievementTrigger.onBudgetProgress(progress)
            } else if (progress > 0.8f) {
                // Превышен порог экономности
            }

            progress
        } else {
            timber.log.Timber.d("Wallet: ${wallet.name}, Limit is zero or negative: ${wallet.limit.amount}")
            0f
        }

    // Для отладки - показываем реальный прогресс или тестовый
    val finalProgress =
        if (rawProgress == 0f && wallet.limit.amount > 0.0) {
            // Если нет потраченной суммы, но есть лимит, показываем хотя бы минимальный прогресс для тестирования
            timber.log.Timber.d("Showing test progress for wallet ${wallet.name} with zero spent amount")
            0.15f // Показываем 15% для тестирования
        } else {
            rawProgress
        }

    val animatedProgress by animateFloatAsState(
        targetValue = min(finalProgress, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "wallet_progress",
    )

    // Определяем цвета на основе прогресса
    val progressColor =
        when {
            finalProgress <= 0.5f -> MaterialTheme.colorScheme.primary
            finalProgress <= 0.8f -> Color(0xFFFFA726) // Оранжевый
            else -> MaterialTheme.colorScheme.error
        }

    val isOverBudget = finalProgress > 1f
    val remaining = wallet.limit - wallet.spent

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(UiR.dimen.wallet_card_corner_radius)),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = dimensionResource(UiR.dimen.wallet_card_elevation),
            ),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Box {
            // Градиентный фон в верхней части
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(UiR.dimen.wallet_gradient_height))
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            progressColor.copy(alpha = 0.1f),
                                            Color.Transparent,
                                        ),
                                ),
                        ),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(UiR.dimen.wallet_card_padding)),
            ) {
                // Заголовок с иконкой кошелька
                WalletHeader(
                    walletName = wallet.name,
                    onManageClick = onManageClick,
                )

                Spacer(modifier = Modifier.height(dimensionResource(UiR.dimen.wallet_section_spacing)))

                // Основная секция с прогрессом и метриками
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Круговой прогресс-индикатор
                    ModernProgressIndicator(
                        progress = animatedProgress,
                        progressColor = progressColor,
                        percentage = (finalProgress * 100).toInt(),
                        isOverBudget = isOverBudget,
                    )

                    Spacer(modifier = Modifier.width(dimensionResource(UiR.dimen.wallet_header_spacing)))

                    // Метрики бюджета
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(UiR.dimen.wallet_metric_spacing)),
                    ) {
                        WalletMetric(
                            label = stringResource(UiR.string.wallet_spent_amount),
                            amount = wallet.spent,
                            color = MaterialTheme.colorScheme.onSurface,
                            trend = TrendType.NEUTRAL,
                        )

                        WalletMetric(
                            label = stringResource(UiR.string.wallet_remaining_amount),
                            amount = remaining,
                            color =
                                if (remaining.isNegative()) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            trend = if (remaining.isNegative()) TrendType.DOWN else TrendType.UP,
                        )

                        WalletMetric(
                            label = stringResource(UiR.string.wallet_budget_limit),
                            amount = wallet.limit,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(UiR.dimen.wallet_section_spacing)))

                // Баланс кошелька в отдельной карточке
                BalanceCard(balance = wallet.balance)

                Spacer(modifier = Modifier.height(dimensionResource(UiR.dimen.wallet_header_spacing)))

                // Быстрые действия
                QuickActionsRow(
                    onSpendClick = onSpendClick,
                    onAddFundsClick = onAddFundsClick,
                    progressColor = progressColor,
                )
            }
        }
    }
}

/**
 * Заголовок карточки кошелька
 */
@Composable
private fun WalletHeader(
    walletName: String,
    onManageClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(UiR.dimen.wallet_stats_icon_size)),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = walletName,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(UiR.string.wallet_overview),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(UiR.dimen.wallet_subtitle_margin_top)),
                )
            }
        }

        onManageClick?.let { onClick ->
            IconButton(
                onClick = onClick,
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timeline,
                    contentDescription = stringResource(UiR.string.wallet_manage_action),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Современный круговой индикатор прогресса
 */
@Composable
private fun ModernProgressIndicator(
    progress: Float,
    progressColor: Color,
    percentage: Int,
    isOverBudget: Boolean,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(dimensionResource(UiR.dimen.wallet_progress_size)),
    ) {
        // Фоновый круг
        Canvas(
            modifier = Modifier.size(dimensionResource(UiR.dimen.wallet_progress_size)),
        ) {
            drawCircle(
                color = progressColor.copy(alpha = 0.1f),
                radius = (size.minDimension - 16.dp.toPx()) / 2,
                style = Stroke(width = 12.dp.toPx()),
            )
        }

        // Прогресс индикатор
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(dimensionResource(UiR.dimen.wallet_progress_size)),
            color = progressColor,
            trackColor = Color.Transparent,
            strokeWidth = dimensionResource(UiR.dimen.wallet_progress_stroke_width),
        )

        // Центральный текст
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$percentage%",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(UiR.dimen.wallet_progress_text_size).value.sp,
                    ),
                color = progressColor,
            )

            if (isOverBudget) {
                Text(
                    text = stringResource(UiR.string.wallet_budget_exceeded),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontSize = dimensionResource(UiR.dimen.wallet_percentage_text_size).value.sp,
                )
            } else {
                Text(
                    text = stringResource(UiR.string.wallet_budget_progress),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = dimensionResource(UiR.dimen.wallet_percentage_text_size).value.sp,
                )
            }
        }
    }
}

/**
 * Тип тренда для метрики
 */
private enum class TrendType { UP, DOWN, NEUTRAL }

/**
 * Метрика кошелька с трендом
 */
@Composable
private fun WalletMetric(
    label: String,
    amount: Money,
    color: Color,
    trend: TrendType = TrendType.NEUTRAL,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = dimensionResource(UiR.dimen.wallet_metric_label_text_size).value.sp,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (trend != TrendType.NEUTRAL) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector =
                        when (trend) {
                            TrendType.UP -> Icons.AutoMirrored.Filled.TrendingUp
                            TrendType.DOWN -> Icons.AutoMirrored.Filled.TrendingDown
                            TrendType.NEUTRAL -> Icons.AutoMirrored.Filled.TrendingUp
                        },
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint =
                        when (trend) {
                            TrendType.UP -> Color(0xFF4CAF50)
                            TrendType.DOWN -> MaterialTheme.colorScheme.error
                            TrendType.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }

        Text(
            text = amount.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = dimensionResource(UiR.dimen.wallet_metric_value_text_size).value.sp,
                ),
            color = color,
        )
    }
}

/**
 * Карточка с балансом кошелька
 */
@Composable
private fun BalanceCard(balance: Money) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(UiR.dimen.wallet_stats_card_corner_radius)),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(UiR.dimen.wallet_stats_card_padding)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(UiR.string.wallet_available_balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Text(
                    text = balance.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(UiR.dimen.icon_size_medium)),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * Ряд быстрых действий
 */
@Composable
private fun QuickActionsRow(
    onSpendClick: () -> Unit,
    onAddFundsClick: (() -> Unit)?,
    progressColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Основная кнопка "Потратить"
        Button(
            onClick = onSpendClick,
            modifier =
                Modifier
                    .weight(1f)
                    .height(dimensionResource(UiR.dimen.wallet_action_button_height)),
            shape = RoundedCornerShape(dimensionResource(UiR.dimen.wallet_action_button_corner_radius)),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = progressColor,
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.Payment,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(UiR.string.wallet_spend_action),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
            )
        }

        // Кнопка пополнения (если передана)
        onAddFundsClick?.let { onClick ->
            FilledTonalButton(
                onClick = onClick,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(dimensionResource(UiR.dimen.wallet_action_button_height)),
                shape = RoundedCornerShape(dimensionResource(UiR.dimen.wallet_action_button_corner_radius)),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(UiR.string.wallet_add_funds_action),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
