package com.davidbugayov.financeanalyzer.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetFinancialGoalsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ManageFinancialGoalUseCase
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel для экрана профиля.
 * Управляет состоянием профиля и обрабатывает события.
 */
class ProfileViewModel(
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase,
    private val getFinancialGoalsUseCase: GetFinancialGoalsUseCase,
    private val manageFinancialGoalUseCase: ManageFinancialGoalUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        // Загружаем финансовые цели при инициализации
        loadFinancialGoals()
        
        // Загружаем настройки уведомлений
        loadNotificationSettings()
        
        // Загружаем информацию о пользователе
        loadUserInfo()
        
        // Загружаем финансовую статистику
        loadFinancialStatistics()
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
            is ProfileEvent.SelectGoal -> {
                // Обработка выбора финансовой цели
                // Здесь можно добавить логику для отображения деталей цели
            }
            is ProfileEvent.ShowAddGoalDialog -> {
                // Показать диалог добавления цели
            }
            is ProfileEvent.ChangeTheme -> {
                _state.update { it.copy(isDarkTheme = event.isDarkTheme) }
                // Здесь можно добавить сохранение настройки темы
            }
            is ProfileEvent.ShowEditProfileDialog -> {
                _state.update { it.copy(isEditingProfile = true) }
            }
            is ProfileEvent.HideEditProfileDialog -> {
                _state.update { it.copy(isEditingProfile = false) }
            }
            is ProfileEvent.UpdateUserInfo -> {
                updateUserInfo(event.name, event.email, event.phone)
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
            is ProfileEvent.ShowAddGoalDialog -> {
                _state.update { it.copy(isAddingGoal = true, isEditingGoal = false, selectedGoal = null) }
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
                // Создаем директорию для экспорта
                val directory = context.getExternalFilesDir(null) ?: context.filesDir
                
                val result = exportTransactionsToCSVUseCase(directory)
                result.collect { exportResult ->
                    if (exportResult.isSuccess) {
                        _state.update { it.copy(
                            isExporting = false,
                            exportSuccess = context.getString(R.string.export_success)
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
     * Загрузка информации о пользователе.
     */
    private fun loadUserInfo() {
        // В реальном приложении здесь будет загрузка из хранилища или API
        // Для примера используем заглушку
        _state.update { it.copy(
            userName = "Иван Иванов",
            userEmail = "ivan@example.com",
            userPhone = "+7 (999) 123-45-67"
        ) }
    }

    /**
     * Обновление информации о пользователе.
     */
    private fun updateUserInfo(name: String, email: String, phone: String) {
        _state.update { it.copy(
            userName = name,
            userEmail = email,
            userPhone = phone,
            isEditingProfile = false
        ) }
        
        // Сохраняем информацию о пользователе
        // В реальном приложении здесь будет сохранение в хранилище или API
    }

    /**
     * Загрузка финансовой статистики.
     */
    private fun loadFinancialStatistics() {
        // В реальном приложении здесь будет загрузка из хранилища или API
        // Для примера используем заглушку
        _state.update { it.copy(
            totalIncome = 150000.0,
            totalExpense = 100000.0,
            balance = 50000.0,
            savingsRate = 33.33
        ) }
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