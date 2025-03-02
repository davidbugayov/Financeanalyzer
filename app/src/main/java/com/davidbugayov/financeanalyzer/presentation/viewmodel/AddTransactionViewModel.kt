package com.davidbugayov.financeanalyzer.presentation.viewmodel

import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository
) : BaseViewModel<AddTransactionViewModel.State, AddTransactionViewModel.Event>() {

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val date: Date = Date(),
        val title: String = "",
        val amount: String = "",
        val category: String = "",
        val isExpense: Boolean = true,
        val note: String = "",
        val isSuccess: Boolean = false
    )

    sealed class Event {
        data class UpdateDate(val date: Date) : Event()
        data class UpdateTitle(val title: String) : Event()
        data class UpdateAmount(val amount: String) : Event()
        data class UpdateCategory(val category: String) : Event()
        data class UpdateIsExpense(val isExpense: Boolean) : Event()
        data class UpdateNote(val note: String) : Event()
        object SaveTransaction : Event()
        object ResetForm : Event()
    }

    override fun createInitialState() = State()

    override fun onEvent(event: Event) {
        when (event) {
            is Event.UpdateDate -> updateState { copy(date = event.date) }
            is Event.UpdateTitle -> updateState { copy(title = event.title) }
            is Event.UpdateAmount -> updateState { copy(amount = event.amount) }
            is Event.UpdateCategory -> updateState { copy(category = event.category) }
            is Event.UpdateIsExpense -> updateState { copy(isExpense = event.isExpense) }
            is Event.UpdateNote -> updateState { copy(note = event.note) }
            is Event.SaveTransaction -> saveTransaction()
            is Event.ResetForm -> resetForm()
        }
    }

    private fun saveTransaction() {
        val currentState = state.value
        if (!validateInput(currentState)) {
            return
        }

        launchInViewModelScope {
            updateState { copy(isLoading = true, error = null) }
            try {
                val transaction = Transaction(
                    date = currentState.date,
                    title = currentState.title,
                    amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                    category = currentState.category,
                    isExpense = currentState.isExpense,
                    note = currentState.note
                )
                transactionRepository.insertTransaction(transaction)
                updateState { copy(isSuccess = true) }
                resetForm()
            } catch (e: Exception) {
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun validateInput(state: State): Boolean {
        if (state.title.isBlank()) {
            updateState { copy(error = "Title cannot be empty") }
            return false
        }
        if (state.amount.isBlank() || state.amount.toDoubleOrNull() == null) {
            updateState { copy(error = "Invalid amount") }
            return false
        }
        if (state.category.isBlank()) {
            updateState { copy(error = "Category cannot be empty") }
            return false
        }
        return true
    }

    private fun resetForm() {
        updateState {
            copy(
                date = Date(),
                title = "",
                amount = "",
                category = "",
                isExpense = true,
                note = "",
                error = null
            )
        }
    }
} 