package com.davidbugayov.financeanalyzer.presentation.budget.model

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet

/**
 * Состояние для экрана бюджета
 *
 * @property categories Список кошельков (бюджетов)
 * @property isLoading Флаг загрузки данных
 * @property error Сообщение об ошибке, если есть
 * @property totalLimit Общий лимит бюджета (сумма всех кошельков)
 * @property totalSpent Общая потраченная сумма (сумма всех кошельков)
 * @property totalWalletBalance Общий баланс кошельков
 * @property selectedPeriodDuration Выбранная продолжительность расчетного периода в днях
 */
data class BudgetState(
    val categories: List<Wallet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalLimit: Money = Money.zero(),
    val totalSpent: Money = Money.zero(),
    val totalWalletBalance: Money = Money.zero(),
    val selectedPeriodDuration: Int = 14,
)
