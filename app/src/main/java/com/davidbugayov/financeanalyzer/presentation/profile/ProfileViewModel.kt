package com.davidbugayov.financeanalyzer.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetFinancialGoalsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ManageFinancialGoalUseCase
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана профиля.
 * Управляет состоянием профиля и обрабатывает события.
 */
class ProfileViewModel(
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase,
    private val getFinancialGoalsUseCase: GetFinancialGoalsUseCase,
    private val manageFinancialGoalUseCase: ManageFinancialGoalUseCase,
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    // Отдельный StateFlow для темы, который можно наблюдать из MainScreen
    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    init {
        // Загружаем финансовые цели при инициализации
        loadFinancialGoals()
        
        // Загружаем настройки уведомлений
        loadNotificationSettings()
        
        // Загружаем финансовую статистику
        loadFinancialStatistics()
        
        // Инициализируем тему из настроек
        val savedTheme = preferencesManager.getThemeMode()
        _state.update { it.copy(themeMode = savedTheme) }
        _themeMode.value = savedTheme
    }

    /**
     * Обработка событий профиля.
     */
    fun onEvent(event: ProfileEvent, context: Context? = null) {
        when (event) {
            is ProfileEvent.ExportTransactionsToCSV -> {
                if (context != null) {
                    exportTransactionsToCSV(context)
                }
            }
            is ProfileEvent.ResetExportState -> {
                _state.update { it.copy(
                    exportSuccess = null,
                    exportError = null
                ) }
            }
            is ProfileEvent.SetExportError -> {
                _state.update { it.copy(
                    exportError = event.message
                ) }
            }
            is ProfileEvent.ShowAddGoalDialog -> {
                _state.update { it.copy(isAddingGoal = true, isEditingGoal = false, selectedGoal = null) }
            }
            is ProfileEvent.ChangeTheme -> {
                viewModelScope.launch {
                    preferencesManager.saveThemeMode(event.theme)
                    _state.update { it.copy(themeMode = event.theme, isEditingTheme = false) }
                    
                    // Обновляем отдельный StateFlow темы
                    _themeMode.value = event.theme
                    
                    // Принудительное оповещение всех наблюдателей о смене темы
                    // Это поможет быстрее обновить системный UI
                    viewModelScope.launch {
                        _themeMode.emit(event.theme)
                    }
                    
                    // Логируем изменение темы
                    AnalyticsUtils.logScreenView("theme_changed", event.theme.name)
                }
            }
            is ProfileEvent.ShowThemeDialog -> {
                _state.update { it.copy(isEditingTheme = true) }
            }
            is ProfileEvent.HideThemeDialog -> {
                _state.update { it.copy(isEditingTheme = false) }
            }
            is ProfileEvent.ShowNotificationSettingsDialog -> {
                _state.update { it.copy(isEditingNotifications = true) }
            }
            is ProfileEvent.HideNotificationSettingsDialog -> {
                _state.update { it.copy(isEditingNotifications = false) }
            }
            is ProfileEvent.UpdateTransactionReminder -> {
                updateTransactionReminder(event.isEnabled, event.reminderTime, context)
            }
            is ProfileEvent.NavigateToLibraries -> {
                // Это событие будет обрабатываться в ProfileScreen
                // через переданный колбэк onNavigateToLibraries
                logLibrariesNavigation()
            }
            
            // События финансовых целей
            is ProfileEvent.LoadFinancialGoals -> {
                loadFinancialGoals()
            }
            is ProfileEvent.SelectGoal -> {
                selectGoal(event.goalId)
            }
            is ProfileEvent.ShowEditGoalDialog -> {
                viewModelScope.launch {
                    manageFinancialGoalUseCase.getGoalById(event.goalId).collect { goal ->
                        goal?.let {
                            _state.update { state ->
                                state.copy(
                                    isEditingGoal = true,
                                    isAddingGoal = false,
                                    selectedGoal = it
                                )
                            }
                        }
                    }
                }
            }
            is ProfileEvent.HideGoalDialog -> {
                _state.update { it.copy(isAddingGoal = false, isEditingGoal = false, selectedGoal = null, goalError = null) }
            }
            is ProfileEvent.AddGoal -> {
                addGoal(event.goal)
            }
            is ProfileEvent.UpdateGoal -> {
                updateGoal(event.goal)
            }
            is ProfileEvent.DeleteGoal -> {
                deleteGoal(event.goalId)
            }
            is ProfileEvent.AddAmountToGoal -> {
                addAmountToGoal(event.goalId, event.amount)
            }
            
            // События настроек
            is ProfileEvent.ChangeLanguage -> {
                _state.update { it.copy(selectedLanguage = event.language) }
                // Здесь можно добавить сохранение настройки в DataStore
            }
            is ProfileEvent.ChangeCurrency -> {
                _state.update { it.copy(selectedCurrency = event.currency) }
                // Здесь можно добавить сохранение настройки в DataStore
            }
            is ProfileEvent.ChangeNotifications -> {
                _state.update { it.copy(isNotificationsEnabled = event.enabled) }
                // Здесь можно добавить сохранение настройки в DataStore
            }
            
            // События безопасности
            is ProfileEvent.ChangeAppLock -> {
                _state.update { it.copy(isAppLockEnabled = event.enabled) }
                // Здесь можно добавить сохранение настройки в DataStore
            }
            is ProfileEvent.ChangeBiometric -> {
                _state.update { it.copy(isBiometricEnabled = event.enabled) }
                // Здесь можно добавить сохранение настройки в DataStore
            }
            
            // События аналитики
            is ProfileEvent.LoadFinancialAnalytics -> {
                loadFinancialAnalytics()
            }
        }
    }

    /**
     * Логирование перехода к экрану библиотек.
     */
    private fun logLibrariesNavigation() {
        // Здесь можно добавить логирование аналитики
        AnalyticsUtils.logScreenView(
            screenName = "libraries",
            screenClass = "LibrariesScreen"
        )
    }

    /**
     * Экспорт транзакций в CSV файл.
     */
    private fun exportTransactionsToCSV(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            
            try {
                // Запускаем экспорт, передавая контекст приложения
                val result = exportTransactionsToCSVUseCase(context)
                result.collect { exportResult ->
                    if (exportResult.isSuccess) {
                        val filePath = exportResult.getOrNull() ?: ""
                        _state.update { it.copy(
                            isExporting = false,
                            exportSuccess = context.getString(R.string.export_success) +
                                    "\nФайл сохранен: $filePath"
                        ) }
                    } else {
                        _state.update { it.copy(
                            isExporting = false,
                            exportError = context.getString(R.string.export_error)
                        ) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isExporting = false,
                    exportError = e.message ?: context.getString(R.string.export_error)
                ) }
            }
        }
    }

    /**
     * Загрузка финансовых целей.
     */
    private fun loadFinancialGoals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                getFinancialGoalsUseCase().collect { goals ->
                    val activeGoals = goals.filter { !it.isCompleted }
                    val completedGoals = goals.filter { it.isCompleted }
                    
                    _state.update { it.copy(
                        isLoading = false,
                        financialGoals = goals,
                        activeGoals = activeGoals,
                        completedGoals = completedGoals
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    goalError = e.message
                ) }
            }
        }
    }

    /**
     * Загрузка настроек уведомлений.
     */
    private fun loadNotificationSettings() {
        // В реальном приложении здесь будет загрузка из хранилища настроек
        // Для примера используем заглушку
        _state.update { it.copy(
            isTransactionReminderEnabled = true,
            transactionReminderTime = Pair(20, 0) // 20:00
        ) }
    }

    /**
     * Обновление настроек уведомлений о транзакциях.
     */
    private fun updateTransactionReminder(isEnabled: Boolean, reminderTime: Pair<Int, Int>?, context: Context?) {
        _state.update { it.copy(
            isTransactionReminderEnabled = isEnabled,
            transactionReminderTime = reminderTime,
            isEditingNotifications = false
        ) }
        
        // Сохраняем настройки в хранилище
        // В реальном приложении здесь будет сохранение в хранилище настроек
        
        // Обновляем расписание уведомлений
        if (context != null) {
            if (isEnabled && reminderTime != null) {
                val (hour, minute) = reminderTime
                notificationScheduler.scheduleTransactionReminder(context, hour, minute)
            } else {
                notificationScheduler.cancelTransactionReminder(context)
            }
        }
    }

    /**
     * Загрузка финансовой статистики на основе реальных данных из базы данных.
     * Публичный метод, чтобы можно было обновить статистику после добавления новой транзакции.
     */
    fun updateFinancialStatistics() {
        loadFinancialStatistics()
    }

    /**
     * Загрузка финансовой статистики на основе реальных данных из базы данных.
     */
    private fun loadFinancialStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            when (val result = loadTransactionsUseCase()) {
                is com.davidbugayov.financeanalyzer.domain.model.Result.Success -> {
                    val transactions = result.data
                    
                    // Рассчитываем общий доход
                    val totalIncome = transactions
                        .filter { !it.isExpense }
                        .map { it.amount }
                        .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
                    
                    // Рассчитываем общие расходы
                    val totalExpense = transactions
                        .filter { it.isExpense }
                        .map { it.amount }
                        .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
                    
                    // Рассчитываем баланс
                    val balance = totalIncome - totalExpense
                    
                    // Рассчитываем норму сбережений (если доход не равен 0)
                    val savingsRate = if (totalIncome > 0) {
                        (balance / totalIncome) * 100
                    } else {
                        0.0
                    }
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            balance = balance,
                            savingsRate = savingsRate
                        )
                    }
                }
                is com.davidbugayov.financeanalyzer.domain.model.Result.Error -> {
                    // В случае ошибки используем нулевые значения
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            totalIncome = 0.0,
                            totalExpense = 0.0,
                            balance = 0.0,
                            savingsRate = 0.0
                        )
                    }
                }
            }
        }
    }

    /**
     * Выбирает финансовую цель по идентификатору.
     * @param goalId Идентификатор цели
     */
    private fun selectGoal(goalId: String) {
        viewModelScope.launch {
            manageFinancialGoalUseCase.getGoalById(goalId).collect { goal ->
                goal?.let {
                    _state.update { state -> state.copy(selectedGoal = it) }
                }
            }
        }
    }
    
    /**
     * Добавляет новую финансовую цель.
     * @param goal Финансовая цель для добавления
     */
    private fun addGoal(goal: FinancialGoal) {
        viewModelScope.launch {
            try {
                manageFinancialGoalUseCase.addGoal(goal)
                _state.update { it.copy(isAddingGoal = false, goalError = null) }
                loadFinancialGoals()
            } catch (e: Exception) {
                _state.update { it.copy(goalError = "Ошибка при добавлении цели: ${e.message}") }
            }
        }
    }
    
    /**
     * Обновляет существующую финансовую цель.
     * @param goal Финансовая цель для обновления
     */
    private fun updateGoal(goal: FinancialGoal) {
        viewModelScope.launch {
            try {
                manageFinancialGoalUseCase.updateGoal(goal)
                _state.update { it.copy(isEditingGoal = false, goalError = null) }
                loadFinancialGoals()
            } catch (e: Exception) {
                _state.update { it.copy(goalError = "Ошибка при обновлении цели: ${e.message}") }
            }
        }
    }
    
    /**
     * Удаляет финансовую цель.
     * @param goalId Идентификатор цели для удаления
     */
    private fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                manageFinancialGoalUseCase.deleteGoal(goalId)
                _state.update { it.copy(selectedGoal = null) }
                loadFinancialGoals()
            } catch (e: Exception) {
                _state.update { it.copy(goalError = "Ошибка при удалении цели: ${e.message}") }
            }
        }
    }
    
    /**
     * Добавляет сумму к текущей сумме финансовой цели.
     * @param goalId Идентификатор цели
     * @param amount Сумма для добавления
     */
    private fun addAmountToGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            try {
                manageFinancialGoalUseCase.addAmountToGoal(goalId, amount)
                loadFinancialGoals()
                // Обновляем выбранную цель, если она открыта
                if (_state.value.selectedGoal?.id == goalId) {
                    selectGoal(goalId)
                }
            } catch (e: Exception) {
                _state.update { it.copy(goalError = "Ошибка при добавлении суммы: ${e.message}") }
            }
        }
    }
    
    /**
     * Загружает финансовую аналитику.
     */
    private fun loadFinancialAnalytics() {
        // В реальном приложении здесь будет вызов соответствующего UseCase
        // Для демонстрации используем тестовые данные
        val totalIncome = 210000.0
        val totalExpense = 160500.0
        val balance = totalIncome - totalExpense
        val savingsRate = (balance / totalIncome) * 100
        
        _state.update { 
            it.copy(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                balance = balance,
                savingsRate = savingsRate
            )
        }
    }
} 