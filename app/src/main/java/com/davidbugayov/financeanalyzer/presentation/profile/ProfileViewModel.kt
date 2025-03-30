package com.davidbugayov.financeanalyzer.presentation.profile

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.presentation.profile.model.ProfileState
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import timber.log.Timber

/**
 * ViewModel для экрана профиля.
 * Управляет состоянием профиля и обрабатывает события.
 */
class ProfileViewModel(
    private val exportTransactionsToCSVUseCase: ExportTransactionsToCSVUseCase,
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    // Отдельный StateFlow для темы, который можно наблюдать из MainScreen
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode
    
    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    // Путь к последнему экспортированному файлу
    private var lastExportedFilePath: String? = null

    init {
        // Загружаем настройки уведомлений
        loadNotificationSettings()
        
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
                    exportTransactionsToCSV(context)
                }
            }
            is ProfileEvent.ShareExportedFile -> {
                if (context != null && !lastExportedFilePath.isNullOrEmpty()) {
                    shareExportedFile(context)
                } else {
                    _state.update { it.copy(
                        exportError = "Файл не найден. Сначала выполните экспорт."
                    ) }
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
     * Экспорт транзакций в CSV файл и автоматическое открытие диалога "поделиться"
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
                        lastExportedFilePath = filePath // Сохраняем путь к файлу
                        
                        _state.update { it.copy(
                            isExporting = false,
                            lastExportFileAvailable = true,
                            exportSuccess = context.getString(R.string.export_success) +
                                    "\nФайл сохранен: деньги_под_контролем_транзакции_${filePath.substringAfterLast('/').substringAfter('_')}" +
                                    "\nПуть: /Downloads"
                        ) }
                        
                        // Логируем успешный экспорт
                        AnalyticsUtils.logScreenView("export_success", "csv")
                        
                        // Автоматически запускаем функцию "поделиться"
                        shareExportedFile(context)
                    } else {
                        val error = exportResult.exceptionOrNull()
                        val errorMessage = when {
                            error is SecurityException -> "Отсутствуют разрешения для сохранения файла. Пожалуйста, предоставьте доступ к хранилищу в настройках."
                            else -> context.getString(R.string.export_error)
                        }
                        
                        _state.update { it.copy(
                            isExporting = false,
                            lastExportFileAvailable = false,
                            exportError = errorMessage
                        ) }
                        
                        // Логируем ошибку экспорта
                        AnalyticsUtils.logScreenView("export_failed", error?.message ?: "unknown")
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isExporting = false,
                    lastExportFileAvailable = false,
                    exportError = e.message ?: context.getString(R.string.export_error)
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
        loadFinancialAnalytics()
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
                        val savingsRate = if (totalIncome > 0) {
                            ((totalIncome - totalExpense) / totalIncome * 100).coerceIn(0.0, 100.0)
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
                        val avgExpense = if (transactions.filter { it.isExpense }.isNotEmpty()) {
                            totalExpense / transactions.filter { it.isExpense }.size
                        } else {
                            0.0
                        }
                        
                        // Форматирование среднего расхода
                        val formattedAvgExpense = String.format("%,.0f ₽", avgExpense)
                        
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
                Timber.e(e, "Ошибка при загрузке финансовой аналитики: ${e.message}")
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }

    /**
     * Поделиться экспортированным файлом через стандартный диалог Android.
     */
    private fun shareExportedFile(context: Context) {
        lastExportedFilePath?.let { filePath ->
            try {
                val shareIntent = exportTransactionsToCSVUseCase.shareCSVFile(context, filePath)
                // Запускаем диалог выбора приложения для отправки
                context.startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
                
                // Логируем событие
                AnalyticsUtils.logScreenView("share_exported_file", "csv")
            } catch (e: Exception) {
                Timber.e(e, "Error sharing CSV file")
                _state.update { it.copy(
                    exportError = "Ошибка при попытке поделиться файлом: ${e.message}"
                ) }
            }
        }
    }
} 