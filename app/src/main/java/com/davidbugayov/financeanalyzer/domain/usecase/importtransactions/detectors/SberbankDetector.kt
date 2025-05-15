package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

/**
 * Детектор для выписок Сбербанка.
 * Определяет, является ли PDF файл выпиской Сбербанка.
 */
class SberbankDetector : BankDetector {

    override val bankName: String = "Сбербанк"

    /**
     * Проверяет, соответствует ли файл формату выписки Сбербанка.
     *
     * @param fileName Имя файла
     * @param fileContent Содержимое файла
     * @return true, если файл соответствует формату выписки Сбербанка
     */
    override fun detect(fileName: String, fileContent: String): Boolean {
        // Проверяем имя файла на совпадение ключевых слов
        val nameMatch = fileName.lowercase().let {
            it.contains("sberbank") || it.contains("сбербанк") || it.contains("сбер")
        }

        // Если имя файла содержит ключевые слова, это может быть выписка Сбербанка
        if (nameMatch) {
            return true
        }

        // Проверяем содержимое файла на характерные признаки выписки Сбербанка
        return fileContent.contains("Сбербанк", ignoreCase = true) ||
                fileContent.contains("Расшифровка операций", ignoreCase = true) ||
                fileContent.contains("СБЕР", ignoreCase = true) ||
                fileContent.contains("ДАТА ОПЕРАЦИИ", ignoreCase = true) ||
                fileContent.contains("Дата обработки", ignoreCase = true) ||
                fileContent.contains("СУММА В ВАЛЮТЕ СЧЁТА", ignoreCase = true)
    }
} 