package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.GetSubcategoryByIdUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryLocalization
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.DefaultCategoryColor
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorDark
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorLight
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorDark
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorLight
import com.davidbugayov.financeanalyzer.ui.theme.TransferColorDark
import com.davidbugayov.financeanalyzer.ui.theme.TransferColorLight
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import timber.log.Timber

object Formatters {
    fun formatAmount(
        money: Money,
        useMinimalDecimals: Boolean = false,
        showCurrency: Boolean = true,
    ): String {
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
    val context = LocalContext.current

    // Получаем UseCase для загрузки подкатегорий
    val getSubcategoryByIdUseCase: GetSubcategoryByIdUseCase = koinInject()

    // Состояние для названия подкатегории
    var subcategoryName by remember { mutableStateOf("") }

    // Загружаем подкатегорию, если есть subcategoryId
    LaunchedEffect(transaction.subcategoryId) {
        transaction.subcategoryId?.let { subcategoryId ->
            try {
                val subcategory = getSubcategoryByIdUseCase(subcategoryId)
                subcategoryName = subcategory?.name ?: ""
            } catch (e: Exception) {
                Timber.e(e.toString())
                subcategoryName = ""
            }
        } ?: run {
            subcategoryName = ""
        }
    }

    val transferCategoryString = context.getString(UiR.string.transaction_transfers).lowercase(Locale.getDefault())

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
                    transaction.category.equals(context.getString(UiR.string.transaction_transfer), ignoreCase = true) -> transferActualColor // Fallback for direct "Перевод" string
                transaction.isExpense -> expenseColor
                else -> incomeColor
            }
        }

    val expenseCategories by categoriesViewModel.expenseCategories.collectAsState()
    val incomeCategories by categoriesViewModel.incomeCategories.collectAsState()

    val uiCategory =
        remember(
            transaction.category,
            transaction.isExpense,
            expenseCategories,
            incomeCategories,
        ) {
            val categories = if (transaction.isExpense) expenseCategories else incomeCategories
            val txKey = CategoryLocalization.keyFor(transaction.category)
            // Сначала пытаемся сопоставить по ключу (нормализованное имя на любом языке), затем по строке
            categories.find { cat ->
                val catKey = CategoryLocalization.keyFor(cat.name)
                (txKey != null && catKey != null && txKey == catKey) || cat.name.equals(
                    transaction.category,
                    ignoreCase = true,
                )
            }
        }

    val categoryActualColor = uiCategory?.color ?: DefaultCategoryColor
    // Если нашли дефолтную категорию по ключу, но иконка в UiCategory отсутствует (локализованное имя),
    // подстрахуемся дефолтной иконкой по ключу transfer/food/...: в нашем поиске выше уже нормализован txKey.
    val fallbackIcon = Icons.Default.Category
    val categoryIcon = uiCategory?.icon ?: fallbackIcon

    val sourceActualColor =
        remember(transaction.sourceColor, transaction.source, isDarkTheme) {
            val colorFromInt = if (transaction.sourceColor != 0) Color(transaction.sourceColor) else null
            // Fallback to a slightly transparent default color if no specific source color is found
            colorFromInt ?: ColorUtils.getSourceColorByName(transaction.source) ?: DefaultCategoryColor.copy(
                alpha = 0.7f,
            )
        }

    val formattedDate =
        remember(transaction.date) {
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

    val currentCurrency by CurrencyProvider.getCurrencyFlow().collectAsState()

    val formattedAmount =
        remember(
            transaction.amount,
            transaction.isExpense,
            transaction.category,
            transferCategoryString,
            currentCurrency,
        ) {
            val moneyAmount = Money(transaction.amount.amount, currentCurrency)

            val isTransfer =
                transaction.category.equals(transferCategoryString, ignoreCase = true) ||
                    transaction.category.equals(context.getString(UiR.string.transaction_transfer), ignoreCase = true)

            val prefix =
                when {
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
                prefix +
                    Formatters.formatAmount(
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
    val targetTranslationYDp = dimensionResource(id = UiR.dimen.spacing_medium)
    val targetTranslationYPx = with(LocalDensity.current) { targetTranslationYDp.toPx() }

    val animatedTranslationY by animateFloatAsState(
        targetValue = if (visible) 0f else targetTranslationYPx, // Use Px value here
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow, // Softer spring
            ),
        label = "TransactionItemTranslationY",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = UiR.dimen.card_horizontal_padding),
                    vertical = dimensionResource(id = UiR.dimen.card_vertical_padding),
                )
                .graphicsLayer {
                    alpha = animatedAlpha
                    translationY = animatedTranslationY
                },
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = dimensionResource(id = UiR.dimen.card_elevation_default),
            ),
        shape = RoundedCornerShape(dimensionResource(id = UiR.dimen.card_corner_radius_medium)),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onClick(transaction) },
                        onLongClick = { onTransactionLongClick(transaction) },
                    )
                    .padding(dimensionResource(id = UiR.dimen.card_content_padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Transaction type indicator bar
            Box(
                modifier =
                    Modifier
                        .width(dimensionResource(id = UiR.dimen.transaction_type_indicator_width))
                        .height(dimensionResource(id = UiR.dimen.icon_container_size_large))
                        .background(
                            color = transactionTypeColor,
                            shape =
                                RoundedCornerShape(
                                    topStart = dimensionResource(id = UiR.dimen.radius_small),
                                    bottomStart = dimensionResource(id = UiR.dimen.radius_small),
                                ),
                        ),
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = UiR.dimen.spacing_medium)))

            // Category Icon
            Box(
                modifier =
                    Modifier
                        .size(dimensionResource(id = UiR.dimen.icon_container_size_large))
                        .clip(CircleShape)
                        .background(categoryActualColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category,
                    tint = Color.White,
                    modifier = Modifier.size(dimensionResource(id = UiR.dimen.icon_size_medium)),
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = UiR.dimen.spacing_medium)))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = CategoryLocalization.displayName(
                        context,
                        transaction.category,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Отображаем подкатегорию, если она есть
                if (subcategoryName.isNotBlank()) {
                    Text(
                        text = subcategoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = dimensionResource(id = UiR.dimen.spacing_tiny)),
                    )
                }

                if (transaction.source.isNotBlank()) {
                    Text(
                        text = transaction.source,
                        style = MaterialTheme.typography.bodySmall,
                        color = sourceActualColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = dimensionResource(id = UiR.dimen.line_chart_stroke_width)),
                    )
                }

                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = dimensionResource(id = UiR.dimen.spacing_small)),
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = UiR.dimen.spacing_small)))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = transactionTypeColor,
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
