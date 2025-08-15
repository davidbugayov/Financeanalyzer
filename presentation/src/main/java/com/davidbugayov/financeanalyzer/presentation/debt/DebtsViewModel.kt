package com.davidbugayov.financeanalyzer.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.domain.usecase.debt.CreateDebtUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.GetDebtsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.RepayDebtUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DebtsState(
    val isLoading: Boolean = false,
    val debts: List<Debt> = emptyList(),
    val error: String? = null,
)

class DebtsViewModel(
    private val createDebtUseCase: CreateDebtUseCase,
    private val getDebtsUseCase: GetDebtsUseCase,
    private val repayDebtUseCase: RepayDebtUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DebtsState())
    val state: StateFlow<DebtsState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { getDebtsUseCase() }
                .onSuccess { _state.value = DebtsState(debts = it) }
                .onFailure { _state.value = DebtsState(error = it.message) }
        }
    }

    fun createDebt(
        title: String,
        counterparty: String,
        type: DebtType,
        amount: Money,
        dueAt: Long?,
        note: String?,
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val debt =
                Debt(
                    id = "",
                    title = title,
                    counterparty = counterparty,
                    type = type,
                    status = DebtStatus.ACTIVE,
                    principal = amount,
                    remaining = amount,
                    createdAt = now,
                    dueAt = dueAt,
                    note = note,
                )
            runCatching { createDebtUseCase(debt) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun repay(
        id: String,
        amount: Money,
    ) {
        viewModelScope.launch {
            runCatching { repayDebtUseCase(id, amount) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
}
