package com.davidbugayov.financeanalyzer.domain.util

import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult

/**
 * Реэкспорт класса Result из core модуля для обратной совместимости
 */
typealias Result<T> = CoreResult<T>
