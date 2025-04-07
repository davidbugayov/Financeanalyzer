package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

class BudgetViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        loadBudgetCategories()
    }

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.LoadCategories -> loadBudgetCategories()
            is BudgetEvent.AddCategory -> addCategory(event.name, event.limit)
            is BudgetEvent.UpdateCategory -> updateCategory(event.category)
            is BudgetEvent.DeleteCategory -> deleteCategory(event.category)
            is BudgetEvent.ClearError -> clearError()
            is BudgetEvent.DistributeIncome -> distributeIncome(event.amount)
            is BudgetEvent.AddFundsToWallet -> addFundsToWallet(event.categoryId, event.amount)
            is BudgetEvent.SpendFromWallet -> spendFromWallet(event.categoryId, event.amount)
            is BudgetEvent.TransferBetweenWallets -> transferBetweenWallets(
                event.fromCategoryId,
                event.toCategoryId,
                event.amount
            )

            is BudgetEvent.SetPeriodDuration -> setPeriodDuration(event.days)
            is BudgetEvent.ResetPeriod -> resetPeriod(event.categoryId)
            is BudgetEvent.ResetAllPeriods -> resetAllPeriods()
        }
    }

    private fun loadBudgetCategories() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Загружаем бюджетные категории из репозитория
                budgetRepository.getAllCategories()
                    .collect { categories ->
                        _state.update {
                            it.copy(
                                categories = categories,
                                isLoading = false
                            )
                        }
                        calculateTotals()
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading budget categories")
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateTotals() {
        val categories = _state.value.categories
        val totalLimit = categories.sumOf { it.limit }
        val totalSpent = categories.sumOf { it.spent }
        val totalWalletBalance = categories.sumOf { it.walletBalance }

        _state.update {
            it.copy(
                totalLimit = totalLimit,
                totalSpent = totalSpent,
                totalWalletBalance = totalWalletBalance
            )
        }
    }

    private fun addCategory(name: String, limit: Double) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Создаем новую категорию
                val newCategory = BudgetCategory(
                    name = name,
                    limit = limit,
                    spent = 0.0,
                    id = "",  // ID будет сгенерирован в репозитории
                    walletBalance = 0.0,
                    periodDuration = _state.value.selectedPeriodDuration
                )

                // Сохраняем в репозиторий
                budgetRepository.addCategory(newCategory)

                // Категории будут обновлены автоматически через Flow
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error adding budget category")
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun updateCategory(category: BudgetCategory) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Обновляем категорию в репозитории
                budgetRepository.updateCategory(category)

                // Категории будут обновлены автоматически через Flow
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error updating budget category")
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteCategory(category: BudgetCategory) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Удаляем категорию из репозитория
                budgetRepository.deleteCategory(category.id)

                // Категории будут обновлены автоматически через Flow
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting budget category")
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    // Распределяет полученный доход по всем категориям согласно их лимитам
    private fun distributeIncome(amount: Double) {
        try {
            if (_state.value.categories.isEmpty() || amount <= 0) {
                return
            }

            val totalLimit = _state.value.totalLimit
            var remainingAmount = amount

            // Обновляем категории, пропорционально распределяя доход
            viewModelScope.launch {
                try {
                    _state.value.categories.forEach { category ->
                        val categoryRatio = category.limit / totalLimit
                        val categoryAmount = (amount * categoryRatio).coerceAtMost(remainingAmount)
                        remainingAmount -= categoryAmount

                        val updatedCategory = category.copy(
                            walletBalance = category.walletBalance + categoryAmount
                        )

                        budgetRepository.updateCategory(updatedCategory)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error updating categories during income distribution")
                    _state.update {
                        it.copy(error = e.message ?: "Ошибка при распределении дохода")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error distributing income")
            _state.update {
                it.copy(error = e.message ?: "Ошибка при распределении дохода")
            }
        }
    }

    private fun addFundsToWallet(categoryId: String, amount: Double) {
        if (amount <= 0) return

        viewModelScope.launch {
            try {
                // Получаем категорию из репозитория
                val category = budgetRepository.getCategoryById(categoryId) ?: return@launch

                // Обновляем баланс кошелька
                val updatedCategory = category.copy(
                    walletBalance = category.walletBalance + amount
                )

                // Сохраняем обновленную категорию
                budgetRepository.updateCategory(updatedCategory)
            } catch (e: Exception) {
                Timber.e(e, "Error adding funds to wallet")
                _state.update {
                    it.copy(error = e.message ?: "Ошибка при добавлении средств")
                }
            }
        }
    }

    private fun spendFromWallet(categoryId: String, amount: Double) {
        if (amount <= 0) return

        viewModelScope.launch {
            try {
                // Получаем категорию из репозитория
                val category = budgetRepository.getCategoryById(categoryId) ?: return@launch

                // Проверяем, достаточно ли средств в кошельке
                if (category.walletBalance < amount) {
                    _state.update {
                        it.copy(error = "Недостаточно средств в кошельке")
                    }
                    return@launch
                }

                // Обновляем баланс кошелька и расходы
                val updatedCategory = category.copy(
                    walletBalance = category.walletBalance - amount,
                    spent = category.spent + amount
                )

                // Сохраняем обновленную категорию
                budgetRepository.updateCategory(updatedCategory)
            } catch (e: Exception) {
                Timber.e(e, "Error spending from wallet")
                _state.update {
                    it.copy(error = e.message ?: "Ошибка при списании средств")
                }
            }
        }
    }

    private fun transferBetweenWallets(
        fromCategoryId: String,
        toCategoryId: String,
        amount: Double
    ) {
        if (amount <= 0 || fromCategoryId == toCategoryId) return

        viewModelScope.launch {
            try {
                // Получаем категории из репозитория
                val fromCategory = budgetRepository.getCategoryById(fromCategoryId) ?: return@launch
                val toCategory = budgetRepository.getCategoryById(toCategoryId) ?: return@launch

                // Проверяем, достаточно ли средств в кошельке
                if (fromCategory.walletBalance < amount) {
                    _state.update {
                        it.copy(error = "Недостаточно средств в исходном кошельке")
                    }
                    return@launch
                }

                // Обновляем оба кошелька
                val updatedFromCategory = fromCategory.copy(
                    walletBalance = fromCategory.walletBalance - amount
                )

                val updatedToCategory = toCategory.copy(
                    walletBalance = toCategory.walletBalance + amount
                )

                // Сохраняем обновленные категории
                budgetRepository.updateCategory(updatedFromCategory)
                budgetRepository.updateCategory(updatedToCategory)
            } catch (e: Exception) {
                Timber.e(e, "Error transferring between wallets")
                _state.update {
                    it.copy(error = e.message ?: "Ошибка при переводе средств")
                }
            }
        }
    }

    private fun setPeriodDuration(days: Int) {
        if (days <= 0) return

        _state.update {
            it.copy(selectedPeriodDuration = days)
        }

        // Обновляем период для всех категорий
        viewModelScope.launch {
            try {
                _state.value.categories.forEach { category ->
                    val updatedCategory = category.copy(periodDuration = days)
                    budgetRepository.updateCategory(updatedCategory)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error setting period duration")
                _state.update {
                    it.copy(error = e.message ?: "Ошибка при установке длительности периода")
                }
            }
        }
    }

    private fun resetPeriod(categoryId: String) {
        viewModelScope.launch {
            try {
                // Получаем категорию из репозитория
                val category = budgetRepository.getCategoryById(categoryId) ?: return@launch

                // Обнуляем затраты и обновляем дату начала периода
                val updatedCategory = category.copy(
                    spent = 0.0,
                    periodStartDate = System.currentTimeMillis()
                )

                // Сохраняем обновленную категорию
                budgetRepository.updateCategory(updatedCategory)
            } catch (e: Exception) {
                Timber.e(e, "Error resetting period")
                _state.update {
                    it.copy(error = e.message ?: "Ошибка при сбросе периода")
                }
            }
        }
    }

    private fun resetAllPeriods() {
        viewModelScope.launch {
            try {
                _state.value.categories.forEach { category ->
                    val updatedCategory = category.copy(
                        spent = 0.0,
                        periodStartDate = System.currentTimeMillis()
                    )
                    budgetRepository.updateCategory(updatedCategory)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error resetting all periods")
                _state.update {
                    it.copy(error = e.message ?: "Ошибка при сбросе всех периодов")
                }
            }
        }
    }

    // Проверяет, не истек ли расчетный период для категорий
    fun checkPeriodsExpiration() {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val expiredCategories = _state.value.categories.filter { category ->
            calendar.timeInMillis = category.periodStartDate
            calendar.add(Calendar.DAY_OF_MONTH, category.periodDuration)
            currentTime > calendar.timeInMillis
        }

        if (expiredCategories.isNotEmpty()) {
            val updatedCategories = _state.value.categories.map { category ->
                calendar.timeInMillis = category.periodStartDate
                calendar.add(Calendar.DAY_OF_MONTH, category.periodDuration)

                if (currentTime > calendar.timeInMillis) {
                    // Период истек, сбрасываем
                    category.copy(
                        spent = 0.0,
                        periodStartDate = System.currentTimeMillis()
                    )
                } else {
                    category
                }
            }

            _state.update {
                it.copy(categories = updatedCategories)
            }

            calculateTotals()
        }
    }

    private fun clearError() {
        _state.update {
            it.copy(error = null)
        }
    }
} 