package com.davidbugayov.financeanalyzer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.model.Transaction
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SharedViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions()
                .catch { e ->
                    _error.value = "Ошибка загрузки транзакций: ${e.message}"
                }
                .collect { transactions ->
                    _transactions.value = transactions
                }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.addTransaction(transaction)
                loadTransactions() // Перезагружаем список после добавления
            } catch (e: Exception) {
                _error.value = "Ошибка добавления транзакции: ${e.message}"
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                loadTransactions() // Перезагружаем список после удаления
            } catch (e: Exception) {
                _error.value = "Ошибка удаления транзакции: ${e.message}"
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                loadTransactions() // Перезагружаем список после обновления
            } catch (e: Exception) {
                _error.value = "Ошибка обновления транзакции: ${e.message}"
            }
        }
    }
}