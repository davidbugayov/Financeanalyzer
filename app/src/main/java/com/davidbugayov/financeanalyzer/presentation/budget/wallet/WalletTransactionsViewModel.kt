package com.davidbugayov.financeanalyzer.presentation.budget.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.model.WalletTransactionsEvent
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.model.WalletTransactionsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber

/**
 * ViewModel для экрана транзакций кошелька
 */
class WalletTransactionsViewModel(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(WalletTransactionsState())
    val state: StateFlow<WalletTransactionsState> = _state.asStateFlow()

    fun onEvent(event: WalletTransactionsEvent) {
        when (event) {
            is WalletTransactionsEvent.LoadWallet -> loadWallet(event.walletId)
            is WalletTransactionsEvent.LoadTransactions -> loadTransactions(event.walletId)
            is WalletTransactionsEvent.LinkCategories -> linkCategories(event.categories)
            is WalletTransactionsEvent.ClearError -> clearError()
        }
    }

    private fun loadWallet(walletId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val wallet = walletRepository.getWalletById(walletId)

                if (wallet == null) {
                    _state.update {
                        it.copy(
                            error = "Кошелек не найден", isLoading = false
                        )
                    }
                    return@launch
                }

                // Проверяем, не истек ли период
                val now = System.currentTimeMillis()
                val periodEnd = wallet.periodStartDate + (wallet.periodDuration * 24 * 60 * 60 * 1000L)

                if (now > periodEnd) {
                    // Период истек, сбрасываем потраченную сумму
                    val updatedWallet = wallet.copy(
                        spent = wallet.spent.copy(amount = java.math.BigDecimal.ZERO),
                        periodStartDate = now,
                        linkedCategories = wallet.linkedCategories
                    )

                    walletRepository.updateWallet(updatedWallet)

                    _state.update {
                        it.copy(
                            wallet = updatedWallet,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            wallet = wallet,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading wallet")
                _state.update {
                    it.copy(
                        error = e.message ?: "Ошибка при загрузке кошелька",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadTransactions(walletId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Получаем кошелек, если он еще не загружен
                val wallet = _state.value.wallet ?: walletRepository.getWalletById(walletId)

                if (wallet == null) {
                    _state.update {
                        it.copy(
                            error = "Кошелек не найден", isLoading = false
                        )
                    }
                    return@launch
                }

                // Загружаем все транзакции
                val allTransactions = transactionRepository.getAllTransactions()

                // Фильтруем транзакции, относящиеся к данному кошельку
                val walletTransactions = allTransactions.filter { transaction -> // Учитываем основное условие: совпадение по имени или ID категории
                    val baseMatch = transaction.category == wallet.name || transaction.categoryId == wallet.id

                    // Проверяем наличие в связанных категориях
                    val linkedCategories = wallet.linkedCategories
                    val linkedCategoryMatch = linkedCategories.isNotEmpty() && (linkedCategories.contains(transaction.category) || linkedCategories.contains(transaction.categoryId))

                    // Транзакция подходит, если выполняется любое из условий
                    baseMatch || linkedCategoryMatch
                }.sortedByDescending { it.date }

                _state.update {
                    it.copy(
                        wallet = wallet,
                        transactions = walletTransactions,
                        isLoading = false
                    )
                }

                Timber.d("Loaded ${walletTransactions.size} transactions for wallet ${wallet.name}")
            } catch (e: Exception) {
                Timber.e(e, "Error loading transactions")
                _state.update {
                    it.copy(
                        error = e.message ?: "Ошибка при загрузке транзакций",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Очищает сообщение об ошибке
     */
    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Связывает выбранные категории с текущим кошельком
     */
    private fun linkCategories(categories: List<String>) {
        viewModelScope.launch {
            try {
                val currentWallet = _state.value.wallet

                if (currentWallet == null) {
                    _state.update {
                        it.copy(
                            error = "Кошелек не найден"
                        )
                    }
                    return@launch
                }

                // Обновляем кошелек с новым списком связанных категорий
                val updatedWallet = currentWallet.copy(
                    linkedCategories = categories
                )

                // Сохраняем обновленный кошелек
                walletRepository.updateWallet(updatedWallet)

                // Обновляем состояние с новым кошельком
                _state.update {
                    it.copy(
                        wallet = updatedWallet
                    )
                }

                // Перезагружаем транзакции, чтобы отразить новые привязки
                loadTransactions(updatedWallet.id)

                Timber.d(
                    "Categories linked to wallet ${updatedWallet.name}: ${categories.joinToString()}"
                )
            } catch (e: Exception) {
                Timber.e(e, "Error linking categories")
                _state.update {
                    it.copy(
                        error = e.message ?: "Ошибка при связывании категорий"
                    )
                }
            }
        }
    }
} 
