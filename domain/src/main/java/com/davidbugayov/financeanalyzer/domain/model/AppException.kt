package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.core.model.AppException as CoreAppException

/**
 * Реэкспорт класса AppException из core модуля для обратной совместимости
 */
typealias AppException = CoreAppException

/**
 * Реэкспорт функции mapException из core модуля для обратной совместимости
 */
fun mapException(exception: Throwable): AppException = CoreAppException.mapException(exception) 