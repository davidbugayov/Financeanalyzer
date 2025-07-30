package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.R
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.ui.utils.ColorUtils
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Переиспользуемый компонент для отображения детальной информации о транзакции.
 * Может использоваться в различных диалогах.
 *
 * @param transaction Транзакция для отображения
 */
@Composable
fun TransactionDetailContent(
    transaction: Transaction,
) {
    val isDarkTheme = isSystemInDarkTheme()

    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        // Основная сумма с цветом
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (transaction.isExpense) {
                    LocalExpenseColor.current.copy(alpha = 0.08f)
                } else {
                    LocalIncomeColor.current.copy(alpha = 0.08f)
                }
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Иконка в круге
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.isExpense) {
                                LocalExpenseColor.current.copy(alpha = 0.15f)
                            } else {
                                LocalIncomeColor.current.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = if (transaction.isExpense) {
                            LocalExpenseColor.current
                        } else {
                            LocalIncomeColor.current
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (transaction.isExpense) {
                        "-" + transaction.amount.abs().formatForDisplay(useMinimalDecimals = true)
                    } else {
                        "+" + transaction.amount.formatForDisplay(useMinimalDecimals = true)
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isExpense) {
                        LocalExpenseColor.current
                    } else {
                        LocalIncomeColor.current
                    },
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Детали транзакции в карточке
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Категория
                DetailRow(
                    label = stringResource(R.string.category),
                    value = transaction.category,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Дата
                DetailRow(
                    label = stringResource(R.string.date),
                    value = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(transaction.date),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Источник
                val effectiveSourceColor = rememberSourceColor(transaction, isDarkTheme)
                DetailRow(
                    label = stringResource(R.string.source),
                    value = transaction.source,
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(effectiveSourceColor, CircleShape)
                        )
                    }
                )

                // Заметка (если есть)
                transaction.note?.let { note ->
                    if (note.isNotBlank()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        DetailRow(
                            label = stringResource(R.string.note),
                            value = note,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Note,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: @Composable (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка
        icon?.let {
            it()
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // Контент
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun rememberSourceColor(
    transaction: Transaction,
    isDarkTheme: Boolean,
): Color {
    return remember(transaction.source, transaction.sourceColor, transaction.isExpense, isDarkTheme) {
        val sourceColorInt = transaction.sourceColor
        val colorFromInt: Color? = if (sourceColorInt != 0) Color(sourceColorInt) else null

        colorFromInt ?: ColorUtils.getEffectiveSourceColor(
            sourceName = transaction.source,
            sourceColorHex = null,
            isExpense = transaction.isExpense,
            isDarkTheme = isDarkTheme,
        )
    }
} 