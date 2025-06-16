package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.core.model.Money as CoreMoney
import com.davidbugayov.financeanalyzer.core.model.Currency as CoreCurrency
import com.davidbugayov.financeanalyzer.core.model.SymbolPosition as CoreSymbolPosition

/**
 * Реэкспорт классов из core модуля для обратной совместимости
 */
typealias Money = CoreMoney
typealias Currency = CoreCurrency
typealias SymbolPosition = CoreSymbolPosition 