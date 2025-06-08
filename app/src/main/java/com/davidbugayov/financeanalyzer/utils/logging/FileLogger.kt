package com.davidbugayov.financeanalyzer.utils.logging

import android.content.Context
import android.util.Log
import com.davidbugayov.financeanalyzer.BuildConfig
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Класс для логирования сообщений в файл и хранения их в памяти для отображения в UI.
 */
object FileLogger {
    private val logBuffer = ConcurrentLinkedQueue<LogEntry>()
    private val maxBufferSize = 5000 // Максимальный размер буфера в памяти
    private var logFile: File? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var isInitialized = false

    // Флаг для предотвращения рекурсии
    private val isLogging = AtomicBoolean(false)

    /**
     * Структура для хранения записи лога
     */
    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val tag: String,
        val message: String
    ) {
        override fun toString(): String {
            val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
            val timeString = formatter.format(Date(timestamp))
            return "[$timeString] $level/$tag: $message"
        }
    }

    /**
     * Инициализирует File Logger
     *
     * @param context Контекст приложения
     */
    fun init(context: Context) {
        if (isInitialized) return

        try {
            // Создаем директорию и файл для логов
            val logsDir = File(context.getExternalFilesDir(null), "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }

            // Имя файла с текущей датой/временем
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val fileName = "log_${dateFormat.format(Date())}.txt"
            logFile = File(logsDir, fileName)

            isInitialized = true

            // Информация о запуске логгера через Timber
            val initMessage = "FileLogger инициализирован. Лог файл: ${logFile?.absolutePath}"
            Timber.i(initMessage)

            // Добавляем запись в буфер
            val entry = LogEntry(
                timestamp = System.currentTimeMillis(),
                level = "I",
                tag = "FileLogger",
                message = initMessage
            )
            logBuffer.add(entry)
            writeToFile(entry.toString())

            // Добавляем обработчик логов в Timber после инициализации
            if (BuildConfig.DEBUG) {
                Timber.plant(FileLoggingTree())
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка инициализации")
        }
    }

    /**
     * Записывает сообщение в лог файл и буфер.
     * Этот метод не должен вызываться напрямую из FileLoggingTree, чтобы избежать рекурсии!
     */
    fun log(priority: Int, tag: String, message: String) {
        // Защита от рекурсии
        if (isLogging.getAndSet(true)) {
            Timber.w("Предотвращена рекурсия при логировании $tag: $message")
            isLogging.set(false)
            return
        }

        try {
            logDirectly(priority, tag, message)
        } finally {
            isLogging.set(false)
        }
    }

    /**
     * Записывает сообщение напрямую в файл и буфер, минуя Timber.
     * Безопасен от рекурсии.
     */
    private fun logDirectly(priority: Int, tag: String, message: String) {
        if (!isInitialized) {
            Timber.log(priority, message)
            return
        }

        // Уровень логирования
        val level = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "?"
        }

        // Создаем запись лога
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message
        )

        // Добавляем запись в буфер
        logBuffer.add(entry)

        // Если буфер слишком большой, удаляем старые записи
        while (logBuffer.size > maxBufferSize) {
            logBuffer.poll()
        }

        // Записываем в файл в фоновом потоке
        executor.execute {
            writeToFile(entry.toString())
        }
    }

    /**
     * Записывает строку в файл лога
     */
    private fun writeToFile(message: String) {
        logFile?.let { file ->
            try {
                FileOutputStream(file, true).use { output ->
                    output.write((message + "\n").toByteArray())
                    output.flush()
                }
            } catch (e: IOException) {
                Timber.e(e, "Ошибка записи в файл")
            }
        }
    }

    /**
     * Возвращает все записи логов из буфера
     */
    fun getLogs(): List<LogEntry> {
        return logBuffer.toList()
    }

    /**
     * Возвращает последние N записей логов из буфера
     */
    fun getLastLogs(count: Int): List<LogEntry> {
        val logs = logBuffer.toList()
        return if (logs.size <= count) logs else logs.takeLast(count)
    }

    /**
     * Возвращает путь к файлу лога
     */
    fun getLogFilePath(): String? {
        return logFile?.absolutePath
    }

    /**
     * Tree для Timber, который пишет логи в файл без рекурсии
     */
    private class FileLoggingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Защита от бесконечной рекурсии при логировании
            try {
                // Игнорируем низкоуровневые логи от самого FileLogger
                if (tag != null && tag.contains("FileLogger")) {
                    return
                }

                // Логирование в файл
                logDirectly(priority, tag ?: "NoTag", message)

                // Если есть исключение, логируем его стектрейс
                if (t != null) {
                    val stackTrace = Log.getStackTraceString(t)
                    logDirectly(priority, tag ?: "NoTag", "Stacktrace: $stackTrace")
                }
            } catch (e: Exception) {
                // В случае ошибки, логируем через стандартный механизм
                Timber.e(e, "Ошибка при логировании")
            }
        }
    }
} 
