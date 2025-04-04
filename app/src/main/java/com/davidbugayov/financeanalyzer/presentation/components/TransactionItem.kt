package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import java.text.SimpleDateFormat
import java.util.Locale

// Создаем SimpleDateFormat один раз и переиспользуем для всех экземпляров
private val DATE_FORMATTER = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

// Кэш для категорий и их иконок для более быстрого поиска
private val CATEGORY_ICON_CACHE = mutableMapOf<Pair<String, Boolean>, ImageVector>()

/**
 * Универсальный компонент для отображения транзакции в списке.
 * Поддерживает как простой клик, так и передачу транзакции в обработчики.
 *
 * @param transaction Транзакция для отображения
 * @param onClick Обработчик клика по транзакции (с передачей транзакции или без)
 * @param onLongClick Обработчик долгого нажатия на транзакцию (с передачей транзакции или без)
 * @param showDivider Показывать разделитель после элемента
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit = {},
    onLongClick: (Transaction) -> Unit = {},
    showDivider: Boolean = true
) {
    // Используем remember для кэширования дорогостоящих вычислений
    val incomeColor = remember { Color(ColorUtils.INCOME_COLOR) }
    val expenseColor = remember { Color(ColorUtils.EXPENSE_COLOR) }
    
    // Форматируем дату только один раз и запоминаем результат
    val formattedDate = remember(transaction.date) { 
        DATE_FORMATTER.format(transaction.date) 
    }
    
    // Получаем иконку и цвет для категории, используя кэширование
    val categoryInfo = remember(transaction.category, transaction.isExpense) {
        getCategoryInfo(transaction.category, transaction.isExpense, incomeColor, expenseColor)
    }
    
    // Определяем цвет источника, используя remember для кэширования
    val sourceColor = remember(transaction.source, transaction.sourceColor, transaction.isExpense) {
        Color(ColorUtils.getEffectiveSourceColor(transaction.source, transaction.sourceColor, transaction.isExpense))
    }
    
    // Предварительно вычисляем и кэшируем форматированную сумму
    val formattedAmount = remember(transaction.amount, transaction.isExpense) {
        val amount = Money(transaction.amount)
        if (transaction.isExpense) {
            "-${amount.abs().formatted(showCurrency = true)}"
        } else {
            "+${amount.abs().formatted(showCurrency = true)}"
        }
    }
    
    // Стабилизируем лямбды обработчиков кликов, чтобы избежать пересоздания при рекомпозиции
    val onClickStable = remember(onClick) { onClick }
    val onLongClickStable = remember(onLongClick) { onLongClick }
    
    Column {
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
            // Иконка категории с цветом источника
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = sourceColor.copy(alpha = 0.3f)
            ) {
                Icon(
                    imageVector = categoryInfo.icon,
                    contentDescription = if (transaction.isExpense) "Расход" else "Доход",
                    tint = sourceColor,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_normal)))
            
            // Информация о транзакции
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Источник и дата на одном уровне
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Источник (если есть)
                    if (transaction.source != null) {
                        Text(
                            text = transaction.source,
                            style = MaterialTheme.typography.bodySmall,
                            color = sourceColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        // Разделитель между источником и датой - очень маленький отступ
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    // Дата транзакции - используем предварительно отформатированную дату
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Комментарий (если есть)
                transaction.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_normal)))
            
            // Сумма транзакции - используем предварительно отформатированное значение
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isExpense) expenseColor else incomeColor
            )
        }
        
        // Разделитель
        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }
}

/**
 * Информация о категории транзакции
 */
data class CategoryInfo(
    val icon: ImageVector,
    val backgroundColor: Color
)

/**
 * Возвращает иконку и цвет для категории транзакции
 * Оптимизировано с использованием кэша для часто используемых категорий
 */
fun getCategoryInfo(
    category: String, 
    isExpense: Boolean,
    incomeColor: Color,
    expenseColor: Color
): CategoryInfo {
    // Попытка получения иконки из кэша
    val cacheKey = category.lowercase() to isExpense
    val cachedIcon = CATEGORY_ICON_CACHE[cacheKey]
    
    if (cachedIcon != null) {
        // Если иконка найдена в кэше, возвращаем ее с соответствующим цветом
        return CategoryInfo(cachedIcon, if (isExpense) expenseColor else incomeColor)
    }
    
    // Если иконки нет в кэше, определяем ее и добавляем в кэш
    val (icon, bgColor) = when {
        !isExpense -> {
            when (category.lowercase()) {
                "зарплата" -> Icons.Default.Payments to incomeColor
                "перевод" -> Icons.Default.MonetizationOn to incomeColor
                else -> Icons.Default.MonetizationOn to incomeColor
            }
        }
        else -> when (category.lowercase()) {
            "продукты" -> Icons.Default.LocalGroceryStore to expenseColor
            "еда", "рестораны" -> Icons.Default.Fastfood to expenseColor
            "транспорт" -> Icons.Default.LocalTaxi to expenseColor
            "связь", "интернет" -> Icons.Default.Wifi to expenseColor
            "развлечения" -> Icons.Default.SportsEsports to expenseColor
            "покупки" -> Icons.Default.ShoppingBag to expenseColor
            "животные" -> Icons.Default.Pets to expenseColor
            "комиссия", "счета" -> Icons.Default.Receipt to expenseColor
            "жилье" -> Icons.Default.Home to expenseColor
            "кредит", "карта" -> Icons.Default.CreditCard to expenseColor
            "работа" -> Icons.Default.WorkOutline to expenseColor
            else -> Icons.Default.ShoppingCart to expenseColor
        }
    }
    
    // Сохраняем иконку в кэш для будущего использования
    CATEGORY_ICON_CACHE[cacheKey] = icon
    
    return CategoryInfo(icon, bgColor)
} 