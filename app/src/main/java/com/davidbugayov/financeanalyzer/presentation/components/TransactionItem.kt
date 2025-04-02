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
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val incomeColor = Color(ColorUtils.INCOME_COLOR)
    val expenseColor = Color(ColorUtils.EXPENSE_COLOR)
    
    // Получаем иконку и цвет для категории
    val categoryInfo = getCategoryInfo(transaction.category, transaction.isExpense, incomeColor, expenseColor)
    
    // Определяем цвет источника
    val sourceColor = Color(ColorUtils.getEffectiveSourceColor(transaction.source, transaction.sourceColor, transaction.isExpense))
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(transaction) },
                    onLongClick = { onLongClick(transaction) }
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
                    transaction.source?.let { source ->
                        Text(
                            text = source,
                            style = MaterialTheme.typography.bodySmall,
                            color = sourceColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        // Разделитель между источником и датой - очень маленький отступ
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    // Дата транзакции
                    Text(
                        text = dateFormatter.format(transaction.date),
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
            
            // Сумма транзакции
            val amount = Money(transaction.amount)
            val formattedAmount = if (transaction.isExpense) {
                "-${amount.abs().formatted(showCurrency = true)}"
            } else {
                "+${amount.abs().formatted(showCurrency = true)}"
            }
            
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isExpense) expenseColor else incomeColor
            )
        }
        
        // Разделитель
        if (showDivider) {
            Divider(
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
 */
fun getCategoryInfo(
    category: String, 
    isExpense: Boolean,
    incomeColor: Color,
    expenseColor: Color
): CategoryInfo {
    val (icon, bgColor) = when {
        !isExpense -> {
            when {
                category.equals("Зарплата", ignoreCase = true) -> 
                    Icons.Default.Payments to incomeColor
                category.equals("Перевод", ignoreCase = true) ->
                    Icons.Default.MonetizationOn to incomeColor
                else -> Icons.Default.MonetizationOn to incomeColor
            }
        }
        category.equals("Продукты", ignoreCase = true) -> 
            Icons.Default.LocalGroceryStore to expenseColor
        category.equals("Еда", ignoreCase = true) || 
        category.equals("Рестораны", ignoreCase = true) -> 
            Icons.Default.Fastfood to expenseColor
        category.equals("Транспорт", ignoreCase = true) ->
            Icons.Default.LocalTaxi to expenseColor
        category.equals("Связь", ignoreCase = true) ||
        category.equals("Интернет", ignoreCase = true) -> 
            Icons.Default.Wifi to expenseColor
        category.equals("Развлечения", ignoreCase = true) ->
            Icons.Default.SportsEsports to expenseColor
        category.equals("Покупки", ignoreCase = true) ->
            Icons.Default.ShoppingBag to expenseColor
        category.equals("Животные", ignoreCase = true) ->
            Icons.Default.Pets to expenseColor
        category.equals("Комиссия", ignoreCase = true) || 
        category.equals("Счета", ignoreCase = true) ->
            Icons.Default.Receipt to expenseColor
        category.equals("Жилье", ignoreCase = true) ->
            Icons.Default.Home to expenseColor
        category.equals("Кредит", ignoreCase = true) ||
        category.equals("Карта", ignoreCase = true) ->
            Icons.Default.CreditCard to expenseColor
        category.equals("Работа", ignoreCase = true) -> 
            Icons.Default.WorkOutline to expenseColor
        else -> if (isExpense) 
            Icons.Default.ShoppingCart to expenseColor
        else 
            Icons.Default.MonetizationOn to incomeColor
    }
    
    return CategoryInfo(icon, bgColor)
} 