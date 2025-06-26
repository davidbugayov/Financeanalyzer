package com.davidbugayov.financeanalyzer.feature.transaction.validation

/**
 * Вспомогательный класс для сбора и применения ошибок валидации
 */
class ValidationBuilder {

    private var hasAmountError = false
    private var hasCategoryError = false
    private var hasSourceError = false
    private var hasWalletError = false

    /**
     * Добавляет ошибку валидации для поля суммы
     */
    fun addAmountError() {
        hasAmountError = true
    }

    /**
     * Добавляет ошибку валидации для поля категории
     */
    fun addCategoryError() {
        hasCategoryError = true
    }

    /**
     * Добавляет ошибку валидации для поля кошелька
     */
    fun addWalletError() {
        hasWalletError = true
    }

    /**
     * Проверяет, есть ли ошибки валидации
     * @return true, если есть хотя бы одна ошибка
     */
    fun hasErrors(): Boolean = hasAmountError || hasCategoryError || hasSourceError || hasWalletError

    /**
     * Строит результат валидации
     * @return ValidationResult с результатами валидации
     */
    fun build(): ValidationResult {
        return ValidationResult(
            isValid = !hasErrors(),
            hasAmountError = hasAmountError,
            hasCategoryError = hasCategoryError,
            hasSourceError = hasSourceError,
            hasWalletError = hasWalletError,
        )
    }
}
