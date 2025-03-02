package com.davidbugayov.financeanalyzer.presentation.viewmodel

import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import com.davidbugayov.financeanalyzer.domain.model.Budget
import com.davidbugayov.financeanalyzer.data.local.model.BudgetWithSpending
import java.util.Date

class BudgetViewModel(
    private val budgetRepository: BudgetRepository
) : BaseViewModel<BudgetViewModel.State, BudgetViewModel.Event>() {

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val budgets: List<BudgetWithSpending> = emptyList(),
        val selectedBudget: Budget? = null,
        val isEditing: Boolean = false,
        // Form fields
        val category: String = "",
        val limit: String = "",
        val startDate: Date = Date(),
        val endDate: Date? = null,
        val isActive: Boolean = true
    )

    sealed class Event {
        object LoadBudgets : Event()
        data class SelectBudget(val budget: Budget) : Event()
        object StartNewBudget : Event()
        object StartEditBudget : Event()
        data class UpdateCategory(val category: String) : Event()
        data class UpdateLimit(val limit: String) : Event()
        data class UpdateStartDate(val date: Date) : Event()
        data class UpdateEndDate(val date: Date?) : Event()
        data class UpdateIsActive(val isActive: Boolean) : Event()
        object SaveBudget : Event()
        object DeleteBudget : Event()
        object CancelEdit : Event()
    }

    override fun createInitialState() = State()

    override fun onEvent(event: Event) {
        when (event) {
            is Event.LoadBudgets -> loadBudgets()
            is Event.SelectBudget -> selectBudget(event.budget)
            is Event.StartNewBudget -> startNewBudget()
            is Event.StartEditBudget -> startEditBudget()
            is Event.UpdateCategory -> updateState { copy(category = event.category) }
            is Event.UpdateLimit -> updateState { copy(limit = event.limit) }
            is Event.UpdateStartDate -> updateState { copy(startDate = event.date) }
            is Event.UpdateEndDate -> updateState { copy(endDate = event.date) }
            is Event.UpdateIsActive -> updateState { copy(isActive = event.isActive) }
            is Event.SaveBudget -> saveBudget()
            is Event.DeleteBudget -> deleteBudget()
            is Event.CancelEdit -> cancelEdit()
        }
    }

    private fun loadBudgets() {
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                budgetRepository.getBudgetsWithSpending()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun selectBudget(budget: Budget) {
        updateState { 
            copy(
                selectedBudget = budget,
                category = budget.category,
                limit = budget.limit.toString(),
                startDate = budget.startDate,
                endDate = budget.endDate,
                isActive = budget.isActive
            )
        }
    }

    private fun startNewBudget() {
        updateState {
            copy(
                selectedBudget = null,
                isEditing = true,
                category = "",
                limit = "",
                startDate = Date(),
                endDate = null,
                isActive = true
            )
        }
    }

    private fun startEditBudget() {
        updateState { copy(isEditing = true) }
    }

    private fun saveBudget() {
        val currentState = state.value
        if (!validateInput(currentState)) {
            return
        }

        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                val budget = Budget(
                    id = currentState.selectedBudget?.id ?: 0,
                    category = currentState.category,
                    limit = currentState.limit.toDoubleOrNull() ?: 0.0,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate,
                    isActive = currentState.isActive
                )
                
                if (currentState.selectedBudget != null) {
                    budgetRepository.updateBudget(budget)
                } else {
                    budgetRepository.insertBudget(budget)
                }
                
                updateState { 
                    copy(
                        isEditing = false,
                        selectedBudget = null
                    )
                }
                loadBudgets()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun deleteBudget() {
        val budget = state.value.selectedBudget ?: return
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                budgetRepository.deleteBudget(budget)
                updateState { 
                    copy(
                        selectedBudget = null,
                        isEditing = false
                    )
                }
                loadBudgets()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun cancelEdit() {
        updateState { 
            copy(
                isEditing = false,
                error = null
            )
        }
        state.value.selectedBudget?.let { selectBudget(it) }
    }

    private fun validateInput(state: State): Boolean {
        if (state.category.isBlank()) {
            updateState { copy(error = "Category cannot be empty") }
            return false
        }
        if (state.limit.isBlank() || state.limit.toDoubleOrNull() == null) {
            updateState { copy(error = "Invalid limit amount") }
            return false
        }
        return true
    }
} 