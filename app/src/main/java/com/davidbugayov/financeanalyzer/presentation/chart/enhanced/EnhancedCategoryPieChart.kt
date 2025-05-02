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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartItemData
import androidx.compose.foundation.layout.Arrangement
import java.math.BigDecimal
import kotlin.math.atan2
import kotlin.math.min
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.colorResource
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import com.davidbugayov.financeanalyzer.R
import timber.log.Timber
import android.graphics.Paint
import android.graphics.Typeface

/**
 * Улучшенная круговая диаграмма категорий, которая показывает распределение категорий 
 * с интерактивной легендой и компактным размером для лучшей наглядности
 * 
 * @param items Список данных PieChartItemData для отображения
 * @param selectedIndex Индекс изначально выбранного сектора
 * @param onSectorClick Обратный вызов при клике на сектор с данными элемента
 * @param modifier Модификатор для диаграммы
 * @param showExpenses Флаг, показывающий расходы (true) или доходы (false)
 * @param onShowExpensesChange Обратный вызов при изменении типа отображаемых данных (расходы/доходы)
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
    
    // Используем белый цвет фона для карточки, как на скриншоте
    val cardColor = colorResource(id = R.color.white)
    
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
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null // Убираем визуальный эффект нажатия
                ) { 
                    // Сбрасываем выбор только если что-то выбрано
                    if (selectedIndices.value.isNotEmpty()) {
                        selectedIndices.value = emptySet()
                        onSectorClick(null)
                    }
                }
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
                    text = stringResource(R.string.expense),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (showExpenses) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (showExpenses) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onShowExpensesChange(true) }
                )
                
                Text(
                    text = stringResource(R.string.income),
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
                    backgroundColor = cardColor
                )
            }
            
            // Разделитель между диаграммой и списком категорий
            Spacer(modifier = Modifier.height(16.dp))
            
            // Заголовок для списка категорий
            Text(
                text = stringResource(id = R.string.enhanced_chart_category_list),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            
            // Сортируем элементы по сумме (от большей к меньшей)
            val sortedItems = filteredData.sortedByDescending { it.amount }
            
            // Рассчитываем высоту для списка категорий (25dp на элемент, минимум 20dp)
            val categoryHeight = 20 + sortedItems.size * 25
            
            // Показываем все категории с высотой, подходящей для количества элементов
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .height(categoryHeight.dp) // Динамическая высота без верхнего ограничения
                    .verticalScroll(rememberScrollState()) // Оставляем скроллинг на всякий случай
            ) {
                // Выводим содержимое категорий для отладки
                sortedItems.forEachIndexed { index, item ->
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

// Вынесем константы для отступов в центральном тексте
private object DonutTextConstants {
    const val AMOUNT_Y_OFFSET_SELECTED = 2.4f
    const val CATEGORY_Y_OFFSET = 1.2f
    const val PERCENT_Y_OFFSET = 5.2f
    
    const val AMOUNT_Y_OFFSET_NORMAL = 1.8f
    const val TYPE_Y_OFFSET = 3.0f
    
    const val AMOUNT_TEXT_SIZE_FACTOR = 0.1f
    const val CATEGORY_TEXT_SIZE_FACTOR = 0.075f
    const val PERCENT_TEXT_SIZE_FACTOR = 0.06f
}

private fun DrawScope.drawInnerText(
    center: Offset,
    size: Size,
    selectedItem: PieChartItemData?,
    totalMoney: Money,
    isIncome: Boolean,
    incomeText: String,
    expenseText: String
) {
    if (selectedItem != null) {
        drawSelectedItemText(center, size, selectedItem, isIncome)
    } else {
        drawTotalAmountText(center, size, totalMoney, isIncome, incomeText, expenseText)
    }
}

private fun DrawScope.drawSelectedItemText(
    center: Offset,
    size: Size,
    selectedItem: PieChartItemData,
    isIncome: Boolean
) {
    drawIntoCanvas { canvas ->
        // Создаем Money из значения
        val itemMoney = Money(BigDecimal.valueOf(selectedItem.amount.toDouble()))
            
        // Определяем цвет в зависимости от типа операции
        val amountColor = if (isIncome) {
            ColorUtils.INCOME_COLOR
        } else {
            ColorUtils.EXPENSE_COLOR
        }
            
        // Рисуем сумму крупно во всю центральную область
        val amountPaint = Paint().apply {
            isAntiAlias = true
            textSize = min(size.width, size.height) * DonutTextConstants.AMOUNT_TEXT_SIZE_FACTOR
            color = amountColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
            
        // Рисуем сумму выше центра
        val amountY = center.y - amountPaint.descent() * DonutTextConstants.AMOUNT_Y_OFFSET_SELECTED
            
        canvas.nativeCanvas.drawText(
            itemMoney.formatForDisplay(),
            center.x,
            amountY,
            amountPaint
        )
            
        // Рисуем название категории под суммой с цветом категории
        val categoryPaint = Paint().apply {
            color = selectedItem.color.toArgb() 
            textAlign = Paint.Align.CENTER
            textSize = min(size.width, size.height) * DonutTextConstants.CATEGORY_TEXT_SIZE_FACTOR
            isFakeBoldText = true
            isAntiAlias = true
        }
            
        // Обрезаем слишком длинные названия категорий
        val categoryName = if (selectedItem.name.length > 15) {
            "${selectedItem.name.take(12)}..."
        } else {
            selectedItem.name
        }
            
        val categoryY = center.y + categoryPaint.descent() * DonutTextConstants.CATEGORY_Y_OFFSET
            
        canvas.nativeCanvas.drawText(
            categoryName,
            center.x,
            categoryY,
            categoryPaint
        )
            
        // Рисуем процент ниже названия категории
        val percentPaint = Paint().apply {
            color = selectedItem.color.copy(alpha = 0.9f).toArgb()
            textAlign = Paint.Align.CENTER
            textSize = min(size.width, size.height) * DonutTextConstants.PERCENT_TEXT_SIZE_FACTOR
            isAntiAlias = true
            isFakeBoldText = true
        }
            
        val percentY = categoryY + percentPaint.descent() * DonutTextConstants.PERCENT_Y_OFFSET
            
        canvas.nativeCanvas.drawText(
            String.format("%.1f%%", selectedItem.percentage),
            center.x,
            percentY,
            percentPaint
        )
    }
}

private fun DrawScope.drawTotalAmountText(
    center: Offset,
    size: Size,
    totalMoney: Money,
    isIncome: Boolean,
    incomeText: String,
    expenseText: String
) {
    drawIntoCanvas { canvas ->
        // Определяем цвет в зависимости от типа операции
        val amountColor = if (isIncome) {
            ColorUtils.INCOME_COLOR
        } else {
            ColorUtils.EXPENSE_COLOR
        }
            
        // Рисуем общую сумму
        val amountPaint = Paint().apply {
            color = amountColor
            textAlign = Paint.Align.CENTER
            textSize = min(size.width, size.height) * 0.11f
            isFakeBoldText = true
            isAntiAlias = true
            letterSpacing = 0.02f
        }
            
        val amountY = center.y - amountPaint.descent() * DonutTextConstants.AMOUNT_Y_OFFSET_NORMAL
            
        canvas.nativeCanvas.drawText(
            totalMoney.formatForDisplay(),
            center.x,
            amountY,
            amountPaint
        )
            
        // Рисуем тип (доход/расход) - с цветом соответствующим типу
        val typePaint = Paint().apply {
            color = if (isIncome) {
                ColorUtils.INCOME_COLOR
            } else {
                ColorUtils.EXPENSE_COLOR
            }
            textAlign = Paint.Align.CENTER
            textSize = min(size.width, size.height) * DonutTextConstants.CATEGORY_TEXT_SIZE_FACTOR
            isFakeBoldText = true
            isAntiAlias = true
        }
            
        val typeY = center.y + typePaint.descent() * DonutTextConstants.TYPE_Y_OFFSET
            
        canvas.nativeCanvas.drawText(
            if (isIncome) incomeText else expenseText,
            center.x,
            typeY,
            typePaint
        )
    }
}

private fun getClickedSectorIndex(
    angle: Float,
    sectorAngles: List<Triple<Float, Float, Float>>,
    distance: Float,
    innerRadius: Float,
    outerRadius: Float
): Int {
    // Если клик в центральную область или за пределами диаграммы - возвращаем -1
    if (distance <= innerRadius || distance > outerRadius) {
        return -1
    }
    
    // Сначала проверяем обычные секторы
    sectorAngles.forEachIndexed { index, (start, sweep, _) ->
        val end = (start + sweep) % 360f
        val containsAngle = if (end < start) {
            // Сектор пересекает границу 0°/360°
            angle in start..360f || angle in 0f..end
        } else {
            // Обычный случай
            angle in start..end
        }
        
        if (containsAngle) {
            return index
        }
    }
    
    // Если обычный алгоритм не нашел сектор, проверяем малые секторы с увеличенной зоной
    sectorAngles.forEachIndexed { index, (start, sweep, _) ->
        if (sweep < 18f) { // Примерно соответствует 5% от полного круга
            // Для маленьких секторов увеличиваем зону обнаружения
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
            val distanceToMid = kotlin.math.min(
                kotlin.math.abs((angle - midAngle + 360f) % 360f),
                kotlin.math.abs((midAngle - angle + 360f) % 360f)
            )
            
            if (distanceToMid <= halfEffective) {
                return index
            }
        }
    }
    
    return -1 // Если сектор не найден
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
    backgroundColor: Color
) {
    // Получаем строковые ресурсы внутри @Composable
    val incomeText = stringResource(id = R.string.enhanced_chart_income_single)
    val expenseText = stringResource(id = R.string.enhanced_chart_expense_single)
    
    val animatedProgress = remember { Animatable(0f) }
    
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
        
        // Сначала вычисляем общую сумму процентов
        val totalPercent = data.sumOf { it.percentage.toDouble() }.toFloat()
        
        // Нормализуем до 100% для корректного отображения
        val normalizationFactor = if (totalPercent > 0) 100f / totalPercent else 1f
        
        data.forEach { item ->  
            // Нормализуем процент
            val normalizedPercentage = item.percentage * normalizationFactor
            val sweepAngle = normalizedPercentage * 3.6f // 360 / 100 = 3.6
            
            // Гарантируем минимальный угол для маленьких секторов при отрисовке - не менее 1°
            val effectiveSweepAngle = kotlin.math.max(sweepAngle, 1f)
            
            angles.add(Triple(currentAngle, effectiveSweepAngle, normalizedPercentage))
            currentAngle += effectiveSweepAngle
        }
        
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
                        val size = this.size
                        val radius = min(size.width, size.height) / 2f * 0.95f
                        val innerRadius = radius * 0.55f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        
                        // Проверяем расстояние от центра
                        val distanceFromCenter = (offset - center).getDistance()
                        
                        // Вычисляем угол клика
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        
                        // Вычисляем угол в радианах и конвертируем в градусы
                        val angleRad = atan2(dy, dx)
                        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                        
                        // Нормализуем в диапазон [0, 360)
                        if (angleDeg < 0) angleDeg += 360f
                        
                        // Получаем выбранный сектор
                        val selectedSector = getClickedSectorIndex(
                            angle = angleDeg,
                            sectorAngles = sectorAngles,
                            distance = distanceFromCenter,
                            innerRadius = innerRadius,
                            outerRadius = radius
                        )
                        
                        // Если сектор найден, логируем информацию
                        if (selectedSector >= 0) {
                            Timber.tag("[D]").d("PieChart: выбран сектор №$selectedSector '${data[selectedSector].name}'")
                        } else {
                            Timber.tag("[D]").d("PieChart: сектор не найден")
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
            drawInnerText(
                center = center,
                size = size,
                selectedItem = selectedItem,
                totalMoney = totalMoney,
                isIncome = isIncome,
                incomeText = incomeText,
                expenseText = expenseText
            )
        }
    }
}

/**
 * Отрисовывает секцию доната (сектор круга с вырезанным центром)
 * 
 * @param center Центр доната
 * @param innerRadius Внутренний радиус (для отверстия)
 * @param outerRadius Внешний радиус
 * @param startAngle Начальный угол в градусах
 * @param sweepAngle Угол сектора в градусах
 * @param color Цвет сектора
 * @param holeColor Цвет центрального отверстия
 * @param addOutline Добавлять ли обводку для выделенного сектора
 * @param alpha Прозрачность (1.0f = полностью непрозрачный)
 */
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
    // Константы для обводки
    val outlineColors = object {
        val outer = Color.White.copy(alpha = 0.4f)
        val inner = Color.White.copy(alpha = 0.3f)
    }
    val outlineWidths = object {
        val outer = 1.5f
        val inner = 1f
    }

    // Рисуем внешний сектор
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = center - Offset(outerRadius, outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = alpha
    )
    
    // Вырезаем центральную часть для создания эффекта доната
    drawArc(
        color = holeColor,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = center - Offset(innerRadius, innerRadius),
        size = Size(innerRadius * 2, innerRadius * 2),
        alpha = alpha
    )
    
    // Добавляем обводку для выделенных секторов
    if (addOutline) {
        // Внешняя обводка
        drawArc(
            color = outlineColors.outer,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = outlineWidths.outer),
            topLeft = center - Offset(outerRadius, outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            alpha = alpha
        )
        
        // Внутренняя обводка
        drawArc(
            color = outlineColors.inner,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = outlineWidths.inner),
            topLeft = center - Offset(innerRadius, innerRadius),
            size = Size(innerRadius * 2, innerRadius * 2),
            alpha = alpha
        )
    }
} 