package com.davidbugayov.financeanalyzer.presentation.transaction.validation

/**
 * Результат валидации данных ввода
 * @param isValid Флаг, указывающий, прошла ли валидация успешно
 * @param hasAmountError Флаг, указывающий на ошибку в поле суммы
 * @param hasCategoryError Флаг, указывающий на ошибку в поле категории
 * @param hasSourceError Флаг, указывающий на ошибку в поле источника
 * @param hasWalletError Флаг, указывающий на ошибку в поле кошелька
 */
data class ValidationResult(
    val isValid: Boolean,
    val hasAmountError: Boolean = false,
    val hasCategoryError: Boolean = false,
    val hasSourceError: Boolean = false,
    val hasWalletError: Boolean = false
) 