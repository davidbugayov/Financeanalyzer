package com.davidbugayov.financeanalyzer.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.domain.usecase.debt.CalculateDebtStatisticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.CheckOverdueDebtsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.CreateDebtUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.ExportDebtsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.GetDebtsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.GetFilteredDebtsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.debt.RepayDebtUseCase
import com.davidbugayov.financeanalyzer.domain.model.DebtStatistics
import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlinx.coroutines.async
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
    private val calculateDebtStatisticsUseCase: CalculateDebtStatisticsUseCase,
    private val getFilteredDebtsUseCase: GetFilteredDebtsUseCase,
    private val exportDebtsToCSVUseCase: ExportDebtsToCSVUseCase,
    private val checkOverdueDebtsUseCase: CheckOverdueDebtsUseCase,
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

    suspend fun getStatistics(): DebtStatistics? {
        return runCatching { calculateDebtStatisticsUseCase() }.getOrNull()
    }

    fun filterDebts(
        type: DebtType? = null,
        status: DebtStatus? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        searchQuery: String? = null,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                getFilteredDebtsUseCase(type, status, minAmount, maxAmount, searchQuery)
            }
                .onSuccess { _state.value = DebtsState(debts = it) }
                .onFailure { _state.value = DebtsState(error = it.message) }
        }
    }

    suspend fun exportToCSV(outputDir: java.io.File): java.io.File? {
        return runCatching {
            val currentDebts = _state.value.debts
            exportDebtsToCSVUseCase(currentDebts, outputDir)
        }.getOrNull()
    }

    fun checkOverdueDebts() {
        viewModelScope.launch {
            runCatching { checkOverdueDebtsUseCase() }
                .onSuccess { result ->
                    if (result.updatedCount > 0) {
                        refresh() // Обновляем список, если были изменения
                    }
                }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
}
