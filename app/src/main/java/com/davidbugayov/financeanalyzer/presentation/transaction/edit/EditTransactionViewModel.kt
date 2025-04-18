package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateTransactionUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class EditTransactionViewModel(
    application: Application,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val categoriesViewModel: CategoriesViewModel
) : AndroidViewModel(application), KoinComponent {

    private val _state = MutableStateFlow(EditTransactionState())
    val state: StateFlow<EditTransactionState> = _state.asStateFlow()

    fun loadTransactionForEdit(transactionId: String) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(transactionId)
            _state.update { it.copy(
                transactionToEdit = transaction,
                title = transaction?.title ?: "",
                amount = transaction?.amount?.toString() ?: "",
                category = transaction?.category ?: "",
                note = transaction?.note ?: "",
                selectedDate = transaction?.date ?: it.selectedDate,
                isExpense = transaction?.isExpense ?: true
            ) }
        }
    }

    fun onEvent(event: EditTransactionEvent) {
        when (event) {
            is EditTransactionEvent.LoadTransaction -> loadTransactionForEdit(event.id)
            is EditTransactionEvent.SubmitEdit -> submitEdit()
            // ...обработка других событий
        }
    }

    private fun submitEdit() {
        // TODO: реализовать сохранение изменений транзакции
    }
} 