package com.davidbugayov.financeanalyzer.domain.usecase.debt

import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository

/**
 * UseCase: создание нового долга.
 * @param repository Репозиторий долгов
 */
class CreateDebtUseCase(
    private val repository: DebtRepository,
) {
    /** Выполняет создание долга и возвращает его id. */
    suspend operator fun invoke(debt: Debt): String = repository.addDebt(debt)
}


