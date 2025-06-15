package com.davidbugayov.financeanalyzer.presentation.profile

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetProfileAnalyticsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.export.ExportTransactionsToCSVUseCase.ExportAction
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.presentation.profile.model.Time
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.presentation.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.ui.theme.AppTheme
import com.davidbugayov.financeanalyzer.utils.INotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import timber.log.Timber
import android.net.Uri
import java.io.File
import com.davidbugayov.financeanalyzer.domain.model.AppProfileAnalytics

class ProfileViewModel(
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase,
    private val getProfileAnalyticsUseCase: GetProfileAnalyticsUseCase,
    private val preferencesManager: PreferencesManager,
    private val notificationScheduler: INotificationScheduler,
    private val navigationManager: NavigationManager,
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
                _state.update { it.copy(isAppLockEnabled = event.enabled) }
            }
            is ProfileEvent.ChangeBiometric -> {
                _state.update { it.copy(isBiometricEnabled = event.enabled) }
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
                // TODO: Logout
            }
        }
    }

    private fun logLibrariesNavigation() {
        AnalyticsUtils.logScreenView(
            screenName = "libraries",
            screenClass = "LibrariesScreen",
        )
    }

    private fun exportTransactionsToCSV(action: ExportAction) {
        viewModelScope.launch {
            try {
                val result = exportTransactionsToCSVUseCase()
                when (result) {
                    is Result.Success<File> -> {
                        val filePath = result.data
                        _state.update { currentState ->
                            currentState.copy(
                                exportSuccess = "Export successful: $filePath",
                                exportedFilePath = filePath.toString(),
                                exportError = null,
                            )
                        }

                        when (action) {
                            ExportAction.SHARE -> {
                                val shareResult = exportTransactionsToCSVUseCase.shareCSVFile(filePath)
                                if (shareResult.isSuccess) {
                                    // Обработка успешного шаринга
                                }
                            }
                            ExportAction.OPEN -> {
                                val openResult = exportTransactionsToCSVUseCase.openCSVFile(filePath)
                                if (openResult.isSuccess) {
                                    // Обработка успешного открытия
                                }
                            }
                            ExportAction.SAVE_ONLY -> {
                                // Ничего не делаем, файл уже сохранен
                            }
                            else -> { /* Обработка других возможных действий */ }
                        }
                    }
                    is Result.Error -> {
                        val exception = result.exception
                        _state.update { currentState ->
                            currentState.copy(
                                exportError = exception.message ?: "Unknown export error",
                                exportSuccess = null,
                                exportedFilePath = null,
                            )
                        }
                    }
                    else -> { /* Обработка других возможных результатов */ }
                }
            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        exportError = e.message ?: "Unknown export error",
                        exportSuccess = null,
                        exportedFilePath = null,
                    )
                }
            }
        }
    }

    private fun loadFinancialAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val result = getProfileAnalyticsUseCase()
                when (result) {
                    is Result.Success<AppProfileAnalytics> -> {
                        val analytics = result.data

                        // Форматируем dateRange в строку
                        val dateRangeStr = try {
                            if (analytics.dateRange != null) {
                                val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                                val first = analytics.dateRange!!.first
                                val second = analytics.dateRange!!.second
                                val startStr = dateFormat.format(first)
                                val endStr = dateFormat.format(second)
                                "$startStr - $endStr"
                            } else {
                                "Все время"
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Ошибка при форматировании dateRange")
                            "Все время"
                        }

                        // Форматируем averageExpense в строку
                        val averageExpenseStr = "${analytics.averageExpense.amount} ₽"

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
                                averageExpense = averageExpenseStr,
                                totalSourcesUsed = analytics.totalSourcesUsed,
                                dateRange = dateRangeStr,
                                error = null,
                            )
                        }
                    }
                    is Result.Error -> {
                        val exception = result.exception
                        Timber.e(exception, "Ошибка при загрузке финансовой аналитики")
                        _state.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = exception.message ?: "Неизвестная ошибка",
                            )
                        }
                    }
                    else -> { /* Обработка других возможных результатов */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке финансовой аналитики")
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка",
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
}
