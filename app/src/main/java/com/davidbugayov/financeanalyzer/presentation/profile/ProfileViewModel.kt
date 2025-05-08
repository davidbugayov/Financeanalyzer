package com.davidbugayov.financeanalyzer.presentation.profile

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase.ExportAction
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.presentation.profile.model.Time
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel для экрана профиля.
 * Управляет состоянием профиля и обрабатывает события.
 */
class ProfileViewModel(
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase,
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    // Отдельный StateFlow для темы, который можно наблюдать из MainScreen
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode
    
    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    // Настройки уведомлений
    var notificationEnabled by mutableStateOf(false)
        private set

    var reminderTime by mutableStateOf(Time(20, 0))
        private set

    init {
        Timber.d("[ProfileViewModel] INIT: isTransactionReminderEnabled=${preferencesManager.isTransactionReminderEnabled()}")
        notificationEnabled = preferencesManager.isTransactionReminderEnabled()
        val (hour, minute) = preferencesManager.getReminderTime()
        reminderTime = Time(hour, minute)
        syncNotificationState()
        
        // Загружаем финансовую статистику
        loadFinancialAnalytics()
        
        // Инициализируем тему из настроек
        val savedTheme = preferencesManager.getThemeMode()
        _state.update { it.copy(themeMode = savedTheme) }
        _themeMode.value = savedTheme
        
        // Подписываемся на обновления метрик
        viewModelScope.launch {
            financialMetrics.balance.collect { balance ->
                _state.update { it.copy(balance = balance) }
            }
        }
        
        viewModelScope.launch {
            financialMetrics.totalIncome.collect { income ->
                _state.update { it.copy(totalIncome = income) }
            }
        }
        
        viewModelScope.launch {
            financialMetrics.totalExpense.collect { expense ->
                _state.update { it.copy(totalExpense = expense) }
            }
        }
    }

    /**
     * Обработка событий профиля.
     */
    fun onEvent(event: ProfileEvent, context: Context? = null) {
        when (event) {
            is ProfileEvent.ExportTransactionsToCSV -> {
                if (context != null) {
                    exportTransactionsToCSV(context, event.action)
                }
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
                context?.let { NotificationScheduler.updateTransactionReminder(it, event.isEnabled, event.reminderTime) }
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
                Timber.d(
                    "[ProfileViewModel] setTransactionReminderEnabled called with enabled=%b, actual value in prefs=%b",
                    event.enabled,
                    preferencesManager.isTransactionReminderEnabled()
                )
                _state.update { it.copy(isNotificationsEnabled = event.enabled) }
                if (event.enabled && context != null) {
                    Timber.d("[ProfileViewModel] Scheduling transaction reminder after enabling notifications")
                    NotificationScheduler.updateTransactionReminder(context, true)
                } else if (context != null) {
                    Timber.d("[ProfileViewModel] Cancelling transaction reminder after disabling notifications")
                    NotificationScheduler.updateTransactionReminder(context, false)
                }
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
    private fun exportTransactionsToCSV(context: Context, selectedAction: ExportAction? = null) {
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
                            exportSuccess = context.getString(R.string.export_success),
                            exportedFilePath = filePath
                        ) }
                        
                        // В зависимости от выбранного действия
                        when (selectedAction) {
                            ExportAction.SHARE -> {
                                // Открываем диалог "Поделиться"
                                val shareIntent = exportTransactionsToCSVUseCase.shareCSVFile(context, filePath)
                                context.startActivity(Intent.createChooser(shareIntent, "Поделиться файлом"))
                            }
                            ExportAction.OPEN -> {
                                // Открываем файл
                                try {
                                    val openIntent = exportTransactionsToCSVUseCase.openCSVFile(context, filePath)
                                    context.startActivity(openIntent)
                                } catch (e: Exception) {
                                    Timber.e(e, "Ошибка открытия файла: ${e.message}")
                                    _state.update { it.copy(
                                        exportError = "Не удалось открыть файл. Установите приложение для просмотра CSV-файлов."
                                    ) }
                                }
                            }
                            ExportAction.SAVE_ONLY -> {
                                // Просто сохраняем файл, ничего не делаем дополнительно
                                _state.update { it.copy(
                                    exportSuccess = context.getString(R.string.export_success) +
                                            "\nФайл сохранен: ${filePath.substringAfterLast('/')}" +
                                            "\nПуть: /Downloads"
                                ) }
                            }
                            null -> {
                                // Для обратной совместимости - если действие не выбрано,
                                // просто показываем сообщение об успешном экспорте
                                _state.update { it.copy(
                                    exportSuccess = context.getString(R.string.export_success) +
                                            "\nФайл сохранен: ${filePath.substringAfterLast('/')}" +
                                            "\nПуть: /Downloads"
                                ) }
                            }
                        }
                        
                        // Логируем успешный экспорт
                        AnalyticsUtils.logScreenView("export_success", "csv")
                    } else {
                        val error = exportResult.exceptionOrNull()
                        val errorMessage = when {
                            error is SecurityException -> "Отсутствуют разрешения для сохранения файла. Пожалуйста, предоставьте доступ к хранилищу в настройках."
                            else -> context.getString(R.string.export_error, error?.message ?: "неизвестная ошибка")
                        }
                        
                        _state.update { it.copy(
                            isExporting = false,
                            exportError = errorMessage
                        ) }
                        
                        // Логируем ошибку экспорта
                        AnalyticsUtils.logScreenView("export_failed", error?.message ?: "unknown")
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isExporting = false,
                    exportError = e.message ?: context.getString(R.string.export_error, "неизвестная ошибка")
                ) }
            }
        }
    }

    /**
     * Загружает финансовую аналитику из базы данных.
     * Рассчитывает общий доход, расходы, баланс и норму сбережений.
     */
    private fun loadFinancialAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Используем общий кэш метрик для базовых показателей
                val totalIncome = financialMetrics.totalIncome.value
                val totalExpense = financialMetrics.totalExpense.value
                val balance = financialMetrics.balance.value
                
                // Для остальных параметров по-прежнему нужно загружать все транзакции
                when (val result = loadTransactionsUseCase()) {
                    is Result.Success -> {
                        val transactions = result.data
                        
                        // Рассчитываем норму сбережений (если есть доход)
                        val savingsRate = if (!totalIncome.isZero()) {
                            // Используем метод percentageDifference из Money для расчета процентного изменения
                            (balance.percentageOf(totalIncome)).coerceIn(0.0, 100.0)
                        } else {
                            0.0
                        }
                        
                        // Расчет общего количества транзакций
                        val totalTransactions = transactions.size
                        
                        // Расчет уникальных категорий
                        val uniqueExpenseCategories = transactions
                            .filter { it.isExpense }
                            .map { it.category }
                            .distinct()
                            .size
                        
                        val uniqueIncomeCategories = transactions
                            .filter { !it.isExpense }
                            .map { it.category }
                            .distinct()
                            .size
                        
                        // Расчет среднего расхода
                        val expenseTransactions = transactions.filter { it.isExpense }
                        val avgExpense = if (expenseTransactions.isNotEmpty()) {
                            // Используем деление на количество транзакций напрямую, без конвертации в Double
                            totalExpense / expenseTransactions.size
                        } else {
                            Money.zero()
                        }
                        
                        // Форматирование среднего расхода с использованием Money
                        val formattedAvgExpense = avgExpense.format()
                        
                        // Расчет количества уникальных источников
                        val uniqueSources = transactions
                            .map { it.source }
                            .distinct()
                            .size
                        
                        // Форматирование диапазона дат
                        val dateRange = if (transactions.isNotEmpty()) {
                            val oldestDate = transactions.minByOrNull { it.date }?.date
                            val newestDate = transactions.maxByOrNull { it.date }?.date
                            
                            if (oldestDate != null && newestDate != null) {
                                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                                "${dateFormat.format(oldestDate)} - ${dateFormat.format(newestDate)}"
                            } else {
                                "Все время"
                            }
                        } else {
                            "Все время"
                        }

                        _state.update { it.copy(
                            isLoading = false,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            balance = balance,
                            savingsRate = savingsRate,
                            totalTransactions = totalTransactions,
                            totalExpenseCategories = uniqueExpenseCategories,
                            totalIncomeCategories = uniqueIncomeCategories,
                            averageExpense = formattedAvgExpense,
                            totalSourcesUsed = uniqueSources,
                            dateRange = dateRange
                        ) }
                    }
                    is Result.Error -> {
                        _state.update { it.copy(
                            isLoading = false,
                            error = result.exception.message
                        ) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, String.format(Locale.getDefault(), "Ошибка при загрузке финансовой аналитики: %s", e.message))
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }

    /**
     * Обновляет время напоминаний
     */
    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            reminderTime = Time(hour, minute)
            preferencesManager.setReminderTime(hour, minute)
        }
    }

    fun syncNotificationState() {
        viewModelScope.launch {
            val remindersEnabled = preferencesManager.isTransactionReminderEnabled()
            val reminderTime = preferencesManager.getReminderTime()
            Timber.d("[ProfileViewModel] syncNotificationState: remindersEnabled=%b, reminderTime=%s", remindersEnabled, reminderTime)
            _state.update {
                it.copy(
                    isNotificationsEnabled = remindersEnabled,
                    isTransactionReminderEnabled = remindersEnabled,
                    transactionReminderTime = reminderTime
                )
            }
        }
    }
} 