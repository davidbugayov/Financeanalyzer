package com.davidbugayov.financeanalyzer.domain.usecase.debt

import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository

/**
 * UseCase: получение долгов.
 */
class GetDebtsUseCase(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(): List<Debt> = repository.getAllDebts()
    suspend fun byType(type: DebtType): List<Debt> = repository.getDebtsByType(type)
}


