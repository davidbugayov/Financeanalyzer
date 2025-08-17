package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Text(
        text = balance.toPlainString(),
        style = MaterialTheme.typography.headlineMedium,
        fontSize =
            dimensionResource(
                UiR.dimen.enhanced_summary_card_balance_font_size,
            ).value.sp,
        fontWeight = FontWeight.Bold,
        color = if (balance.amount >= BigDecimal.ZERO) incomeColor else expenseColor,
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
    val cardColor = MaterialTheme.colorScheme.background
    val balanceTextColor = if (balance.amount >= BigDecimal.ZERO) incomeColor else expenseColor
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(width = 3.dp, color = balanceTextColor),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical =
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_padding_vertical,
                            ),
                        horizontal =
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_padding_horizontal,
                            ),
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BalanceCardTitle(balance)
            Spacer(
                modifier =
                    Modifier.height(
                        dimensionResource(UiR.dimen.enhanced_summary_card_spacing),
                    ),
            )
            // Баланс в центре, снизу две цветные плашки доходов/расходов
            BalanceCardAmount(balance)
            Spacer(
                modifier =
                    Modifier.height(
                        dimensionResource(UiR.dimen.enhanced_summary_card_spacing),
                    ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AmountPill(
                    labelRes = UiR.string.income,
                    amountText = income.toPlainString(),
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
                    amountText = expense.toPlainString(),
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.06f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
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
