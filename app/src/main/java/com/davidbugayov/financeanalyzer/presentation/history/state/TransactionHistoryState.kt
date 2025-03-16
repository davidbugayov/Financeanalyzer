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
 * @property selectedCategory Выбранная категория для фильтрации (null для всех категорий)
 * @property groupingType Тип группировки транзакций (по дням, неделям, месяцам)
 * @property periodType Тип периода для фильтрации
 * @property startDate Начальная дата для кастомного периода
 * @property endDate Конечная дата для кастомного периода
 * @property categoryStats Статистика по категории: (текущая сумма, предыдущая сумма, процент изменения)
 * @property showPeriodDialog Флаг отображения диалога выбора периода
 * @property showCategoryDialog Флаг отображения диалога выбора категории
 * @property showStartDatePicker Флаг отображения диалога выбора начальной даты
 * @property showEndDatePicker Флаг отображения диалога выбора конечной даты
 * @property transactionToDelete Транзакция, которую нужно удалить (null, если нет)
 * @property categoryToDelete Пара (название категории, isExpense)
 * @property expenseCategories Список категорий расходов
 * @property incomeCategories Список категорий доходов
 */
data class TransactionHistoryState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val groupingType: GroupingType = GroupingType.MONTH,
    val periodType: PeriodType = PeriodType.MONTH,
    val startDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
    val endDate: Date = Calendar.getInstance().time,
    val categoryStats: Triple<Money, Money, Int?>? = null,
    val showPeriodDialog: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val transactionToDelete: Transaction? = null,
    val categoryToDelete: Pair<String, Boolean>? = null,
    val expenseCategories: List<CategoryItem> = emptyList(),
    val incomeCategories: List<CategoryItem> = emptyList()
) 