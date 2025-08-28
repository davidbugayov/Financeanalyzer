package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import java.math.BigDecimal

/**
 * Компонент для отображения текущего баланса пользователя.
 *
 * @param balance Текущий баланс пользователя
 */
@Composable
private fun BalanceCardTitle(balance: Money) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val titleColor =
        if (balance.amount >= BigDecimal.ZERO) {
            incomeColor.copy(alpha = 0.7f)
        } else {
            expenseColor.copy(alpha = 0.7f)
        }
    Text(
        text = stringResource(UiR.string.current_balance),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = titleColor,
    )
}

@Composable
private fun BalanceCardAmount(balance: Money) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val textColor = if (balance.amount >= BigDecimal.ZERO) incomeColor else expenseColor

    // Добавляем знак к балансу
    val displayText = if (balance.amount >= BigDecimal.ZERO) {
        "+${balance.formatForDisplay(showCurrency = true)}"
    } else {
        balance.formatForDisplay(showCurrency = true)
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.headlineMedium,
        fontSize =
            dimensionResource(
                UiR.dimen.enhanced_summary_card_balance_font_size,
            ).value.sp,
        fontWeight = FontWeight.ExtraBold,
        color = textColor,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun BalanceCard(
    balance: Money,
    income: Money,
    expense: Money,
    modifier: Modifier = Modifier,
) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val balanceTextColor = if (balance.amount >= BigDecimal.ZERO) incomeColor else expenseColor

    // Создаем градиентную заливку
    val gradientBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = if (balance.amount >= BigDecimal.ZERO) {
            listOf(
                incomeColor.copy(alpha = 0.08f),
                incomeColor.copy(alpha = 0.04f),
                incomeColor.copy(alpha = 0.01f)
            )
        } else {
            listOf(
                expenseColor.copy(alpha = 0.08f),
                expenseColor.copy(alpha = 0.04f),
                expenseColor.copy(alpha = 0.01f)
            )
        }
    )

    // Анимация появления
    val scale = remember { Animatable(0.95f) }
    LaunchedEffect(balance) {
        scale.animateTo(1f, tween(300))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(width = 2.dp, color = balanceTextColor.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(
                        vertical = 16.dp,
                        horizontal = 16.dp,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BalanceCardTitle(balance)
            Spacer(
                modifier =
                    Modifier.height(
                        8.dp,
                    ),
            )
            // Баланс в центре, снизу две цветные плашки доходов/расходов
            BalanceCardAmount(balance)
            Spacer(
                modifier =
                    Modifier.height(
                        8.dp,
                    ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AmountPill(
                    labelRes = UiR.string.income,
                    amountText = income.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                    icon = Icons.Filled.ArrowUpward,
                    color = incomeColor,
                    modifier = Modifier.weight(1f),
                )
                Spacer(
                    modifier =
                        Modifier.width(
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_spacing,
                            ),
                        ),
                )
                AmountPill(
                    labelRes = UiR.string.expenses,
                    amountText = expense.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                    icon = Icons.Filled.ArrowDownward,
                    color = expenseColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AmountPill(
    labelRes: Int,
    amountText: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val pillGradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(
            color.copy(alpha = 0.1f),
            color.copy(alpha = 0.05f),
            color.copy(alpha = 0.02f)
        )
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(width = 1.dp, color = color.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier
                .background(pillGradient)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = stringResource(id = labelRes),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize =
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_label_font_size,
                            ).value.sp,
                    ),
                color = color,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier =
                        Modifier.size(
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_icon_size,
                            ),
                        ),
                )
                Spacer(
                    modifier =
                        Modifier.width(
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_spacing,
                            ),
                        ),
                )
                Text(
                    text = amountText,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize =
                                dimensionResource(
                                    UiR.dimen.enhanced_summary_card_income_expense_font_size,
                                ).value.sp,
                        ),
                    color = color,
                )
            }
        }
    }
}
