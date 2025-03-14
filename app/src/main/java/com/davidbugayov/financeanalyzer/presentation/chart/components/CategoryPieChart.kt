package com.davidbugayov.financeanalyzer.presentation.chart.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
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
    modifier: Modifier = Modifier,
    isIncome: Boolean = false
) {
    if (data.isEmpty()) return
    
    val total = data.values.fold(Money.zero()) { acc, value -> acc + value }
    val density = LocalDensity.current
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val expenseColor = LocalExpenseColor.current

    // Получаем строки заранее
    val incomeText = stringResource(R.string.income_label_short)
    val expenseText = stringResource(R.string.expense_label_short)

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

    // Convert dimension resources to TextUnit (sp)
    val textSizeXXXLarge = with(density) { 32.sp }
    val textSizeNormal = with(density) { 16.sp }

    // Создаем Paint объекты для текста
    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { textSizeXXXLarge.toPx() }
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val subTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { textSizeNormal.toPx() }
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
                .size(dimensionResource(R.dimen.chart_height_large))
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
                    selectedCategory = category
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
                if (isIncome) incomeText else expenseText,
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
fun CategoryPieChartList(
    data: Map<String, Money>,
    modifier: Modifier = Modifier,
    isIncome: Boolean = false
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
            .padding(horizontal = dimensionResource(R.dimen.spacing_large))
    ) {
        sortedData.forEach { (category, amount) ->
            val percentage = amount.amount.toDouble() / total.amount.toDouble() * 100

            CategoryListItem(
                categoryName = category,
                amount = amount,
                percentage = percentage,
                color = colorMap[category] ?: Color.Gray,
                modifier = Modifier
            )
        }
    }
}

/**
 * Generates a list of colors for the pie chart segments.
 *
 * @param count Number of colors needed
 * @param isIncome Whether the data represents income (true) or expenses (false)
 * @return List of colors
 */
private fun generateCategoryColors(count: Int, isIncome: Boolean): List<Color> {
    val baseColor = if (isIncome) {
        Color(0xFF66BB6A) // Green for income
    } else {
        Color(0xFFEF5350) // Red for expenses
    }

    return List(count) { index ->
        val hue = (baseColor.hue + index * 30f) % 360f
        Color.hsv(
            hue = hue,
            saturation = 0.7f,
            value = 0.9f
        )
    }
}

/**
 * Extension function to get the hue component of a Color.
 */
private val Color.hue: Float
    get() {
        val min = minOf(red, green, blue)
        val max = maxOf(red, green, blue)

        if (min == max) return 0f

        val hue = when (max) {
            red -> (green - blue) / (max - min) * 60f
            green -> (blue - red) / (max - min) * 60f + 120f
            blue -> (red - green) / (max - min) * 60f + 240f
            else -> 0f
        }

        return (hue + 360f) % 360f
    }

/**
 * Creates a color from HSV values.
 */
private fun Color.Companion.hsv(hue: Float, saturation: Float, value: Float): Color {
    val c = value * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
    val m = value - c

    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = r + m,
        green = g + m,
        blue = b + m,
        alpha = 1f
    )
} 