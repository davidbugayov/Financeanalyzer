package com.davidbugayov.financeanalyzer.presentation.viewmodel

import com.davidbugayov.financeanalyzer.domain.repository.SavingGoalRepository
import com.davidbugayov.financeanalyzer.domain.model.SavingGoal
import java.util.Date

class GoalsViewModel(
    private val savingGoalRepository: SavingGoalRepository
) : BaseViewModel<GoalsViewModel.State, GoalsViewModel.Event>() {

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val goals: List<SavingGoal> = emptyList(),
        val selectedGoal: SavingGoal? = null,
        val isEditing: Boolean = false,
        // Form fields
        val name: String = "",
        val targetAmount: String = "",
        val currentAmount: String = "0",
        val deadline: Date? = null,
        val isActive: Boolean = true
    )

    sealed class Event {
        object LoadGoals : Event()
        data class SelectGoal(val goal: SavingGoal) : Event()
        object StartNewGoal : Event()
        object StartEditGoal : Event()
        data class UpdateName(val name: String) : Event()
        data class UpdateTargetAmount(val amount: String) : Event()
        data class UpdateCurrentAmount(val amount: String) : Event()
        data class UpdateDeadline(val date: Date?) : Event()
        data class UpdateIsActive(val isActive: Boolean) : Event()
        data class AddProgress(val amount: Double) : Event()
        object SaveGoal : Event()
        object DeleteGoal : Event()
        object CancelEdit : Event()
    }

    override fun createInitialState() = State()

    override fun onEvent(event: Event) {
        when (event) {
            is Event.LoadGoals -> loadGoals()
            is Event.SelectGoal -> selectGoal(event.goal)
            is Event.StartNewGoal -> startNewGoal()
            is Event.StartEditGoal -> startEditGoal()
            is Event.UpdateName -> updateState { copy(name = event.name) }
            is Event.UpdateTargetAmount -> updateState { copy(targetAmount = event.amount) }
            is Event.UpdateCurrentAmount -> updateState { copy(currentAmount = event.amount) }
            is Event.UpdateDeadline -> updateState { copy(deadline = event.date) }
            is Event.UpdateIsActive -> updateState { copy(isActive = event.isActive) }
            is Event.AddProgress -> addProgress(event.amount)
            is Event.SaveGoal -> saveGoal()
            is Event.DeleteGoal -> deleteGoal()
            is Event.CancelEdit -> cancelEdit()
        }
    }

    private fun loadGoals() {
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                savingGoalRepository.getActiveGoals()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun selectGoal(goal: SavingGoal) {
        updateState { 
            copy(
                selectedGoal = goal,
                name = goal.name,
                targetAmount = goal.targetAmount.toString(),
                currentAmount = goal.currentAmount.toString(),
                deadline = goal.deadline,
                isActive = goal.isActive
            )
        }
    }

    private fun startNewGoal() {
        updateState {
            copy(
                selectedGoal = null,
                isEditing = true,
                name = "",
                targetAmount = "",
                currentAmount = "0",
                deadline = null,
                isActive = true
            )
        }
    }

    private fun startEditGoal() {
        updateState { copy(isEditing = true) }
    }

    private fun addProgress(amount: Double) {
        val goalId = state.value.selectedGoal?.id?.toLong() ?: return
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                savingGoalRepository.updateGoalProgress(goalId, amount)
                loadGoals()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun saveGoal() {
        val currentState = state.value
        if (!validateInput(currentState)) {
            return
        }

        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                val goal = SavingGoal(
                    id = currentState.selectedGoal?.id ?: 0,
                    name = currentState.name,
                    targetAmount = currentState.targetAmount.toDoubleOrNull() ?: 0.0,
                    currentAmount = currentState.currentAmount.toDoubleOrNull() ?: 0.0,
                    deadline = currentState.deadline,
                    isActive = currentState.isActive,
                    startDate = Date()
                )
                
                if (currentState.selectedGoal != null) {
                    savingGoalRepository.updateGoal(goal)
                } else {
                    savingGoalRepository.insertGoal(goal)
                }
                
                updateState { 
                    copy(
                        isEditing = false,
                        selectedGoal = null
                    )
                }
                loadGoals()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun deleteGoal() {
        val goal = state.value.selectedGoal ?: return
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                savingGoalRepository.deleteGoal(goal)
                updateState { 
                    copy(
                        selectedGoal = null,
                        isEditing = false
                    )
                }
                loadGoals()
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
        state.value.selectedGoal?.let { selectGoal(it) }
    }

    private fun validateInput(state: State): Boolean {
        if (state.name.isBlank()) {
            updateState { copy(error = "Name cannot be empty") }
            return false
        }
        if (state.targetAmount.isBlank() || state.targetAmount.toDoubleOrNull() == null) {
            updateState { copy(error = "Invalid target amount") }
            return false
        }
        if (state.currentAmount.isBlank() || state.currentAmount.toDoubleOrNull() == null) {
            updateState { copy(error = "Invalid current amount") }
            return false
        }
        return true
    }
} 