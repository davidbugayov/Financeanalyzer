package com.davidbugayov.financeanalyzer.presentation.import_transaction

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportResult
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportState
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsIntent
import com.davidbugayov.financeanalyzer.presentation.import_transaction.utils.ImportErrorHandler
import java.io.BufferedInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * ViewModel для экрана импорта транзакций.
 * Управляет процессом импорта и состоянием UI.
 * Использует паттерн MVI (Model-View-Intent).
 */
class ImportTransactionsViewModel(
    private val importTransactionsUseCase: ImportTransactionsUseCase,
    application: Application,
) : AndroidViewModel(application), KoinComponent {
    // Инъекция TransactionDao через Koin
    private val transactionDao: TransactionDao by inject()

    // UI состояние для отображения на экране
    private val _state = MutableStateFlow(ImportState())
    val state: StateFlow<ImportState> = _state.asStateFlow()

    // Для обратной совместимости с предыдущим подходом
    private val _uiState = MutableLiveData<ImportUiState>(ImportUiState.Initial)
    val uiState: LiveData<ImportUiState> = _uiState

    // Наблюдатель за прямыми результатами импорта
    private val directResultObserver =
        Observer<ImportResult.Success?> { result ->
            result?.let {
                Timber.i(
                    "Получен прямой результат импорта: importedCount=${it.importedCount}, skippedCount=${it.skippedCount}",
                )
                setSuccessState(it.importedCount, it.skippedCount, it.bankName)
            }
        }

    init {
        // Инициализируем наблюдение за прямыми результатами импорта
        ImportResult.directResultLiveData.observeForever(directResultObserver)
    }

    override fun onCleared() {
        super.onCleared()
        // Удаляем наблюдателя при уничтожении ViewModel
        ImportResult.directResultLiveData.removeObserver(directResultObserver)
    }

    /**
     * Обрабатывает намерения пользователя в соответствии с паттерном MVI.
     *
     * @param intent Намерение пользователя
     */
    fun handleIntent(intent: ImportTransactionsIntent) {
        when (intent) {
            is ImportTransactionsIntent.StartImport -> startImport(intent.uri)
            is ImportTransactionsIntent.RefreshLogs -> refreshLogs()
            is ImportTransactionsIntent.ResetState -> resetState()
        }
    }

    /**
     * Устанавливает состояние успешного импорта с указанным количеством транзакций.
     */
    private fun setSuccessState(
        importedCount: Int,
        skippedCount: Int,
        bankNameFromResult: String? = null,
    ) {
        val context = getApplication<Application>().applicationContext

        // Проверяем для определения фактически использованного обработчика
        val actualBankName =
            bankNameFromResult ?: when {
                // Если файл - справка о движении, это скорее всего Тинькофф
                _state.value.fileName.contains("Справка_о_движении", ignoreCase = true) -> "Тинькофф"
                else -> _state.value.bankName
            }

        val bankInfo = actualBankName ?: _state.value.fileName
        val successMessage = context.getString(R.string.import_success_message, importedCount, skippedCount, bankInfo)

        Timber.i("Импорт завершен: импортировано=$importedCount, пропущено=$skippedCount, банк=$actualBankName")

        // Создаем новый объект состояния
        val newState =
            ImportState(
                isLoading = false,
                progress = 100,
                progressMessage = context.getString(R.string.import_progress_completed, bankInfo),
                successCount = importedCount,
                skippedCount = skippedCount,
                successMessage = successMessage,
                error = null, // Гарантируем, что ошибка сброшена
                fileName = _state.value.fileName, // Сохраняем имя файла
                bankName = actualBankName, // Используем скорректированное название банка
            )

        // Устанавливаем новое состояние
        _state.value = newState
        _uiState.value =
            ImportUiState.Success(
                message = successMessage,
                importedCount = importedCount,
                skippedCount = skippedCount,
            )

        // Состояние успешно обновлено
    }

    /**
     * Запускает импорт транзакций из указанного файла.
     * @param uri URI файла для импорта
     */
    @Suppress("USELESS_IS_CHECK")
    private fun startImport(uri: Uri) {
        if (_state.value.isLoading) {
            Timber.d("Импорт уже выполняется, запрос игнорируется")
            return
        }

        // Сначала полностью сбрасываем состояние перед новым импортом
        resetState()

        Timber.d("Начинаем импорт файла с URI: $uri")

        // Получаем MIME-тип и имя файла для диагностики
        var fileName = "Файл"
        try {
            val mimeType = getApplication<Application>().contentResolver.getType(uri)
            getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                        Timber.d("Импорт файла: $fileName, тип: $mimeType")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении информации о файле")
        }

        // Определяем тип банка по имени файла или расширению
        var bankName = determineBankName(fileName)

        // Проверяем содержимое PDF-файла для более точного определения банка
        if (fileName.endsWith(".pdf", ignoreCase = true)) {
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                    val buffer = ByteArray(8192) // Увеличиваем размер буфера для лучшего обнаружения
                    val bis = BufferedInputStream(inputStream)
                    val bytesRead = bis.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        val content = String(buffer, 0, bytesRead)

                        // Специальная обработка для файлов "Справка о движении средств"
                        if (fileName.contains("Справка_о_движении", ignoreCase = true) ||
                            content.contains("Справка о движении средств", ignoreCase = true)
                        ) {
                            // Проверка на Тинькофф
                            if (content.contains("ТБАНК", ignoreCase = true) ||
                                content.contains("TBANK", ignoreCase = true) ||
                                content.contains("Тинькофф", ignoreCase = true) ||
                                content.contains("Tinkoff", ignoreCase = true)
                            ) {
                                bankName = "Тинькофф"
                            }
                            // Проверка на Сбербанк
                            else if (content.contains("Сбербанк", ignoreCase = true) ||
                                content.contains("Sberbank", ignoreCase = true) ||
                                content.contains("ПАО СБЕРБАНК", ignoreCase = true)
                            ) {
                                bankName = "Сбербанк"
                            }
                        }
                        // Специальная обработка для выписок по счету дебетовой карты
                        else if (fileName.contains("Выписка по счёту", ignoreCase = true) ||
                            content.contains("Выписка по счёту", ignoreCase = true)
                        ) {
                            // Проверка на Сбербанк
                            if (content.contains("СберБанк", ignoreCase = true) ||
                                content.contains("Сбербанк", ignoreCase = true) ||
                                content.contains("Sberbank", ignoreCase = true) ||
                                content.contains("www.sberbank.ru", ignoreCase = true)
                            ) {
                                bankName = "Сбербанк"
                            }
                        } else {
                            // Общая проверка для других файлов
                            if (content.contains("ТБАНК", ignoreCase = true) ||
                                content.contains("TBANK", ignoreCase = true) ||
                                content.contains("Тинькофф", ignoreCase = true) ||
                                content.contains("Tinkoff", ignoreCase = true)
                            ) {
                                bankName = "Тинькофф"
                            }
                            // Проверка на Сбербанк
                            else if (content.contains("Сбербанк", ignoreCase = true) ||
                                content.contains("СберБанк", ignoreCase = true) ||
                                content.contains("Sberbank", ignoreCase = true) ||
                                content.contains("www.sberbank.ru", ignoreCase = true)
                            ) {
                                bankName = "Сбербанк"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при чтении содержимого файла для определения банка")
            }
        }

        // Дополнительная проверка для файлов справки о движении средств
        if (fileName.contains("Справка_о_движении", ignoreCase = true)) {
            if (bankName == null || bankName == "PDF-выписка") {
                // Если банк не определен, но файл похож на выписку Тинькофф
                bankName = "Тинькофф"
            }
        }

        // Дополнительная проверка для выписок по счету дебетовой карты
        if (fileName.contains("Выписка по счёту", ignoreCase = true) ||
            fileName.contains("Выписка по счету", ignoreCase = true)
        ) {
            if (bankName == null || bankName == "PDF-выписка") {
                // Если банк не определен, но файл похож на выписку Сбербанка
                bankName = "Сбербанк"
            }
        }

        Timber.i("Определен банк для импорта: $bankName")

        // Обновляем состояние, показывая процесс загрузки
        val context = getApplication<Application>().applicationContext
        val startMessage = context.getString(R.string.import_progress_starting, bankName ?: fileName)
        _state.value =
            _state.value.copy(
                isLoading = true,
                progress = 0,
                progressMessage = startMessage,
                error = null,
                successCount = 0,
                skippedCount = 0,
                fileName = fileName,
                bankName = bankName,
            )

        // Для обратной совместимости
        _uiState.postValue(ImportUiState.Loading(startMessage))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                importTransactionsUseCase.importTransactions(uri) { current, total, message ->
                    val progress = if (total > 0) (current * 100 / total) else 0
                    // Во время прогресса убираем ошибку, если она была
                    _state.value =
                        _state.value.copy(
                            progress = progress,
                            progressMessage = message,
                            error = null, // Важно: убираем ошибку во время прогресса
                        )
                    _uiState.postValue(ImportUiState.Loading(message, progress))
                }.collect { result ->
                    // Handle different result types based on their actual class
                    when {
                        result is CoreResult.Success<*> -> {
                            // Безопасное извлечение данных
                            val data = result.data
                            var importedCount = 0
                            var skippedCount = 0

                            // Проверяем тип данных для извлечения значений
                            when (data) {
                                is Pair<*, *> -> {
                                    val first = data.first
                                    val second = data.second
                                    if (first is Int) {
                                        importedCount = first
                                    } else if (first is Number) {
                                        importedCount = first.toInt()
                                    }
                                    if (second is Int) {
                                        skippedCount = second
                                    } else if (second is Number) {
                                        skippedCount = second.toInt()
                                    }
                                }
                                else -> {
                                    Timber.d("Неизвестный тип данных в CoreResult.Success: ${data?.javaClass?.name}")
                                }
                            }

                            Timber.i("Импорт завершен успешно! Импортировано: $importedCount, Пропущено: $skippedCount")

                            // Устанавливаем состояние успешного импорта через главный поток
                            viewModelScope.launch(Dispatchers.Main) {
                                setSuccessState(importedCount, skippedCount, bankName)
                            }

                            // Триггеры достижений за импорт из банков
                            triggerBankImportAchievements(bankName)

                            // Проверка наличия транзакций в базе
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    val count = transactionDao.getTransactionsCount()
                                    Timber.i("Проверка после импорта: всего транзакций в базе данных: $count")
                                } catch (e: Exception) {
                                    Timber.e(e, "Ошибка при проверке количества транзакций после импорта")
                                }
                            }
                        }
                        result is CoreResult.Error -> {
                            // Получаем исходное сообщение об ошибке
                            val originalMessage = result.exception?.message ?: "Неизвестная ошибка"

                            // Используем отдельный класс для обработки ошибок
                            val context = getApplication<Application>().applicationContext
                            val errorHandler = ImportErrorHandler(context)
                            val userFriendlyMessage = errorHandler.getUserFriendlyErrorMessage(originalMessage)

                            Timber.e(result.exception, "Ошибка импорта: $originalMessage")
                            _state.value =
                                _state.value.copy(
                                    isLoading = false,
                                    error = userFriendlyMessage,
                                    progress = 0,
                                    progressMessage = "",
                                    fileName = _state.value.fileName, // Сохраняем имя файла
                                )
                            _uiState.postValue(ImportUiState.Error(userFriendlyMessage))
                        }
                        result is ImportResult.Progress -> {
                            // Обрабатываем прогресс, но не устанавливаем ошибку
                            val progress = if (result.total > 0) (result.current * 100 / result.total) else 0
                            _state.value =
                                _state.value.copy(
                                    isLoading = true,
                                    progress = progress,
                                    progressMessage = result.message,
                                    error = null, // Важно: убираем ошибку во время прогресса
                                )
                            _uiState.postValue(ImportUiState.Loading(result.message, progress))
                        }
                        result is ImportResult.Error -> {
                            // Обработка ошибки из ImportResult.Error
                            val originalMessage = result.message ?: result.exception?.message ?: "Неизвестная ошибка"
                            val context = getApplication<Application>().applicationContext
                            val errorHandler = ImportErrorHandler(context)
                            val userFriendlyMessage = errorHandler.getUserFriendlyErrorMessage(originalMessage)

                            Timber.e(result.exception, "Ошибка импорта (ImportResult.Error): $originalMessage")
                            _state.value =
                                _state.value.copy(
                                    isLoading = false,
                                    error = userFriendlyMessage,
                                    progress = 0,
                                    progressMessage = "",
                                    fileName = _state.value.fileName, // Сохраняем имя файла
                                )
                            _uiState.postValue(ImportUiState.Error(userFriendlyMessage))
                        }
                        result is ImportResult.Success -> {
                            // Обработка успешного результата из ImportResult.Success
                            val importedCount = result.importedCount
                            val skippedCount = result.skippedCount
                            val bankName = result.bankName

                            Timber.i(
                                "Импорт завершен успешно через ImportResult.Success! Импортировано: $importedCount, Пропущено: $skippedCount, Банк: $bankName",
                            )

                            // Устанавливаем состояние успешного импорта через главный поток
                            viewModelScope.launch(Dispatchers.Main) {
                                setSuccessState(importedCount, skippedCount, bankName)
                            }

                            // Триггеры достижений за импорт из банков
                            triggerBankImportAchievements(bankName)

                            // Проверка наличия транзакций в базе
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    val count = transactionDao.getTransactionsCount()
                                    Timber.i("Проверка после импорта: всего транзакций в базе данных: $count")
                                } catch (e: Exception) {
                                    Timber.e(e, "Ошибка при проверке количества транзакций после импорта")
                                }
                            }
                        }
                        else -> {
                            Timber.w("Получен неизвестный тип результата: ${result?.javaClass?.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Необработанное исключение при импорте: ${e.message}")
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка",
                        fileName = _state.value.fileName, // Сохраняем имя файла
                    )
                _uiState.postValue(ImportUiState.Error(e.message ?: "Неизвестная ошибка"))
            }
        }
    }

    /**
     * Обновляет логи импорта.
     * Эта функция в настоящее время ничего не делает, но оставлена для
     * совместимости с интерфейсом ImportTransactionsIntent.
     */
    private fun refreshLogs() {
        // В текущей реализации не используется
    }

    /**
     * Сбрасывает состояние импорта.
     * Вызывается при выходе с экрана или при выборе нового файла.
     */
    private fun resetState() {
        Timber.d("Сброс состояния импорта")

        // Создаем новый объект состояния со значениями по умолчанию
        _state.value =
            ImportState(
                isLoading = false,
                progress = 0,
                progressMessage = "",
                successCount = 0,
                skippedCount = 0,
                successMessage = "",
                error = null,
                fileName = "", // Сбрасываем имя файла
                bankName = null, // Сбрасываем название банка
            )

        // Сбрасываем состояние для обратной совместимости
        _uiState.value = ImportUiState.Initial

        // Убедимся, что прямой результат тоже сброшен
        ImportResult.directResultLiveData.postValue(null)
    }

    /**
     * Определяет название банка по имени файла
     */
    private fun determineBankName(fileName: String): String? {
        val lowerFileName = fileName.lowercase()
        Timber.d("🏦 Определение банка для файла: '$fileName' (lowercased: '$lowerFileName')")

        return when {
            // Сбербанк - различные варианты
            lowerFileName.contains("sber") ||
                lowerFileName.contains("сбер") ||
                lowerFileName.contains("выписка по счёту дебетовой карты") ||
                lowerFileName.contains("выписка по счету дебетовой карты") ||
                lowerFileName.contains("справка_о_движении") -> {
                Timber.d("🏦 Определен банк: Сбербанк")
                "Сбербанк"
            }

            // Тинькофф - различные варианты
            lowerFileName.contains("tinkoff") ||
                lowerFileName.contains("тиньк") ||
                lowerFileName.contains("тинь") ||
                lowerFileName.contains("tbank") -> {
                Timber.d("🏦 Определен банк: Тинькофф")
                "Тинькофф"
            }

            // Альфа-банк - различные варианты
            lowerFileName.contains("alfa") ||
                lowerFileName.contains("альфа") ||
                lowerFileName.contains("alpha") -> {
                Timber.d("🏦 Определен банк: Альфа-Банк")
                "Альфа-Банк"
            }

            // ВТБ
            lowerFileName.contains("vtb") ||
                lowerFileName.contains("втб") -> {
                Timber.d("🏦 Определен банк: ВТБ")
                "ВТБ"
            }

            // Райффайзен
            lowerFileName.contains("raif") ||
                lowerFileName.contains("райф") -> {
                Timber.d("🏦 Определен банк: Райффайзен")
                "Райффайзен"
            }

            // Газпромбанк
            lowerFileName.contains("gazprom") ||
                lowerFileName.contains("газпром") -> {
                Timber.d("🏦 Определен банк: Газпромбанк")
                "Газпромбанк"
            }

            // Озон Банк
            lowerFileName.contains("ozon") ||
                lowerFileName.contains("озон") -> {
                Timber.d("🏦 Определен банк: Озон Банк")
                "Озон Банк"
            }

            // Определение по типу файла
            lowerFileName.endsWith(".pdf") -> {
                Timber.d("🏦 Определен тип: PDF-выписка")
                "PDF-выписка"
            }
            lowerFileName.endsWith(".csv") -> {
                Timber.d("🏦 Определен тип: CSV-выписка")
                "CSV-выписка"
            }
            lowerFileName.endsWith(".xlsx") || lowerFileName.endsWith(".xls") -> {
                Timber.d("🏦 Определен тип: Excel-выписка")
                "Excel-выписка"
            }
            else -> {
                Timber.w("🏦 Банк не определен для файла: '$fileName'")
                null
            }
        }
    }

    /**
     * Триггеры достижений за импорт из банков
     */
    private fun triggerBankImportAchievements(bankName: String?) {
        Timber.d("🏆 Вызов триггеров достижений для банка: '$bankName'")
        Timber.d("🏆 bankName?.lowercase() = '${bankName?.lowercase()}'")
        
        when (bankName?.lowercase()) {
            "тинькофф", "тинь", "tinkoff", "tbank", "тинькофф банк (pdf)", "тинькофф pdf" -> {
                Timber.d("🏆 Триггер Тинькофф")
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "tinkoff_importer",
                )
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "multi_bank_importer",
                )
            }
            "сбербанк", "сбер", "sberbank", "сберbank", "sber", "pao сбербанк", "пао сбербанк", "sberbank pdf", "сбербанк pdf", "сбербанк (pdf)", "сбербанк pdf" -> {
                Timber.d("🏆 Триггер Сбербанк")
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "sberbank_importer",
                )
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "multi_bank_importer",
                )
            }
            "альфа-банк", "альфа", "alfa", "alpha" -> {
                Timber.d("🏆 Триггер Альфа-Банк")
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "alfabank_importer",
                )
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "multi_bank_importer",
                )
            }
            "озон банк", "озон", "ozon", "ozon банк (pdf)", "ozon pdf" -> {
                Timber.d("🏆 Триггер OZON Банк")
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "ozon_importer",
                )
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "multi_bank_importer",
                )
            }
            "csv-выписка", "generic csv", "generic csv (configurable)" -> {
                Timber.d("🏆 Триггер CSV импорт")
                com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onMilestoneReached(
                    "csv_importer",
                )
            }
            else -> {
                Timber.w("🏆 Банк '$bankName' не распознан для достижений. Проверьте логику определения банка!")
                Timber.w("🏆 Доступные варианты для Сбербанка: 'сбербанк', 'сбер', 'sberbank'")
            }
        }
    }
}

/**
 * Состояния UI для экрана импорта (для обратной совместимости).
 */
sealed class ImportUiState {
    /**
     * Начальное состояние, до начала импорта.
     */
    object Initial : ImportUiState()

    /**
     * Состояние загрузки/импорта.
     * @param message Информационное сообщение
     * @param progress Прогресс импорта (0-100)
     */
    data class Loading(val message: String, val progress: Int = 0) : ImportUiState()

    /**
     * Состояние успешного завершения импорта.
     * @param message Сообщение об успешном завершении
     * @param importedCount Количество импортированных транзакций
     * @param skippedCount Количество пропущенных транзакций
     */
    data class Success(
        val message: String,
        val importedCount: Int,
        val skippedCount: Int,
    ) : ImportUiState()

    /**
     * Состояние ошибки.
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : ImportUiState()
}
