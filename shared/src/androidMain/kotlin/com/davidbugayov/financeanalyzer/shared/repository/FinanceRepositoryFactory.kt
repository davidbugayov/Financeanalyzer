package com.davidbugayov.financeanalyzer.shared.repository

/**
 * Android-specific фабрика для создания FinanceRepository.
 * Создает AndroidFinanceRepository с инъекцией зависимостей.
 */
actual object FinanceRepositoryFactory {

    actual fun create(): FinanceRepository {
        return AndroidFinanceRepository()
    }
}
