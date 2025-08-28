package com.davidbugayov.financeanalyzer.shared.repository

/**
 * Фабрика для создания FinanceRepository в зависимости от платформы.
 * Использует expect/actual паттерн для platform-specific реализаций.
 */
expect object FinanceRepositoryFactory {
    /**
     * Создает экземпляр FinanceRepository для текущей платформы
     */
    fun create(): FinanceRepository
}
