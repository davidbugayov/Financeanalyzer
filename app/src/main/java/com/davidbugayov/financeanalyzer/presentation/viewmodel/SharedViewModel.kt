package com.davidbugayov.financeanalyzer.presentation.viewmodel

import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import com.davidbugayov.financeanalyzer.domain.repository.SavingGoalRepository
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Budget
import com.davidbugayov.financeanalyzer.domain.model.SavingGoal
import kotlinx.coroutines.flow.Flow

class SharedViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val savingGoalRepository: SavingGoalRepository
) : BaseViewModel<SharedViewModel.State, SharedViewModel.Event>() {

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val transactions: List<Transaction> = emptyList(),
        val budgets: List<Budget> = emptyList(),
        val goals: List<SavingGoal> = emptyList()
    )

    sealed class Event {
        object LoadData : Event()
        object RefreshData : Event()
        data class DeleteTransaction(val transaction: Transaction) : Event()
        data class DeleteBudget(val budget: Budget) : Event()
        data class DeleteGoal(val goal: SavingGoal) : Event()
    }

    override fun createInitialState() = State()

    override fun onEvent(event: Event) {
        when (event) {
            is Event.LoadData -> loadAllData()
            is Event.RefreshData -> refreshData()
            is Event.DeleteTransaction -> deleteTransaction(event.transaction)
            is Event.DeleteBudget -> deleteBudget(event.budget)
            is Event.DeleteGoal -> deleteGoal(event.goal)
        }
    }

    private fun loadAllData() {
        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                // Load data from repositories
                transactionRepository.getAllTransactions()
                budgetRepository.getActiveBudgets()
                savingGoalRepository.getActiveGoals()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun refreshData() {
        loadAllData()
    }

    private fun deleteTransaction(transaction: Transaction) {
        launchInViewModelScope {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    private fun deleteBudget(budget: Budget) {
        launchInViewModelScope {
            budgetRepository.deleteBudget(budget)
        }
    }

    private fun deleteGoal(goal: SavingGoal) {
        launchInViewModelScope {
            savingGoalRepository.deleteGoal(goal)
        }
    }
} 