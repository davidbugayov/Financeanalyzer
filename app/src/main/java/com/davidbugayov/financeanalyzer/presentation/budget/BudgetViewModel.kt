package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetEvent
import com.davidbugayov.financeanalyzer.presentation.budget.model.BudgetState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.UUID

class BudgetViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        loadBudgetCategories()
        calculateTotals()
        
        // Создаем примеры категорий через небольшую задержку,
        // чтобы дать время на загрузку существующих категорий
        viewModelScope.launch {
            delay(500)
            createSampleBudgetCategories()
        }
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

    /**
     * Создает образцы бюджетных категорий, если их еще нет
     */
    private fun createSampleBudgetCategories() {
        viewModelScope.launch {
            try {
                // Проверяем, есть ли уже категории
                if (_state.value.categories.isEmpty()) {
                    Timber.d("Создаем примеры категорий бюджета")
                    
                    // Примеры категорий
                    val sampleCategories = listOf(
                        BudgetCategory(
                            name = "Продукты", 
                            limit = 5000.0, 
                            spent = 0.0, 
                            id = UUID.randomUUID().toString(),
                            walletBalance = 5000.0
                        ),
                        BudgetCategory(
                            name = "Развлечения", 
                            limit = 3000.0, 
                            spent = 0.0, 
                            id = UUID.randomUUID().toString(),
                            walletBalance = 3000.0
                        ),
                        BudgetCategory(
                            name = "Транспорт", 
                            limit = 2000.0, 
                            spent = 0.0, 
                            id = UUID.randomUUID().toString(),
                            walletBalance = 2000.0
                        )
                    )
                    
                    // Добавляем категории в репозиторий
                    sampleCategories.forEach { category ->
                        budgetRepository.addBudgetCategory(category)
                    }
                    
                    // Перезагружаем категории
                    loadBudgetCategories()
                    
                    Timber.d("Примеры категорий бюджета созданы")
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при создании примеров категорий бюджета")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при создании примеров категорий бюджета"
                    )
                }
            }
        }
    }

    private fun loadBudgetCategories() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Загружаем категории из репозитория
                val categories = budgetRepository.getAllBudgetCategories()
                
                _state.update { 
                    it.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
                
                calculateTotals()
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
                
                val newCategory = BudgetCategory(
                    name = name, 
                    limit = limit, 
                    spent = 0.0, 
                    id = UUID.randomUUID().toString(),
                    walletBalance = 0.0
                )
                
                // Добавляем категорию в репозиторий
                budgetRepository.addBudgetCategory(newCategory)
                
                // Перезагружаем категории
                loadBudgetCategories()
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
                budgetRepository.updateBudgetCategory(category)
                
                // Перезагружаем категории
                loadBudgetCategories()
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
                budgetRepository.deleteBudgetCategory(category)
                
                // Перезагружаем категории
                loadBudgetCategories()
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
        viewModelScope.launch {
            try {
                if (_state.value.categories.isEmpty() || amount <= 0) {
                    return@launch
                }
                
                val totalLimit = _state.value.totalLimit
                var remainingAmount = amount
                
                // Обновляем категории, пропорционально распределяя доход
                val updatedCategories = _state.value.categories.map { category ->
                    val categoryRatio = category.limit / totalLimit
                    val categoryAmount = (amount * categoryRatio).coerceAtMost(remainingAmount)
                    remainingAmount -= categoryAmount
                    
                    category.copy(
                        walletBalance = category.walletBalance + categoryAmount
                    )
                }
                
                // Обновляем каждую категорию в репозитории
                updatedCategories.forEach { category ->
                    budgetRepository.updateBudgetCategory(category)
                }
                
                // Перезагружаем категории
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error distributing income")
                _state.update { 
                    it.copy(error = e.message ?: "Ошибка при распределении дохода")
                }
            }
        }
    }
    
    // Добавляет средства в кошелек конкретной категории
    private fun addFundsToWallet(categoryId: String, amount: Double) {
        viewModelScope.launch {
            try {
                if (amount <= 0) return@launch
                
                // Находим категорию по ID
                val category = budgetRepository.getBudgetCategoryById(categoryId) ?: return@launch
                
                // Обновляем категорию
                val updatedCategory = category.copy(
                    walletBalance = category.walletBalance + amount
                )
                
                // Сохраняем обновленную категорию
                budgetRepository.updateBudgetCategory(updatedCategory)
                
                // Перезагружаем категории
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error adding funds to wallet")
                _state.update { 
                    it.copy(error = e.message ?: "Ошибка при добавлении средств в кошелек")
                }
            }
        }
    }
    
    // Списывает средства из кошелька и увеличивает счетчик потраченных средств
    private fun spendFromWallet(categoryId: String, amount: Double) {
        viewModelScope.launch {
            try {
                if (amount <= 0) return@launch
                
                // Находим категорию по ID
                val category = budgetRepository.getBudgetCategoryById(categoryId) ?: return@launch
                
                // Проверяем, достаточно ли средств
                if (category.walletBalance < amount) {
                    _state.update { 
                        it.copy(error = "Недостаточно средств в кошельке '${category.name}'")
                    }
                    return@launch
                }
                
                // Обновляем категорию
                val updatedCategory = category.copy(
                    walletBalance = category.walletBalance - amount,
                    spent = category.spent + amount
                )
                
                // Сохраняем обновленную категорию
                budgetRepository.updateBudgetCategory(updatedCategory)
                
                // Перезагружаем категории
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error spending from wallet")
                _state.update { 
                    it.copy(error = e.message ?: "Ошибка при списании средств из кошелька")
                }
            }
        }
    }
    
    // Перевод средств между кошельками
    private fun transferBetweenWallets(fromCategoryId: String, toCategoryId: String, amount: Double) {
        viewModelScope.launch {
            try {
                if (amount <= 0 || fromCategoryId == toCategoryId) return@launch
                
                // Находим категории по ID
                val fromCategory = budgetRepository.getBudgetCategoryById(fromCategoryId) ?: return@launch
                val toCategory = budgetRepository.getBudgetCategoryById(toCategoryId) ?: return@launch
                
                // Проверяем, достаточно ли средств
                if (fromCategory.walletBalance < amount) {
                    _state.update { 
                        it.copy(error = "Недостаточно средств в кошельке '${fromCategory.name}'")
                    }
                    return@launch
                }
                
                // Обновляем категории
                val updatedFromCategory = fromCategory.copy(
                    walletBalance = fromCategory.walletBalance - amount
                )
                val updatedToCategory = toCategory.copy(
                    walletBalance = toCategory.walletBalance + amount
                )
                
                // Сохраняем обновленные категории
                budgetRepository.updateBudgetCategory(updatedFromCategory)
                budgetRepository.updateBudgetCategory(updatedToCategory)
                
                // Перезагружаем категории
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error transferring between wallets")
                _state.update { 
                    it.copy(error = e.message ?: "Ошибка при переводе средств между кошельками")
                }
            }
        }
    }
    
    // Изменяет продолжительность расчетного периода
    private fun setPeriodDuration(days: Int) {
        if (days < 1) return
        
        _state.update { 
            it.copy(selectedPeriodDuration = days)
        }
    }
    
    // Сбрасывает расчетный период для указанной категории
    private fun resetPeriod(categoryId: String) {
        viewModelScope.launch {
            try {
                // Находим категорию по ID
                val category = budgetRepository.getBudgetCategoryById(categoryId) ?: return@launch
                
                // Обновляем категорию
                val updatedCategory = category.copy(
                    spent = 0.0,
                    periodStartDate = System.currentTimeMillis()
                )
                
                // Сохраняем обновленную категорию
                budgetRepository.updateBudgetCategory(updatedCategory)
                
                // Перезагружаем категории
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error resetting period")
                _state.update { 
                    it.copy(error = e.message ?: "Ошибка при сбросе расчетного периода")
                }
            }
        }
    }
    
    // Сбрасывает расчетный период для всех категорий
    private fun resetAllPeriods() {
        viewModelScope.launch {
            try {
                // Получаем все категории
                val categories = budgetRepository.getAllBudgetCategories()
                
                // Обновляем каждую категорию
                categories.forEach { category ->
                    val updatedCategory = category.copy(
                        spent = 0.0,
                        periodStartDate = System.currentTimeMillis()
                    )
                    budgetRepository.updateBudgetCategory(updatedCategory)
                }
                
                // Перезагружаем категории
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error resetting all periods")
                _state.update { 
                    it.copy(error = e.message ?: "Ошибка при сбросе всех расчетных периодов")
                }
            }
        }
    }

    // Проверяет, не истек ли расчетный период для категорий
    fun checkPeriodsExpiration() {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                
                // Получаем все категории
                val categories = budgetRepository.getAllBudgetCategories()
                
                val expiredCategories = categories.filter { category ->
                    calendar.timeInMillis = category.periodStartDate
                    calendar.add(Calendar.DAY_OF_MONTH, category.periodDuration)
                    currentTime > calendar.timeInMillis
                }
                
                if (expiredCategories.isNotEmpty()) {
                    // Обновляем каждую просроченную категорию
                    expiredCategories.forEach { category ->
                        val updatedCategory = category.copy(
                            spent = 0.0,
                            periodStartDate = System.currentTimeMillis()
                        )
                        budgetRepository.updateBudgetCategory(updatedCategory)
                    }
                    
                    // Перезагружаем категории
                    loadBudgetCategories()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking periods expiration")
                _state.update { 
                    it.copy(error = e.message ?: "Ошибка при проверке истечения расчетных периодов")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 