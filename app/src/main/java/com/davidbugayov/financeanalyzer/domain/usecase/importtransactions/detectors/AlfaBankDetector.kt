package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.detectors

/**
 * Детектор для выписок Альфа-Банка.
 * Определяет, является ли Excel файл выпиской Альфа-Банка.
 */
class AlfaBankDetector : BankDetector {

    override val bankName: String = "Альфа-Банк"

    /**
     * Проверяет, соответствует ли файл формату выписки Альфа-Банка.
     *
     * @param fileName Имя файла
     * @param fileContent Содержимое файла (для Excel файлов может быть пустым)
     * @return true, если файл соответствует формату выписки Альфа-Банка
     */
    override fun detect(fileName: String, fileContent: String): Boolean {
        // Для Excel файлов мы обычно полагаемся на имя файла, так как
        // извлечение содержимого Excel без специальных библиотек затруднительно

        // Проверяем имя файла на совпадение ключевых слов
        val nameMatch = fileName.lowercase().let {
            it.contains("alfa") || it.contains("альфа") ||
                    it.contains("alfabank") || it.contains("альфабанк")
        }

        // Проверяем расширение файла - должен быть Excel (.xls или .xlsx)
        val isExcelFile = fileName.lowercase().endsWith(".xls") ||
                fileName.lowercase().endsWith(".xlsx")

        return nameMatch && isExcelFile
    }
} 