package com.davidbugayov.financeanalyzer.presentation.budget.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.model.WalletType
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.shared.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Состояние мастера создания кошелька */
data class WalletSetupState(
    val name: String = "",
    val type: WalletType = WalletType.CARD,
    val isGoal: Boolean = false,
    val goalAmountText: String = "",
    val goalDateMillis: Long? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
)

class WalletSetupViewModel(
    private val walletRepository: WalletRepository,
    private val navigationManager: NavigationManager,
) : ViewModel(),
    KoinComponent {
    private val resourceProvider: ResourceProvider by inject()
    private val _state = MutableStateFlow(WalletSetupState())
    val state: StateFlow<WalletSetupState> = _state

    /**
     * Обновить название кошелька
     */
    fun updateName(value: String) {
        _state.value = _state.value.copy(name = value, error = null)
    }

    /**
     * Обновить тип кошелька
     */
    fun updateType(type: WalletType) {
        _state.value = _state.value.copy(type = type)
    }

    /**
     * Переключить режим накопительной цели
     */
    fun toggleGoal(enabled: Boolean) {
        _state.value =
            _state.value.copy(
                isGoal = enabled,
                goalAmountText = if (!enabled) "" else _state.value.goalAmountText,
            )
    }

    /**
     * Обновить текст целевой суммы
     */
    fun updateGoalAmountText(text: String) {
        _state.value = _state.value.copy(goalAmountText = text, error = null)
    }

    /**
     * Создать новый кошелёк
     */
    fun createWallet() {
        val s = _state.value

        // Валидация данных
        if (s.name.isBlank()) {
            _state.value = s.copy(error = resourceProvider.getString(UiR.string.error_enter_wallet_name))
            return
        }

        if (s.isGoal && s.goalAmountText.isBlank()) {
            _state.value = s.copy(error = resourceProvider.getString(UiR.string.error_enter_target_amount))
            return
        }

        val goalAmount =
            if (s.isGoal && s.goalAmountText.isNotBlank()) {
                try {
                    val amount = s.goalAmountText.toDouble()
                    if (amount <= 0) {
                        _state.value =
                            s.copy(error = resourceProvider.getString(UiR.string.error_target_amount_positive))
                        return
                    }
                    Money.fromMajor(amount)
                } catch (_: Exception) {
                    _state.value = s.copy(error = resourceProvider.getString(UiR.string.error_enter_valid_amount))
                    return
                }
            } else {
                null
            }

        _state.value = s.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val wallet =
                    Wallet(
                        name = s.name.trim(),
                        limit = Money.zero(),
                        spent = Money.zero(),
                        id = UUID.randomUUID().toString(),
                        balance = Money.zero(),
                        type = s.type,
                        goalAmount = goalAmount,
                        goalDate = s.goalDateMillis,
                    )

                walletRepository.addWallet(wallet)

                // Триггер ачивки "Первый бюджет" при создании кошелька
                AchievementTrigger.onBudgetCreated()

                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                    )

                // Небольшая задержка для показа успешного состояния
                kotlinx.coroutines.delay(500)

                // Возвращаемся к экрану бюджета где будет отображен новый кошелёк
                navigationManager.navigate(NavigationManager.Command.NavigateUp)
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        error = resourceProvider.getString(UiR.string.error_creating_wallet, e.message ?: ""),
                    )
            }
        }
    }

    /**
     * Навигация назад
     */
    fun navigateBack() {
        navigationManager.navigate(NavigationManager.Command.NavigateUp)
    }
}
