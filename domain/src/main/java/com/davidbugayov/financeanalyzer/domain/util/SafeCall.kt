package com.davidbugayov.financeanalyzer.domain.util

import com.davidbugayov.financeanalyzer.core.util.safeCall as coreSafeCall
import com.davidbugayov.financeanalyzer.core.util.safeCallSync as coreSafeCallSync

/**
 * Реэкспорт функции safeCall из core модуля для обратной совместимости
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> = coreSafeCall(block)

/**
 * Реэкспорт функции safeCallSync из core модуля для обратной совместимости
 */
fun <T> safeCallSync(block: () -> T): Result<T> = coreSafeCallSync(block)
