package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorInt
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorInt
import com.davidbugayov.financeanalyzer.ui.theme.TransferColorInt
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.absoluteValue

/**
 * Реализация UseCase для импорта транзакций из CSV файла.
 * Поддерживает стандартный формат CSV с заголовками.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class ImportFromCSVUseCase(
    private val repository: TransactionRepositoryImpl,
    private val context: Context
) : ImportTransactionsUseCase {

    // Флаг для отслеживания активного процесса импорта
    private val isImporting = AtomicBoolean(false)
    
    // Максимальное количество транзакций для одного импорта
    private val MAX_IMPORT_LIMIT = 2000

    /**
     * Импортирует транзакции из CSV файла.
     *
     * @param uri URI CSV файла для импорта
     * @return Flow с результатами импорта для обновления UI
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        // Проверяем, нет ли уже активного импорта
        if (!isImporting.compareAndSet(false, true)) {
            Timber.e("ИМПОРТ: Попытка запустить импорт, когда другой импорт уже выполняется")
            emit(ImportResult.Error(message = "Импорт уже выполняется. Дождитесь завершения."))
            return@flow
        }
        
        try {
            Timber.d("ИМПОРТ: Начало импорта из URI: $uri")
            
            // Открываем поток в IO контексте
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть файл (null inputStream)")

            // Проверка на возможные проблемы с кодировкой
            emit(ImportResult.Progress(
                current = 0,
                total = 100,
                message = "Анализ файла и кодировки..."
            ))
            
            var inputStreamToUse = inputStream
            try {
                // Проверим, поддерживает ли поток mark/reset
                if (inputStreamToUse.markSupported()) {
                    // Предварительно прочитаем первые 1000 байт для определения возможных проблем с кодировкой
                    inputStreamToUse.mark(1000)
                    val bytes = ByteArray(1000)
                    val bytesRead = inputStreamToUse.read(bytes)
                    inputStreamToUse.reset() // Сбрасываем позицию, чтобы начать чтение заново

                    if (bytesRead > 0) {
                        val preview = bytes.copyOf(bytesRead).toString(Charsets.UTF_8)
                        Timber.d("ИМПОРТ: Предпросмотр файла (UTF-8): $preview")
                    }
                } else {
                    Timber.w("ИМПОРТ: Поток не поддерживает mark/reset, пропускаем предварительный анализ")
                    // Закрываем текущий поток и открываем новый, так как мы не можем сбросить позицию
                    try {
                        inputStreamToUse.close()
                    } catch (ex: Exception) {
                        Timber.e(
                            ex,
                            "ИМПОРТ: Ошибка закрытия потока, который не поддерживает mark/reset"
                        )
                    }

                    inputStreamToUse = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Не удалось повторно открыть файл")
                }
            } catch (e: Exception) {
                Timber.w(e, "ИМПОРТ: Не удалось выполнить предварительный анализ файла")
                // Если возникла ошибка при анализе, пробуем заново открыть поток
                try {
                    inputStreamToUse.close()
                } catch (ex: Exception) {
                    Timber.e(ex, "ИМПОРТ: Ошибка закрытия потока при обработке ошибки анализа")
                }

                inputStreamToUse = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Не удалось повторно открыть файл после ошибки анализа")
            }

            val reader = BufferedReader(InputStreamReader(inputStreamToUse, Charsets.UTF_8))

            // Читаем заголовки для определения индексов колонок
            val headerLine = reader.readLine()
                ?: throw IllegalArgumentException("Файл пуст или некорректен (null headerLine)")
            
            if (headerLine.isBlank()) {
                reader.close()
                Timber.e("ИМПОРТ: Пустой заголовок CSV")
                throw IllegalArgumentException("Файл пуст или некорректен (пустой заголовок)")
            }
            
            // Детальное логирование заголовка для отладки
            Timber.d("ИМПОРТ: Прочитан заголовок: '$headerLine'")
            
            // Определяем разделитель (запятая, точка с запятой или табуляция)
            val delimiter = detectDelimiter(headerLine)
            Timber.d("ИМПОРТ: Определен разделитель CSV: '$delimiter'")
            
            val header = headerLine.split(delimiter)
            Timber.d("ИМПОРТ: Разбиты заголовки (${header.size}): $header")
            
            // Проверяем, что заголовок имеет достаточное количество колонок
            if (header.size < 2) {
                Timber.e("ИМПОРТ: Некорректный формат заголовка CSV. Обнаружено только ${header.size} колонок")
                throw IllegalArgumentException("Некорректный формат CSV. Заголовок содержит недостаточно колонок (${header.size})")
            }

            // Определяем индексы колонок
            val idIndex = header.indexOfFirst { it.equals("ID", ignoreCase = true) }
            Timber.d("ИМПОРТ: Индекс ID: $idIndex (найдено: ${if(idIndex >= 0) header[idIndex] else "нет"})")
            
            val dateIndex = header.indexOfFirst { 
                it.equals("Дата", ignoreCase = true) || it.equals("Date", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Даты: $dateIndex (найдено: ${if(dateIndex >= 0) header[dateIndex] else "нет"})")
            
            val categoryIndex = header.indexOfFirst { 
                it.equals("Категория", ignoreCase = true) || it.equals("Category", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Категории: $categoryIndex (найдено: ${if(categoryIndex >= 0) header[categoryIndex] else "нет"})")
            
            val amountIndex = header.indexOfFirst { 
                it.equals("Сумма", ignoreCase = true) || it.equals("Amount", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Суммы: $amountIndex (найдено: ${if(amountIndex >= 0) header[amountIndex] else "нет"})")
            
            val typeIndex = header.indexOfFirst { 
                it.equals("Тип", ignoreCase = true) || it.equals("IsExpense", ignoreCase = true) || it.equals("Type", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Типа: $typeIndex (найдено: ${if(typeIndex >= 0) header[typeIndex] else "нет"})")
            
            val noteIndex = header.indexOfFirst { 
                it.equals("Примечание", ignoreCase = true) || it.equals("Note", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Примечания: $noteIndex (найдено: ${if(noteIndex >= 0) header[noteIndex] else "нет"})")
            
            val sourceIndex = header.indexOfFirst { 
                it.equals("Источник", ignoreCase = true) || it.equals("Source", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Источника: $sourceIndex (найдено: ${if(sourceIndex >= 0) header[sourceIndex] else "нет"})")
            
            val sourceColorIndex = header.indexOfFirst { 
                it.equals("SourceColor", ignoreCase = true) || it.equals("ЦветИсточника", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Цвета источника: $sourceColorIndex (найдено: ${if(sourceColorIndex >= 0) header[sourceColorIndex] else "нет"})")
            
            val titleIndex = header.indexOfFirst { 
                it.equals("Title", ignoreCase = true) || it.equals("Название", ignoreCase = true)
            }
            Timber.d("ИМПОРТ: Индекс Названия: $titleIndex (найдено: ${if(titleIndex >= 0) header[titleIndex] else "нет"})")
            
            Timber.d("ИМПОРТ: Индексы колонок: ID=$idIndex, Дата=$dateIndex, Категория=$categoryIndex, " +
                     "Сумма=$amountIndex, Тип=$typeIndex, Примечание=$noteIndex, Источник=$sourceIndex, Название=$titleIndex")

            // Проверяем обязательные поля
            if (dateIndex == -1 || amountIndex == -1) {
                Timber.e("ИМПОРТ: Обязательные колонки отсутствуют: dateIndex=$dateIndex, amountIndex=$amountIndex")
                throw IllegalArgumentException("В файле отсутствуют обязательные колонки (дата, сумма)")
            }

            // Считаем количество строк для прогресса
            var lineCount = 0
            try {
                reader.use { r ->
                    while (r.readLine() != null) {
                        lineCount++
                    }
                }
                Timber.d("ИМПОРТ: Количество строк в файле (без заголовка): $lineCount")
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка подсчета строк")
                throw IllegalArgumentException("Ошибка при анализе файла: ${e.message}")
            }

            // Ограничиваем количество импортируемых транзакций
            if (lineCount > MAX_IMPORT_LIMIT) {
                Timber.w("ИМПОРТ: Превышен лимит импорта. Найдено $lineCount строк, максимум $MAX_IMPORT_LIMIT")
                emit(ImportResult.Error(message = "Слишком много строк в файле ($lineCount). Максимум $MAX_IMPORT_LIMIT транзакций за один раз."))
                return@flow
            }

            // Заново открываем поток для чтения данных
            try {
                inputStreamToUse.close()
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка закрытия исходного потока")
            }

            val dataStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка повторного открытия URI: $uri")
                throw IllegalArgumentException("Не удалось повторно открыть файл: ${e.message}")
            }
                    ?: throw IllegalArgumentException("Не удалось повторно открыть файл (null dataStream)")

            val dataReader = try {
                BufferedReader(InputStreamReader(dataStream, Charsets.UTF_8))
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка создания второго BufferedReader")
                dataStream.close()
                throw IllegalArgumentException("Ошибка чтения данных: ${e.message}")
            }
            
            // Пропускаем заголовок
            try {
                dataReader.readLine()
                Timber.d("ИМПОРТ: Заголовок пропущен, начинаем чтение данных")
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка пропуска заголовка")
                dataReader.close()
                dataStream.close()
                throw IllegalArgumentException("Ошибка при чтении файла: ${e.message}")
            }

            val dateFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            )
            var currentLine = 0
            var importedCount = 0
            var skippedCount = 0
            var totalAmount = Money.zero()

            // Читаем и обрабатываем данные
            try {
                dataReader.useLines { lines ->
                    lines.forEach { line ->
                        currentLine++

                        // Пропускаем пустые строки
                        if (line.isBlank()) {
                            Timber.w("ИМПОРТ: Пропускаем пустую строку на позиции $currentLine")
                            skippedCount++
                            return@forEach
                        }

                        Timber.d("ИМПОРТ: Обработка строки $currentLine: '$line'")

                        // Эмитим прогресс каждые 10 записей
                        if (currentLine % 10 == 0) {
                            emit(
                                ImportResult.Progress(
                                    current = currentLine,
                                    total = lineCount,
                                    message = "Импортируется $currentLine из $lineCount транзакций"
                                )
                            )
                        }

                        try {
                            // Проверяем, нет ли в строке проблемных символов
                            Timber.d("ИМПОРТ: Проверка наличия проблемных символов в строке '$line'")
                            val problematicChars = line.toCharArray()
                                .filter { it.code < 32 && it != '\n' && it != '\r' && it != '\t' }
                            if (problematicChars.isNotEmpty()) {
                                Timber.w(
                                    "ИМПОРТ: В строке обнаружены проблемные символы: ${
                                        problematicChars.joinToString {
                                            "0x${
                                                it.code.toString(
                                                    16
                                                )
                                            }"
                                        }
                                    }"
                                )
                            }

                            // Лог кодовых точек для диагностики проблем с кодировкой
                            val codePoints = line.toCharArray().map { it.code }
                            Timber.d("ИМПОРТ: Кодовые точки строки: $codePoints")

                            val values = parseCsvLine(line, delimiter)
                            Timber.d("ИМПОРТ: Распарсенные значения (${values.size} элементов): $values")

                            // Дополнительная проверка на размер массива и индексы
                            if (dateIndex >= values.size || amountIndex >= values.size) {
                                Timber.w(
                                    "ИМПОРТ: Пропускаем строку с недостаточным количеством полей. " +
                                            "Индексы: dateIndex=$dateIndex, amountIndex=$amountIndex, размер массива=${values.size}"
                                )
                                skippedCount++
                                return@forEach
                            }

                            val rawDateValue: String
                            val rawAmountValue: String

                            try {
                                rawDateValue = values[dateIndex]
                                rawAmountValue = values[amountIndex]
                                Timber.d("ИМПОРТ: Сырые значения: дата='$rawDateValue', сумма='$rawAmountValue'")
                            } catch (e: Exception) {
                                Timber.e(
                                    e,
                                    "ИМПОРТ: Ошибка доступа к данным. dateIndex=$dateIndex, amountIndex=$amountIndex, size=${values.size}"
                                )
                                skippedCount++
                                return@forEach
                            }

                            val dateString = cleanField(rawDateValue)
                            val amountString = cleanField(rawAmountValue).replace(",", ".")

                            Timber.d("ИМПОРТ: Обрабатываем дату: '$dateString', сумму: '$amountString'")

                            // Парсим дату
                            val date = try {
                                var parsedDate: Date? = null
                                var parseException: Exception? = null

                                Timber.d("ИМПОРТ: Пытаемся распарсить дату '$dateString'")

                                // Специальная обработка для format yyyy-MM-dd_HH-mm-ss
                                if (dateString.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}".toRegex())) {
                                    try {
                                        Timber.d("ИМПОРТ: Обнаружен формат даты yyyy-MM-dd_HH-mm-ss: '$dateString'")

                                        // Пробуем напрямую через SimpleDateFormat
                                        try {
                                            val date = SimpleDateFormat(
                                                "yyyy-MM-dd_HH-mm-ss",
                                                Locale.getDefault()
                                            ).parse(dateString)
                                            if (date != null) {
                                                Timber.d("ИМПОРТ: Дата успешно распарсена специальным форматом через SimpleDateFormat: $dateString -> ${date.time}")
                                                parsedDate = date
                                            }
                                        } catch (e: Exception) {
                                            Timber.w(
                                                e,
                                                "ИМПОРТ: Не удалось распарсить дату через SimpleDateFormat: '$dateString'"
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Timber.w(
                                            e,
                                            "ИМПОРТ: Ошибка при специальной обработке даты: '$dateString'"
                                        )
                                    }
                                }

                                // Если специальная обработка не сработала, продолжаем обычную
                                if (parsedDate == null) {
                                    // Пробуем разные форматы даты
                                    for (format in dateFormats) {
                                        try {
                                            parsedDate = format.parse(dateString)
                                            if (parsedDate != null) {
                                                Timber.d("ИМПОРТ: Дата успешно распарсена форматом ${format.toPattern()}: $dateString -> ${parsedDate.time}")
                                                break
                                            }
                                        } catch (e: Exception) {
                                            parseException = e
                                            Timber.v("ИМПОРТ: Не удалось распарсить дату '$dateString' форматом ${format.toPattern()}: ${e.message}")
                                            // Пробуем следующий формат
                                        }
                                    }
                                }

                                // Если до сих пор не получилось, пробуем разобрать дату вручную
                                if (parsedDate == null) {
                                    Timber.w("ИМПОРТ: Не удалось распарсить дату '$dateString' ни одним из форматов: ${parseException?.message}")

                                    // Для формата yyyy-MM-dd_HH-mm-ss пробуем разбить строку
                                    if (dateString.contains("_")) {
                                        val parts = dateString.split("_")
                                        if (parts.size == 2) {
                                            val datePart = parts[0] // yyyy-MM-dd
                                            val timePart = parts[1] // HH-mm-ss

                                            Timber.d("ИМПОРТ: Разбили дату на части: датa='$datePart', время='$timePart'")

                                            // Преобразуем формат времени с "-" на ":"
                                            val timePartFormatted = timePart.replace("-", ":")
                                            val fullDateStr = "$datePart $timePartFormatted"

                                            Timber.d("ИМПОРТ: Пытаемся распарсить дату в стандартном формате: '$fullDateStr'")

                                            try {
                                                parsedDate = SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm:ss",
                                                    Locale.getDefault()
                                                ).parse(fullDateStr)
                                                if (parsedDate != null) {
                                                    Timber.d("ИМПОРТ: Дата успешно распарсена через ручное преобразование: $fullDateStr -> ${parsedDate.time}")
                                                }
                                            } catch (e: Exception) {
                                                Timber.w(
                                                    e,
                                                    "ИМПОРТ: Не удалось распарсить дату через ручное преобразование: '$fullDateStr'"
                                                )
                                            }
                                        }
                                    }
                                }

                                // Последняя попытка - извлечь только yyyy-MM-dd
                                if (parsedDate == null) {
                                    Timber.w("ИМПОРТ: Пытаемся извлечь дату из строки '$dateString'")

                                    // Пытаемся извлечь дату из строки
                                    val datePattern = "\\d{4}-\\d{2}-\\d{2}".toRegex()
                                    val datePart = datePattern.find(dateString)?.value
                                    if (datePart != null) {
                                        Timber.d("ИМПОРТ: Извлечена часть даты из строки: $datePart")
                                        try {
                                            parsedDate = SimpleDateFormat(
                                                "yyyy-MM-dd",
                                                Locale.getDefault()
                                            ).parse(datePart)
                                            if (parsedDate != null) {
                                                Timber.d("ИМПОРТ: Дата успешно распарсена из части строки: $datePart -> $parsedDate")
                                            } else {
                                                Timber.w("ИМПОРТ: Не удалось распарсить дату из части строки: $datePart")
                                            }
                                        } catch (e: Exception) {
                                            Timber.w("ИМПОРТ: Ошибка при парсинге даты из части строки: ${e.message}")
                                        }
                                    } else {
                                        Timber.w("ИМПОРТ: Не удалось извлечь часть даты из строки")
                                    }
                                }

                                // Возвращаем результат парсинга или текущую дату
                                when {
                                    parsedDate != null -> parsedDate
                                    else -> {
                                        Timber.w("ИМПОРТ: Не удалось распарсить дату никаким способом, используем текущую дату")
                                        Date()
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.w("ИМПОРТ: Критическая ошибка парсинга даты '$dateString': ${e.message}")
                                Date() // Используем текущую дату при ошибке парсинга
                            }

                            // Парсим сумму
                            val amountValue = try {
                                val parsedAmount = amountString.toDouble()
                                Timber.d("ИМПОРТ: Сумма успешно распарсена: $amountString -> $parsedAmount")
                                parsedAmount
                            } catch (e: Exception) {
                                Timber.w("ИМПОРТ: Ошибка парсинга суммы '$amountString': ${e.message}")
                                0.0 // Пропускаем при ошибке парсинга суммы
                            }

                            // Создаем объект Money с положительным значением суммы
                            val amount = Money(amountValue.absoluteValue)

                            // Определяем тип транзакции
                            val isExpense = if (typeIndex != -1 && values.size > typeIndex) {
                                val typeValue = cleanField(values[typeIndex])
                                val isExp = if (typeValue.equals(
                                        "true",
                                        ignoreCase = true
                                    ) || typeValue.equals("1")
                                ) {
                                    true
                                } else if (typeValue.equals(
                                        "false",
                                        ignoreCase = true
                                    ) || typeValue.equals("0")
                                ) {
                                    false
                                } else {
                                    typeValue.equals("Расход", ignoreCase = true)
                                }
                                Timber.d("ИМПОРТ: Тип транзакции определен из поля: $isExp (значение: $typeValue)")
                                isExp
                            } else {
                                val isExp = amountValue < 0 // Отрицательная сумма = расход
                                Timber.d("ИМПОРТ: Тип транзакции определен по сумме: $isExp (сумма: $amountValue)")
                                isExp
                            }

                            // Определяем категорию
                            val category =
                                if (categoryIndex != -1 && values.size > categoryIndex) {
                                    cleanField(values[categoryIndex])
                                } else {
                                    if (isExpense) "Другое" else "Другое"
                                }

                            // Определяем примечание
                            var note: String? = null
                            if (noteIndex != -1 && values.size > noteIndex) {
                                note = cleanField(values[noteIndex])
                            }

                            // Определяем источник
                            val source = if (sourceIndex != -1 && values.size > sourceIndex) {
                                cleanField(values[sourceIndex])
                            } else {
                                "Импорт CSV"
                            }

                            // Проверяем, не является ли это переводом
                            val isTransfer = category.contains("перевод", ignoreCase = true) ||
                                    category.contains("transfer", ignoreCase = true)

                            // Создаем транзакцию с уникальным ID на основе времени и случайности
                            var transaction = Transaction(
                                id = "csv_${date.time}_${System.nanoTime()}",
                                amount = Money(amountValue.absoluteValue),
                                category = category,
                                date = date,
                                isExpense = isExpense,
                                note = note,
                                source = source,
                                sourceColor = ColorUtils.getSourceColorByName(source)?.toArgb()
                                    ?: if (isExpense) ExpenseColorInt else IncomeColorInt,
                                isTransfer = isTransfer
                            )

                            Timber.d("ИМПОРТ: Создана транзакция: id=${transaction.id}, дата=${transaction.date}, сумма=${transaction.amount}, категория=${transaction.category}, расход=${transaction.isExpense}")

                            // Обновляем цвет источника на основе типа транзакции
                            val finalSourceColor = when {
                                isTransfer -> TransferColorInt // Для переводов всегда цвет перевода
                                sourceColorIndex != -1 && values.size > sourceColorIndex -> {
                                    // Если цвет указан в CSV и он валидный (не 0, не true/false)
                                    // Эта логика парсинга цвета из CSV может быть сложной и зависит от формата
                                    // Пока оставим простой вариант: если есть значение, пытаемся его использовать как Int
                                    // В идеале, CSV должен содержать ARGB Int или HEX
                                    val rawSourceColor = cleanField(values[sourceColorIndex])
                                    rawSourceColor.toIntOrNull()?.takeIf { it != 0 }
                                        ?: ColorUtils.getSourceColorByName(source)?.toArgb()
                                        ?: if (isExpense) ExpenseColorInt else IncomeColorInt
                                }
                                else -> {
                                    // Если имя источника пустое, но есть цвет, используем его
                                    val defaultSourceColor = if (isExpense) ExpenseColorInt else IncomeColorInt
                                    ColorUtils.getSourceColorByName(source)?.toArgb() ?: defaultSourceColor
                                }
                            }

                            transaction = transaction.copy(sourceColor = finalSourceColor)

                            // Сохраняем транзакцию
                            Timber.d("ИМПОРТ: Сохраняем транзакцию в базу данных")
                            repository.addTransaction(transaction)
                            Timber.d("ИМПОРТ: Транзакция успешно сохранена")

                            importedCount++
                            // Учитываем сумму транзакции в общей сумме
                            totalAmount = if (isExpense) 
                                totalAmount - amount
                            else 
                                totalAmount + amount
                        } catch (e: Exception) {
                            Timber.e(e, "ИМПОРТ: Ошибка при импорте строки $currentLine: $line")
                            skippedCount++
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка чтения строк данных")
                throw IllegalArgumentException("Ошибка при чтении данных: ${e.message}")
            }

            try {
                dataReader.close()
                dataStream.close()
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка закрытия потоков после чтения")
            }

            Timber.d("ИМПОРТ: Импорт завершен. Импортировано: $importedCount, пропущено: $skippedCount, общая сумма: $totalAmount")
            emit(
                ImportResult.Success(
                    importedCount = importedCount,
                    skippedCount = skippedCount,
                    totalAmount = totalAmount
                )
            )

        } catch (e: Exception) {
            Timber.e(e, "ИМПОРТ: Критическая ошибка импорта: ${e.message}")
            emit(
                ImportResult.Error(
                    message = "Ошибка импорта: ${e.message}",
                    exception = e
                )
            )
        } finally {
            // Освобождаем флаг, позволяя запускать новые импорты
            isImporting.set(false)
            Timber.d("ИМПОРТ: Импорт завершен, флаг импорта сброшен")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Парсит строку CSV с учетом кавычек и экранирования.
     *
     * @param line Строка CSV
     * @param delimiter Разделитель полей
     * @return Список значений полей
     */
    private fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
        Timber.d("ИМПОРТ: Парсинг строки CSV: '$line' (разделитель: '$delimiter')")
        
        val values = mutableListOf<String>()
        var value = StringBuilder()
        var inQuotes = false
        var i = 0
        
        // Предварительная обработка строки для исправления проблем с переносами строк
        // Заменяем все возможные комбинации переносов строк на пробелы
        val processedLine = line
            .replace("\r\n", " ")
            .replace("\n", " ")
            .replace("\r", " ")
        
        Timber.d("ИМПОРТ: Предобработанная строка: '$processedLine'")
        
        while (i < processedLine.length) {
            val char = processedLine[i]
            
            when {
                // Обработка экранированных двойных кавычек внутри кавычек
                char == '\"' && inQuotes && i + 1 < processedLine.length && processedLine[i + 1] == '\"' -> {
                    value.append('\"')
                    i += 2 // Пропускаем обе кавычки
                }
                
                // Начало или конец поля в кавычках
                char == '\"' -> {
                    inQuotes = !inQuotes
                    i++
                }
                
                // Разделитель полей (если не внутри кавычек)
                char == delimiter && !inQuotes -> {
                    values.add(value.toString().trim())
                    value = StringBuilder()
                    i++
                }
                
                // Обычный символ
                else -> {
                    value.append(char)
                    i++
                }
            }
        }
        
        // Добавляем последнее поле
        values.add(value.toString().trim())
        
        // Удаляем пустые поля в конце, если они есть
        while (values.isNotEmpty() && values.last().isBlank()) {
            values.removeAt(values.size - 1)
        }
        
        Timber.d("ИМПОРТ: Результат парсинга (${values.size} элементов): $values")
        return values
    }
    
    /**
     * Очищает значение поля от кавычек и лишних пробелов
     */
    private fun cleanField(value: String): String {
        val cleaned = value.trim().let {
            // Если поле начинается и заканчивается кавычками, удаляем их
            if (it.startsWith("\"") && it.endsWith("\"") && it.length >= 2) {
                it.substring(1, it.length - 1)
            } else {
                it
            }
        }
        
        Timber.d("ИМПОРТ: Очищено поле: '$value' -> '$cleaned'")
        return cleaned
    }

    /**
     * Определяет разделитель в строке CSV (запятая, точка с запятой или табуляция).
     *
     * @param line Первая строка CSV файла
     * @return Определенный разделитель
     */
    private fun detectDelimiter(line: String): Char {
        val countComma = line.count { it == ',' }
        val countSemicolon = line.count { it == ';' }
        val countTab = line.count { it == '\t' }
        
        Timber.d("ИМПОРТ: Подсчет разделителей в заголовке: запятых=$countComma, точек с запятой=$countSemicolon, табуляций=$countTab")
        
        return when {
            countSemicolon > countComma && countSemicolon > countTab -> ';'
            countTab > countComma && countTab > countSemicolon -> '\t'
            else -> ','
        }
    }
} 