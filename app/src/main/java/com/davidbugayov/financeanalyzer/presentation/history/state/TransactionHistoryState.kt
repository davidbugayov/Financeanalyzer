package com.davidbugayov.financeanalyzer.presentation.history.state

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Calendar
import java.util.Date

/**
 * Состояние экрана истории транзакций.
 * Содержит данные для отображения списка транзакций, фильтров и статистики.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @property transactions Полный список всех транзакций
 * @property filteredTransactions Отфильтрованный список транзакций по выбранным критериям
 * @property isLoading Флаг загрузки данных
 * @property error Текст ошибки (null если ошибок нет)
 * @property selectedCategory Выбранная категория для фильтрации (null для всех категорий)
 * @property groupingType Тип группировки транзакций (по дням, неделям, месяцам)
 * @property periodType Тип периода для фильтрации
 * @property startDate Начальная дата выбранного периода
 * @property endDate Конечная дата выбранного периода
 * @property categoryStats Статистика по категории: (текущая сумма, предыдущая сумма, процент изменения)
 * @property showPeriodDialog Флаг отображения диалога выбора периода
 * @property showCategoryDialog Флаг отображения диалога выбора категории
 * @property showStartDatePicker Флаг отображения календаря начальной даты
 * @property showEndDatePicker Флаг отображения календаря конечной даты
 */
data class TransactionHistoryState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val groupingType: GroupingType = GroupingType.MONTH,
    val periodType: PeriodType = PeriodType.MONTH,
    val startDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
    val endDate: Date = Date(),
    val categoryStats: Triple<Double, Double, Int?>? = null,
    val showPeriodDialog: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false
) 