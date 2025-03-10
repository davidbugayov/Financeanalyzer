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
            .background(Color(0xFF1C1B1F)),
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
                    )
                )

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
                color = Color(0xFF1C1B1F),
                radius = radius * 0.55f,
                center = center
            )

            // Отображаем общую сумму в центре
            drawContext.canvas.nativeCanvas.drawText(
                total.format(false),
                center.x,
                center.y,
                textPaint
            )

            val subText = if (isIncome) "доход" else "расход"
            drawContext.canvas.nativeCanvas.drawText(
                subText,
                center.x,
                center.y + with(density) { 30.dp.toPx() },
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

    // Отображаем общую сумму и название
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1B1F))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = total.format(false),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isIncome) "доход" else "расход",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }

    // Отображаем список категорий
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1B1F))
            .padding(horizontal = 16.dp)
    ) {
        sortedData.forEach { (category, amount) ->
            val percentage = amount.amount.toDouble() / total.amount.toDouble() * 100

            CategoryListItem(
                categoryName = category,
                amount = amount,
                percentage = percentage,
                color = colorMap[category] ?: Color.Gray,
                modifier = Modifier.clickable { onCategorySelected(category) }
            )
        }
    }
} 