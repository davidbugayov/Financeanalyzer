package com.davidbugayov.financeanalyzer.presentation.chart.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor

/**
 * Круговая диаграмма для отображения распределения расходов/доходов по категориям.
 * Включает в себя анимацию, интерактивность и легенду с процентным соотношением каждой категории.
 *
 * @param data Карта с данными, где ключ - название категории, значение - сумма
 * @param isIncome Флаг, указывающий тип данных (true - доходы, false - расходы)
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun CategoryPieChart(
    data: Map<String, Money>,
    isIncome: Boolean = false,
    modifier: Modifier = Modifier,
    onCategorySelected: (String) -> Unit = {}
) {
    if (data.isEmpty()) return
    
    val total = data.values.fold(Money.zero()) { acc, value -> acc + value }
    val density = LocalDensity.current
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val expenseColor = LocalExpenseColor.current

    // Преобразуем Compose Color в Int для Paint
    val textColor = if (isIncome) {
        android.graphics.Color.argb(
            255,
            102, // 0x66
            187, // 0xBB
            106  // 0x6A
        )
    } else {
        android.graphics.Color.argb(
            (expenseColor.alpha * 255).toInt(),
            (expenseColor.red * 255).toInt(),
            (expenseColor.green * 255).toInt(),
            (expenseColor.blue * 255).toInt()
        )
    }

    val textColorAlpha = if (isIncome) {
        android.graphics.Color.argb(
            (255 * 0.7f).toInt(),
            102, // 0x66
            187, // 0xBB
            106  // 0x6A
        )
    } else {
        android.graphics.Color.argb(
            (expenseColor.alpha * 255 * 0.7f).toInt(),
            (expenseColor.red * 255).toInt(),
            (expenseColor.green * 255).toInt(),
            (expenseColor.blue * 255).toInt()
        )
    }

    // Цвета для диаграммы
    val colors = if (isIncome) {
        listOf(
            Color(0xFF66BB6A), // Зеленый
            Color(0xFF81C784),
            Color(0xFF4CAF50),
            Color(0xFF2E7D32),
            Color(0xFF43A047),
            Color(0xFF388E3C),
            Color(0xFF1B5E20),
            Color(0xFF00C853),
            Color(0xFF00E676)
        )
    } else {
        listOf(
            Color(0xFFEF5350), // Красный
            Color(0xFFE57373),
            Color(0xFFEF9A9A),
            Color(0xFFD32F2F),
            Color(0xFFC62828),
            Color(0xFFB71C1C),
            Color(0xFFFF8A80),
            Color(0xFFFF5252),
            Color(0xFFFF1744)
        )
    }

    // Сортируем данные по убыванию для лучшего отображения
    val sortedData = data.entries.sortedByDescending { it.value.amount }

    // Создаем карту цветов для категорий
    val colorMap = sortedData.mapIndexed { index, entry ->
        entry.key to colors[index % colors.size]
    }.toMap()

    // Состояние для выбранной категории
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Анимация для выделения сектора
    val animatedProgress = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500),
        label = "PieChartAnimation"
    )

    // Создаем Paint объекты для текста
    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 32.sp.toPx() }
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val subTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 16.sp.toPx() }
            textAlign = Paint.Align.CENTER
            alpha = 180
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(surfaceVariantColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2
            val center = Offset(canvasWidth / 2, canvasHeight / 2)

            var startAngle = -90f // Начинаем с верхней точки
            val angleProgress = animatedProgress.value

            // Рисуем секторы
            sortedData.forEach { (category, value) ->
                val sweepAngle = (value.amount.toDouble() / total.amount.toDouble() * 360 * angleProgress).toFloat()
                val isSelected = category == selectedCategory

                // Рисуем сектор
                drawArc(
                    color = colorMap[category] ?: Color.Gray,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(
                        center.x - radius + (if (isSelected) -4f else 0f),
                        center.y - radius + (if (isSelected) -4f else 0f)
                    ),
                    size = Size(
                        radius * 2 + (if (isSelected) 8f else 0f),
                        radius * 2 + (if (isSelected) 8f else 0f)
                    ),
                    alpha = if (isSelected) 1f else 0.8f
                )

                if (isSelected) {
                    onCategorySelected(category)
                }

                // Рисуем тонкую белую обводку
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 1f),
                    topLeft = Offset(
                        center.x - radius + (if (isSelected) -4f else 0f),
                        center.y - radius + (if (isSelected) -4f else 0f)
                    ),
                    size = Size(
                        radius * 2 + (if (isSelected) 8f else 0f),
                        radius * 2 + (if (isSelected) 8f else 0f)
                    )
                )

                startAngle += sweepAngle
            }

            // Рисуем внутренний круг
            drawCircle(
                color = surfaceVariantColor,
                radius = radius * 0.55f,
                center = center
            )

            // Отображаем общую сумму в центре
            textPaint.apply {
                color = textColor
            }
            drawContext.canvas.nativeCanvas.drawText(
                total.format(true),
                center.x,
                center.y - with(density) { 10.dp.toPx() },
                textPaint
            )

            // Отображаем "расход" под суммой
            subTextPaint.apply {
                color = textColorAlpha
            }
            drawContext.canvas.nativeCanvas.drawText(
                if (isIncome) "доход" else "расход",
                center.x,
                center.y + with(density) { 20.dp.toPx() },
                subTextPaint
            )
        }
    }
}

/**
 * Компонент для отображения списка категорий
 */
@Composable
fun CategoryList(
    data: Map<String, Money>,
    isIncome: Boolean = false,
    modifier: Modifier = Modifier,
    onCategorySelected: (String) -> Unit = {}
) {
    if (data.isEmpty()) return

    val total = data.values.fold(Money.zero()) { acc, value -> acc + value }
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    // Цвета для категорий
    val colors = if (isIncome) {
        listOf(
            Color(0xFF66BB6A), // Зеленый
            Color(0xFF81C784),
            Color(0xFF4CAF50),
            Color(0xFF2E7D32),
            Color(0xFF43A047),
            Color(0xFF388E3C),
            Color(0xFF1B5E20),
            Color(0xFF00C853),
            Color(0xFF00E676)
        )
    } else {
        listOf(
            Color(0xFFEF5350), // Красный
            Color(0xFFE57373),
            Color(0xFFEF9A9A),
            Color(0xFFD32F2F),
            Color(0xFFC62828),
            Color(0xFFB71C1C),
            Color(0xFFFF8A80),
            Color(0xFFFF5252),
            Color(0xFFFF1744)
        )
    }

    // Сортируем данные по убыванию для лучшего отображения
    val sortedData = data.entries.sortedByDescending { it.value.amount }

    // Создаем карту цветов для категорий
    val colorMap = sortedData.mapIndexed { index, entry ->
        entry.key to colors[index % colors.size]
    }.toMap()

    // Отображаем список категорий
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceVariantColor)
            .padding(horizontal = 16.dp)
    ) {
        sortedData.forEach { (category, amount) ->
            val percentage = amount.amount.toDouble() / total.amount.toDouble() * 100

            CategoryListItem(
                categoryName = category,
                amount = amount,
                percentage = percentage,
                color = colorMap[category] ?: Color.Gray,
                isIncome = isIncome,
                modifier = Modifier.clickable { onCategorySelected(category) }
            )
        }
    }
}

/**
 * Элемент списка категорий
 */
@Composable
fun CategoryListItem(
    categoryName: String,
    amount: Money,
    percentage: Double,
    color: Color,
    isIncome: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val amountColor = color  // Используем тот же цвет, что и в секторе диаграммы

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(color.copy(alpha = 0.1f))
            .padding(8.dp)  // Добавляем внутренний отступ для лучшего вида
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor
        )

        Text(
            text = "${amount.format(true)} (${String.format("%.1f", percentage)}%)",
            style = MaterialTheme.typography.bodyMedium,
            color = amountColor
        )
    }
} 