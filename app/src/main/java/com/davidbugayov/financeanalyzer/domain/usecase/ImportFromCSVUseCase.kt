package com.davidbugayov.financeanalyzer.domain.usecase

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.ImportResult
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlin.math.min
import java.util.concurrent.atomic.AtomicBoolean

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
            val inputStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка открытия URI: $uri")
                throw IllegalArgumentException("Не удалось открыть файл: ${e.message}")
            } ?: throw IllegalArgumentException("Не удалось открыть файл (null inputStream)")

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
                        Timber.e(ex, "ИМПОРТ: Ошибка закрытия потока, который не поддерживает mark/reset")
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

            val reader = try {
                BufferedReader(InputStreamReader(inputStreamToUse, Charsets.UTF_8))
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка создания BufferedReader")
                inputStreamToUse.close()
                throw IllegalArgumentException("Ошибка чтения файла: ${e.message}")
            }

            // Читаем заголовки для определения индексов колонок
            val headerLine = try {
                reader.readLine()
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка чтения заголовка")
                reader.close()
                throw IllegalArgumentException("Ошибка чтения заголовка: ${e.message}")
            }
            
            if (headerLine.isNullOrBlank()) {
                Timber.e("ИМПОРТ: Пустой заголовок CSV")
                reader.close()
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
            } ?: throw IllegalArgumentException("Не удалось повторно открыть файл (null dataStream)")

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
            var totalAmount = 0.0

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
                            val problematicChars = line.toCharArray().filter { it.code < 32 && it != '\n' && it != '\r' && it != '\t' }
                            if (problematicChars.isNotEmpty()) {
                                Timber.w("ИМПОРТ: В строке обнаружены проблемные символы: ${problematicChars.joinToString { "0x${it.code.toString(16)}" }}")
                            }
                            
                            // Лог кодовых точек для диагностики проблем с кодировкой
                            val codePoints = line.toCharArray().map { it.code }
                            Timber.d("ИМПОРТ: Кодовые точки строки: $codePoints")

                            val values = parseCsvLine(line, delimiter)
                            Timber.d("ИМПОРТ: Распарсенные значения (${values.size} элементов): $values")

                            // Дополнительная проверка на размер массива и индексы
                            if (dateIndex >= values.size || amountIndex >= values.size) {
                                Timber.w("ИМПОРТ: Пропускаем строку с недостаточным количеством полей. " +
                                         "Индексы: dateIndex=$dateIndex, amountIndex=$amountIndex, размер массива=${values.size}")
                                skippedCount++
                                return@forEach
                            }

                            var rawDateValue = ""
                            var rawAmountValue = ""
                            
                            try {
                                rawDateValue = values[dateIndex]
                                rawAmountValue = values[amountIndex]
                                Timber.d("ИМПОРТ: Сырые значения: дата='$rawDateValue', сумма='$rawAmountValue'")
                            } catch (e: Exception) {
                                Timber.e(e, "ИМПОРТ: Ошибка доступа к данным. dateIndex=$dateIndex, amountIndex=$amountIndex, size=${values.size}")
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
                                parsedDate = if (dateString.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}".toRegex())) {
                                    try {
                                        Timber.d("ИМПОРТ: Обнаружен формат даты yyyy-MM-dd_HH-mm-ss: '$dateString'")
                                        
                                        // Пробуем напрямую через SimpleDateFormat
                                        try {
                                            val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).parse(dateString)
                                            if (date != null) {
                                                Timber.d("ИМПОРТ: Дата успешно распарсена специальным форматом через SimpleDateFormat: $dateString -> ${date.time}")
                                                date
                                            } else null
                                        } catch (e: Exception) {
                                            Timber.w(e, "ИМПОРТ: Не удалось распарсить дату через SimpleDateFormat: '$dateString'")
                                            null
                                        }
                                    } catch (e: Exception) {
                                        Timber.w(e, "ИМПОРТ: Ошибка при специальной обработке даты: '$dateString'")
                                        null
                                    }
                                } else null
                                
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
                                                parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fullDateStr)
                                                if (parsedDate != null) {
                                                    Timber.d("ИМПОРТ: Дата успешно распарсена через ручное преобразование: $fullDateStr -> ${parsedDate.time}")
                                                }
                                            } catch (e: Exception) {
                                                Timber.w(e, "ИМПОРТ: Не удалось распарсить дату через ручное преобразование: '$fullDateStr'")
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
                                            parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(datePart)
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
                                
                                // Если всё равно не удалось распарсить дату, используем текущую
                                if (parsedDate == null) {
                                    Timber.w("ИМПОРТ: Не удалось распарсить дату никаким способом, используем текущую дату")
                                    Date()
                                } else {
                                    parsedDate
                                }
                            } catch (e: Exception) {
                                Timber.w("ИМПОРТ: Критическая ошибка парсинга даты '$dateString': ${e.message}")
                                Date() // Используем текущую дату при ошибке парсинга
                            }

                            // Парсим сумму
                            val amount = try {
                                val parsedAmount = amountString.toDouble()
                                Timber.d("ИМПОРТ: Сумма успешно распарсена: $amountString -> $parsedAmount")
                                parsedAmount
                            } catch (e: Exception) {
                                Timber.w("ИМПОРТ: Ошибка парсинга суммы '$amountString': ${e.message}")
                                0.0 // Пропускаем при ошибке парсинга суммы
                            }

                            // Определяем тип транзакции
                            val isExpense = if (typeIndex != -1 && values.size > typeIndex) {
                                val typeValue = cleanField(values[typeIndex])
                                val isExp = if (typeValue.equals("true", ignoreCase = true) || typeValue.equals("1")) {
                                    true
                                } else if (typeValue.equals("false", ignoreCase = true) || typeValue.equals("0")) {
                                    false
                                } else {
                                    typeValue.equals("Расход", ignoreCase = true)
                                }
                                Timber.d("ИМПОРТ: Тип транзакции определен из поля: $isExp (значение: $typeValue)")
                                isExp
                            } else {
                                val isExp = amount < 0 // Отрицательная сумма = расход
                                Timber.d("ИМПОРТ: Тип транзакции определен по сумме: $isExp (сумма: $amount)")
                                isExp
                            }

                            // Определяем категорию
                            val category = if (categoryIndex != -1 && values.size > categoryIndex) {
                                cleanField(values[categoryIndex])
                            } else {
                                if (isExpense) "Другое" else "Другое"
                            }

                            // Определяем примечание
                            var note: String? = null
                            if (noteIndex != -1 && values.size > noteIndex) {
                                note = cleanField(values[noteIndex])
                                Timber.d("ИМПОРТ: Примечание из CSV: '$note'")
                            } else if (titleIndex != -1 && values.size > titleIndex) {
                                // Если нет примечания, но есть название - используем его как примечание
                                note = cleanField(values[titleIndex])
                                Timber.d("ИМПОРТ: Используем Title как примечание: '$note'")
                            } else {
                                Timber.d("ИМПОРТ: Поле примечания отсутствует или за пределами значений")
                            }

                            // Определяем источник
                            val rawSource = if (sourceIndex != -1 && values.size > sourceIndex) {
                                values[sourceIndex]
                            } else {
                                "Импорт"
                            }
                            
                            // Обработка источника с переносом строки
                            val source = cleanField(rawSource).let { src ->
                                // Если источник короткий и похож на часть имени банка, пробуем исправить
                                when {
                                    src.equals("Сб", ignoreCase = true) -> "Сбер"
                                    src.equals("ер", ignoreCase = true) -> "Сбер" 
                                    src.equals("Тинь", ignoreCase = true) -> "Т-Банк"
                                    src.equals("кофф", ignoreCase = true) -> "Т-Банк"
                                    src.equals("Аль", ignoreCase = true) -> "Альфа"
                                    src.equals("фа", ignoreCase = true) -> "Альфа"
                                    src.contains("\n") -> src.replace("\n", " ").trim()
                                    else -> src
                                }
                            }

                            // Проверяем значения перед созданием транзакции
                            if (amount == 0.0) {
                                Timber.w("ИМПОРТ: Пропускаем транзакцию с нулевой суммой")
                                skippedCount++
                                return@forEach
                            }

                            // Создаем объект транзакции
                            val generatedId = "import_${date.time}_${System.nanoTime()}"
                            val transactionId = if (idIndex != -1 && values.size > idIndex) {
                                val csvId = cleanField(values[idIndex])
                                // Ограничиваем длину ID до 50 символов для предотвращения проблем с длинными ID
                                if (csvId.length > 50) {
                                    Timber.w("ИМПОРТ: ID слишком длинный (${csvId.length} символов), обрезаем: $csvId")
                                    csvId.substring(0, 50)
                                } else {
                                    csvId
                                }
                            } else {
                                generatedId
                            }
                            
                            // Проверяем существование транзакции с таким ID
                            val existingTransaction = try {
                                repository.getTransactionById(transactionId)
                            } catch (e: Exception) {
                                Timber.w("ИМПОРТ: Ошибка проверки существующей транзакции: ${e.message}")
                                null
                            }
                            
                            if (existingTransaction != null) {
                                Timber.w("ИМПОРТ: Транзакция с ID $transactionId уже существует, пропускаем")
                                skippedCount++
                                return@forEach
                            }

                            // Принудительная задержка между транзакциями для предотвращения перегрузки базы данных
                            if (currentLine % 20 == 0) {
                                Timber.d("ИМПОРТ: Делаем небольшую паузу после 20 транзакций")
                                delay(100) // Пауза 100 мс после каждых 20 транзакций
                            }

                            // Ограничения длины полей
                            val limitedCategory = if (category.length > 100) {
                                Timber.w("ИМПОРТ: Обрезаем слишком длинную категорию: ${category.length} символов")
                                category.substring(0, 100)
                            } else {
                                category
                            }
                            
                            val limitedNote = note?.let {
                                if (it.length > 1000) {
                                    Timber.w("ИМПОРТ: Обрезаем слишком длинное примечание: ${it.length} символов")
                                    it.substring(0, 1000)
                                } else {
                                    it
                                }
                            }
                            
                            val limitedSource = if (source.length > 100) {
                                Timber.w("ИМПОРТ: Обрезаем слишком длинный источник: ${source.length} символов")
                                source.substring(0, 100)
                            } else {
                                source
                            }

                            val transaction = Transaction(
                                id = transactionId,
                                amount = amount.absoluteValue, // Храним положительное значение
                                category = limitedCategory,
                                isExpense = isExpense,
                                date = date,
                                note = limitedNote,
                                source = limitedSource
                            )
                            
                            Timber.d("ИМПОРТ: Создана транзакция: id=${transaction.id}, дата=${transaction.date}, сумма=${transaction.amount}, категория=${transaction.category}, расход=${transaction.isExpense}")

                            // Сохраняем транзакцию
                            Timber.d("ИМПОРТ: Сохраняем транзакцию в базу данных")
                            repository.addTransaction(transaction)
                            Timber.d("ИМПОРТ: Транзакция успешно сохранена")

                            importedCount++
                            totalAmount += amount.absoluteValue
                        } catch (e: Exception) {
                            Timber.e(e, "ИМПОРТ: Ошибка при импорте строки $currentLine: $line")
                            skippedCount++
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ИМПОРТ: Ошибка чтения строк данных")
                throw IllegalArgumentException("Ошибка при чтении данных: ${e.message}")
            } finally {
                try {
                    dataReader.close()
                    dataStream.close()
                } catch (e: Exception) {
                    Timber.e(e, "ИМПОРТ: Ошибка закрытия потоков после чтения")
                }
            }

            Timber.d("ИМПОРТ: Импорт завершен. Импортировано: $importedCount, пропущено: $skippedCount, общая сумма: $totalAmount")
            // Отправляем результат успешного импорта
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
    }

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