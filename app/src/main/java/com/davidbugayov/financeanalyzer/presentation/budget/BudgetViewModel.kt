package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
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
import java.math.BigDecimal

class BudgetViewModel(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        // Очищаем все существующие кошельки
        clearExistingWallets()
        
        // Загружаем категории бюджета
        loadBudgetCategories()
        calculateTotals()
        
        // Создаем тестовый кошелек через небольшую задержку,
        // чтобы дать время на загрузку существующих кошельков
        viewModelScope.launch {
            createSampleWallets()
        }
    }

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.LoadCategories -> loadBudgetCategories()
            is BudgetEvent.AddCategory -> addCategory(event.name, event.limit)
            is BudgetEvent.UpdateCategory -> updateCategory(event.category)
            is BudgetEvent.DeleteCategory -> deleteCategory(event.category)
            is BudgetEvent.ClearError -> clearError()
            is BudgetEvent.SetError -> setError(event.message)
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
     * Создает образцы кошельков, если их еще нет
     */
    private fun createSampleWallets() {
        viewModelScope.launch {
            try {
                // Проверяем, есть ли уже кошельки
                if (_state.value.categories.isEmpty()) {
                    Timber.d("Создаем тестовый кошелек")
                    
                    // Создаем только один тестовый кошелек - "Развлечения"
                    val entertainmentWalletId = UUID.randomUUID().toString()
                    val entertainmentWallet = Wallet(
                        name = "Развлечения", 
                        limit = Money(3000.0), 
                        spent = Money(500.0), 
                        id = entertainmentWalletId,
                        balance = Money(2500.0),
                        // Связанные категории
                        linkedCategories = listOf("Кино", "Игры", "Рестораны", "Театр", "Концерты")
                    )
                    
                    // Добавляем кошелек в репозиторий
                    walletRepository.addWallet(entertainmentWallet)
                    
                    // Добавляем несколько транзакций для этого кошелька
                    try {
                        // Транзакция "Кино"
                        transactionRepository.addTransaction(
                            Transaction(
                                amount = Money(-250.0),
                                category = "Кино",
                                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
                                isExpense = true,
                                note = "Поход в кинотеатр",
                                source = "Сбер",
                                sourceColor = 0xFF21A038.toInt(),
                                categoryId = entertainmentWalletId
                            )
                        )
                        
                        // Транзакция "Рестораны"
                        transactionRepository.addTransaction(
                            Transaction(
                                amount = Money(-150.0),
                                category = "Рестораны",
                                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                                isExpense = true,
                                note = "Кофе и десерт",
                                source = "Наличные",
                                sourceColor = 0xFF777777.toInt(),
                                categoryId = entertainmentWalletId
                            )
                        )
                        
                        // Транзакция "Игры"
                        transactionRepository.addTransaction(
                            Transaction(
                                amount = Money(-100.0),
                                category = "Игры",
                                date = Calendar.getInstance().time,
                                isExpense = true,
                                note = "Покупка игры в Steam",
                                source = "Т-Банк",
                                sourceColor = 0xFFCF102D.toInt(),
                                categoryId = entertainmentWalletId
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при создании тестовых транзакций")
                    }
                    
                    // Перезагружаем кошельки
                    loadBudgetCategories()
                    
                    Timber.d("Тестовый кошелек создан")
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при создании тестового кошелька")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при создании тестового кошелька"
                    )
                }
            }
        }
    }

    private fun loadBudgetCategories() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Загружаем кошельки из репозитория
                val wallets = walletRepository.getAllWallets()
                
                _state.update { 
                    it.copy(
                        categories = wallets,
                        isLoading = false
                    )
                }
                
                calculateTotals()
                
                // Получаем самые свежие транзакции перед обновлением сумм расходов
                val transactions = transactionRepository.getAllTransactions()
                Timber.d("Получено ${transactions.size} транзакций для расчета трат кошельков")
                
                // Обновляем суммы трат для всех кошельков
                updateSpentAmounts(wallets)
            } catch (e: Exception) {
                Timber.e(e, "Error loading wallets")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Обновляет суммы трат для всех кошельков на основе транзакций
     */
    private fun updateSpentAmounts(wallets: List<Wallet>) {
        viewModelScope.launch {
            try {
                // Загружаем все транзакции
                val allTransactions = transactionRepository.getAllTransactions()
                
                // Фильтруем только расходные транзакции
                val expenseTransactions = allTransactions.filter { it.isExpense }
                
                // Обрабатываем каждый кошелек
                wallets.forEach { wallet ->
                    // Находим транзакции, относящиеся к этому кошельку
                    val walletTransactions = expenseTransactions.filter { 
                        it.category == wallet.name || it.categoryId == wallet.id 
                    }
                    
                    // Рассчитываем сумму трат
                    val totalSpent = walletTransactions.fold(Money(0.0)) { acc, transaction ->
                        acc.plus(transaction.amount.abs())
                    }
                    
                    // Если сумма трат изменилась, обновляем кошелек
                    if (totalSpent != wallet.spent) {
                        // Явно указываем linkedCategories со значением по умолчанию,
                        // чтобы избежать NullPointerException для кошельков без этого поля
                        val updatedWallet = wallet.copy(
                            spent = totalSpent,
                            linkedCategories = wallet.linkedCategories ?: emptyList()
                        )
                        walletRepository.updateWallet(updatedWallet)
                    }
                }
                
                // Перезагружаем кошельки с обновленными данными
                val updatedWallets = walletRepository.getAllWallets()
                _state.update { it.copy(categories = updatedWallets) }
                
                // Пересчитываем итоги
                calculateTotals()
            } catch (e: Exception) {
                Timber.e(e, "Error updating spent amounts: ${e.message}")
            }
        }
    }

    private fun calculateTotals() {
        val wallets = _state.value.categories
        
        // Используем методы Money для суммирования вместо преобразования в Double
        val totalLimit = wallets.fold(Money(0.0)) { acc, wallet -> acc.plus(wallet.limit) }
        val totalSpent = wallets.fold(Money(0.0)) { acc, wallet -> acc.plus(wallet.spent) }
        val totalWalletBalance = wallets.fold(Money(0.0)) { acc, wallet -> acc.plus(wallet.balance) }
        
        _state.update { 
            it.copy(
                totalLimit = totalLimit,
                totalSpent = totalSpent,
                totalWalletBalance = totalWalletBalance
            )
        }
    }

    private fun addCategory(name: String, limit: Money) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val newWallet = Wallet(
                    name = name, 
                    limit = limit, 
                    spent = Money(0.0), 
                    id = UUID.randomUUID().toString(),
                    balance = Money(0.0)
                )
                
                // Добавляем кошелек в репозиторий
                walletRepository.addWallet(newWallet)
                
                // Перезагружаем кошельки
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error adding wallet")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun updateCategory(wallet: Wallet) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Обновляем кошелек в репозитории
                walletRepository.updateWallet(wallet)
                
                // Перезагружаем кошельки
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error updating wallet")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteCategory(wallet: Wallet) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Удаляем кошелек из репозитория
                walletRepository.deleteWallet(wallet)
                
                // Перезагружаем кошельки
                loadBudgetCategories()
            } catch (e: Exception) {
                Timber.e(e, "Error deleting wallet")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun setError(message: String) {
        _state.update { it.copy(error = message) }
    }

    /**
     * Распределяет доход по всем кошелькам пропорционально их лимитам
     */
    private fun distributeIncome(amount: Money) {
        viewModelScope.launch {
            try {
                val wallets = _state.value.categories
                
                if (wallets.isEmpty()) {
                    Timber.d("Нет кошельков для распределения дохода")
                    return@launch
                }
                
                // Рассчитываем общий лимит
                val totalLimit = wallets.fold(Money(0.0)) { acc, wallet -> 
                    acc.plus(wallet.limit) 
                }
                
                if (totalLimit.amount <= BigDecimal.ZERO) {
                    Timber.d("Нулевой или отрицательный общий лимит, невозможно распределить доход")
                    return@launch
                }
                
                // Распределяем доход по кошелькам пропорционально их лимитам
                wallets.forEach { wallet ->
                    // Рассчитываем долю кошелька от общего лимита
                    val proportion = wallet.limit.amount / totalLimit.amount
                    
                    // Рассчитываем сумму для добавления в этот кошелек
                    val amountToAdd = Money(amount.amount * proportion)
                    
                    // Обновляем баланс кошелька
                    val updatedWallet = wallet.copy(
                        balance = wallet.balance.plus(amountToAdd),
                        linkedCategories = wallet.linkedCategories ?: emptyList()
                    )
                    
                    // Сохраняем обновленный кошелек
                    walletRepository.updateWallet(updatedWallet)
                }
                
                // Перезагружаем кошельки
                loadBudgetCategories()
                
                Timber.d("Доход успешно распределен")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при распределении дохода")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при распределении дохода"
                    )
                }
            }
        }
    }

    /**
     * Добавляет средства в выбранный кошелек
     */
    private fun addFundsToWallet(walletId: String, amount: Money) {
        viewModelScope.launch {
            try {
                // Получаем кошелек по ID
                val wallet = walletRepository.getWalletById(walletId) ?: return@launch
                
                // Обновляем баланс кошелька
                val updatedWallet = wallet.copy(
                    balance = wallet.balance.plus(amount),
                    linkedCategories = wallet.linkedCategories ?: emptyList()
                )
                
                // Сохраняем обновленный кошелек
                walletRepository.updateWallet(updatedWallet)
                
                // Перезагружаем кошельки
                loadBudgetCategories()
                
                Timber.d("Средства успешно добавлены в кошелек")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при добавлении средств в кошелек")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при добавлении средств в кошелек"
                    )
                }
            }
        }
    }

    /**
     * Тратит средства из выбранного кошелька
     */
    private fun spendFromWallet(walletId: String, amount: Money) {
        viewModelScope.launch {
            try {
                // Получаем кошелек по ID
                val wallet = walletRepository.getWalletById(walletId) ?: return@launch
                
                // Проверяем, достаточно ли средств
                if (wallet.balance.amount < amount.amount) {
                    _state.update { 
                        it.copy(
                            error = "Недостаточно средств в кошельке"
                        )
                    }
                    return@launch
                }
                
                // Обновляем баланс и сумму трат кошелька
                val updatedWallet = wallet.copy(
                    balance = wallet.balance.minus(amount),
                    spent = wallet.spent.plus(amount),
                    linkedCategories = wallet.linkedCategories ?: emptyList()
                )
                
                // Сохраняем обновленный кошелек
                walletRepository.updateWallet(updatedWallet)
                
                // Перезагружаем кошельки
                loadBudgetCategories()
                
                Timber.d("Средства успешно потрачены из кошелька")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при трате средств из кошелька")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при трате средств из кошелька"
                    )
                }
            }
        }
    }

    /**
     * Переводит средства между кошельками
     */
    private fun transferBetweenWallets(fromWalletId: String, toWalletId: String, amount: Money) {
        viewModelScope.launch {
            try {
                // Получаем кошельки по ID
                val fromWallet = walletRepository.getWalletById(fromWalletId) ?: return@launch
                val toWallet = walletRepository.getWalletById(toWalletId) ?: return@launch
                
                // Проверяем, достаточно ли средств в исходном кошельке
                if (fromWallet.balance.amount < amount.amount) {
                    _state.update { 
                        it.copy(
                            error = "Недостаточно средств в исходном кошельке"
                        )
                    }
                    return@launch
                }
                
                // Обновляем балансы кошельков
                val updatedFromWallet = fromWallet.copy(
                    balance = fromWallet.balance.minus(amount),
                    linkedCategories = fromWallet.linkedCategories ?: emptyList()
                )
                
                val updatedToWallet = toWallet.copy(
                    balance = toWallet.balance.plus(amount),
                    linkedCategories = toWallet.linkedCategories ?: emptyList()
                )
                
                // Сохраняем обновленные кошельки
                walletRepository.updateWallet(updatedFromWallet)
                walletRepository.updateWallet(updatedToWallet)
                
                // Перезагружаем кошельки
                loadBudgetCategories()
                
                Timber.d("Средства успешно переведены между кошельками")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при переводе средств между кошельками")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при переводе средств между кошельками"
                    )
                }
            }
        }
    }

    /**
     * Устанавливает продолжительность периода для всех кошельков
     */
    private fun setPeriodDuration(days: Int) {
        viewModelScope.launch {
            try {
                // Получаем все кошельки
                val wallets = walletRepository.getAllWallets()
                
                // Устанавливаем новую продолжительность периода для каждого кошелька
                wallets.forEach { wallet ->
                    val updatedWallet = wallet.copy(
                        periodDuration = days,
                        linkedCategories = wallet.linkedCategories ?: emptyList()
                    )
                    walletRepository.updateWallet(updatedWallet)
                }
                
                // Обновляем состояние
                _state.update { it.copy(selectedPeriodDuration = days) }
                
                // Перезагружаем кошельки
                loadBudgetCategories()
                
                Timber.d("Продолжительность периода успешно обновлена для всех кошельков")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при установке продолжительности периода")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при установке продолжительности периода"
                    )
                }
            }
        }
    }

    /**
     * Сбрасывает период для конкретного кошелька
     */
    private fun resetPeriod(walletId: String) {
        viewModelScope.launch {
            try {
                // Получаем кошелек по ID
                val wallet = walletRepository.getWalletById(walletId) ?: return@launch
                
                // Сбрасываем период и потраченную сумму
                val updatedWallet = wallet.copy(
                    periodStartDate = System.currentTimeMillis(),
                    spent = Money(0.0),
                    linkedCategories = wallet.linkedCategories ?: emptyList()
                )
                
                // Сохраняем обновленный кошелек
                walletRepository.updateWallet(updatedWallet)
                
                // Перезагружаем кошельки
                loadBudgetCategories()
                
                Timber.d("Период успешно сброшен для кошелька")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при сбросе периода для кошелька")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при сбросе периода для кошелька"
                    )
                }
            }
        }
    }

    /**
     * Сбрасывает периоды для всех кошельков
     */
    private fun resetAllPeriods() {
        viewModelScope.launch {
            try {
                // Получаем все кошельки
                val wallets = walletRepository.getAllWallets()
                
                // Сбрасываем периоды и потраченные суммы для всех кошельков
                wallets.forEach { wallet ->
                    val updatedWallet = wallet.copy(
                        periodStartDate = System.currentTimeMillis(),
                        spent = Money(0.0),
                        linkedCategories = wallet.linkedCategories ?: emptyList()
                    )
                    walletRepository.updateWallet(updatedWallet)
                }
                
                // Перезагружаем кошельки
                loadBudgetCategories()
                
                Timber.d("Периоды успешно сброшены для всех кошельков")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при сбросе периодов для всех кошельков")
                _state.update { 
                    it.copy(
                        error = e.message ?: "Ошибка при сбросе периодов для всех кошельков"
                    )
                }
            }
        }
    }

    /**
     * Очищает все существующие кошельки (кроме тех, которые имеют транзакции)
     */
    private fun clearExistingWallets() {
        viewModelScope.launch {
            try {
                val wallets = walletRepository.getAllWallets()
                
                // Удаляем все кошельки с определенными именами 
                // (сохраняем кошельки созданные пользователем)
                val walletsToRemove = wallets.filter { 
                    it.name == "Продукты" || 
                    it.name == "Транспорт" || 
                    // Можно добавить и другие имена тестовых кошельков, если они есть
                    it.name == "Развлечения" && it.linkedCategories.isEmpty() // Удаляем пустой кошелек "Развлечения" без связанных категорий
                }
                
                // Удаляем выбранные кошельки
                walletsToRemove.forEach { wallet ->
                    walletRepository.deleteWallet(wallet)
                }
                
                Timber.d("Очищены существующие тестовые кошельки: ${walletsToRemove.size}")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при очистке существующих кошельков")
            }
        }
    }
} 