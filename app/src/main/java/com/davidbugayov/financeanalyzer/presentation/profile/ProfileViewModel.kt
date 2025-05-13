package com.davidbugayov.financeanalyzer.presentation.profile

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase.ExportAction
import com.davidbugayov.financeanalyzer.domain.usecase.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.presentation.profile.model.Time
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.INotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для экрана профиля.
 * Управляет состоянием профиля и обрабатывает события.
 */
class ProfileViewModel(
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase,
    private val getProfileAnalyticsUseCase: GetProfileAnalyticsUseCase,
    private val preferencesManager: PreferencesManager,
    private val notificationScheduler: INotificationScheduler,
    private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _intentCommands = MutableSharedFlow<Intent>()
    val intentCommands: SharedFlow<Intent> = _intentCommands.asSharedFlow()

    init {
        Timber.d("[ProfileViewModel] INIT")
        syncNotificationState()
        
        loadFinancialAnalytics()

        _state.update { it.copy(themeMode = preferencesManager.getThemeMode()) }
        preferencesManager.themeModeFlow
            .onEach { newTheme ->
                _state.update { it.copy(themeMode = newTheme) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Обработка событий профиля.
     */
    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.ExportTransactionsToCSV -> {
                event.action?.let {
                    exportTransactionsToCSV(it)
                } ?: Timber.w("ExportTransactionsToCSV event received with null action")
            }
            is ProfileEvent.ResetExportState -> {
                _state.update { it.copy(
                    exportSuccess = null,
                    exportError = null,
                    exportedFilePath = null
                ) }
            }
            is ProfileEvent.SetExportError -> {
                _state.update { it.copy(
                    exportError = event.message
                ) }
            }
            is ProfileEvent.ChangeTheme -> {
                viewModelScope.launch {
                    preferencesManager.saveThemeMode(event.theme)
                    _state.update { it.copy(isEditingTheme = false) }
                    
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
                // 1. Сохраняем настройки в PreferencesManager
                preferencesManager.setTransactionReminderEnabled(event.isEnabled)
                event.reminderTime?.let {
                    preferencesManager.setReminderTime(it.first, it.second)
                }

                // 2. Обновляем NotificationScheduler
                val timeForScheduler: Time? = event.reminderTime?.let { Time(it.first, it.second) }
                notificationScheduler.updateTransactionReminder(event.isEnabled, timeForScheduler)

                // 3. Синхронизируем ProfileState из PreferencesManager
                syncNotificationState()

                // 4. Закрываем диалог настроек уведомлений
                _state.update { it.copy(isEditingNotifications = false) }
            }
            is ProfileEvent.NavigateToLibraries -> {
                // Это событие будет обрабатываться в ProfileScreen
                // через переданный колбэк onNavigateToLibraries
                logLibrariesNavigation()
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
                Timber.d("[ProfileViewModel] ChangeNotifications: enabled=${event.enabled}")
                preferencesManager.setTransactionReminderEnabled(event.enabled)
                notificationScheduler.updateTransactionReminder(event.enabled, null)
                Timber.d("[ProfileViewModel] Calling syncNotificationState after ChangeNotifications")
                syncNotificationState()
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
    private fun exportTransactionsToCSV(action: ExportAction) {
        viewModelScope.launch {
            exportTransactionsToCSVUseCase().collect { result ->
                if (result.isSuccess) {
                    val filePath = result.getOrNull() ?: return@collect
                    _state.update { currentState ->
                        currentState.copy(
                            exportSuccess = "Export successful: $filePath",
                            exportedFilePath = filePath,
                            exportError = null
                        )
                    }
                    val intentToLaunch: Intent? = when (action) {
                        ExportAction.SHARE -> exportTransactionsToCSVUseCase.shareCSVFile(filePath)
                        ExportAction.OPEN -> exportTransactionsToCSVUseCase.openCSVFile(filePath)
                        ExportAction.SAVE_ONLY -> null
                    }
                    intentToLaunch?.let {
                        _intentCommands.emit(it)
                    }
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            exportError = result.exceptionOrNull()?.message ?: "Unknown export error",
                            exportSuccess = null,
                            exportedFilePath = null
                        )
                    }
                }
            }
        }
    }

    /**
     * Загружает финансовую аналитику из базы данных.
     * Рассчитывает общий доход, расходы, баланс и норму сбережений.
     */
    private fun loadFinancialAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = getProfileAnalyticsUseCase.execute()) {
                is Result.Success -> {
                    val analytics = result.data
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            totalIncome = analytics.totalIncome,
                            totalExpense = analytics.totalExpense,
                            balance = analytics.balance,
                            savingsRate = analytics.savingsRate,
                            totalTransactions = analytics.totalTransactions,
                            totalExpenseCategories = analytics.totalExpenseCategories,
                            totalIncomeCategories = analytics.totalIncomeCategories,
                            averageExpense = analytics.averageExpense,
                            totalSourcesUsed = analytics.totalSourcesUsed,
                            dateRange = analytics.dateRange,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Ошибка при загрузке финансовой аналитики")
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Error loading analytics"
                        )
                    }
                }
            }
        }
    }

    fun syncNotificationState() {
        viewModelScope.launch {
            val remindersEnabled = preferencesManager.isTransactionReminderEnabled()
            val reminderTimePair = preferencesManager.getReminderTime()
            val permission = PermissionUtils.hasNotificationPermission(appContext)
            Timber.d(
                "[ProfileViewModel] syncNotificationState: remindersEnabled=%b, reminderTime=%s, hasPermission=%b",
                remindersEnabled,
                reminderTimePair,
                permission
            )
            _state.update {
                it.copy(
                    isTransactionReminderEnabled = remindersEnabled,
                    transactionReminderTime = Time(reminderTimePair.first, reminderTimePair.second),
                    hasNotificationPermission = permission
                )
            }
        }
    }
} 