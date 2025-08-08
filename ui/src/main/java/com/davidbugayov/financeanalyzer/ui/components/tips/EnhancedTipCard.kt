package com.davidbugayov.financeanalyzer.ui.components.tips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Улучшенная карточка с финансовыми советами
 *
 * @param tips Список советов для отображения
 * @param onDismiss Колбэк для закрытия карточки
 * @param onActionClick Колбэк для действий по совету
 */
@Composable
fun EnhancedTipCard(
    tips: List<FinancialTip>,
    onDismiss: () -> Unit = {},
    onActionClick: (FinancialTip) -> Unit = {},
) {
    if (tips.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    var currentTipIndex by remember { mutableStateOf(0) }

    val currentTip = tips[currentTipIndex]
    val hasMultipleTips = tips.size > 1

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
        ) {
            // Заголовок с иконкой категории и кнопкой закрытия
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Иконка категории с мягким фоном
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = currentTip.category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(id = currentTip.titleResId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        // Показываем категорию для персонализированных советов
                        if (currentTip.isPersonalized) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.personalized_tip),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                Row {
                    // Кнопка развернуть/свернуть для множественных советов
                    if (hasMultipleTips) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription =
                                    if (isExpanded) {
                                        stringResource(R.string.tips_collapse)
                                    } else {
                                        stringResource(
                                            R.string.tips_expand,
                                        )
                                    },
                            )
                        }
                    }

                    // Кнопка закрытия
                    IconButton(
                        onClick = onDismiss,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.tips_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Описание текущего совета
            Text(
                text =
                    if (currentTip.descriptionArgs.isEmpty()) {
                        stringResource(id = currentTip.descriptionResId)
                    } else {
                        stringResource(
                            id = currentTip.descriptionResId,
                            *currentTip.descriptionArgs.toTypedArray(),
                        )
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
            )

            // Кнопка действия
            currentTip.actionResId?.let { actionTextRes ->
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { onActionClick(currentTip) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(id = actionTextRes))
                }
            }

            // Расширенный список советов
            AnimatedVisibility(
                visible = isExpanded && hasMultipleTips,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.all_recommendations),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(Modifier.height(8.dp))

                    // Горизонтальный список категорий
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(tips.groupBy { it.category }.keys.toList()) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = currentTip.category == category,
                                onClick = {
                                    // Находим первый совет этой категории
                                    val index = tips.indexOfFirst { it.category == category }
                                    if (index != -1) {
                                        currentTipIndex = index
                                    }
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Список всех советов текущей категории
                    tips.filter { it.category == currentTip.category }.forEach { tip ->
                        if (tip != currentTip) {
                            TipItem(
                                tip = tip,
                                onClick = {
                                    currentTipIndex = tips.indexOf(tip)
                                },
                                onActionClick = onActionClick,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Чип категории
 */
@Composable
private fun CategoryChip(
    category: TipCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        border =
            BorderStroke(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            ),
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/**
 * Элемент списка советов
 */
@Composable
private fun TipItem(
    tip: FinancialTip,
    onClick: () -> Unit,
    onActionClick: (FinancialTip) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = stringResource(id = tip.titleResId),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(id = tip.descriptionResId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            tip.actionResId?.let { actionTextRes ->
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onActionClick(tip) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = actionTextRes),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
