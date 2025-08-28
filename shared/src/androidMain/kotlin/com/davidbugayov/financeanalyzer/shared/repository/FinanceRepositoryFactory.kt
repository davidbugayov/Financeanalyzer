package com.davidbugayov.financeanalyzer.shared.repository

/**
 * Android-specific фабрика для создания FinanceRepository.
 * Создает простую заглушку для начальной интеграции.
 */
actual object FinanceRepositoryFactory {

    actual fun create(): FinanceRepository {
        // Пока возвращаем простую заглушку
        // Полная реализация с domain репозиториями будет добавлена позже
        return AndroidFinanceRepository()
    }
}
