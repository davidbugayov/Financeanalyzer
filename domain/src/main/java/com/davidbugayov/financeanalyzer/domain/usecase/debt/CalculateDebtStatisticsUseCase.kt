package com.davidbugayov.financeanalyzer.domain.usecase.debt

import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatistics
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository
import com.davidbugayov.financeanalyzer.shared.model.Money

/**
 * UseCase: расчет статистики по долгам.
 */
class CalculateDebtStatisticsUseCase(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(): DebtStatistics {
        val allDebts = repository.getAllDebts()

        val activeDebts = allDebts.filter { it.status == DebtStatus.ACTIVE }
        val paidDebts = allDebts.filter { it.status == DebtStatus.PAID }
        val overdueDebts = allDebts.filter { it.status == DebtStatus.OVERDUE }

        val borrowedDebts = activeDebts.filter { it.type == DebtType.BORROWED }
        val lentDebts = activeDebts.filter { it.type == DebtType.LENT }

        val totalBorrowed = borrowedDebts.sumOf { it.remaining.amount }
        val totalLent = lentDebts.sumOf { it.remaining.amount }
        val netDebt = totalBorrowed - totalLent

        val overdueAmount = overdueDebts.sumOf { it.remaining.amount }

        return DebtStatistics(
            totalActiveDebts = activeDebts.size,
            totalBorrowed = Money.fromMajor(totalBorrowed, com.davidbugayov.financeanalyzer.shared.model.Currency.RUB),
            totalLent = Money.fromMajor(totalLent, com.davidbugayov.financeanalyzer.shared.model.Currency.RUB),
            netDebt = Money.fromMajor(netDebt, com.davidbugayov.financeanalyzer.shared.model.Currency.RUB),
            overdueDebtsCount = overdueDebts.size,
            overdueAmount = Money.fromMajor(overdueAmount, com.davidbugayov.financeanalyzer.shared.model.Currency.RUB),
            paidDebtsCount = paidDebts.size,
            totalDebtsEver = allDebts.size,
        )
    }
}
