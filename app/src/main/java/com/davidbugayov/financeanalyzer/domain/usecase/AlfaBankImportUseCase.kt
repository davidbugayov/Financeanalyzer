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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.absoluteValue

/**
 * Реализация импорта транзакций из выписки Альфа-Банка.
 * Поддерживает формат выписки из личного кабинета и мобильного приложения.
 * Также поддерживает формат XLSX из Альфа-Банка.
 *
 * @param repository Репозиторий для сохранения импортированных транзакций
 * @param context Контекст приложения для доступа к файловой системе
 */
class AlfaBankImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Альфа-Банк"

    // Форматы даты, используемые в выписках Альфа-Банка
    private val dateFormats = listOf(
        SimpleDateFormat("dd.MM.yyyy", Locale("ru")),
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru")),
        SimpleDateFormat("yyyy-MM-dd", Locale("ru"))
    )

    // Паттерны для проверки формата
    private val headerPattern = Pattern.compile(".*Дата.*Сумма.*Категория.*", Pattern.DOTALL)

    /**
     * Проверяет, соответствует ли файл формату выписки Альфа-Банка.
     */
    override fun isValidFormat(reader: BufferedReader): Boolean {
        val headerLines = StringBuilder()
        reader.useLines { lines ->
            lines.take(10).forEach { line ->
                headerLines.append(line)
                headerLines.append("\n")
            }
        }

        val headerText = headerLines.toString().lowercase()
        return headerText.contains("альфа") ||
                headerText.contains("alfa") ||
                headerText.contains("alfabank") ||
                (headerText.contains("дата") &&
                        headerText.contains("описание") &&
                        headerText.contains("сумма")) ||
                headerPattern.matcher(headerText).matches()
    }
    
    /**
     * Переопределен метод invoke для обработки как CSV/TXT, так и XLSX файлов Альфа-Банка.
     * Определяет тип файла по расширению и вызывает соответствующий метод импорта.
     */
    override suspend fun invoke(uri: Uri): Flow<ImportResult> = flow {
        // Проверяем расширение файла
        val fileName = getFileName(uri)
        val fileExtension = fileName.substringAfterLast('.', "").lowercase()

        when (fileExtension) {
            "xlsx" -> {
                // Для XLSX используем специальный метод импорта
                Timber.d("АЛЬФА-ИМПОРТ: Обнаружен файл XLSX, используем специальный импорт для Excel")
                try {
                    importFromExcel(uri) { result -> emit(result) }
                } catch (e: Throwable) {
                    // Если произошла ошибка при импорте XLSX, логируем ее
                    Timber.e(e, "АЛЬФА-ИМПОРТ: Ошибка при импорте XLSX, пробуем альтернативный метод")
                    
                    // Пробуем прочитать XLSX как текстовый файл (если это экспорт с разделителями)
                    try {
                        Timber.d("АЛЬФА-ИМПОРТ: Пробуем обработать XLSX как текстовый файл")
                        emit(ImportResult.Progress(current = 20, total = 100, message = "Пробуем альтернативный метод импорта..."))
                        
                        // Открываем файл как текстовый и пробуем прочитать
                        val inputStream = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalArgumentException("Не удалось открыть файл")
                        
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        
                        if (isValidFormat(reader)) {
                            Timber.d("АЛЬФА-ИМПОРТ: XLSX файл имеет текстовый формат, который можно обработать")
                            
                            // Заново открываем поток для чтения данных
                            inputStream.close()
                            
                            val dataStream = context.contentResolver.openInputStream(uri)
                                ?: throw IllegalArgumentException("Не удалось открыть файл")
                            
                            val dataReader = BufferedReader(InputStreamReader(dataStream))
                            
                            // Пропускаем заголовок
                            skipHeaders(dataReader)
                            
                            // Считаем количество строк для отображения прогресса
                            val totalLines = countTransactionLines(uri)
                            
                            var currentLine = 0
                            var importedCount = 0
                            var skippedCount = 0
                            var totalAmount = Money.zero()
                            
                            // Читаем и обрабатываем строки с транзакциями
                            dataReader.useLines { lines ->
                                lines.forEach { line ->
                                    if (shouldSkipLine(line)) {
                                        return@forEach
                                    }
                                    
                                    currentLine++
                                    
                                    try {
                                        // Парсим строку и получаем транзакцию
                                        val transaction = parseLine(line)
                                        
                                        // Добавляем информацию о банке как источнике
                                        val transactionWithSource = transaction.copy(
                                            source = bankName
                                        )
                                        
                                        // Сохраняем только транзакции с положительной суммой
                                        if (transactionWithSource.amount > Money.zero()) {
                                            repository.addTransaction(transactionWithSource)
                                            
                                            importedCount++
                                            // Учитываем сумму транзакции в общей сумме
                                            totalAmount = if (transactionWithSource.isExpense) 
                                                totalAmount - transactionWithSource.amount 
                                            else 
                                                totalAmount + transactionWithSource.amount
                                        } else {
                                            skippedCount++
                                            Timber.d("АЛЬФА-ИМПОРТ: Пропущена транзакция с нулевой суммой")
                                        }
                                    } catch (e: Exception) {
                                        skippedCount++
                                        Timber.e(e, "АЛЬФА-ИМПОРТ: Ошибка при обработке строки $currentLine: ${e.message}")
                                    }
                                    
                                    // Эмитим прогресс каждые 10 записей
                                    if (currentLine % 10 == 0) {
                                        emit(ImportResult.Progress(
                                            current = currentLine,
                                            total = totalLines,
                                            message = "Импортируется $currentLine из $totalLines транзакций из $bankName"
                                        ))
                                    }
                                }
                            }
                            
                            if (importedCount > 0) {
                                // Отправляем результат успешного импорта
                                emit(
                                    ImportResult.Success(
                                        importedCount = importedCount,
                                        skippedCount = skippedCount,
                                        totalAmount = totalAmount
                                    )
                                )
                            } else {
                                emit(ImportResult.Error("Не найдено транзакций для импорта в файле (альтернативный метод)"))
                            }
                        } else {
                            Timber.d("АЛЬФА-ИМПОРТ: XLSX файл не удалось прочитать как текстовый")
                            emit(ImportResult.Error("Не удалось обработать XLSX файл. Попробуйте экспортировать выписку в формате CSV или TXT."))
                        }
                    } catch (fallbackError: Exception) {
                        Timber.e(fallbackError, "АЛЬФА-ИМПОРТ: Альтернативный метод тоже не сработал")
                        emit(ImportResult.Error("Не удалось обработать XLSX файл: ${fallbackError.message}\nАльтернативный метод не сработал: ${fallbackError.message}"))
                    }
                }
            }
            else -> {
                // Для CSV и других форматов используем стандартный метод с фильтрацией
                Timber.d("АЛЬФА-ИМПОРТ: Обнаружен файл CSV/TXT, используем стандартный импорт с фильтрацией")
                
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Не удалось открыть файл")
                
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                // Проверяем, что формат файла соответствует ожидаемому для данного банка
                val validFormat = isValidFormat(reader)
                
                if (!validFormat) {
                    emit(ImportResult.Error("Формат файла не соответствует формату $bankName"))
                    return@flow
                }
                
                // Заново открываем поток для чтения данных
                inputStream.close()
                
                val dataStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Не удалось открыть файл")
                
                val dataReader = BufferedReader(InputStreamReader(dataStream))
                
                // Пропускаем заголовок (количество строк зависит от конкретного банка)
                skipHeaders(dataReader)
                
                // Считаем количество строк для отображения прогресса
                val totalLines = countTransactionLines(uri)
                
                var currentLine = 0
                var importedCount = 0
                var skippedCount = 0
                var totalAmount = Money.zero()
                
                // Читаем и обрабатываем строки с транзакциями
                dataReader.useLines { lines ->
                    lines.forEach { line ->
                        if (shouldSkipLine(line)) {
                            return@forEach
                        }
                        
                        currentLine++
                        
                        try {
                            // Парсим строку и получаем транзакцию
                            val transaction = parseLine(line)
                            
                            // Добавляем информацию о банке как источнике
                            val transactionWithSource = transaction.copy(
                                source = bankName
                            )
                            
                            // Сохраняем только транзакции с положительной суммой
                            if (transactionWithSource.amount > Money.zero()) {
                                // Сохраняем транзакцию без создания новых категорий
                                repository.addTransaction(transactionWithSource)
                                
                                importedCount++
                                // Учитываем сумму транзакции в общей сумме
                                totalAmount = if (transactionWithSource.isExpense) 
                                    totalAmount - transactionWithSource.amount 
                                else 
                                    totalAmount + transactionWithSource.amount
                            } else {
                                skippedCount++
                                Timber.d("АЛЬФА-ИМПОРТ: Пропущена транзакция с нулевой суммой")
                            }
                        } catch (e: Exception) {
                            skippedCount++
                            Timber.e(e, "АЛЬФА-ИМПОРТ: Ошибка при обработке строки $currentLine: ${e.message}")
                        }
                        
                        // Эмитим прогресс каждые 10 записей
                        if (currentLine % 10 == 0) {
                            emit(ImportResult.Progress(
                                current = currentLine,
                                total = totalLines,
                                message = "Импортируется $currentLine из $totalLines транзакций из $bankName"
                            ))
                        }
                    }
                }
                
                // Отправляем результат успешного импорта
                emit(
                    ImportResult.Success(
                        importedCount = importedCount,
                        skippedCount = skippedCount,
                        totalAmount = totalAmount
                    )
                )
            }
        }
    }
    
    /**
     * Получает имя файла из URI.
     */
    private fun getFileName(uri: Uri): String {
        // Пробуем получить имя файла из метаданных
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex("_display_name")
                    if (displayNameIndex != -1) {
                        return cursor.getString(displayNameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "АЛЬФА-ИМПОРТ: Ошибка при получении имени файла из метаданных")
        }

        // Если не удалось получить имя из метаданных, извлекаем из URI
        val path = uri.path
        return path?.substringAfterLast('/') ?: "unknown.csv"
    }
    
    /**
     * Импортирует транзакции из Excel файла.
     */
    private suspend fun importFromExcel(
        uri: Uri, 
        emit: suspend (ImportResult) -> Unit
    ) {
        try {
            emit(ImportResult.Progress(current = 0, total = 100, message = "Чтение XLSX файла..."))
            
            // Открываем поток для чтения Excel файла
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Не удалось открыть файл")
            
            inputStream.use { stream ->
                // Импортируем данные из Excel
                val transactions = parseExcelFile(stream)
                
                // Фильтруем: оставляем только транзакции с положительной суммой
                val filteredTransactions = transactions.filter { it.amount > Money.zero() }
                
                // Сохраняем транзакции
                if (filteredTransactions.isNotEmpty()) {
                    emit(ImportResult.Progress(
                        current = 50,
                        total = 100,
                        message = "Сохранение ${filteredTransactions.size} транзакций..."
                    ))
                    
                    // Сохраняем пакетами по 50 транзакций
                    val batchSize = 50
                    filteredTransactions.chunked(batchSize).forEachIndexed { index, batch ->
                        // Сохраняем пакет транзакций по одной
                        batch.forEach { transaction ->
                            repository.addTransaction(transaction)
                        }
                        
                        val progress = 50 + (index + 1) * 50 / ((filteredTransactions.size / batchSize) + 1)
                        emit(ImportResult.Progress(
                            current = progress,
                            total = 100,
                            message = "Сохранено ${(index + 1) * batchSize} транзакций..."
                        ))
                    }
                    
                    // Используем правильные имена параметров из класса ImportResult.Success
                    emit(ImportResult.Success(
                        importedCount = filteredTransactions.size,
                        skippedCount = transactions.size - filteredTransactions.size,
                        totalAmount = filteredTransactions.fold(Money.zero()) { acc, transaction ->
                            if (transaction.isExpense) acc - transaction.amount else acc + transaction.amount 
                        }
                    ))
                } else {
                    emit(ImportResult.Error(message = "Не найдено транзакций с положительными суммами в файле"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "АЛЬФА-ИМПОРТ: Ошибка при импорте из XLSX")
            emit(ImportResult.Error(message = "Ошибка при импорте XLSX: ${e.message}", exception = e))
        }
    }
    
    /**
     * Парсит Excel файл и возвращает список транзакций.
     */
    private fun parseExcelFile(inputStream: InputStream): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        try {
            // Отключаем логирование POI, чтобы избежать проблем
            System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.NullLogger")
            
            // Открываем Excel файл с использованием try-with-resources
            XSSFWorkbook(inputStream).use { wb ->
                Timber.d("АЛЬФА-ИМПОРТ: XLSX файл успешно открыт, количество листов: ${wb.numberOfSheets}")
                
                // Обрабатываем первый лист (обычно выписка на первом листе)
                val sheet = wb.getSheetAt(0)
                Timber.d("АЛЬФА-ИМПОРТ: Обрабатываем лист: ${sheet.sheetName}, общее число строк: ${sheet.lastRowNum + 1}")
                
                // Находим заголовки колонок
                val headerRow = findHeaderRow(sheet)
                if (headerRow != null) {
                    Timber.d("АЛЬФА-ИМПОРТ: Найдена строка с заголовками на позиции: ${headerRow.rowNum}")
                    
                    // Определяем индексы нужных колонок
                    val columnIndices = findColumnIndices(headerRow)
                    Timber.d("АЛЬФА-ИМПОРТ: Определены индексы колонок: $columnIndices")
                    
                    // Обрабатываем строки с данными
                    var processedRows = 0
                    var skippedRows = 0
                    
                    for (rowIndex in headerRow.rowNum + 1 until sheet.lastRowNum + 1) {
                        val row = sheet.getRow(rowIndex) ?: continue
                        
                        // Пропускаем пустые строки или строки с итогами
                        if (shouldSkipExcelRow(row)) {
                            skippedRows++
                            continue
                        }
                        
                        try {
                            // Преобразуем строку Excel в транзакцию
                            val transaction = parseExcelRow(row, columnIndices)
                            transactions.add(transaction)
                            processedRows++
                            
                            if (processedRows % 10 == 0) {
                                Timber.d("АЛЬФА-ИМПОРТ: Обработано $processedRows строк из XLSX")
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "АЛЬФА-ИМПОРТ: Ошибка при обработке строки Excel №${rowIndex}")
                            skippedRows++
                        }
                    }
                    
                    Timber.d("АЛЬФА-ИМПОРТ: Всего обработано $processedRows строк, пропущено $skippedRows строк")
                } else {
                    Timber.w("АЛЬФА-ИМПОРТ: Не найдена строка с заголовками в файле")
                }
            }
            
            Timber.d("АЛЬФА-ИМПОРТ: Excel файл успешно закрыт")
        } catch (e: Throwable) {
            // Ловим любые ошибки, включая Error
            Timber.e(e, "АЛЬФА-ИМПОРТ: Ошибка при парсинге Excel файла: ${e.javaClass.name} - ${e.message}")
        } finally {
            // Закрываем поток входных данных в блоке finally
            try {
                inputStream.close()
            } catch (_: Exception) {
                // Игнорируем ошибки при закрытии
            }
        }
        
        Timber.d("АЛЬФА-ИМПОРТ: Итого найдено транзакций: ${transactions.size}")
        return transactions
    }
    
    /**
     * Находит строку с заголовками в Excel файле.
     */
    private fun findHeaderRow(sheet: Sheet): Row? {
        // Ищем строку с заголовками в первых 30 строках (увеличено с 15)
        for (i in 0 until minOf(30, sheet.lastRowNum + 1)) {
            val row = sheet.getRow(i) ?: continue
            
            // Преобразуем строку в текст для проверки заголовков
            val rowText = (0 until row.lastCellNum)
                .mapNotNull { row.getCell(it)?.toString() }
                .joinToString(" ")
                .lowercase()
            
            Timber.d("АЛЬФА-ИМПОРТ: Проверка строки ${i + 1} на заголовок: \"$rowText\"")
            
            // Проверяем, содержит ли строка ключевые слова из заголовков
            val containsDate = rowText.contains("дата")
            val containsAmount = rowText.contains("сумма")
            val containsCategory = rowText.contains("категория")
            val containsDescription = rowText.contains("описание") || rowText.contains("примечание")
            val containsCode = rowText.contains("код")
            val containsStatus = rowText.contains("статус")

            // Дополнительно проверяем специфичные заголовки Альфа-Банка
            if ((containsDate && containsAmount) ||
                (containsDate && containsStatus) ||
                (containsDate && containsCode) ||
                rowText.contains("дата операции") ||
                rowText.contains("дата проводки") ||
                containsCategory ||
                containsDescription) {

                Timber.d(
                    "АЛЬФА-ИМПОРТ: Найден заголовок в строке %d, содержит: дата=%s, сумма=%s, категория=%s, описание=%s, код=%s, статус=%s",
                    i + 1, containsDate, containsAmount, containsCategory, containsDescription, containsCode, containsStatus
                )
                return row
            }
        }
        
        // Если не нашли заголовок, пробуем по другим критериям (таблица без явных заголовков)
        Timber.d("АЛЬФА-ИМПОРТ: Не найдены стандартные заголовки, пробуем определить таблицу по содержимому")
        
        // Ищем строку, которая выглядит как первая строка с данными
        for (i in 15 until minOf(50, sheet.lastRowNum + 1)) {
            val row = sheet.getRow(i) ?: continue
            
            // Проверяем, похоже ли содержимое на дату и транзакцию
            if (isLikelyTransactionRow(row)) {
                Timber.d("АЛЬФА-ИМПОРТ: Найдена строка, похожая на транзакцию на позиции ${i}. Используем предыдущую строку как заголовок")
                
                // Используем предыдущую строку как заголовок, если она существует
                val headerIdx = i - 1
                val headerRow = sheet.getRow(headerIdx)
                if (headerRow != null) {
                    Timber.d("АЛЬФА-ИМПОРТ: Используем строку ${headerIdx + 1} как заголовок")
                    return headerRow
                }
                
                // Если предыдущей строки нет, создаем виртуальный заголовок
                Timber.d("АЛЬФА-ИМПОРТ: Используем текущую строку ${i + 1} как заголовок и для данных")
                return row
            }
        }
        
        return null
    }
    
    /**
     * Проверяет, похожа ли строка на строку с данными транзакции
     */
    private fun isLikelyTransactionRow(row: Row): Boolean {
        // Получаем текст первых нескольких ячеек
        val cellTexts = (0 until minOf(6, row.lastCellNum.toInt()))
            .mapNotNull { row.getCell(it)?.toString() }
        
        if (cellTexts.isEmpty()) return false
        
        // Проверяем, есть ли в строке что-то похожее на дату
        val hasDate = cellTexts.any { 
            it.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}.*")) || 
            it.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))
        }
        
        // Проверяем, есть ли в строке что-то похожее на сумму с валютой
        val hasAmount = cellTexts.any {
            it.replace(" ", "").matches(Regex("-?\\d+([.,]\\d+)?([₽$€руб]|RUB)?"))
        }
        
        Timber.d("АЛЬФА-ИМПОРТ: Проверка строки на транзакцию. Ячейки: ${cellTexts.joinToString(", ")}. Похоже на дату: $hasDate, на сумму: $hasAmount")
        
        return hasDate || hasAmount
    }
    
    /**
     * Находит индексы нужных колонок в заголовке Excel.
     */
    private fun findColumnIndices(headerRow: Row): Map<String, Int> {
        val columnIndices = mutableMapOf<String, Int>()
        
        // Сначала пробуем найти индексы по заголовкам
        for (i in 0 until headerRow.lastCellNum) {
            val cell = headerRow.getCell(i) ?: continue
            val cellValue = cell.toString().lowercase().trim()
            
            Timber.d("АЛЬФА-ИМПОРТ: Проверка колонки $i: \"$cellValue\"")
            
            when {
                cellValue.contains("дата") && 
                (cellValue.contains("операции") || cellValue.contains("проводки")) -> {
                    columnIndices["date"] = i
                    Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'date'")
                }
                cellValue.contains("дата") && !columnIndices.containsKey("date") -> {
                    columnIndices["date"] = i
                    Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'date'")
                }
                cellValue.contains("сумма") || 
                cellValue.matches(Regex(".*сум.*вал.*счет.*")) -> {
                    columnIndices["amount"] = i
                    Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'amount'")
                }
                cellValue.contains("код") -> {
                    columnIndices["code"] = i
                    Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'code'")
                }
                cellValue.contains("категория") -> {
                    columnIndices["category"] = i
                    Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'category'")
                }
                cellValue.contains("описание") || 
                cellValue.contains("примечание") || 
                cellValue.contains("назначение") -> {
                    columnIndices["description"] = i
                    Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'description'")
                }
                cellValue.contains("статус") -> {
                    columnIndices["status"] = i 
                    Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'status'")
                }
            }
        }
        
        // Если не нашли основные колонки, попробуем определить их эвристически
        if (!columnIndices.containsKey("date") || !columnIndices.containsKey("amount")) {
            Timber.d("АЛЬФА-ИМПОРТ: Не найдены основные колонки по заголовкам, пробуем эвристический подход")
            
            // Анализируем содержимое строки или следующих строк для определения колонок
            val isFirstRowData = isLikelyTransactionRow(headerRow)
            
            if (isFirstRowData) {
                Timber.d("АЛЬФА-ИМПОРТ: Строка с заголовками похожа на строку с данными, создаем виртуальные индексы")
                
                // Прямой анализ строки для определения колонок
                for (i in 0 until headerRow.lastCellNum) {
                    val cell = headerRow.getCell(i) ?: continue
                    val cellValue = cell.toString()
                    
                    // Определяем колонки по содержимому
                    when {
                        // Дата в формате ДД.ММ.ГГГГ
                        cellValue.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}.*")) && !columnIndices.containsKey("date") -> {
                            columnIndices["date"] = i
                            Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'date' по содержимому")
                        }
                        // Код транзакции (буква + цифры)
                        cellValue.matches(Regex("[A-Za-zА-Яа-я]\\d+.*")) && !columnIndices.containsKey("code") -> {
                            columnIndices["code"] = i
                            Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'code' по содержимому")
                        }
                        // Сумма с валютой
                        cellValue.replace(" ", "").matches(Regex("-?\\d+([.,]\\d+)?([₽$€руб]|RUB)?")) && !columnIndices.containsKey("amount") -> {
                            columnIndices["amount"] = i
                            Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'amount' по содержимому")
                        }
                        // Статус (строка без цифр и специальных символов)
                        cellValue.matches(Regex("[А-Яа-я]+")) && !columnIndices.containsKey("status") -> {
                            columnIndices["status"] = i
                            Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'status' по содержимому")
                        }
                        // Длинный текст - вероятно описание
                        cellValue.length > 20 && !columnIndices.containsKey("description") -> {
                            columnIndices["description"] = i
                            Timber.d("АЛЬФА-ИМПОРТ: Колонка $i определена как 'description' по содержимому")
                        }
                    }
                }
            }
            
            // Если это выписка Альфа-Банка, и мы знаем её структуру
            if (columnIndices.isEmpty() || (!columnIndices.containsKey("date") && !columnIndices.containsKey("amount"))) {
                Timber.d("АЛЬФА-ИМПОРТ: Не удалось определить колонки, используем стандартную структуру Альфа-Банка")
                
                // Используем типичную структуру выписки Альфа
                if (headerRow.lastCellNum >= 6) {
                    columnIndices["date"] = 0  // Первая колонка - дата операции
                    columnIndices["date2"] = 1 // Вторая колонка - дата проводки
                    columnIndices["code"] = 2  // Третья колонка - код операции
                    columnIndices["category"] = 3 // Четвертая колонка - категория
                    columnIndices["description"] = 4 // Пятая колонка - описание
                    columnIndices["amount"] = 5 // Шестая колонка - сумма
                    
                    if (headerRow.lastCellNum >= 7) {
                        columnIndices["status"] = 6 // Седьмая колонка - статус
                    }
                    
                    Timber.d("АЛЬФА-ИМПОРТ: Установлены стандартные индексы колонок Альфа-Банка")
                }
            }
        }
        
        Timber.d("АЛЬФА-ИМПОРТ: Итоговые индексы колонок: $columnIndices")
        return columnIndices
    }
    
    /**
     * Проверяет, нужно ли пропустить строку в Excel.
     */
    private fun shouldSkipExcelRow(row: Row): Boolean {
        // Получаем содержимое первой непустой ячейки
        val firstCell = (0 until row.lastCellNum)
            .mapNotNull { row.getCell(it)?.toString() }
            .firstOrNull { it.isNotBlank() }
            ?: return true // Пропускаем полностью пустые строки
        
        // Пропускаем строки с итогами и прочими служебными данными
        val skip = firstCell.contains("Итого") || 
               firstCell.contains("Остаток") ||
               firstCell.contains("Оборот") || 
               firstCell.contains("Сумма")
               
        if (skip) {
            Timber.d("АЛЬФА-ИМПОРТ: Пропускаем служебную строку: \"$firstCell\"")
        }
        
        return skip
    }
    
    /**
     * Парсит строку из Excel в объект транзакции.
     */
    private fun parseExcelRow(row: Row, columnIndices: Map<String, Int>): Transaction {
        // Получаем дату
        val dateCell = columnIndices["date"]?.let { row.getCell(it) }
        val date = when {
            dateCell != null -> {
                try {
                    when (dateCell.cellType) {
                        CellType.NUMERIC -> {
                            if (DateUtil.isCellDateFormatted(dateCell)) {
                                dateCell.dateCellValue
                            } else {
                                // Если это числовая дата, но не в формате даты Excel
                                parseDate(dateCell.toString())
                            }
                        }
                        else -> parseDate(dateCell.toString())
                    }
                } catch (e: Exception) {
                    Timber.w(e, "АЛЬФА-ИМПОРТ: Ошибка парсинга даты: $dateCell")
                    Date() // Если не удалось распарсить, используем текущую дату
                }
            }
            else -> Date() // Если нет ячейки с датой, используем текущую дату
        }
        
        // Получаем сумму и определяем тип транзакции
        val amountCell = columnIndices["amount"]?.let { row.getCell(it) }
        val amountString = amountCell?.toString() ?: "0"
        val cleanAmount = amountString.replace("\\s".toRegex(), "")
            .replace(",", ".")
            .replace("[^\\d.-]".toRegex(), "")
        val amountValue = cleanAmount.toDoubleOrNull() ?: 0.0
        
        // Создаем объект Money
        val amount = Money(amountValue.absoluteValue)
        
        // Считаем расходом, если сумма отрицательная или содержит минус
        val isExpense = amountValue < 0 || amountString.contains("-")
        
        // Получаем описание
        val descriptionCell = columnIndices["description"]?.let { row.getCell(it) }
        val description = descriptionCell?.toString() ?: ""
        
        // Получаем категорию или определяем ее из описания
        val categoryCell = columnIndices["category"]?.let { row.getCell(it) }
        val category = if (categoryCell != null && categoryCell.toString().isNotBlank()) {
            categoryCell.toString()
        } else {
            inferCategoryFromDescription(description, isExpense)
        }
        
        // Проверяем, является ли это переводом
        val isTransfer = category == "Переводы" || description.lowercase().contains("перевод")
        
        // Создаем транзакцию
        val transaction = Transaction(
            id = "alfabank_${date.time}_${System.nanoTime()}",
            amount = amount,
            category = category,
            date = date,
            isExpense = isExpense,
            note = description,
            source = "Альфа-Банк",
            sourceColor = ColorUtils.getSourceColorByName(bankName)?.toArgb() ?: (if (isTransfer) TransferColorInt else
                if (isExpense) ExpenseColorInt else IncomeColorInt),
            isTransfer = isTransfer
        )
        
        return transaction
    }

    /**
     * Пропускает заголовки в выписке Альфа-Банка.
     */
    override fun skipHeaders(reader: BufferedReader) {
        var headerFound = false
        var linesSkipped = 0

        while (!headerFound && linesSkipped < 15) {
            val line = reader.readLine() ?: break
            linesSkipped++

            headerFound = line.contains("Дата операции") ||
                    line.contains("Дата,") ||
                    (line.contains("Сумма") &&
                            (line.contains("Описание") || line.contains("Категория")))
        }
    }

    /**
     * Парсит строку из выписки Альфа-Банка в объект Transaction.
     */
    override fun parseLine(line: String): Transaction {
        // Разбиваем строку на поля, учитывая потенциальные разделители
        val parts = if (line.contains(";")) {
            line.split(";")
        } else if (line.contains(",") && line.count { it == ',' } > 1) {
            line.split(",")
        } else {
            line.split("\t")
        }.map { it.trim() }

        if (parts.size < 3) {
            throw IllegalArgumentException("Недостаточно полей для парсинга транзакции")
        }

        // Определяем индексы
        val dateIndex = findDateIndex(parts)
        val amountIndex = findAmountIndex(parts)
        val descriptionIndex = findDescriptionIndex(parts)
        val categoryIndex = findCategoryIndex(parts)

        if (dateIndex == -1 || amountIndex == -1) {
            throw IllegalArgumentException("Не удалось определить основные поля")
        }

        // Парсим дату
        val date = parseDate(parts[dateIndex])

        // Парсим сумму и определяем тип транзакции
        val amountString = parts[amountIndex].replace("\\s".toRegex(), "")
            .replace(",", ".").replace("[^\\d.-]".toRegex(), "")
        val amountValue = amountString.toDoubleOrNull() ?: 0.0
        val isExpense = amountValue < 0 ||
                parts.any {
                    it.contains("списание", ignoreCase = true) ||
                            it.contains("расход", ignoreCase = true)
                }

        // Создаем объект Money с положительным значением
        val amount = Money(amountValue.absoluteValue)

        // Определяем описание
        val description = if (descriptionIndex != -1 && descriptionIndex < parts.size) {
            parts[descriptionIndex]
        } else {
            ""
        }

        // Определяем категорию из выписки или инферим из описания
        val category = if (categoryIndex != -1 && categoryIndex < parts.size && parts[categoryIndex].isNotBlank()) {
            parts[categoryIndex]
        } else {
            inferCategoryFromDescription(description, isExpense)
        }

        // Проверяем, является ли это переводом
        val isTransfer = category == "Переводы" || description.lowercase().contains("перевод")
        
        // Создаем транзакцию
        val transaction = Transaction(
            id = "alfabank_${date.time}_${System.nanoTime()}",
            amount = amount,
            category = category,
            date = date,
            isExpense = isExpense,
            note = description,
            source = "Альфа-Банк",
            sourceColor = ColorUtils.getSourceColorByName(bankName)?.toArgb() ?: (if (isTransfer) TransferColorInt else
                if (isExpense) ExpenseColorInt else IncomeColorInt),
            isTransfer = isTransfer
        )

        return transaction
    }

    /**
     * Парсит дату из строки в различных форматах.
     */
    private fun parseDate(dateString: String): Date {
        for (format in dateFormats) {
            try {
                return format.parse(dateString) ?: continue
            } catch (_: Exception) {
                // Пробуем следующий формат
            }
        }
        return Date() // Если не удалось распарсить, возвращаем текущую дату
    }

    /**
     * Находит индекс поля с датой.
     */
    private fun findDateIndex(parts: List<String>): Int {
        // Сначала ищем по заголовку "Дата операции"
        val dateHeaderIndex = parts.indexOfFirst { part ->
            part.equals("Дата операции", ignoreCase = true) || 
            part.equals("Дата", ignoreCase = true)
        }
        
        if (dateHeaderIndex != -1) return dateHeaderIndex
        
        // Если заголовок не найден, используем старый метод
        return parts.indexOfFirst { part ->
            part.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}.*")) ||
                    part.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))
        }
    }

    /**
     * Находит индекс поля с суммой.
     */
    private fun findAmountIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            val normalized = part.replace("\\s".toRegex(), "")
            normalized.contains(Regex("-?\\d+[,.][\\d]+")) &&
                    (normalized.contains("₽") || normalized.contains("RUB") ||
                            normalized.contains("руб") || !normalized.contains("[a-zA-Zа-яА-Я]".toRegex()))
        }
    }

    /**
     * Находит индекс поля с описанием операции.
     */
    private fun findDescriptionIndex(parts: List<String>): Int {
        // Сначала проверяем по заголовкам
        val descIndex = parts.indexOfFirst { part ->
            part.equals("Описание", ignoreCase = true) || 
            part.equals("Примечание", ignoreCase = true)
        }
        
        if (descIndex != -1) return descIndex
        
        // Если не нашли по заголовку, используем эвристику
        return parts.indexOfFirst { part ->
            part.length > 5 &&
                    !part.contains(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) && // Не содержит дату
                    !part.contains(Regex("\\d+[,.]\\d+")) && // Не содержит сумму
                    part.isNotBlank()
        }
    }
    
    /**
     * Находит индекс поля с категорией.
     */
    private fun findCategoryIndex(parts: List<String>): Int {
        return parts.indexOfFirst { part ->
            part.equals("Категория", ignoreCase = true)
        }
    }

    /**
     * Определяет категорию на основе описания транзакции.
     */
    private fun inferCategoryFromDescription(description: String, isExpense: Boolean): String {
        val lowercaseDesc = description.lowercase()

        return when {
            lowercaseDesc.contains("перевод") -> "Переводы"
            lowercaseDesc.contains("зарплата") || lowercaseDesc.contains("аванс") -> "Зарплата"
            lowercaseDesc.contains("кафе") || lowercaseDesc.contains("ресторан") -> "Рестораны"
            lowercaseDesc.contains("такси") || lowercaseDesc.contains("метро") ||
                    lowercaseDesc.contains("автобус") -> "Транспорт"

            lowercaseDesc.contains("аптека") || lowercaseDesc.contains("клиника") -> "Здоровье"
            lowercaseDesc.contains("супермаркет") || lowercaseDesc.contains("продукты") ||
                    lowercaseDesc.contains("магазин") -> "Продукты"

            lowercaseDesc.contains("жкх") || lowercaseDesc.contains("коммунал") -> "Коммунальные платежи"
            lowercaseDesc.contains("мтс") || lowercaseDesc.contains("билайн") ||
                    lowercaseDesc.contains("мегафон") -> "Связь"

            lowercaseDesc.contains("одежда") || lowercaseDesc.contains("обувь") -> "Одежда"
            else -> if (isExpense) "Другое" else "Доход"
        }
    }

    /**
     * Проверяет, пропускать ли строку.
     */
    override fun shouldSkipLine(line: String): Boolean {
        return line.isBlank() ||
                line.startsWith("Дата") || // Заголовок таблицы
                line.contains("Итого:") ||
                line.contains("Остаток:") ||
                line.contains("Оборот:")
    }
} 