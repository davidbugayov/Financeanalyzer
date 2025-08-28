package com.davidbugayov.financeanalyzer.feature.profile

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import com.davidbugayov.financeanalyzer.feature.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.feature.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.feature.security.manager.SecurityManager
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.shared.SharedFacade
import com.davidbugayov.financeanalyzer.shared.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.AppTheme
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import com.davidbugayov.financeanalyzer.utils.INotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import com.davidbugayov.financeanalyzer.utils.Time
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
import org.koin.core.context.GlobalContext
import timber.log.Timber

class ProfileViewModel(
    private val sharedFacade: SharedFacade,
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

        val langLabel =
            GlobalContext.get().get<ResourceProvider>().getString(
                UiR.string.settings_language_current_value,
            )

        _state.update {
            it.copy(
                themeMode = preferencesManager.getThemeMode(),
                selectedCurrency = preferencesManager.getCurrency(),
                selectedLanguage = langLabel,
            )
        }
        preferencesManager.themeModeFlow
            .onEach { newTheme ->
                _state.update { it.copy(themeMode = newTheme) }
            }.launchIn(viewModelScope)

        // Подписываемся на изменения валюты
        viewModelScope.launch {
            CurrencyProvider.getCurrencyFlow().collect { newCurrency ->
                _state.update { it.copy(selectedCurrency = newCurrency) }
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.ExportTransactionsToCSV -> {
                exportTransactionsToCSV()
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
                    com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsProviderBridge
                        .getProvider()?.logEvent(
                            "theme_changed",
                            mapOf("theme" to event.theme.name),
                        )
                }
            }
            is ProfileEvent.ShowThemeDialog -> {
                _state.update { it.copy(isEditingTheme = true) }
            }
            is ProfileEvent.HideThemeDialog -> {
                _state.update { it.copy(isEditingTheme = false) }
            }
            is ProfileEvent.ShowLanguageDialog -> {
                _state.update { it.copy(showLanguageDialog = true) }
            }
            is ProfileEvent.HideLanguageDialog -> {
                _state.update { it.copy(showLanguageDialog = false) }
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
                preferencesManager.setAppLanguage(event.language)
                val label =
                    GlobalContext.get().get<ResourceProvider>().getString(
                        UiR.string.settings_language_current_value,
                    )
                _state.update { it.copy(selectedLanguage = label, showLanguageDialog = false) }
            }
            is ProfileEvent.ChangeCurrency -> {
                viewModelScope.launch {
                    // Сохраняем валюту в PreferencesManager
                    preferencesManager.saveCurrency(event.currency)
                    _state.update { it.copy(selectedCurrency = event.currency, showCurrencyDialog = false) }

                    // Логируем изменение валюты
                    com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsProviderBridge
                        .getProvider()?.logEvent(
                            "currency_changed",
                            mapOf("currency" to event.currency.name),
                        )
                }
            }
            is ProfileEvent.ShowCurrencyDialog -> {
                _state.update { it.copy(showCurrencyDialog = true) }
            }
            is ProfileEvent.HideCurrencyDialog -> {
                _state.update { it.copy(showCurrencyDialog = false) }
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
                    if (event.enabled &&
                        securityManager.isBiometricSupported() &&
                        securityManager.isBiometricEnrolled()
                    ) {
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

    private fun exportTransactionsToCSV() {
        viewModelScope.launch {
            try {
                val filePath = sharedFacade.exportTransactionsCsv(emptyList())
                if (filePath.isNotBlank()) {
                    _state.update { currentState ->
                        currentState.copy(
                            exportSuccess =
                                GlobalContext
                                    .get()
                                    .get<ResourceProvider>()
                                    .getString(UiR.string.export_success_path, filePath),
                            exportedFilePath = filePath,
                            exportError = null,
                        )
                    }

                    // Триггеры достижений за экспорт
                    AchievementTrigger.onTransactionExported()
                    AchievementTrigger.onMilestoneReached("export_master")
                    AchievementTrigger.onMilestoneReached("backup_enthusiast")
                }
            } catch (exception: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        exportError =
                            exception.message ?: GlobalContext
                                .get()
                                .get<ResourceProvider>()
                                .getString(UiR.string.export_unknown_error),
                        exportSuccess = null,
                        exportedFilePath = null,
                    )
                }
                throw exception
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
