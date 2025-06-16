package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.core.util.safeCall
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository

/**
 * Use case для загрузки транзакций из репозитория.
 * Обеспечивает безопасную загрузку данных с обработкой ошибок.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 *
 * @property repository Репозиторий для работы с транзакциями
 */
class LoadTransactionsUseCase(
    private val repository: ITransactionRepository,
) {

    /**
     * Загружает все транзакции из репозитория.
     * Использует safeCall для обработки возможных ошибок.
     *
     * @return Result с списком транзакций в случае успеха или ошибкой в случае неудачи
     */
    suspend operator fun invoke(): CoreResult<List<Transaction>> {
        return safeCall {
            repository.loadTransactions()
        }
    }
}
