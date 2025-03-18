package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.amountFormatted
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_alfa
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_gazprombank
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_raiffeisen
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_sber
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_tinkoff
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_vtb
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Возвращает иконку для категории транзакции.
 *
 * @param category Название категории
 * @param isExpense Является ли транзакция расходом
 * @return Иконка для категории
 */
@Composable
fun getCategoryIcon(category: String, isExpense: Boolean): ImageVector {
    return when {
        // Иконки для расходов
        isExpense && category.contains("Продукты", ignoreCase = true) -> Icons.Default.LocalGroceryStore
        isExpense && category.contains("Кафе", ignoreCase = true) -> Icons.Default.Fastfood
        isExpense && category.contains("Рестораны", ignoreCase = true) -> Icons.Default.Fastfood
        isExpense && category.contains("Транспорт", ignoreCase = true) -> Icons.Default.DirectionsCar
        isExpense && category.contains("Жилье", ignoreCase = true) -> Icons.Default.Home
        isExpense && category.contains("Коммунальные", ignoreCase = true) -> Icons.Default.Home
        isExpense && category.contains("Здоровье", ignoreCase = true) -> Icons.Default.LocalHospital
        isExpense && category.contains("Медицина", ignoreCase = true) -> Icons.Default.LocalHospital
        isExpense && category.contains("Развлечения", ignoreCase = true) -> Icons.Default.TheaterComedy
        isExpense && category.contains("Покупки", ignoreCase = true) -> Icons.Default.ShoppingCart
        isExpense && category.contains("Образование", ignoreCase = true) -> Icons.Default.School
        isExpense && category.contains("Связь", ignoreCase = true) -> Icons.Default.Smartphone
        isExpense && category.contains("Игры", ignoreCase = true) -> Icons.Default.SportsEsports
        isExpense && category.contains("Услуги", ignoreCase = true) -> Icons.Default.Work
        isExpense && category.contains("Благотворительность", ignoreCase = true) -> Icons.Default.Payments
        isExpense && category.contains("Кредит", ignoreCase = true) -> Icons.Default.CreditCard

        // Иконки для доходов
        !isExpense && category.contains("Зарплата", ignoreCase = true) -> Icons.Default.Work
        !isExpense && category.contains("Фриланс", ignoreCase = true) -> Icons.Default.Work
        !isExpense && category.contains("Подработка", ignoreCase = true) -> Icons.Default.Work
        !isExpense && category.contains("Инвестиции", ignoreCase = true) -> Icons.Default.AttachMoney
        !isExpense && category.contains("Проценты", ignoreCase = true) -> Icons.Default.AttachMoney
        !isExpense && category.contains("Дивиденды", ignoreCase = true) -> Icons.Default.AttachMoney
        !isExpense && category.contains("Подарок", ignoreCase = true) -> Icons.Default.Payments

        // Стандартные иконки для доходов/расходов
        isExpense -> Icons.Default.ArrowDownward
        else -> Icons.Default.ArrowUpward
    }
}

/**
 * Возвращает иконку и цвет для банка.
 *
 * @param bankName Название банка
 * @return Пара из иконки и цвета для банка
 */
@Composable
fun getBankIcon(bankName: String): Pair<ImageVector, Color> {
    return when {
        bankName.contains("Сбер", ignoreCase = true) ->
            Pair(Icons.Default.AccountBalance, md_theme_sber)

        bankName.contains("Альфа", ignoreCase = true) ->
            Pair(Icons.Default.AccountBalance, md_theme_alfa)

        bankName.contains("Тинькофф", ignoreCase = true) ->
            Pair(Icons.Default.CreditCard, md_theme_tinkoff)

        bankName.contains("ВТБ", ignoreCase = true) ->
            Pair(Icons.Default.AccountBalance, md_theme_vtb)

        bankName.contains("Газпром", ignoreCase = true) ->
            Pair(Icons.Default.AccountBalance, md_theme_gazprombank)

        bankName.contains("Райффайзен", ignoreCase = true) ->
            Pair(Icons.Default.AccountBalance, md_theme_raiffeisen)

        bankName.contains("Наличные", ignoreCase = true) ->
            Pair(Icons.Default.LocalAtm, MaterialTheme.colorScheme.tertiary)

        else -> Pair(Icons.Default.CreditCard, MaterialTheme.colorScheme.tertiary)
    }
}

/**
 * Элемент списка транзакций с визуальным индикатором типа (доход/расход).
 */
@Composable
fun TransactionItem(transaction: Transaction) {
    // Используем локаль устройства для форматирования даты
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val formattedDate = remember(transaction.date) { dateFormat.format(transaction.date) }

    // Определяем цвета в зависимости от типа транзакции
    val indicatorColor = if (transaction.isExpense)
        Color(0xFFB71C1C) // Темно-красный для расходов
    else
        Color(0xFF2E7D32) // Темно-зеленый для доходов
        
    val backgroundTint = if (transaction.isExpense)
        Color(0xFFFFE0E0) // Светло-красный фон для расходов
    else
        Color(0xFFE0F7E0) // Светло-зеленый фон для доходов

    // Получаем иконку для категории
    val categoryIcon = getCategoryIcon(transaction.category, transaction.isExpense)

    // Получаем иконки и цвета для банков
    val (sourceIcon, sourceColor) = getBankIcon(transaction.source ?: "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Вертикальная цветовая полоса-индикатор
        // Box(
        //     modifier = Modifier
        //         .width(4.dp)
        //         .height(60.dp)
        //         .clip(MaterialTheme.shapes.small)
        //         .background(indicatorColor)
        // )

        Spacer(modifier = Modifier.width(8.dp))

        // Иконка категории транзакции
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = transaction.category,
                tint = indicatorColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Информация о транзакции
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Информация о категории и дате
            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Информация о банках (источник -> получатель)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Иконка банка-источника
                Icon(
                    imageVector = sourceIcon,
                    contentDescription = transaction.source,
                    tint = sourceColor,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(modifier = Modifier.width(2.dp))

                // Название банка-источника
                Text(
                    text = transaction.source ?: "",
                    fontSize = 11.sp,
                    color = sourceColor,
                    fontWeight = FontWeight.Medium
                )

                // Если получатель "Трата", показываем текст "снятие" или "пополнение"
                Spacer(modifier = Modifier.width(4.dp))

                val operationText = if (transaction.isExpense)
                    stringResource(R.string.cash_withdrawal)
                else
                    stringResource(R.string.cash_deposit)

                Text(
                    text = "• $operationText",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Примечание к транзакции
            transaction.note?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Сумма транзакции
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = transaction.amountFormatted(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = indicatorColor
            )
        }
    }
}

/**
 * Компонент для отображения транзакции с поддержкой нажатия и долгого нажатия.
 *
 * @param transaction Транзакция для отображения
 * @param onClick Callback, вызываемый при нажатии на транзакцию
 * @param onLongClick Callback, вызываемый при долгом нажатии на транзакцию
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItemWithActions(
    transaction: Transaction,
    onClick: (Transaction) -> Unit,
    onLongClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(transaction) },
                    onLongClick = { onLongClick(transaction) }
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            TransactionItem(transaction = transaction)
        }
    }
}

/**
 * Диалог подтверждения удаления транзакции.
 *
 * @param transaction Транзакция для удаления
 * @param onConfirm Callback, вызываемый при подтверждении удаления
 * @param onDismiss Callback, вызываемый при отмене удаления
 */
@Composable
fun DeleteTransactionDialog(
    transaction: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val indicatorColor = if (transaction.isExpense)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.primary

    val categoryIcon = getCategoryIcon(transaction.category, transaction.isExpense)
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val formattedDate = remember(transaction.date) { dateFormat.format(transaction.date) }

    // Получаем иконки и цвета для банков
    val (sourceIcon, sourceColor) = getBankIcon(transaction.source ?: "")

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = categoryIcon,
                contentDescription = transaction.category,
                tint = indicatorColor,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.delete_transaction),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Информация о категории и дате
                Text(
                    text = stringResource(
                        R.string.category_date_format,
                        transaction.category,
                        formattedDate
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Информация о банках (источник -> получатель)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Иконка банка-источника
                    Icon(
                        imageVector = sourceIcon,
                        contentDescription = transaction.source,
                        tint = sourceColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Название банка-источника
                    Text(
                        text = transaction.source ?: "",
                        fontSize = 14.sp,
                        color = sourceColor,
                        fontWeight = FontWeight.Medium
                    )

                        // Если трата то "снятие" или "пополнение"
                        Spacer(modifier = Modifier.width(8.dp))

                        val operationText = if (transaction.isExpense)
                            stringResource(R.string.cash_withdrawal)
                        else
                            stringResource(R.string.cash_deposit)

                        Text(
                            text = "• $operationText",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Сумма транзакции
                Text(
                    text = stringResource(
                        R.string.delete_transaction_confirmation,
                        transaction.category,
                        String.format("%.2f", transaction.amount)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                transaction.note?.let {
                    if (it.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 