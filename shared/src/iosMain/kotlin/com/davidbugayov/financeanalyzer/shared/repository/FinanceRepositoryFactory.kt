package com.davidbugayov.financeanalyzer.shared.repository

/**
 * iOS-specific фабрика для создания FinanceRepository.
 */
actual object FinanceRepositoryFactory {
    actual fun create(): FinanceRepository {
        return IosFinanceRepository()
    }
}
