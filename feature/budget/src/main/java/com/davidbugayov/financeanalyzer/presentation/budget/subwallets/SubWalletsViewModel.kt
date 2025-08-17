package com.davidbugayov.financeanalyzer.presentation.budget.subwallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubWalletsState(
    val parentWallet: Wallet? = null,
    val subWallets: List<Wallet> = emptyList(),
    val totalSubWalletAmount: Money = Money.zero(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SubWalletsViewModel(
    private val parentWalletId: String,
    private val walletRepository: WalletRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SubWalletsState())
    val state: StateFlow<SubWalletsState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // Загружаем родительский кошелек
                val parentWallet = walletRepository.getWalletById(parentWalletId)
                if (parentWallet != null) {
                    _state.value = _state.value.copy(parentWallet = parentWallet)

                    // Загружаем подкошельки
                    val subWallets = walletRepository.getSubWallets(parentWalletId)
                    val totalAmount =
                        subWallets.fold(Money.zero()) { acc, wallet ->
                            acc + wallet.balance
                        }

                    _state.value =
                        _state.value.copy(
                            subWallets = subWallets,
                            totalSubWalletAmount = totalAmount,
                            isLoading = false,
                        )
                } else {
                    _state.value =
                        _state.value.copy(
                            error = "Кошелек не найден",
                            isLoading = false,
                        )
                }
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        error = e.message ?: "Неизвестная ошибка",
                        isLoading = false,
                    )
            }
        }
    }

    fun editSubWallet(subWalletId: String) {
        // TODO: Навигация к экрану редактирования
    }

    fun deleteSubWallet(subWalletId: String) {
        viewModelScope.launch {
            try {
                val wallet = walletRepository.getWalletById(subWalletId)
                if (wallet != null) {
                    walletRepository.deleteWallet(wallet)
                    // Перезагружаем данные
                    loadData()
                } else {
                    _state.value =
                        _state.value.copy(
                            error = "Подкошелек не найден",
                        )
                }
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        error = e.message ?: "Неизвестная ошибка",
                    )
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
