package com.davidbugayov.financeanalyzer.presentation.history.state

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
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
 * @property groupedTransactions Группированные транзакции
 * @property isLoading Флаг загрузки данных
 * @property error Текст ошибки (null если ошибок нет)
 * @property selectedCategories Список выбранных категорий для фильтрации (пустой список для всех категорий)
 * @property selectedSources Список выбранных источников для фильтрации (пустой список для всех источников)
 * @property groupingType Тип группировки транзакций (по дням, неделям, месяцам)
 * @property periodType Тип периода для фильтрации
 * @property startDate Начальная дата для кастомного периода
 * @property endDate Конечная дата для кастомного периода
 * @property categoryStats Статистика по категории: (текущая сумма, предыдущая сумма, процент изменения)
 * @property showPeriodDialog Флаг отображения диалога выбора периода
 * @property showCategoryDialog Флаг отображения диалога выбора категории
 * @property showSourceDialog Флаг отображения диалога выбора источника
 * @property showStartDatePicker Флаг отображения диалога выбора начальной даты
 * @property showEndDatePicker Флаг отображения диалога выбора конечной даты
 * @property transactionToDelete Транзакция, которую нужно удалить (null, если нет)
 * @property categoryToDelete Пара (название категории, isExpense)
 * @property sourceToDelete Источник, который нужно удалить (null, если нет)
 * @property expenseCategories Список категорий расходов
 * @property incomeCategories Список категорий доходов
 * @property protectedCategories Список защищенных категорий
 */
data class TransactionHistoryState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategories: List<String> = emptyList(),
    val selectedSources: List<String> = emptyList(),
    val groupingType: GroupingType = GroupingType.MONTH,
    val periodType: PeriodType = PeriodType.MONTH,
    val startDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
    val endDate: Date = Calendar.getInstance().time,
    val categoryStats: Triple<Money, Money, Int?>? = null,
    val showPeriodDialog: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showSourceDialog: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val transactionToDelete: Transaction? = null,
    val categoryToDelete: Pair<String, Boolean>? = null,
    val sourceToDelete: String? = null,
    val expenseCategories: List<CategoryItem> = emptyList(),
    val incomeCategories: List<CategoryItem> = emptyList(),
    val protectedCategories: List<String> = listOf("Другое") // Категории, которые нельзя удалить
) 