package com.davidbugayov.financeanalyzer.presentation.chart.statistic.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.feature.statistics.R
import com.davidbugayov.financeanalyzer.feature.statistics.dialogs.PeriodSelectionDialog
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.viewmodel.EnhancedFinanceChartViewModel
import com.davidbugayov.financeanalyzer.presentation.util.UiUtils
import com.davidbugayov.financeanalyzer.ui.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.DateUtils
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.delay
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Улучшенная карточка с информацией о балансе, доходах и расходах.
 * Дизайн основан на современном минималистичном стиле с анимациями.
 * Карточка имеет цветную рамку в зависимости от баланса (зеленую при положительном, красную при отрицательном).
 *
 * @param income Общий доход за период
 * @param expense Общие расходы за период
 * @param modifier Модификатор для настройки внешнего вида
 * @param startDate Начальная дата для отображения календаря
 * @param endDate Конечная дата для отображения календаря
 * @param periodType Тип периода
 * @param viewModel ViewModel графиков для обновления данных при изменении периода
 */
@Composable
fun EnhancedSummaryCard(
    income: Money,
    expense: Money,
    modifier: Modifier = Modifier,
    startDate: Date = Date(),
    endDate: Date = Date(),
    periodType: PeriodType = PeriodType.MONTH,
    viewModel: EnhancedFinanceChartViewModel? = null,
) {
    val balance = income.minus(expense)
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current

    // Состояние для отображения диалогов выбора периода и дат
    var showPeriodDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Используем только входящие параметры для периода
    var selectedPeriodType by remember { mutableStateOf(periodType) }
    var currentStartDate by remember { mutableStateOf(startDate) }
    var currentEndDate by remember { mutableStateOf(endDate) }

    val context = LocalContext.current

    // Форматируем период
    val formattedPeriod by remember(selectedPeriodType, currentStartDate, currentEndDate) {
        derivedStateOf {
            UiUtils.formatPeriod(
                context,
                selectedPeriodType,
                currentStartDate,
                currentEndDate,
            )
        }
    }

    // Анимация для появления элементов
    var visible by remember { mutableStateOf(false) }
    val balanceScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    )

    // Запуск анимации
    LaunchedEffect(key1 = Unit) {
        delay(100)
        visible = true
    }

    LaunchedEffect(periodType) {
        selectedPeriodType = periodType
    }

    // Определяем цвет рамки в зависимости от баланса (как в BalanceCard)
    val balanceTextColor =
        if (balance.amount >= BigDecimal.ZERO) {
            incomeColor // Зеленый цвет для положительного баланса
        } else {
            expenseColor // Красный цвет для отрицательного баланса
        }

    // Диалог выбора периода
    if (showPeriodDialog) {
        PeriodSelectionDialog(
            selectedPeriod = selectedPeriodType,
            startDate = currentStartDate,
            endDate = currentEndDate,
            onPeriodSelected = { periodType ->
                selectedPeriodType = periodType
                if (periodType != PeriodType.CUSTOM) {
                    val (newStartDate, newEndDate) =
                        DateUtils.updatePeriodDates(
                            periodType = periodType,
                            currentStartDate = currentStartDate,
                            currentEndDate = currentEndDate,
                        )
                    currentStartDate = newStartDate
                    currentEndDate = newEndDate
                    // Обновляем данные через EnhancedFinanceChartViewModel
                    viewModel?.handleIntent(
                        EnhancedFinanceChartIntent.ChangePeriod(
                            periodType,
                            newStartDate,
                            newEndDate,
                        ),
                    )
                    showPeriodDialog = false
                }
            },
            onStartDateClick = {
                showStartDatePicker = true
            },
            onEndDateClick = {
                showEndDatePicker = true
            },
            onConfirm = {
                showPeriodDialog = false
                viewModel?.handleIntent(
                    EnhancedFinanceChartIntent.ChangePeriod(
                        PeriodType.CUSTOM,
                        currentStartDate,
                        currentEndDate,
                    ),
                )
            },
            onDismiss = {
                showPeriodDialog = false
            },
        )
    }

    // Диалоги выбора дат
    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = currentStartDate,
            maxDate = minOf(currentEndDate, Calendar.getInstance().time),
            onDateSelected = { date ->
                currentStartDate = date
                showStartDatePicker = false
            },
            onDismiss = {
                showStartDatePicker = false
            },
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = currentEndDate,
            minDate = currentStartDate,
            maxDate = Calendar.getInstance().time,
            onDateSelected = { date ->
                currentEndDate = date
                showEndDatePicker = false
            },
            onDismiss = {
                showEndDatePicker = false
            },
        )
    }

    Card(
        modifier = modifier,
        shape =
            RoundedCornerShape(
                dimensionResource(UiR.dimen.enhanced_summary_card_corner_radius),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        border =
            BorderStroke(
                width = 3.dp,
                color = balanceTextColor,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_padding_horizontal,
                            ),
                        vertical =
                            dimensionResource(
                                UiR.dimen.enhanced_summary_card_padding_vertical,
                            ),
                    ),
        ) {
            // Используем Column с анимацией alpha
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha),
                verticalArrangement =
                    Arrangement.spacedBy(
                        dimensionResource(UiR.dimen.enhanced_summary_card_spacing),
                    ),
            ) {
                // Период с возможностью клика для открытия диалога выбора периода
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .clickable { showPeriodDialog = true }
                            .padding(vertical = 2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = stringResource(UiR.string.select_period),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    // Показываем текущий период
                    Text(
                        text = formattedPeriod,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    )
                }

                // Баланс - крупная сумма с минимальными отступами
                Text(
                    text = balance.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize =
                                dimensionResource(
                                    UiR.dimen.enhanced_summary_card_balance_font_size,
                                ).value.sp * balanceScale,
                            letterSpacing =
                                dimensionResource(
                                    UiR.dimen.enhanced_summary_card_balance_letter_spacing,
                                ).value.sp,
                        ),
                    color = if (balance.amount.signum() >= 0) incomeColor else expenseColor,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, bottom = 4.dp)
                            .graphicsLayer {
                                translationY = (1f - balanceScale) * -12f
                            },
                )

                // Строка с доходами и расходами
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Доходы
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = incomeColor,
                                modifier =
                                    Modifier
                                        .padding(end = 4.dp)
                                        .height(
                                            dimensionResource(
                                                UiR.dimen.enhanced_summary_card_icon_size,
                                            ),
                                        )
                                        .width(
                                            dimensionResource(
                                                UiR.dimen.enhanced_summary_card_icon_size,
                                            ),
                                        ),
                            )
                            Text(
                                text = income.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize =
                                            dimensionResource(
                                                UiR.dimen.enhanced_summary_card_income_expense_font_size,
                                            ).value.sp,
                                    ),
                                color = incomeColor,
                            )
                        }

                        Text(
                            text = "Доходы",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontSize =
                                        dimensionResource(
                                            UiR.dimen.enhanced_summary_card_label_font_size,
                                        ).value.sp,
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 20.dp, top = 2.dp),
                        )
                    }

                    // Расходы
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = expenseColor,
                                modifier =
                                    Modifier
                                        .padding(end = 4.dp)
                                        .height(
                                            dimensionResource(
                                                UiR.dimen.enhanced_summary_card_icon_size,
                                            ),
                                        )
                                        .width(
                                            dimensionResource(
                                                UiR.dimen.enhanced_summary_card_icon_size,
                                            ),
                                        ),
                            )
                            Text(
                                text = expense.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize =
                                            dimensionResource(
                                                UiR.dimen.enhanced_summary_card_income_expense_font_size,
                                            ).value.sp,
                                    ),
                                color = expenseColor,
                            )
                        }

                        Text(
                            text = "Расходы",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontSize =
                                        dimensionResource(
                                            UiR.dimen.enhanced_summary_card_label_font_size,
                                        ).value.sp,
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(end = 20.dp, top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}