package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import android.util.Log
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
import androidx.compose.foundation.layout.Arrangement
import java.math.BigDecimal
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import timber.log.Timber
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
    val filteredData = remember(items, showExpenses) {
        items.filter { it.percentage > 0f }
    }
    
    Timber.d("FinanceAnalyzer: EnhancedCategoryPieChart: инициализация с ${items.size} элементами, отфильтровано до ${filteredData.size}")
    Log.d("[D]", "EnhancedCategoryPieChart: инициализация с ${items.size} элементами, отфильтровано до ${filteredData.size}")
    
    if (filteredData.isEmpty()) {
        // Show empty state if no valid data
        Timber.d("FinanceAnalyzer: EnhancedCategoryPieChart: нет данных для отображения")
        Log.d("[D]", "EnhancedCategoryPieChart: нет данных для отображения")
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
    
    Timber.d("FinanceAnalyzer: EnhancedCategoryPieChart: общая сумма ${totalMoney.formatForDisplay()}")
    Log.d("[D]", "EnhancedCategoryPieChart: общая сумма ${totalMoney.formatForDisplay()}")
    
    // State for selected indices - сбрасываем при смене типа (доходы/расходы)
    val selectedIndices = remember(selectedIndex, filteredData, showExpenses) { 
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
    val centerTextColor = MaterialTheme.colorScheme.onBackground
    
    // Используем белый цвет фона для карточки, как на скриншоте
    val cardColor = Color.White
    
    // Увеличиваем карточку для отображения всех категорий
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // Горизонтальный переключатель доходы/расходы
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
            
            // Диаграмма сверху (по центру) с меньшей высотой
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
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
                    centerTextColor = centerTextColor,
                    backgroundColor = cardColor
                )
            }
            
            // Разделитель между диаграммой и списком категорий
            Spacer(modifier = Modifier.height(16.dp))
            
            // Заголовок для списка категорий
            Text(
                text = "Список категорий",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            
            // Сортируем элементы по сумме (от большей к меньшей)
            val sortedItems = filteredData.sortedByDescending { it.amount }
            
            // Выводим логи для отслеживания количества категорий
            Timber.d("FinanceAnalyzer: EnhancedCategoryPieChart: список категорий содержит ${sortedItems.size} элементов")
            Log.d("[D]", "EnhancedCategoryPieChart: список категорий содержит ${sortedItems.size} элементов")
            
            // Рассчитываем высоту для списка категорий (30dp на элемент, минимум 150dp)
            val categoryHeight = (sortedItems.size * 30).coerceAtLeast(150).coerceAtMost(500)
            
            // Показываем все категории с ограниченной высотой и скроллингом
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .height(categoryHeight.dp) // Обязательно задаем высоту для компонента со скроллингом
                    .verticalScroll(rememberScrollState())
            ) {
                // Выводим содержимое категорий для отладки
                sortedItems.forEachIndexed { index, item ->
                    Timber.d("FinanceAnalyzer: Категория #$index: ${item.name}, сумма: ${item.amount}, процент: ${item.percentage}%")
                    Log.d("[D]", "Категория #$index: ${item.name}, сумма: ${item.amount}, процент: ${item.percentage}%")
                    
                    // Находим оригинальный индекс элемента в несортированном списке для правильной обработки выбора
                    val originalIndex = filteredData.indexOfFirst { it.id == item.id }
                    val isSelected = selectedIndices.value.contains(originalIndex)
                    
                    // Create a Money object from the amount
                    val money = Money(BigDecimal.valueOf(item.amount.toDouble()))
                    
                    // Строка категории (максимально компактная)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                selectedIndices.value = when {
                                    selectedIndices.value.contains(originalIndex) -> emptySet()
                                    else -> setOf(originalIndex)
                                }
                                
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
                            .background(
                                if (isSelected) item.color.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Цветной индикатор категории
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = item.color,
                                    shape = if (isSelected) RoundedCornerShape(3.dp) else CircleShape
                                )
                        )
                        
                        // Название категории
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Сумма
                        Text(
                            text = money.formatForDisplay(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        // Процент с цветом категории для выделенного элемента
                        Text(
                            text = String.format("%.1f%%", item.percentage),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(45.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                    
                    // Тонкий разделитель после каждого элемента, кроме последнего
                    if (index < sortedItems.size - 1) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        )
                    }
                }
            }
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
                textSize = min(size.width, size.height) * 0.13f // Увеличиваем размер суммы
                isFakeBoldText = true
                isAntiAlias = true
                letterSpacing = 0.02f
            }
            
            // Сумма располагается выше центра
            val amountY = center.y - amountPaint.descent() * 1.5f
            
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
                textSize = min(size.width, size.height) * 0.09f // Увеличиваем размер названия категории
                isFakeBoldText = true
                isAntiAlias = true
            }
            
            // Обрезаем слишком длинные названия категорий
            val categoryName = if (selectedItem.name.length > 15) {
                "${selectedItem.name.take(12)}..."
            } else {
                selectedItem.name
            }
            
            // Название категории чуть ниже центра
            val categoryY = center.y + categoryPaint.descent() * 0.5f
            
            canvas.nativeCanvas.drawText(
                categoryName,
                center.x,
                categoryY,
                categoryPaint
            )
            
            // Рисуем процент ниже названия категории
            val percentPaint = android.graphics.Paint().apply {
                color = selectedItem.color.copy(alpha = 0.9f).toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = min(size.width, size.height) * 0.07f
                isAntiAlias = true
                isFakeBoldText = true
            }
            
            // Процент располагается ниже названия категории
            val percentY = categoryY + percentPaint.descent() * 2.5f
            
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
                textSize = min(size.width, size.height) * 0.13f // Увеличиваем размер
                isFakeBoldText = true
                isAntiAlias = true
                letterSpacing = 0.02f
            }
            
            // Сумма располагается выше центра
            val amountY = center.y - amountPaint.descent() * 0.5f
            
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
                textSize = min(size.width, size.height) * 0.09f // Увеличиваем размер
                isFakeBoldText = true
                isAntiAlias = true
            }
            
            // Тип располагается ниже суммы
            val typeY = center.y + typePaint.descent() * 2.5f
            
            canvas.nativeCanvas.drawText(
                if (isIncome) "Доход" else "Расход",
                center.x,
                typeY,
                typePaint
            )
        }
    }
}

private fun getClickedSectorIndex(
    angle: Float,
    sectorAngles: List<Triple<Float, Float, Float>>
): Int {
    // Сначала проверяем секторы с очень маленьким углом (менее 5%)
    // Для них используем увеличенную область обнаружения
    Log.d("[D]", "PieChart: обрабатываем клик по углу: $angle, всего секторов: ${sectorAngles.size}")
    
    // Подробный лог всех секторов для отладки
    sectorAngles.forEachIndexed { idx, (start, sweep, percent) ->
        val end = (start + sweep) % 360f
        Log.d("[D]", "PieChart: сектор №$idx: от $start° до $end° (размер $sweep°), процент: $percent%")
    }
    
    // Починим расчет углов
    // При проверке малых секторов, используем увеличенную область
    for (i in sectorAngles.indices) {
        val (startAngle, sweepAngle, percentage) = sectorAngles[i]
        
        // Вычисляем конечный угол сектора
        val endAngle = (startAngle + sweepAngle) % 360f
        
        // Если сектор очень маленький (менее 5%), проверяем с увеличенной областью
        if (percentage < 5.0f) {
            // Центральный угол сектора
            val midAngle = if (endAngle > startAngle) {
                startAngle + sweepAngle / 2
            } else {
                // Если сектор пересекает 0°/360°
                (startAngle + sweepAngle / 2) % 360f
            }
            
            // Увеличиваем угол сектора в 3 раза для детекции клика, но не более 30 градусов
            val effectiveSweep = kotlin.math.min(sweepAngle * 3.0f, 30f)
            
            // Проверяем расстояние от угла клика до центра сектора
            val distance = kotlin.math.min(
                kotlin.math.abs((angle - midAngle + 360) % 360),
                kotlin.math.abs((midAngle - angle + 360) % 360)
            )
            
            // Если угол находится в пределах половины эффективного угла от центра сектора
            if (distance <= effectiveSweep / 2) {
                Log.d("[D]", "PieChart: обнаружен малый сектор №$i, процент: $percentage, начальный угол: $startAngle, угол сектора: $sweepAngle, эффективный угол: $effectiveSweep, расстояние: $distance")
                return i
            }
        }
    }
    
    // Если не найдены маленькие секторы, проверяем все секторы как обычно
    for (i in sectorAngles.indices) {
        val (startAngle, sweepAngle, percentage) = sectorAngles[i]
        
        // Вычисляем конечный угол сектора
        val endAngle = (startAngle + sweepAngle) % 360f
        
        // Определяем, находится ли угол клика в этом секторе
        val inSector = if (endAngle < startAngle) {
            // Сектор пересекает линию 0°/360°
            angle >= startAngle || angle < endAngle
        } else {
            // Обычный случай
            angle >= startAngle && angle < endAngle
        }
        
        if (inSector) {
            Log.d("[D]", "PieChart: обнаружен обычный сектор №$i, процент: $percentage, начальный угол: $startAngle, конечный угол: $endAngle, угол сектора: $sweepAngle, угол клика: $angle")
            return i
        }
    }
    
    Log.d("[D]", "PieChart: сектор не найден для угла: $angle")
    return -1
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
    centerTextColor: Color,
    backgroundColor: Color
) {
    val animatedProgress = remember { Animatable(0f) }
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Log.d("[D]", "DrawPieChart: инициализация с ${data.size} элементами, выбрано: ${selectedIndices.size}")
    
    LaunchedEffect(data, isIncome) {
        animatedProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(0) // Мгновенно сбрасываем прогресс
        )
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }
    
    // Рассчитываем углы секторов для отрисовки и обработки кликов
    val sectorAngles = remember(data, isIncome) {
        val angles = mutableListOf<Triple<Float, Float, Float>>() // startAngle, sweepAngle, percentage
        var currentAngle = 0f
        var totalPercentage = 0f
        
        // Сначала вычисляем общую сумму процентов
        val totalPercent = data.sumOf { it.percentage.toDouble() }.toFloat()
        Log.d("[D]", "PieChart: общая сумма процентов: $totalPercent%")
        
        // Нормализуем до 100% для корректного отображения
        val normalizationFactor = if (totalPercent > 0) 100f / totalPercent else 1f
        
        data.forEachIndexed { index, item ->
            // Нормализуем процент
            val normalizedPercentage = item.percentage * normalizationFactor
            val sweepAngle = normalizedPercentage * 3.6f // 360 / 100 = 3.6
            
            // Гарантируем минимальный угол для маленьких секторов при отрисовке - не менее 1°
            val effectiveSweepAngle = kotlin.math.max(sweepAngle, 1f)
            
            angles.add(Triple(currentAngle, effectiveSweepAngle, normalizedPercentage))
            currentAngle += effectiveSweepAngle
            totalPercentage += normalizedPercentage
            
            Log.d("[D]", "Сектор №$index: '${item.name}' $normalizedPercentage%, угол: $effectiveSweepAngle°, от ${currentAngle - effectiveSweepAngle}° до ${currentAngle}°")
        }
        
        Log.d("[D]", "PieChart: итоговая сумма процентов: $totalPercentage%, итоговый угол: $currentAngle°")
        angles
    }
    
    // Уникальный ключ для поддержки обновления pointerInput при изменении типа диаграммы
    val pointerInputKey = remember(isIncome) { System.currentTimeMillis() }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
    Canvas(
            modifier = Modifier
            .fillMaxSize()
                .padding(5.dp)
            .pointerInput(pointerInputKey) {
                detectTapGestures { offset ->
                        Log.d("[D]", "PieChart: обнаружен клик с ключом $pointerInputKey, isIncome = $isIncome, всего секторов: ${sectorAngles.size}")
                        
                        val size = this.size
                        val radius = min(size.width, size.height) / 2f * 0.95f
                        val innerRadius = radius * 0.55f
                    val center = Offset(size.width / 2f, size.height / 2f)
                        
                        // Проверяем расстояние от центра
                        val distanceFromCenter = (offset - center).getDistance()
                        
                        // Если клик в центральную область или за пределами диаграммы - сбрасываем выбор
                        if (distanceFromCenter <= innerRadius || distanceFromCenter > radius) {
                            Log.d("[D]", "PieChart: клик за пределами активной области, сбрасываем выбор")
                            if (selectedIndices.isNotEmpty()) onSectorClick(-1)
                            return@detectTapGestures
                        }
                        
                        // Вычисляем угол клика
                        // В Canvas угол 0° направлен вправо и увеличивается против часовой стрелки
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        
                        // Вычисляем угол в радианах и конвертируем в градусы
                        val angleRad = atan2(dy, dx)
                        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                        
                        // Нормализуем в диапазон [0, 360)
                        if (angleDeg < 0) angleDeg += 360f
                        
                        // Определяем квадрант для отладки
                        val quadrant = when {
                            dx >= 0 && dy < 0 -> "I (верх-право)"
                            dx < 0 && dy < 0 -> "II (верх-лево)"
                            dx < 0 && dy >= 0 -> "III (низ-лево)"
                            else -> "IV (низ-право)"
                        }
                        
                        Log.d("[D]", "PieChart: клик - координаты: (${offset.x}, ${offset.y}), квадрант: $quadrant, угол: $angleDeg°")
                        
                        // Прямой поиск по секторам
                        var selectedSector = -1
                        
                        // Перебираем все секторы
                        sectorAngles.forEachIndexed { index, (start, sweep, percentage) ->
                            val end = (start + sweep) % 360f
                            val containsAngle = if (end < start) {
                                // Сектор пересекает границу 0°/360°
                                angleDeg in start..360f || angleDeg in 0f..end
                            } else {
                                // Обычный случай
                                angleDeg in start..end
                            }
                            
                            if (containsAngle) {
                                selectedSector = index
                                Log.d("[D]", "PieChart: угол $angleDeg° попадает в сектор №$index (${data[index].name}) - от $start° до $end°")
                                return@forEachIndexed
                            }
                        }
                        
                        // Если обычный алгоритм не нашел сектор, проверяем малые секторы с увеличенной зоной
                        if (selectedSector == -1) {
                            sectorAngles.forEachIndexed { index, (start, sweep, percentage) ->
                                if (percentage < 5.0f) {
                                    // Для маленьких секторов (менее 5%) увеличиваем зону обнаружения
                                    val end = (start + sweep) % 360f
                                    // Центр сектора
                                    val midAngle = if (end < start) {
                                        (start + 180f) % 360f
                                    } else {
                                        (start + sweep / 2f) % 360f
                                    }
                                    
                                    // Увеличиваем область до максимум 30°, но не менее размера сектора
                                    val effectiveAngle = kotlin.math.min(sweep * 3f, 30f)
                                    val halfEffective = effectiveAngle / 2f
                                    
                                    // Проверка расстояния от центра сектора до места клика
                                    val distance = kotlin.math.min(
                                        kotlin.math.abs((angleDeg - midAngle + 360f) % 360f),
                                        kotlin.math.abs((midAngle - angleDeg + 360f) % 360f)
                                    )
                                    
                                    if (distance <= halfEffective) {
                                        selectedSector = index
                                        Log.d("[D]", "PieChart: малый сектор №$index (${data[index].name}) обнаружен, расстояние = $distance°, допустимое = $halfEffective°")
                                        return@forEachIndexed
                                    }
                                }
                            }
                        }
                        
                        // Уведомляем о выбранном секторе
                        if (selectedSector >= 0) {
                            Log.d("[D]", "PieChart: выбран сектор №$selectedSector '${data[selectedSector].name}'")
                        } else {
                            Log.d("[D]", "PieChart: сектор не найден")
                            // Если сектор не найден и есть выбранный элемент - сбрасываем выбор
                            if (selectedIndices.isNotEmpty()) onSectorClick(-1)
                        }
                        
                        onSectorClick(selectedSector)
                    }
                }
        ) {
            val progressValue = animatedProgress.value
            val size = this.size
            val radius = min(size.width, size.height) / 2f * 0.95f
            val innerRadius = radius * 0.55f
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Рисуем все секторы
            sectorAngles.forEachIndexed { index, (startAngle, sweepAngle, _) ->
                val isSelected = selectedIndices.contains(index)
                val animatedSweepAngle = sweepAngle * progressValue
                
                // Выбранный сектор немного увеличиваем и добавляем обводку
                val sectorRadius = if (isSelected) radius * 1.05f else radius
                
                drawDonutSection(
                    center = center,
                    innerRadius = innerRadius,
                    outerRadius = sectorRadius,
                    startAngle = startAngle,
                    sweepAngle = animatedSweepAngle,
                    color = data[index].color,
                    holeColor = backgroundColor, // Используем белый цвет для отверстия пончика
                    addOutline = isSelected,
                    alpha = 1f  // Всегда полная непрозрачность для всех секторов
                )
            }
            
            // Дополнительно рисуем полный круг в центре для чистого белого фона
            drawCircle(
                color = backgroundColor,
                radius = innerRadius,
                center = center
            )
            
            // Рисуем текст в центре
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
    // Сортируем элементы по сумме (от большей к меньшей)
    val sortedItems = remember(items) {
        items.sortedByDescending { it.amount }
    }
    
    // Используем LazyColumn для отображения списка категорий
    // Увеличиваем высоту до 450dp (в 1.5 раза больше)
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
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
            
            // Строка категории (уменьшаем отступы для компактности)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(originalIndex) }
                    .background(
                        // Используем цвет категории с низкой прозрачностью при выделении
                        if (isSelected) item.color.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .padding(vertical = 10.dp, horizontal = 8.dp), // Уменьшаем вертикальный padding для более компактного отображения
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Цветной индикатор категории
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = item.color,
                            shape = if (isSelected) RoundedCornerShape(4.dp) else CircleShape
                        )
                )
                
                // Название категории
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Сумма
                Text(
                    text = money.formatForDisplay(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                // Процент с цветом категории для выделенного элемента
                Text(
                    text = String.format("%.1f%%", item.percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(45.dp),
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
    
    // Cut out the inner circle to create the donut hole with white background
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