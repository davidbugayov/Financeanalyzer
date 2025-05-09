package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalTransferColor
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

// Функция для создания форматтера с текущей локалью
private fun getDateFormatter(): SimpleDateFormat {
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
}

// Кэш для категорий и их иконок для более быстрого поиска
private data class CategoryDisplayInfo(val icon: ImageVector, val color: Color)

private fun getCategoryDisplayInfo(
    categoryName: String,
    isExpense: Boolean,
    defaultIncomeColor: Color,
    defaultExpenseColor: Color,
    transferColor: Color,
    defaultOtherColor: Color,
    incomeCategoryColors: Map<String, Color>,
    expenseCategoryColors: Map<String, Color>
): CategoryDisplayInfo {
    // TODO: Заменить эту логику на использование CategoryIconProvider
    // Цвет теперь берется из карт или дефолтных значений
    val icon = Icons.Default.Category // Placeholder, используйте CategoryIconProvider

    val specificColor = if (isExpense) {
        expenseCategoryColors[categoryName]
    } else {
        incomeCategoryColors[categoryName]
    }

    val color = when {
        specificColor != null -> specificColor
        categoryName.equals("Перевод", ignoreCase = true) -> transferColor
        categoryName.equals("Другое", ignoreCase = true) || categoryName.equals("Прочие операции", ignoreCase = true) -> defaultOtherColor
        isExpense -> defaultExpenseColor
        else -> defaultIncomeColor
    }
    return CategoryDisplayInfo(icon, color)
}

/**
 * Элемент списка транзакций с оптимизацией производительности
 * 
 * @param transaction Транзакция для отображения
 * @param onClick Обработчик клика по элементу
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    showDate: Boolean = true,
    animated: Boolean = true,
    animationDelay: Long = 0L,
    showDivider: Boolean = true,
    onClick: (Transaction) -> Unit = {},
    onTransactionLongClick: (Transaction) -> Unit
) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val otherColor = MaterialTheme.colorScheme.onSurfaceVariant // для категории "Прочее" или если цвет не найден
    val isDarkTheme = isSystemInDarkTheme()
    val transferColor = LocalTransferColor.current // Получаем transferColor здесь

    // Получаем карты цветов категорий
    val incomeColorsMap = com.davidbugayov.financeanalyzer.ui.theme.incomeCategoryColorsMap
    val expenseColorsMap = com.davidbugayov.financeanalyzer.ui.theme.expenseCategoryColorsMap

    val formattedDate = remember(transaction.date) {
        getDateFormatter().format(transaction.date) 
    }

    val categoryDisplayInfo = remember(
        transaction.category,
        transaction.isExpense,
        incomeColor,
        expenseColor,
        transferColor,
        otherColor,
        incomeColorsMap,
        expenseColorsMap
    ) {
        getCategoryDisplayInfo(
            categoryName = transaction.category,
            isExpense = transaction.isExpense,
            defaultIncomeColor = incomeColor,
            defaultExpenseColor = expenseColor,
            transferColor = transferColor,
            defaultOtherColor = otherColor,
            incomeCategoryColors = incomeColorsMap,
            expenseCategoryColors = expenseColorsMap
        )
    }

    // Определяем цвет источника
    val sourceColor = remember(transaction.source, transaction.sourceColor, transaction.isExpense, isDarkTheme) {
        val sourceColorInt = transaction.sourceColor
        var colorFromInt: Color? = null
        if (sourceColorInt != 0) { // 0 может быть маркером отсутствия цвета
            colorFromInt = try {
                Color(sourceColorInt)
            } catch (_: Exception) {
                null
            }
        }
        colorFromInt ?: ColorUtils.getEffectiveSourceColor(
            sourceName = transaction.source,
            sourceColorHex = null, // Если sourceColorInt не подошел, пробуем по имени, без HEX
            isExpense = transaction.isExpense,
            isDarkTheme = isDarkTheme
        )
    }
    
    val surfaceColor = remember(sourceColor) { sourceColor.copy(alpha = 0.3f) }
    
    val formattedAmount = remember(transaction.amount, transaction.isExpense) {
        val amount = transaction.amount
        val prefix = if (transaction.isExpense) "-" else "+"
        // Если это перевод, не показываем +/- ??? - нужно уточнить бизнес-логику
        // if (transaction.category.equals("Перевод", ignoreCase = true)) "${amount.abs().format(showCurrency = true)}"
        // else 
        "$prefix${amount.abs().format(showCurrency = true)}"
    }

    val onClickStable = remember { onClick }
    val onLongClickStable = remember { onTransactionLongClick }
    
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (animated && animationDelay > 0) {
            delay(animationDelay)
        }
        visible = true
    }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible || !animated) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    val animatedOffset by animateFloatAsState(
        targetValue = if (visible || !animated) 0f else 20f, 
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offset"
    )
    
    Column(
        modifier = Modifier
            .graphicsLayer {
                alpha = if (animated) animatedAlpha else 1f
                translationY = if (animated) animatedOffset else 0f
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClickStable(transaction) },
                    onLongClick = { onLongClickStable(transaction) }
                )
                .padding(
                    horizontal = dimensionResource(id = R.dimen.spacing_normal),
                    vertical = dimensionResource(id = R.dimen.spacing_medium)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = surfaceColor // Фон иконки категории
            ) {
                Icon(
                    imageVector = categoryDisplayInfo.icon, // Иконка категории
                    contentDescription = if (transaction.isExpense) "Расход" else "Доход",
                    tint = sourceColor, // Цвет иконки категории = цвету источника (или категории?)
                    // Логичнее чтобы tint был categoryDisplayInfo.color, а sourceColor был у текста источника.
                    // Пока оставлю sourceColor для консистентности с предыдущей версией.
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_normal)))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.category.take(20) + if (transaction.category.length > 20) "..." else "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = transaction.source.take(15) + if (transaction.source.length > 15) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = sourceColor, // Цвет текста источника
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false) 
                    )
                    transaction.note?.takeIf { it.isNotBlank() }?.let { noteContent ->
                        Text(
                            text = " • ${noteContent.take(20)}${if (noteContent.length > 20) "..." else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.category.equals("Перевод", ignoreCase = true)) MaterialTheme.colorScheme.onSurface
                    else if (transaction.isExpense) expenseColor else incomeColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (showDate) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
        if (showDivider) {
            // Используем HorizontalDivider вместо Divider для Material3
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.spacing_normal) + 40.dp + dimensionResource(id = R.dimen.spacing_normal)),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        }
    }
} 