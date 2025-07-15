package com.davidbugayov.financeanalyzer.feature.profile

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.feature.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.feature.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.feature.security.manager.SecurityManager
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.ui.theme.AppTheme
import com.davidbugayov.financeanalyzer.utils.INotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import com.davidbugayov.financeanalyzer.utils.Time
import java.io.File
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

class ProfileViewModel(
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase,
    private val getProfileAnalyticsUseCase: GetProfileAnalyticsUseCase,
    private val preferencesManager: PreferencesManager,
    private val notificationScheduler: INotificationScheduler,
    private val navigationManager: NavigationManager,
    private val securityManager: SecurityManager,
    val userEventTracker: UserEventTracker,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _intentCommands = MutableSharedFlow<Intent>()
    val intentCommands: SharedFlow<Intent> = _intentCommands.asSharedFlow()

    init {
        Timber.d("[ProfileViewModel] INIT")
        syncNotificationState()
        syncSecurityState()

        loadFinancialAnalytics()

        _state.update { it.copy(themeMode = preferencesManager.getThemeMode()) }
        preferencesManager.themeModeFlow
            .onEach { newTheme ->
                _state.update { it.copy(themeMode = newTheme) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.ExportTransactionsToCSV -> {
                event.action?.let {
                    exportTransactionsToCSV(it)
                } ?: Timber.w("ExportTransactionsToCSV event received with null action")
            }
            is ProfileEvent.ResetExportState -> {
                _state.update {
                    it.copy(
                        exportSuccess = null,
                        exportError = null,
                        exportedFilePath = null,
                    )
                }
            }
            is ProfileEvent.SetExportError -> {
                _state.update {
                    it.copy(
                        exportError = event.message,
                    )
                }
            }
            is ProfileEvent.ChangeTheme -> {
                viewModelScope.launch {
                    // Сохраняем тему в PreferencesManager
                    preferencesManager.saveThemeMode(event.theme)
                    _state.update { it.copy(isEditingTheme = false) }

                    // Обновляем глобальную тему приложения
                    AppTheme.setTheme(event.theme)

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
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.Libraries.route))
            }
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
                Timber.d(
                    "[ProfileViewModel] Calling syncNotificationState after ChangeNotifications",
                )
                syncNotificationState()
            }
            is ProfileEvent.ChangeAppLock -> {
                viewModelScope.launch {
                    preferencesManager.setAppLockEnabled(event.enabled)
                    if (!event.enabled) {
                        // Если блокировка отключена, отключаем и биометрию
                        preferencesManager.setBiometricEnabled(false)
                    }

                    // Логируем изменение настройки блокировки приложения
                    AnalyticsUtils.logSecurityAppLockChanged(event.enabled)

                    syncSecurityState()
                }
            }
            is ProfileEvent.ChangeBiometric -> {
                viewModelScope.launch {
                    if (event.enabled && securityManager.isBiometricSupported() && securityManager.isBiometricEnrolled()) {
                        preferencesManager.setBiometricEnabled(event.enabled)
                        AnalyticsUtils.logSecurityBiometricChanged(true)
                    } else if (!event.enabled) {
                        preferencesManager.setBiometricEnabled(false)
                        AnalyticsUtils.logSecurityBiometricChanged(false)
                    }
                    syncSecurityState()
                }
            }
            is ProfileEvent.ShowPinSetupDialog -> {
                _state.update { it.copy(isEditingPinCode = true) }
            }
            is ProfileEvent.HidePinSetupDialog -> {
                _state.update { it.copy(isEditingPinCode = false) }
            }
            is ProfileEvent.SetPinCode -> {
                viewModelScope.launch {
                    val existingPin = preferencesManager.getPinCode()
                    val isFirstSetup = existingPin == null

                    preferencesManager.setPinCode(event.pinCode)
                    preferencesManager.setAppLockEnabled(true) // Автоматически включаем блокировку при установке PIN

                    // Логируем установку или изменение PIN кода
                    AnalyticsUtils.logSecurityPinSetup(isFirstSetup)

                    _state.update { it.copy(isEditingPinCode = false) }
                    syncSecurityState()
                }
            }
            is ProfileEvent.LoadFinancialAnalytics -> {
                loadFinancialAnalytics()
            }
            is ProfileEvent.NavigateToFinancialStatistics -> {
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(
                        Screen.FinancialStatistics.createRoute(null, null),
                    ),
                )
            }
            is ProfileEvent.NavigateToBudget -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.Budget.route))
            }
            is ProfileEvent.NavigateToExportImport -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.ExportImport.route))
            }
            is ProfileEvent.NavigateToAchievements -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.Achievements.route))
            }
            is ProfileEvent.NavigateBack -> {
                navigationManager.navigate(NavigationManager.Command.NavigateUp)
            }
            is ProfileEvent.Logout -> {
                // TODO: Implement logout functionality
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_IS_CHECK")
    private fun exportTransactionsToCSV(action: ExportTransactionsToCSVUseCase.ExportAction) {
        viewModelScope.launch {
            try {
                val result = exportTransactionsToCSVUseCase()
                when (result) {
                    is CoreResult.Success<File> -> {
                        val filePath = result.data
                        _state.update { currentState ->
                            currentState.copy(
                                exportSuccess = "Export successful: $filePath",
                                exportedFilePath = filePath.toString(),
                                exportError = null,
                            )
                        }

                        // Триггеры достижений за экспорт
                        com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                            "export_master",
                        )
                        com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                            "backup_enthusiast",
                        )

                        when (action) {
                            ExportTransactionsToCSVUseCase.ExportAction.SHARE -> {
                                val shareResult = exportTransactionsToCSVUseCase.shareCSVFile(filePath)
                                if (shareResult is CoreResult.Success) {
                                    Timber.d("[ProfileViewModel] File shared successfully")
                                }
                            }
                            ExportTransactionsToCSVUseCase.ExportAction.OPEN -> {
                                val openResult = exportTransactionsToCSVUseCase.openCSVFile(filePath)
                                if (openResult is CoreResult.Success) {
                                    Timber.d("[ProfileViewModel] File opened successfully")
                                }
                            }
                            ExportTransactionsToCSVUseCase.ExportAction.SAVE_ONLY -> {
                                // Ничего не делаем, файл уже сохранен
                            }
                            else -> {
                                // Обработка других возможных действий
                            }
                        }
                    }
                    is CoreResult.Error -> {
                        val exception = result.exception
                        _state.update { currentState ->
                            currentState.copy(
                                exportError = exception.message ?: "Unknown export error",
                                exportSuccess = null,
                                exportedFilePath = null,
                            )
                        }
                    }
                }
            } catch (exception: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        exportError = exception.message ?: "Unknown export error",
                        exportSuccess = null,
                        exportedFilePath = null,
                    )
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_IS_CHECK")
    private fun loadFinancialAnalytics() {
        viewModelScope.launch {
            Timber.d("[ProfileViewModel] Starting loadFinancialAnalytics")
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                Timber.d("[ProfileViewModel] Calling getProfileAnalyticsUseCase")
                val result = getProfileAnalyticsUseCase()
                Timber.d("[ProfileViewModel] Got result from getProfileAnalyticsUseCase: $result")

                if (result is CoreResult.Success) {
                    // Безопасное приведение типа
                    val analytics = result.data

                    Timber.d(
                        "[ProfileViewModel] Success! Analytics data: income=${analytics.totalIncome.amount}, expense=${analytics.totalExpense.amount}, balance=${analytics.balance.amount}, savingsRate=${analytics.savingsRate}",
                    )
                    Timber.d(
                        "[ProfileViewModel] More analytics: transactions=${analytics.totalTransactions}, expenseCategories=${analytics.totalExpenseCategories}, incomeCategories=${analytics.totalIncomeCategories}",
                    )

                    // Форматируем dateRange в строку
                    val dateRangeStr =
                        try {
                            val dateRange = analytics.dateRange
                            if (dateRange != null) {
                                val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                                val startStr = dateFormat.format(dateRange.first)
                                val endStr = dateFormat.format(dateRange.second)
                                "$startStr - $endStr"
                            } else {
                                "Все время"
                            }
                        } catch (exception: Exception) {
                            Timber.e(exception, "Ошибка при форматировании dateRange")
                            "Все время"
                        }

                    // Форматируем averageExpense в строку
                    val averageExpenseStr = analytics.averageExpense.formatForDisplay(useMinimalDecimals = true)

                    Timber.d(
                        "[ProfileViewModel] Formatted values: dateRange=$dateRangeStr, averageExpense=$averageExpenseStr",
                    )

                    val newState =
                        _state.value.copy(
                            isLoading = false,
                            totalIncome = analytics.totalIncome,
                            totalExpense = analytics.totalExpense,
                            balance = analytics.balance,
                            savingsRate = analytics.savingsRate,
                            totalTransactions = analytics.totalTransactions,
                            totalExpenseCategories = analytics.totalExpenseCategories,
                            totalIncomeCategories = analytics.totalIncomeCategories,
                            averageExpense = averageExpenseStr,
                            totalSourcesUsed = analytics.totalSourcesUsed,
                            dateRange = dateRangeStr,
                            error = null,
                        )

                    _state.update { newState }

                    Timber.d(
                        "[ProfileViewModel] State updated with analytics data. New state: income=${newState.totalIncome.amount}, expense=${newState.totalExpense.amount}, balance=${newState.balance.amount}",
                    )
                } else if (result is CoreResult.Error) {
                    val exception = result.exception
                    Timber.e(exception, "[ProfileViewModel] Ошибка при загрузке финансовой аналитики")
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = exception.message ?: "Неизвестная ошибка",
                        )
                    }
                } else {
                    Timber.w("[ProfileViewModel] Unexpected result type: $result")
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = "Неизвестный тип результата",
                        )
                    }
                }
            } catch (_: Exception) {
                Timber.e("[ProfileViewModel] Exception in loadFinancialAnalytics")
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Неизвестная ошибка",
                    )
                }
            }
        }
    }

    private fun syncNotificationState() {
        val isEnabled = preferencesManager.isTransactionReminderEnabled()
        val timePair = preferencesManager.getReminderTime()
        val time = Time(timePair.first, timePair.second)
        _state.update {
            it.copy(
                isTransactionReminderEnabled = isEnabled,
                transactionReminderTime = time,
            )
        }
        Timber.d("[ProfileViewModel] syncNotificationState: isEnabled=$isEnabled, time=$time")
    }

    private fun syncSecurityState() {
        val isAppLockEnabled = preferencesManager.isAppLockEnabled()
        val isBiometricEnabled = preferencesManager.isBiometricEnabled()
        val isBiometricAvailable = securityManager.isBiometricSupported() && securityManager.isBiometricEnrolled()

        _state.update {
            it.copy(
                isAppLockEnabled = isAppLockEnabled,
                isBiometricEnabled = isBiometricEnabled,
                isBiometricAvailable = isBiometricAvailable,
            )
        }
        Timber.d(
            "[ProfileViewModel] syncSecurityState: isAppLockEnabled=$isAppLockEnabled, isBiometricEnabled=$isBiometricEnabled, isBiometricAvailable=$isBiometricAvailable",
        )
    }
}
