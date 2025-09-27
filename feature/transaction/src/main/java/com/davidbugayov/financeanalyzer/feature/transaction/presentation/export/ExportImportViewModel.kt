package com.davidbugayov.financeanalyzer.feature.transaction.presentation.export

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.shared.SharedFacade
import com.davidbugayov.financeanalyzer.shared.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.shared.model.ExportAction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для экрана экспорта/импорта транзакций.
 * Управляет состоянием UI и делегирует фактические операции экспорта/импорта соответствующим use case.
 */
class ExportImportViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val sharedFacade = SharedFacade()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _exportError = MutableStateFlow<String?>(null)
    val exportError: StateFlow<String?> = _exportError.asStateFlow()

    /**
     * Экспортирует транзакции в CSV и выполняет выбранное действие
     */
    fun exportTransactions(action: ExportAction) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportError.value = null
            _exportResult.value = null

            try {
                Timber.d("[ExportImportViewModel] Начинаем экспорт с действием: $action")

                // Получаем все транзакции
                val transactions = sharedFacade.loadTransactions()

                if (transactions.isEmpty()) {
                    _exportError.value = "Нет транзакций для экспорта"
                    return@launch
                }

                // Экспортируем в CSV
                val csvContent = sharedFacade.exportTransactionsCsv(transactions)
                Timber.d("[ExportImportViewModel] CSV сгенерирован, размер: ${csvContent.length}")

                // Сохраняем в файл
                val file = saveCsvToFile(csvContent)
                Timber.d("[ExportImportViewModel] Файл сохранен: ${file.absolutePath}")

                // Триггеры достижений за экспорт
                AchievementTrigger.onTransactionExported()
                AchievementTrigger.onMilestoneReached("export_master")
                AchievementTrigger.onMilestoneReached("backup_enthusiast")

                // Выполняем выбранное действие
                when (action) {
                    ExportAction.SHARE -> {
                        val shareResult = shareFile(file)
                        handleActionResult(shareResult, "поделиться файлом")
                    }
                    ExportAction.OPEN -> {
                        val openResult = openFile(file)
                        handleActionResult(openResult, "открыть файл")
                    }
                    ExportAction.SAVE -> {
                        _exportResult.value = "Файл сохранен: ${file.absolutePath}"
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Неизвестная ошибка"
                Timber.e(e, "[ExportImportViewModel] Исключение при экспорте: $errorMsg")
                _exportError.value = errorMsg
            } finally {
                _isExporting.value = false
            }
        }
    }

    /**
     * Обрабатывает результат действия после экспорта
     */
    private fun handleActionResult(
        result: Result<Unit>,
        actionName: String,
    ) {
        when (result) {
            is Result.Success -> {
                _exportResult.value = "Экспорт завершен, удалось $actionName"
            }
            is Result.Error -> {
                val errorMsg = result.exception.message ?: "Неизвестная ошибка"
                _exportError.value = "Файл создан, но не удалось $actionName: $errorMsg"
            }
        }
    }

    /**
     * Сбрасывает состояние результатов экспорта
     */
    fun clearExportMessages() {
        _exportResult.value = null
        _exportError.value = null
    }

    /**
     * Сохраняет CSV содержимое в файл
     */
    private fun saveCsvToFile(csvContent: String): File {
        val context = getApplication<Application>()
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val fileName = "transactions_export_$timestamp.csv"

        // Сохраняем в кэш директорию приложения
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { outputStream ->
            outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    /**
     * Открывает файл через системный диалог
     */
    private fun openFile(file: File): Result<Unit> =
        try {
            val context = getApplication<Application>()
            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )

            // Создаем Intent для просмотра CSV файла
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "text/csv")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            // Проверяем есть ли приложения которые могут открыть CSV
            val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)

            if (resolveInfos.isNotEmpty()) {
                // Показываем диалог выбора приложения
                val chooserIntent = Intent.createChooser(intent, "Открыть CSV файл в приложении:")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

                Timber.d("Файл открыт в системном диалоге выбора приложений")
                Result.Success(Unit)
            } else {
                // Если нет приложений для CSV, попробуем как текстовый файл
                val textIntent =
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "text/plain")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                val textChooserIntent = Intent.createChooser(textIntent, "Открыть файл как текст:")
                textChooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(textChooserIntent)

                Timber.d("Файл открыт как текстовый файл")
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при открытии файла")
            Result.Error(
                com.davidbugayov.financeanalyzer.core.model.AppException.FileSystem.ReadError(
                    "Не удалось открыть файл: ${e.message}",
                    e,
                ),
            )
        }

    /**
     * Делится файлом через системный диалог
     */
    private fun shareFile(file: File): Result<Unit> =
        try {
            val context = getApplication<Application>()
            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )

            // Создаем Intent для отправки файла
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Экспорт транзакций из Финансового Анализатора")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "📊 Файл с экспортированными транзакциями из приложения Деньги под Контролем.\n\nВ файле содержится подробная информация о ваших доходах и расходах в формате CSV.",
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            // Проверяем есть ли приложения для отправки
            val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)

            if (resolveInfos.isNotEmpty()) {
                val chooserIntent = Intent.createChooser(intent, "📤 Поделиться файлом через:")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

                Timber.d("Показан диалог выбора приложений для отправки. Доступно приложений: ${resolveInfos.size}")
                Result.Success(Unit)
            } else {
                Timber.w("Нет приложений для отправки файлов")
                Result.Error(
                    com.davidbugayov.financeanalyzer.core.model.AppException.FileSystem.ReadError(
                        "Нет приложений для отправки файлов",
                        null,
                    ),
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при отправке файла")
            Result.Error(
                com.davidbugayov.financeanalyzer.core.model.AppException.FileSystem.ReadError(
                    "Не удалось поделиться файлом: ${e.message}",
                    e,
                ),
            )
        }
}
