package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.ui.theme.DefaultCategoryColor
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorDark
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorLight
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorDark
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorLight
import com.davidbugayov.financeanalyzer.ui.theme.TransferColorDark
import com.davidbugayov.financeanalyzer.ui.theme.TransferColorLight
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

object Formatters {

    fun formatAmount(money: Money, useMinimalDecimals: Boolean = false, showCurrency: Boolean = true): String {
        return money.formatForDisplay(showCurrency = showCurrency, useMinimalDecimals = useMinimalDecimals)
    }
}

private fun getDayMonthFormatter(): SimpleDateFormat {
    return SimpleDateFormat("d MMM", Locale.getDefault())
}

private fun getDayMonthYearFormatter(): SimpleDateFormat {
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault())
}

/**
 * Элемент списка транзакций с улучшенным дизайном и интеграцией ViewModel.
 *
 * @param transaction Транзакция для отображения.
 * @param categoriesViewModel ViewModel для получения данных о категориях (иконки, цвета).
 * @param animated Флаг для включения/выключения анимации появления.
 * @param animationDelay Задержка перед началом анимации.
 * @param onClick Обработчик короткого нажатия на элемент.
 * @param onTransactionLongClick Обработчик долгого нажатия на элемент.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    categoriesViewModel: CategoriesViewModel,
    animated: Boolean = true,
    animationDelay: Long = 0L,
    onClick: (Transaction) -> Unit = {},
    onTransactionLongClick: (Transaction) -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()

    val transferCategoryString = stringResource(id = R.string.category_transfer).lowercase(
        Locale.getDefault(),
    )

    val incomeColor = if (isDarkTheme) IncomeColorDark else IncomeColorLight
    val expenseColor = if (isDarkTheme) ExpenseColorDark else ExpenseColorLight
    val transferActualColor = if (isDarkTheme) TransferColorDark else TransferColorLight

    val transactionTypeColor =
        remember(
            transaction.category,
            transaction.isExpense,
            transferActualColor,
            expenseColor,
            incomeColor,
            transferCategoryString,
        ) {
            when {
                transaction.category.equals(transferCategoryString, ignoreCase = true) ||
                    transaction.category.equals("Перевод", ignoreCase = true) -> transferActualColor // Fallback for direct "Перевод" string
                transaction.isExpense -> expenseColor
                else -> incomeColor
            }
        }

    val expenseCategories by categoriesViewModel.expenseCategories.collectAsState()
    val incomeCategories by categoriesViewModel.incomeCategories.collectAsState()

    val uiCategory = remember(
        transaction.category,
        transaction.isExpense,
        expenseCategories,
        incomeCategories,
    ) {
        val categories = if (transaction.isExpense) expenseCategories else incomeCategories
        categories.find { it.name.equals(transaction.category, ignoreCase = true) }
    }

    val categoryActualColor = uiCategory?.color ?: DefaultCategoryColor
    val categoryIcon = uiCategory?.icon ?: Icons.Default.Category

    val sourceActualColor = remember(transaction.sourceColor, transaction.source, isDarkTheme) {
        val colorFromInt = if (transaction.sourceColor != 0) Color(transaction.sourceColor) else null
        // Fallback to a slightly transparent default color if no specific source color is found
        colorFromInt ?: ColorUtils.getSourceColorByName(transaction.source) ?: DefaultCategoryColor.copy(
            alpha = 0.7f,
        )
    }

    val formattedDate = remember(transaction.date) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.time = transaction.date
        val transactionYear = calendar.get(Calendar.YEAR)
        if (transactionYear == currentYear) {
            getDayMonthFormatter().format(transaction.date)
        } else {
            getDayMonthYearFormatter().format(transaction.date)
        }
    }

    val formattedAmount = remember(
        transaction.amount,
        transaction.isExpense,
        transaction.category,
        transferCategoryString,
    ) {
        val moneyAmount = transaction.amount

        val isTransfer = transaction.category.equals(transferCategoryString, ignoreCase = true) ||
            transaction.category.equals("Перевод", ignoreCase = true)

        val prefix = when {
            isTransfer -> "" // Transfers usually don't need a +/- sign from this logic, Money.format handles it if needed
            transaction.isExpense -> "-"
            else -> "+"
        }

        if (isTransfer) {
            // For transfers, display absolute amount, sign logic might be inherent or not needed
            Formatters.formatAmount(
                moneyAmount.abs(),
                useMinimalDecimals = true,
            )
        } else {
            prefix + Formatters.formatAmount(
                moneyAmount.abs(),
                useMinimalDecimals = true,
            )
        }
    }

    var visible by remember { mutableStateOf(!animated) }

    LaunchedEffect(Unit) {
        if (animated) {
            if (animationDelay > 0L) {
                delay(animationDelay)
            }
            visible = true
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "TransactionItemAlpha",
    )

    // Corrected Dp to Float conversion for targetValue
    val targetTranslationYDp = dimensionResource(id = R.dimen.spacing_medium)
    val targetTranslationYPx = with(LocalDensity.current) { targetTranslationYDp.toPx() }

    val animatedTranslationY by animateFloatAsState(
        targetValue = if (visible) 0f else targetTranslationYPx, // Use Px value here
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow, // Softer spring
        ),
        label = "TransactionItemTranslationY",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.card_horizontal_padding), // e.g., 12dp or 16dp from dimens
                vertical = dimensionResource(id = R.dimen.card_vertical_padding), // e.g., 6dp or 8dp from dimens
            )
            .graphicsLayer {
                alpha = animatedAlpha
                translationY = animatedTranslationY
            }
            .combinedClickable(
                onClick = { onClick(transaction) },
                onLongClick = { onTransactionLongClick(transaction) },
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.card_elevation_default),
        ), // e.g., 2dp or 4dp
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.card_corner_radius_medium)), // e.g., 12dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ), // Slightly different surface for differentiation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.card_content_padding_medium)), // e.g., 12dp or 16dp
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Transaction type indicator bar
            Box(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.transaction_type_indicator_width)) // e.g., 4dp
                    .height(
                        dimensionResource(id = R.dimen.icon_container_size_large),
                    ) // e.g., 48dp, to match a slightly larger icon area
                    .background(
                        color = transactionTypeColor,
                        shape = RoundedCornerShape(
                            topStart = dimensionResource(id = R.dimen.radius_small), // e.g., 4dp
                            bottomStart = dimensionResource(id = R.dimen.radius_small),
                        ),
                    ),
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium))) // e.g., 12dp

            // Category Icon
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.icon_container_size_large)) // e.g., 48dp
                    .clip(CircleShape)
                    .background(categoryActualColor.copy(alpha = 0.2f)), // Slightly more pronounced background
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category, // For accessibility
                    tint = categoryActualColor,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium)), // e.g., 24dp
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium))) // e.g., 12dp

            // Transaction Details: Category, Description, (New) Source, and Date
            Column(
                modifier = Modifier.weight(1f), // Takes available space
                verticalArrangement = Arrangement.Center, // Vertically center content within this column
            ) {
                // Category Name
                Text(
                    text = transaction.category.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }, // Capitalize first letter
                    style = MaterialTheme.typography.titleMedium, // Slightly larger/bolder for category
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Source Name - NEW
                if (transaction.source.isNotBlank()) {
                    Text(
                        text = transaction.source,
                        style = MaterialTheme.typography.bodySmall, // Smaller font for source
                        color = sourceActualColor, // Apply source color
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.line_chart_stroke_width),
                        ), // Placeholder, will be updated or requires user to add to dimens.xml
                    )
                }

                // Note (formerly Description)
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note ?: "", // Use safe call with default empty string
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Slightly muted color
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.spacing_small),
                        ), // Placeholder, will be updated or requires user to add to dimens.xml
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small))) // e.g., 8dp

            // Amount and Date Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center, // Vertically center content within this column
            ) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium, // Consistent with category title
                    fontWeight = FontWeight.Bold,
                    color = transactionTypeColor, // Color indicates income/expense/transfer
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall, // Smaller font for date
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Muted color
                )
            }
        }
    }
}

// Make sure you have these (or similar) dimension resources defined in your dimens.xml:
// <dimen name="card_horizontal_padding">12dp</dimen>
// <dimen name="card_vertical_padding">6dp</dimen>
// <dimen name="card_elevation_default">2dp</dimen>
// <dimen name="card_corner_radius_medium">12dp</dimen>
// <dimen name="card_content_padding_medium">12dp</dimen>
// <dimen name="transaction_type_indicator_width">4dp</dimen>
// <dimen name="icon_container_size_large">48dp</dimen>
// <dimen name="icon_size_medium">24dp</dimen>
// <dimen name="source_indicator_size">8dp</dimen>
// <dimen name="amount_type_icon_size">16dp</dimen>
// <dimen name="spacing_large">16dp</dimen>
// <dimen name="spacing_medium">12dp</dimen>
// <dimen name="spacing_xsmall">4dp</dimen>
// <dimen name="spacing_xxsmall">2dp</dimen>

// And string resources:
// <string name="unknown_source">Неизвестный источник</string>
// <string name="expense_transaction">Расходная транзакция</string>
// <string name="income_transaction">Доходная транзакция</string>
