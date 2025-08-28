package com.davidbugayov.financeanalyzer.domain.usecase.debt

import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.OverdueCheckResult
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository

/**
 * UseCase: проверка просроченных долгов и обновление их статуса.
 */
class CheckOverdueDebtsUseCase(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(): OverdueCheckResult {
        val allDebts = repository.getAllDebts()
        val now = System.currentTimeMillis()

        val activeDebtsWithDueDate = allDebts.filter {
            it.status == DebtStatus.ACTIVE && it.dueAt != null
        }

        val overdueDebts = mutableListOf<Debt>()
        var updatedCount = 0

        for (debt in activeDebtsWithDueDate) {
            if (debt.dueAt!! < now) {
                // Долг просрочен
                repository.setDebtStatus(debt.id, DebtStatus.OVERDUE)
                overdueDebts.add(debt)
                updatedCount++
            }
        }

        return OverdueCheckResult(
            overdueDebts = overdueDebts,
            updatedCount = updatedCount,
        )
    }
}
