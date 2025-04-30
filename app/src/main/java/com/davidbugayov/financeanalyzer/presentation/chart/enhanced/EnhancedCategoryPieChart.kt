package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartItemData
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils.PieChartUtilsExt
import androidx.compose.foundation.layout.Arrangement
import java.math.BigDecimal
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
/**
 * Enhanced category pie chart that shows categories distribution with an interactive legend
 * and compact size for better visibility
 * 
 * @param items List of PieChartItemData to display
 * @param selectedIndex Initial selected sector index
 * @param onSectorClick Callback when a sector is clicked with the item data
 * @param modifier Modifier for the chart
 */
@Composable
fun EnhancedCategoryPieChart(
    items: List<PieChartItemData>,
    selectedIndex: Int? = null,
    onSectorClick: (PieChartItemData?) -> Unit = {},
    modifier: Modifier = Modifier,
    showExpenses: Boolean = true,
    onShowExpensesChange: (Boolean) -> Unit = {}
) {
    // Filter out items with zero or negative percentages
    val filteredData = remember(items) {
        items.filter { it.percentage > 0f }
    }
    
    if (filteredData.isEmpty()) {
        // Show empty state if no valid data
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет доступных данных",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    
    // Calculate total amount from the filtered data
    val totalAmount = filteredData.sumOf { it.amount.toDouble() }.toFloat()
    val totalMoney = Money(BigDecimal.valueOf(totalAmount.toDouble()))
    
    // State for selected indices
    val selectedIndices = remember(selectedIndex, filteredData) { 
        mutableStateOf(
            if (selectedIndex != null && selectedIndex >= 0 && selectedIndex < filteredData.size) 
                setOf(selectedIndex) 
            else 
                emptySet()
        )
    }
    
    // Get selected item if any
    val selectedItem = when {
        selectedIndices.value.isEmpty() -> null
        else -> {
            val index = selectedIndices.value.first()
            if (index >= 0 && index < filteredData.size) filteredData[index] else null
        }
    }
    
    // Determine if the first category is income
    val isIncome = filteredData.firstOrNull()?.category?.isExpense == false
    
    // Use this color for center text
    val centerTextColor = MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
        ) {
            // Горизонтальный переключатель доходы/расходы
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Расходы",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (showExpenses) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (showExpenses) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onShowExpensesChange(true) }
                )
                
                Text(
                    text = "Доходы",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (!showExpenses) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (!showExpenses) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onShowExpensesChange(false) }
                )
            }
            
            // Диаграмма сверху (по центру)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                DrawPieChart(
                    modifier = Modifier.size(220.dp),
                    data = filteredData,
                    selectedIndices = selectedIndices.value,
                    onSectorClick = { index ->
                        // Обновляем выбранные индексы
                        selectedIndices.value = when {
                            // Если кликнули по центру или повторно по тому же сектору - сбрасываем выбор
                            index < 0 || selectedIndices.value.contains(index) -> emptySet()
                            // Иначе выбираем новый сектор, если индекс валидный
                            index < filteredData.size -> setOf(index)
                            // В случае невалидного индекса (что не должно происходить) - сохраняем текущее состояние
                            else -> selectedIndices.value
                        }
                        
                        // Оповещаем о выбранной категории или null, если ничего не выбрано
                        val newSelectedItem = if (selectedIndices.value.isEmpty()) {
                            null
                        } else {
                            val selectedIdx = selectedIndices.value.first()
                            if (selectedIdx >= 0 && selectedIdx < filteredData.size) {
                                filteredData[selectedIdx]
                            } else {
                                null
                            }
                        }
                        
                        onSectorClick(newSelectedItem)
                    },
                    totalMoney = totalMoney,
                    selectedItem = selectedItem,
                    isIncome = isIncome,
                    centerTextColor = centerTextColor
                )
            }
            
            // Список категорий снизу
            DrawCategoryLegend(
                items = filteredData,
                selectedIndices = selectedIndices.value,
                onItemClick = { index ->
                    // Тот же подход, что и в onSectorClick
                    selectedIndices.value = when {
                        // Если уже выбран этот элемент - сбрасываем выбор
                        selectedIndices.value.contains(index) -> emptySet()
                        // Иначе выбираем новый элемент, если индекс валидный
                        index >= 0 && index < filteredData.size -> setOf(index)
                        // В случае невалидного индекса - сохраняем текущее состояние
                        else -> selectedIndices.value
                    }
                    
                    // Оповещаем о выбранной категории или null, если ничего не выбрано
                    val newSelectedItem = if (selectedIndices.value.isEmpty()) {
                        null
                    } else {
                        val selectedIdx = selectedIndices.value.first()
                        if (selectedIdx >= 0 && selectedIdx < filteredData.size) {
                            filteredData[selectedIdx]
                        } else {
                            null
                        }
                    }
                    
                    onSectorClick(newSelectedItem)
                }
            )
        }
    }
}

private fun DrawScope.drawInnerText(
    center: Offset,
    size: Size,
    selectedItem: PieChartItemData?,
    totalMoney: Money,
    isIncome: Boolean,
    centerTextColor: Color
) {
    if (selectedItem != null) {
        // При выбранной категории показываем информацию о категории
        drawIntoCanvas { canvas ->
            // Создаем Money из значения
            val itemMoney = Money(BigDecimal.valueOf(selectedItem.amount.toDouble()))
            
            // Рисуем сумму выбранной категории крупным шрифтом
            val amountPaint = android.graphics.Paint().apply {
                color = centerTextColor.toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = min(size.width, size.height) * 0.10f
                isFakeBoldText = true
                isAntiAlias = true
                letterSpacing = 0.02f
            }
            
            // Сумма располагается выше центра
            val amountY = center.y - amountPaint.descent() * 2
            
            canvas.nativeCanvas.drawText(
                itemMoney.formatForDisplay(),
                center.x,
                amountY,
                amountPaint
            )
            
            // Рисуем название категории под суммой с цветом категории
            val categoryPaint = android.graphics.Paint().apply {
                color = selectedItem.color.toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = min(size.width, size.height) * 0.07f
                isFakeBoldText = true
                isAntiAlias = true
                letterSpacing = 0.01f
            }
            
            // Обрезаем слишком длинные названия категорий
            val categoryName = if (selectedItem.name.length > 12) {
                "${selectedItem.name.take(10)}..."
            } else {
                selectedItem.name
            }
            
            // Название категории располагается ровно по центру
            val categoryY = center.y + categoryPaint.descent()
            
            canvas.nativeCanvas.drawText(
                categoryName,
                center.x,
                categoryY,
                categoryPaint
            )
            
            // Рисуем процент ниже названия категории
            val percentPaint = android.graphics.Paint().apply {
                color = selectedItem.color.copy(alpha = 0.8f).toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = min(size.width, size.height) * 0.06f
                isAntiAlias = true
            }
            
            // Процент располагается ниже названия категории
            val percentY = categoryY + percentPaint.descent() * 3
            
            canvas.nativeCanvas.drawText(
                String.format("%.1f%%", selectedItem.percentage),
                center.x,
                percentY,
                percentPaint
            )
        }
    } else {
        // При отсутствии выбранной категории показываем общую сумму и тип
        drawIntoCanvas { canvas ->
            // Рисуем общую сумму
            val amountPaint = android.graphics.Paint().apply {
                color = centerTextColor.toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = min(size.width, size.height) * 0.10f
                isFakeBoldText = true
                isAntiAlias = true
                letterSpacing = 0.02f
            }
            
            // Сумма располагается выше центра
            val amountY = center.y - amountPaint.descent()
            
            canvas.nativeCanvas.drawText(
                totalMoney.formatForDisplay(),
                center.x,
                amountY,
                amountPaint
            )
            
            // Рисуем тип (доход/расход) - с цветом соответствующим типу
            val typePaint = android.graphics.Paint().apply {
                color = if (isIncome) {
                    android.graphics.Color.parseColor("#4CAF50") // Зеленый для доходов
                } else {
                    android.graphics.Color.parseColor("#E53935") // Красный для расходов
                }
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = min(size.width, size.height) * 0.07f
                isFakeBoldText = true
                isAntiAlias = true
                letterSpacing = 0.01f
            }
            
            // Тип располагается ниже суммы
            val typeY = center.y + typePaint.descent() * 2
            
            canvas.nativeCanvas.drawText(
                if (isIncome) "Доход" else "Расход",
                center.x,
                typeY,
                typePaint
            )
        }
    }
}

@Composable
private fun DrawPieChart(
    modifier: Modifier,
    data: List<PieChartItemData>,
    selectedIndices: Set<Int>,
    onSectorClick: (Int) -> Unit,
    totalMoney: Money,
    selectedItem: PieChartItemData?,
    isIncome: Boolean,
    centerTextColor: Color
) {
    val animatedProgress = remember { Animatable(0f) }
    
    // Get the surface color outside of Canvas DrawScope since MaterialTheme can only be accessed in a Composable context
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    // Логгируем данные секторов для отладки
    LaunchedEffect(data) {
        timber.log.Timber.d("Данные диаграммы:")
        data.forEachIndexed { index, item ->
            timber.log.Timber.d("Сектор $index: ${item.name}, ${item.percentage}%, ${item.amount}")
        }
    }
    
    // Улучшенное отслеживание углов секторов для использования при клике
    val sectorAngles = remember(data) {
        val angles = mutableListOf<Triple<Float, Float, Float>>() // startAngle, sweepAngle, percentage
        var currentAngle = 0f
        
        data.forEach { item ->
            // Используем процент для расчета угла сектора
            val sweepAngle = item.percentage * 360f / 100f
            angles.add(Triple(currentAngle, sweepAngle, item.percentage))
            currentAngle += sweepAngle
        }
        
        angles
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
    Canvas(
            modifier = Modifier
            .fillMaxSize()
                .padding(5.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                        val size = this.size
                        val radius = min(size.width, size.height) / 2f * 0.95f
                        val innerRadius = radius * 0.55f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        
                        // Проверяем, клик был внутри пончика или нет
                        val distanceFromCenter = (offset - center).getDistance()
                        
                        timber.log.Timber.d("Клик: x=${offset.x}, y=${offset.y}, центр: x=${center.x}, y=${center.y}, расстояние=$distanceFromCenter")
                        
                        if (distanceFromCenter <= innerRadius) {
                            // Клик в центральную область - сбрасываем выделение
                            timber.log.Timber.d("Клик в центральную область")
                            if (selectedIndices.isNotEmpty()) {
                                onSectorClick(-1)
                            }
                            return@detectTapGestures
                        }
                    
                        if (distanceFromCenter > radius) {
                            // Клик за пределами диаграммы
                            timber.log.Timber.d("Клик за пределами диаграммы")
                            return@detectTapGestures
                        }
                        
                        // Определяем угол клика относительно центра
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        
                        // Угол в радианах от -π до π
                        val radianAngle = atan2(dy, dx)
                        
                        // Преобразуем в градусы от 0 до 360
                        var angle = radianAngle * 180f / kotlin.math.PI.toFloat()
                        if (angle < 0) angle += 360f
                        
                        // Преобразуем в систему координат Canvas (0 градусов вверху, по часовой стрелке)
                        // В Canvas угол 0 находится справа и увеличивается против часовой стрелки
                        // Поэтому нам нужно перевести в противоположную систему координат
                        var canvasAngle = (90f - angle) % 360f
                        if (canvasAngle < 0) canvasAngle += 360f
                        
                        timber.log.Timber.d("Угол клика (градусы): $angle°")
                        timber.log.Timber.d("Угол клика (canvas): $canvasAngle°")
                        
                        // Улучшенная логика определения сектора
                        // Мы будем учитывать не только углы, но и минимальный размер сектора для удобства выбора
                        
                        // Перебираем все секторы
                        var bestMatchIndex = -1
                        var minAngleDiff = Float.MAX_VALUE
                        for (i in sectorAngles.indices) {
                            val (startAngle, sweepAngle, percentage) = sectorAngles[i]
                            val sectorMiddleAngle = (startAngle + sweepAngle / 2) % 360f
                            
                            // Для очень маленьких секторов (менее 5%) увеличиваем "область клика"
                            val effectiveSweepAngle = if (percentage < 5f) sweepAngle * 1.5f else sweepAngle
                            val halfEffectiveSweep = effectiveSweepAngle / 2
                            
                            // Рассчитываем разницу между углом клика и центром сектора
                            var angleDiff = kotlin.math.abs(sectorMiddleAngle - canvasAngle)
                            // Учитываем переход через 0/360 градусов
                            if (angleDiff > 180f) angleDiff = 360f - angleDiff
                            
                            timber.log.Timber.d("Сектор $i (${data[i].name}): центр=$sectorMiddleAngle°, разница=$angleDiff°")
                            
                            // Проверяем, находится ли угол клика в диапазоне сектора с учетом эффективного угла
                            if (angleDiff <= halfEffectiveSweep || angleDiff < minAngleDiff) {
                                minAngleDiff = angleDiff
                                bestMatchIndex = i
                            }
                        }
                        
                        // Выбираем наиболее подходящий сектор
                        if (bestMatchIndex >= 0) {
                            timber.log.Timber.d("ВЫБРАН сектор $bestMatchIndex: ${data[bestMatchIndex].name}")
                            onSectorClick(bestMatchIndex)
                        } else {
                            timber.log.Timber.d("Сектор не найден для угла $canvasAngle°")
                        }
                    }
                }
        ) {
            val progressValue = animatedProgress.value
            val size = this.size
            val radius = min(size.width, size.height) / 2f * 0.95f
            val innerRadius = radius * 0.55f
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Рисуем все секторы по сохраненным углам
            sectorAngles.forEachIndexed { index, (startAngle, sweepAngle, _) ->
                val isSelected = selectedIndices.contains(index)
                val animatedSweepAngle = sweepAngle * progressValue
                
                // Выбранный сектор делаем чуть больше
                val sectorRadius = if (isSelected) radius * 1.05f else radius
                
                // Используем функцию для рисования сектора пончика
                drawDonutSection(
                    center = center,
                    innerRadius = innerRadius,
                    outerRadius = sectorRadius,
                    startAngle = startAngle,
                    sweepAngle = animatedSweepAngle,
                    color = data[index].color,
                    holeColor = surfaceColor,
                    addOutline = isSelected,
                    alpha = if (selectedIndices.isNotEmpty() && !isSelected) 0.5f else 1f
                )
            }
            
            // Рисуем информацию в центре
            drawInnerText(center, size, selectedItem, totalMoney, isIncome, centerTextColor)
        }
    }
}

/**
 * Composable that draws the category legend
 */
@Composable
private fun DrawCategoryLegend(
    items: List<PieChartItemData>,
    selectedIndices: Set<Int>,
    onItemClick: (Int) -> Unit
) {
    // Сохраняем стабильный список, сортируем элементы по сумме (от большей к меньшей)
    val sortedItems = remember(items) {
        items.sortedByDescending { it.amount }
    }
    
    // Используем LazyColumn для отображения списка категорий
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 8.dp)
    ) {
        items(
            items = sortedItems,
            key = { item -> item.id }
        ) { item ->
            // Находим оригинальный индекс элемента в несортированном списке для правильной обработки выбора
            val originalIndex = items.indexOfFirst { it.id == item.id }
            val isSelected = selectedIndices.contains(originalIndex)
            
            // Create a Money object from the amount
            val money = Money(BigDecimal.valueOf(item.amount.toDouble()))
            
            // Строка категории - делаем элементы крупнее для удобства нажатия
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(originalIndex) }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .padding(vertical = 10.dp, horizontal = 8.dp), // Увеличенный вертикальный отступ
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Цветной индикатор категории - делаем крупнее
                Box(
                    modifier = Modifier
                        .size(16.dp) // Увеличенный размер индикатора
                        .background(
                            color = item.color,
                            shape = if (isSelected) RoundedCornerShape(3.dp) else CircleShape
                        )
                )
                
                // Название категории - увеличиваем размер текста
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy( // изменяем с bodySmall на bodyMedium
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            
                // Сумма - увеличиваем размер текста
                Text(
                    text = money.formatForDisplay(),
                    style = MaterialTheme.typography.bodyMedium.copy( // изменяем с bodySmall на bodyMedium
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                // Процент - увеличиваем размер и ширину
                Text(
                    text = String.format("%.1f%%", item.percentage),
                    style = MaterialTheme.typography.bodyMedium, // изменяем с bodySmall на bodyMedium
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(55.dp), // Увеличиваем ширину для процентов
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

// New function to draw a donut section with configurable hole color
private fun DrawScope.drawDonutSection(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color,
    holeColor: Color,
    addOutline: Boolean = false,
    alpha: Float = 1f
) {
    // Цвета для обводки
    val outlineColorOuter = Color.White.copy(alpha = 0.4f)
    val outlineColorInner = Color.White.copy(alpha = 0.3f)
    val outlineWidthOuter = 1.5f
    val outlineWidthInner = 1f

    // Draw the outer arc
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = center - Offset(outerRadius, outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = alpha
    )
    
    // Cut out the inner circle to create the donut hole with specified color
    drawArc(
        color = holeColor,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = center - Offset(innerRadius, innerRadius),
        size = Size(innerRadius * 2, innerRadius * 2),
        alpha = alpha
    )
    
    // Add outline for selected sectors
    if (addOutline) {
        // Outer outline
        drawArc(
            color = outlineColorOuter,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = outlineWidthOuter),
            topLeft = center - Offset(outerRadius, outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            alpha = alpha
        )
        
        // Inner outline
        drawArc(
            color = outlineColorInner,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = outlineWidthInner),
            topLeft = center - Offset(innerRadius, innerRadius),
            size = Size(innerRadius * 2, innerRadius * 2),
            alpha = alpha
        )
    }
} 