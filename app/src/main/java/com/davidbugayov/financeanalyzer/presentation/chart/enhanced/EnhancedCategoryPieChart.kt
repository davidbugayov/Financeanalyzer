package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

// Add these imports and variable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

// List to store sector positions for interaction
private val sectorPositions = mutableListOf<PieSectorInfo>()

/**
 * Улучшенный пирограф для отображения распределения по категориям.
 * Включает интерактивность, анимацию и современный дизайн.
 *
 * @param data Карта с данными категорий, где ключ - название категории, значение - сумма
 * @param isIncome Флаг, указывающий тип данных (true - доходы, false - расходы)
 * @param onCategorySelected Обработчик выбора категории (опционально)
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun EnhancedCategoryPieChart(
    data: Map<String, Money>,
    isIncome: Boolean = false,
    onCategorySelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartState(isIncome = isIncome, modifier = modifier)
        return
    }

    // Общая сумма для расчета процентов
    val total = data.values.fold(Money.zero()) { acc, value -> acc + value }

    // Сортируем данные по убыванию суммы
    val sortedData = data.entries.sortedByDescending { it.value.amount }

    // Определяем цвета для категорий
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val baseColor = if (isIncome) incomeColor else expenseColor

    // Создаем список цветов для категорий
    val categoryColors = remember(sortedData.size, isIncome) {
        getCategoryColors(sortedData.size, isIncome)
    }

    // Состояние для выбранной категории
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Состояние анимации
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "PieChartAnimation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с переключателем типа (доходы/расходы)
            Text(
                text = if (isIncome) "Доходы" else "Расходы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = baseColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Пирограф с центральной суммой
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable {
                        // Обработка клика по всей карточке для сброса выбора
                        selectedCategory = null
                        onCategorySelected?.invoke("")
                    },
                contentAlignment = Alignment.Center
            ) {
                // Сам пирограф
                PieChartCanvas(
                    data = sortedData,
                    total = total,
                    categoryColors = categoryColors,
                    animatedProgress = animatedProgress,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        Timber.d("PieChart: выбрана категория '$category', текущая: $selectedCategory, это доход: $isIncome")
                        // Если пришла пустая строка или текущая категория, сбрасываем выбор
                        selectedCategory = if (category.isEmpty() || selectedCategory == category) null else category
                        Timber.d("PieChart: обновлена выбранная категория на: $selectedCategory")
                        if (category.isNotEmpty() || selectedCategory == null) {
                            Timber.d("PieChart: вызываем onCategorySelected callback с: $category")
                            onCategorySelected?.invoke(category)
                        }
                    },
                    modifier = Modifier.matchParentSize()
                )

                // Показываем центральный контент в зависимости от выбора категории
                if (selectedCategory != null) {
                    // Отображаем информацию о выбранной категории
                    val categoryData = sortedData.firstOrNull { it.key == selectedCategory }
                    categoryData?.let { (category, value) ->
                        val categoryIndex = sortedData.indexOfFirst { it.key == category }
                        val categoryColor = categoryColors[categoryIndex % categoryColors.size]
                        val percentage = (value.amount.toDouble() / total.amount.toDouble()) * 100

                        SelectedCategoryInfoBox(
                            category = category,
                            value = value,
                            percentage = percentage,
                            color = categoryColor
                        )
                    }
                } else {
                    // Центральная сумма (общий итог)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = total.format(true),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = baseColor
                        )
                        Text(
                            text = if (isIncome) "Доход" else "Расход",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Легенда с категориями
            CategoryLegend(
                categories = sortedData,
                total = total,
                categoryColors = categoryColors,
                selectedCategory = selectedCategory,
                isIncome = isIncome,
                onCategorySelected = { category ->
                    Timber.d("Легенда: выбрана категория '$category', текущая: $selectedCategory, это доход: $isIncome")
                    selectedCategory = if (selectedCategory == category) null else category
                    Timber.d("Легенда: обновлена выбранная категория на: $selectedCategory")
                    onCategorySelected?.invoke(category)
                }
            )
        }
    }
}

/**
 * Canvas для рисования пирографа
 */
@Composable
private fun PieChartCanvas(
    data: List<Map.Entry<String, Money>>,
    total: Money,
    categoryColors: List<Color>,
    animatedProgress: Float,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LocalDensity.current
    remember { -90f }

    // Extract theme colors outside of DrawScope
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    val centerX = canvasWidth / 2
                    val centerY = canvasHeight / 2
                    val radius = minOf(centerX, centerY) * 0.9f

                    // Проверяем, попала ли точка в круг
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val distanceFromCenter = sqrt(dx * dx + dy * dy)

                    // Если клик за пределами пирога - отправляем пустую строку
                    if (distanceFromCenter > radius) {
                        onCategorySelected("")
                        return@detectTapGestures
                    }

                    // Вычисляем угол клика
                    val angle = (atan2(dy, dx) * 180 / PI).toFloat()
                    val normalizedAngle = if (angle < 0) angle + 360 else angle
                    val adjustedAngle = (normalizedAngle + 90) % 360

                    Timber.d("Клик: угол=$adjustedAngle, координаты: x=${offset.x}, y=${offset.y}")

                    // Проверяем, является ли клик внутри внутреннего круга (дырки)
                    val innerCircleRadius = minOf(centerX, centerY) * 0.42f * 0.65f // такой же как innerRadius in Canvas
                    if (distanceFromCenter < innerCircleRadius) {
                        Timber.d("Клик внутри дырки пирографа")
                        onCategorySelected("")
                        return@detectTapGestures
                    }

                    // Определяем, на какой сектор произошел клик с более подробной отладкой
                    var currentAngle = 0f
                    for ((_, entry) in data.withIndex()) {
                        val sweepAngle = (entry.value.amount.toFloat() / total.amount.toFloat()) * 360f * animatedProgress
                        val sectorStart = currentAngle
                        val sectorEnd = currentAngle + sweepAngle

                        Timber.d("Сектор '${entry.key}': start=$sectorStart, end=$sectorEnd, value=${entry.value}, sweepAngle=$sweepAngle")

                        // Проверка с учетом возможного перехода через 0/360 градусов
                        val contains = if (sectorStart <= sectorEnd) {
                            adjustedAngle >= sectorStart && adjustedAngle <= sectorEnd
                        } else {
                            // Сектор переходит через 0/360
                            adjustedAngle >= sectorStart || adjustedAngle <= sectorEnd
                        }

                        if (contains) {
                            Timber.d("Выбран сектор: ${entry.key}")
                            onCategorySelected(entry.key)
                            return@detectTapGestures
                        }

                        currentAngle += sweepAngle
                    }

                    // Если не попали ни в один сектор
                    Timber.d("Клик не попал ни в один сектор")
                    onCategorySelected("")
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = min(canvasWidth, canvasHeight) * 0.42f
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val innerRadius = radius * 0.65f

        var startAngle = -90f

        // Очищаем предыдущие позиции
        sectorPositions.clear()

        // Рисуем фоновый круг
        drawCircle(
            color = surfaceVariantColor.copy(alpha = 0.3f),
            radius = radius,
            center = Offset(centerX, centerY)
        )

        // Рисуем секторы
        data.forEachIndexed { index, (category, value) ->
            val sweepAngle = (value.amount.toFloat() / total.amount.toFloat()) * 360f * animatedProgress
            val isSelected = category == selectedCategory

            // Рисуем сектор
            drawPieSection(
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                color = categoryColors[index % categoryColors.size],
                isSelected = isSelected,
                centerX = centerX,
                centerY = centerY,
                radius = radius
            )

            // Запоминаем позицию сектора для обработки кликов
            sectorPositions.add(
                PieSectorInfo(
                    category = category,
                    center = Offset(centerX, centerY),
                    outerRadius = radius,
                    innerRadius = innerRadius,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle
                )
            )

            startAngle += sweepAngle
        }

        // Рисуем внутренний круг, чтобы сделать пончик вместо пирога
        drawCircle(
            color = surfaceColor,
            radius = innerRadius,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Рисование сектора пирографа
 */
private fun DrawScope.drawPieSection(
    startAngle: Float,
    sweepAngle: Float,
    color: Color,
    isSelected: Boolean,
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    // Увеличиваем выбранный сектор и добавляем тень
    val sectionRadius = if (isSelected) radius * 1.08f else radius

    // Добавляем отступ между секторами
    val gap = 2f
    val adjustedSweepAngle = if (sweepAngle > 5f) sweepAngle - gap else sweepAngle

    // Рисуем сектор
    rotate(startAngle + sweepAngle / 2) {
        // Если сектор выбран, добавляем тень
        if (isSelected) {
            // Рисуем тень под сектором
            drawArc(
                color = Color.Black.copy(alpha = 0.1f),
                startAngle = -adjustedSweepAngle / 2,
                sweepAngle = adjustedSweepAngle,
                useCenter = true,
                topLeft = Offset(
                    centerX - sectionRadius - 4f,
                    centerY - sectionRadius - 4f
                ),
                size = Size(
                    (sectionRadius + 4f) * 2,
                    (sectionRadius + 4f) * 2
                )
            )
        }

        // Рисуем основной сектор
        drawArc(
            color = if (isSelected) color.copy(alpha = 1f) else color.copy(alpha = 0.9f),
            startAngle = -adjustedSweepAngle / 2,
            sweepAngle = adjustedSweepAngle,
            useCenter = true,
            topLeft = Offset(
                centerX - sectionRadius,
                centerY - sectionRadius
            ),
            size = Size(
                sectionRadius * 2,
                sectionRadius * 2
            )
        )

        // Добавляем обводку для выделенного сектора
        if (isSelected) {
            // Внешняя обводка
            drawArc(
                color = Color.White,
                startAngle = -adjustedSweepAngle / 2,
                sweepAngle = adjustedSweepAngle,
                useCenter = false,
                style = Stroke(width = 3f),
                topLeft = Offset(
                    centerX - sectionRadius,
                    centerY - sectionRadius
                ),
                size = Size(
                    sectionRadius * 2,
                    sectionRadius * 2
                ),
                alpha = 0.7f
            )

            // Внутренняя обводка
            drawArc(
                color = Color.White,
                startAngle = -adjustedSweepAngle / 2,
                sweepAngle = adjustedSweepAngle,
                useCenter = false,
                style = Stroke(width = 2f),
                topLeft = Offset(
                    centerX - sectionRadius * 0.65f,
                    centerY - sectionRadius * 0.65f
                ),
                size = Size(
                    sectionRadius * 2 * 0.65f,
                    sectionRadius * 2 * 0.65f
                ),
                alpha = 0.7f
            )
        }
    }
}

/**
 * Легенда с категориями, процентами и суммами
 */
@Composable
private fun CategoryLegend(
    categories: List<Map.Entry<String, Money>>,
    total: Money,
    categoryColors: List<Color>,
    selectedCategory: String?,
    isIncome: Boolean,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        categories.forEachIndexed { index, (category, value) ->
            val isSelected = category == selectedCategory
            val percentage = if (!total.isZero()) {
                (value.amount.toDouble() / total.amount.toDouble()) * 100
            } else 0.0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) categoryColors[index % categoryColors.size].copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onCategorySelected(category) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Цветной индикатор категории
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(categoryColors[index % categoryColors.size])
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Название категории
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Сумма и процент в одной строке
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = value.format(false),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) categoryColors[index % categoryColors.size]
                        else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = String.format("%.1f%%", percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) categoryColors[index % categoryColors.size].copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // Убираем разделители для более компактного вида как на скриншоте
        }
    }
}

/**
 * Пустое состояние для отображения, когда нет данных
 */
@Composable
private fun EmptyChartState(
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isIncome) "Нет данных о доходах" else "Нет данных о расходах",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Добавьте транзакции, чтобы увидеть график",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Генерирует цвета для категорий
 */
private fun getCategoryColors(count: Int, isIncome: Boolean): List<Color> {
    return if (isIncome) {
        listOf(
            Color(0xFF4CAF50), // Green
            Color(0xFF2196F3), // Blue
            Color(0xFF9C27B0), // Purple
            Color(0xFF3F51B5), // Indigo
            Color(0xFF00BCD4), // Cyan
            Color(0xFF009688), // Teal
            Color(0xFF8BC34A), // Light Green
            Color(0xFF673AB7), // Deep Purple
            Color(0xFFCDDC39), // Lime
            Color(0xFF00ACC1), // Cyan variant
            Color(0xFF43A047), // Green variant
            Color(0xFF1E88E5)  // Blue variant
        )
    } else {
        listOf(
            Color(0xFFF44336), // Red
            Color(0xFFFF9800), // Orange
            Color(0xFFE91E63), // Pink
            Color(0xFFFF5722), // Deep Orange
            Color(0xFFFFC107), // Amber
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFE53935), // Red variant
            Color(0xFFD81B60), // Pink variant
            Color(0xFFEF6C00), // Orange variant
            Color(0xFFF4511E), // Deep Orange variant
            Color(0xFFFB8C00), // Orange variant
            Color(0xFFFFCA28)  // Amber variant
        )
    }
}

/**
 * Класс для хранения информации о секторе пирографа
 */
private data class PieSectorInfo(
    val category: String,
    val center: Offset,
    val outerRadius: Float,
    val innerRadius: Float,
    val startAngle: Float,
    val sweepAngle: Float
)

/**
 * Проверяет, находится ли точка внутри сектора кольцевого графика
 */
private fun isPointInSector(
    point: Offset,
    center: Offset,
    radius: Float,
    innerRadius: Float,
    startAngle: Float,
    sweepAngle: Float
): Boolean {
    // Расстояние от центра до точки
    val dx = point.x - center.x
    val dy = point.y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    // Проверяем, находится ли точка в пределах кольца
    if (distance < innerRadius || distance > radius) {
        return false
    }

    // Вычисляем угол в градусах
    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

    // Преобразуем угол к нашей системе координат
    if (angle < 0) angle += 360f

    // Преобразуем startAngle в диапазон 0-360
    var sectorStartAngle = startAngle % 360
    if (sectorStartAngle < 0) sectorStartAngle += 360f

    // Вычисляем конечный угол сектора
    val sectorEndAngle = (sectorStartAngle + sweepAngle) % 360

    // Проверяем, попадает ли угол в диапазон сектора
    return if (sectorStartAngle <= sectorEndAngle) {
        angle >= sectorStartAngle && angle <= sectorEndAngle
    } else {
        // Сектор переходит через 0 градусов
        angle >= sectorStartAngle || angle <= sectorEndAngle
    }
}

/**
 * Информационный блок для выбранной категории
 */
@Composable
private fun SelectedCategoryInfoBox(
    category: String,
    value: Money,
    percentage: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
    ) {
        // Сумма (более крупно)
        Text(
            text = value.format(true),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        // Название категории под суммой
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
} 