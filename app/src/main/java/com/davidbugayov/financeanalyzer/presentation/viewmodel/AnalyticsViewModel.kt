package com.davidbugayov.financeanalyzer.presentation.viewmodel

import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.data.local.model.CategoryTotal
import java.util.Date
import java.util.Calendar

class AnalyticsViewModel(
    private val transactionRepository: TransactionRepository
) : BaseViewModel<AnalyticsViewModel.State, AnalyticsViewModel.Event>() {

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val startDate: Date = getStartOfMonth(),
        val endDate: Date = Date(),
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val categoryTotals: List<CategoryTotal> = emptyList()
    )

    sealed class Event {
        data class UpdateDateRange(val startDate: Date, val endDate: Date) : Event()
        object LoadAnalytics : Event()
        object ExportData : Event()
    }

    override fun createInitialState() = State()

    override fun onEvent(event: Event) {
        when (event) {
            is Event.UpdateDateRange -> updateDateRange(event.startDate, event.endDate)
            is Event.LoadAnalytics -> loadAnalytics()
            is Event.ExportData -> exportData()
        }
    }

    private fun updateDateRange(startDate: Date, endDate: Date) {
        updateState { 
            copy(
                startDate = startDate,
                endDate = endDate
            )
        }
        loadAnalytics()
    }

    private fun loadAnalytics() {
        val currentState = state.value
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                // Load income total
                transactionRepository.getTotalByType(
                    isExpense = false,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate
                )

                // Load expense total
                transactionRepository.getTotalByType(
                    isExpense = true,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate
                )

                // Load category totals
                transactionRepository.getCategoryTotals(
                    startDate = currentState.startDate,
                    endDate = currentState.endDate
                )
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun exportData() {
        val currentState = state.value
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                transactionRepository.exportTransactions(
                    startDate = currentState.startDate,
                    endDate = currentState.endDate
                )
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    companion object {
        private fun getStartOfMonth(): Date {
            return Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }
    }
} 