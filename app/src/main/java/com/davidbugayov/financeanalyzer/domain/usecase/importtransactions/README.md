# Добавление поддержки нового банка для импорта транзакций

Данная инструкция описывает, как добавить поддержку нового банка для импорта транзакций в приложение
Finance Analyzer.

## Архитектура импорта

Архитектура импорта транзакций построена по принципу разделения ответственности и использует
паттерны "Стратегия", "Фабрика" и "Цепочка обязанностей":

1. **Детекторы форматов файлов** (`FileDetector`) - определяют тип файла (PDF, CSV, Excel)
2. **Обработчики банков** (`BankHandler`) - определяют, к какому банку относится файл и создают
   соответствующий импортер
3. **Импортеры транзакций** (`ImportTransactionsUseCase`) - выполняют фактический парсинг и импорт
   транзакций

Добавление нового банка требует реализации нескольких компонентов:

1. **UseCase для импорта** - класс, который наследуется от `BankImportUseCase` и реализует логику
   парсинга выписки конкретного банка
2. **Обработчик банка** - класс, который наследуется от `AbstractBankHandler` и определяет, может ли
   данный банк обработать файл

## Шаги для добавления нового банка

### 1. Создание класса для импорта транзакций

Создайте класс, который наследуется от `BankImportUseCase`:

```kotlin
package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions

import android.content.Context
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.io.BufferedReader

class NewBankImportUseCase(
    repository: TransactionRepositoryImpl,
    context: Context
) : BankImportUseCase(repository, context) {

    override val bankName: String = "Название банка"

    override fun isValidFormat(reader: BufferedReader): Boolean {
        // Проверка формата файла выписки
        // Возвращает true, если формат соответствует данному банку
    }

    override fun skipHeaders(reader: BufferedReader) {
        // Пропуск заголовков в файле выписки
        // Например, reader.readLine() для пропуска строк
    }

    override fun parseLine(line: String): Transaction {
        // Парсинг строки выписки и преобразование в объект Transaction
        // Анализ полей и разбор даты, суммы, описания и т.д.
    }

    override fun shouldSkipLine(line: String): Boolean {
        // Проверка, нужно ли пропустить строку (например, итоговые суммы)
        // По умолчанию пропускаются только пустые строки
        return super.shouldSkipLine(line) || line.contains("Итого")
    }
}
```

### 2. Создание обработчика банка

Создайте класс, который наследуется от `AbstractBankHandler` в пакете `handlers`:

```kotlin
package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers

import android.content.Context
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.NewBankImportUseCase

class NewBankHandler(
    repository: TransactionRepositoryImpl,
    context: Context
) : AbstractBankHandler(repository, context) {

    override val bankName: String = "Название банка"

    // Укажите поддерживаемые форматы (PDF, CSV, Excel)
    override fun supportsPDF(): Boolean = true

    // или
    override fun supportsCSV(): Boolean = true

    // или
    override fun supportsExcel(): Boolean = true

    override fun createImporter(): ImportTransactionsUseCase {
        return NewBankImportUseCase(repository, context)
    }

    override fun getFileNameKeywords(): List<String> {
        return listOf("название_банка", "bank_name")
    }

    // Переопределите этот метод, если нужна специальная логика определения банка
    override fun canHandle(fileName: String, fileContent: String): Boolean {
        // Проверка по имени файла
        if (super.canHandle(fileName, fileContent)) {
            return true
        }

        // Проверка по содержимому файла (для PDF)
        return fileContent.contains("Специфичный текст банка", ignoreCase = true)
    }
}
```

### 3. Регистрация обработчика в фабрике

Добавьте регистрацию обработчика в методе `registerBankHandlers()` класса `ImportFactory`:

```kotlin
private fun registerBankHandlers() {
    // ... существующие обработчики

    // Добавление нового обработчика
    bankHandlers.add(
        com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.NewBankHandler(
            repository, context
        )
    )
}
```

## Тестирование

После добавления нового банка, рекомендуется протестировать импорт на нескольких образцах выписок:

1. Убедитесь, что `ImportFactory` правильно определяет банк по имени и содержимому файла
2. Проверьте, что транзакции корректно импортируются с правильными датами, суммами и описаниями
3. Протестируйте граничные случаи (пустые строки, специальные символы в описаниях, разные форматы
   дат)

## Пример расширения для поддержки новых форматов файлов

Если требуется добавить поддержку нового формата файлов (не PDF, CSV или Excel), необходимо:

1. Добавить новый тип файла в перечисление `FileType`
2. Создать новую реализацию интерфейса `FileDetector`
3. Зарегистрировать детектор в методе `registerFileDetectors()` класса `ImportFactory`
4. Добавить обработку нового типа файла в методе `createImporter()` класса `ImportFactory`