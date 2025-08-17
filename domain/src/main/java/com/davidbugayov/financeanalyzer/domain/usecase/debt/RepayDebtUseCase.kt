package com.davidbugayov.financeanalyzer.domain.usecase.debt

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository

/**
 * UseCase: частичное/полное погашение долга на сумму amount.
 */
class RepayDebtUseCase(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(id: String, amount: Money): Money = repository.repayPart(id, amount)
}


