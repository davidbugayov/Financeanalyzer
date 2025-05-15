package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

/**
 * Детектор для выписок Тинькофф Банка (Т-Банк).
 * Определяет, является ли PDF файл выпиской Тинькофф Банка.
 */
class TinkoffDetector : BankDetector {

    override val bankName: String = "Т-Банк"

    /**
     * Проверяет, соответствует ли файл формату выписки Тинькофф Банка.
     *
     * @param fileName Имя файла
     * @param fileContent Содержимое файла
     * @return true, если файл соответствует формату выписки Тинькофф Банка
     */
    override fun detect(fileName: String, fileContent: String): Boolean {
        // Проверяем имя файла на совпадение ключевых слов
        val nameMatch = fileName.lowercase().let {
            it.contains("tinkoff") || it.contains("тинькофф") ||
                    it.contains("т-банк") || it.contains("tbank")
        }

        // Если имя файла содержит ключевые слова, это может быть выписка Тинькофф Банка
        if (nameMatch) {
            return true
        }

        // Проверяем содержимое файла на характерные признаки выписки Тинькофф Банка
        return fileContent.contains("Tinkoff", ignoreCase = true) ||
                fileContent.contains("Тинькофф", ignoreCase = true) ||
                fileContent.contains("Т-Банк", ignoreCase = true) ||
                fileContent.contains("ТБАНК", ignoreCase = true) ||
                fileContent.contains("TBANK.RU", ignoreCase = true) ||
                fileContent.contains("АКЦИОНЕРНОЕ ОБЩЕСТВО «ТБАНК»", ignoreCase = true) ||
                fileContent.contains("Справка о движении средств", ignoreCase = true) ||
                (fileContent.contains("Движение средств за период", ignoreCase = true) &&
                        fileContent.contains("Дата и время операции", ignoreCase = true)) ||
                (fileContent.contains("Внутрибанковский перевод", ignoreCase = true) &&
                        fileContent.contains("с договора", ignoreCase = true))
    }
} 