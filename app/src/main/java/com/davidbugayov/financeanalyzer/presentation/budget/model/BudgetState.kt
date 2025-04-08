package com.davidbugayov.financeanalyzer.presentation.budget.model

import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Состояние для экрана бюджета
 *
 * @property categories Список бюджетных категорий
 * @property isLoading Флаг загрузки данных
 * @property error Сообщение об ошибке, если есть
 * @property totalLimit Общий лимит бюджета (сумма всех категорий)
 * @property totalSpent Общая потраченная сумма (сумма всех категорий)
 * @property totalWalletBalance Общий баланс "виртуальных кошельков"
 * @property selectedPeriodDuration Выбранная продолжительность расчетного периода в днях
 */
data class BudgetState(
    val categories: List<BudgetCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalLimit: Money = Money(0.0),
    val totalSpent: Money = Money(0.0),
    val totalWalletBalance: Money = Money(0.0),
    val selectedPeriodDuration: Int = 14
) 