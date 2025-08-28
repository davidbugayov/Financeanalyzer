package com.davidbugayov.financeanalyzer.domain.usecase.debt

import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository

/**
 * UseCase: получение долгов с фильтрацией.
 */
class GetFilteredDebtsUseCase(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(
        type: DebtType? = null,
        status: DebtStatus? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        searchQuery: String? = null,
    ): List<Debt> {
        val allDebts = repository.getAllDebts()

        return allDebts.filter { debt ->
            // Фильтр по типу долга
            if (type != null && debt.type != type) return@filter false

            // Фильтр по статусу
            if (status != null && debt.status != status) return@filter false

            // Фильтр по сумме
            val remainingAmount = debt.remaining.amount.toDouble()
            if (minAmount != null && remainingAmount < minAmount) return@filter false
            if (maxAmount != null && remainingAmount > maxAmount) return@filter false

            // Поиск по названию или контрагенту
            if (searchQuery != null && searchQuery.isNotBlank()) {
                val query = searchQuery.lowercase()
                val matchesTitle = debt.title.lowercase().contains(query)
                val matchesCounterparty = debt.counterparty.lowercase().contains(query)
                if (!matchesTitle && !matchesCounterparty) return@filter false
            }

            true
        }.sortedWith(
            compareBy<Debt> { it.status != DebtStatus.ACTIVE } // Активные долги в начале
                .thenBy { it.dueAt } // По сроку погашения
                .thenByDescending { it.principal.amount } // По сумме (большие суммы выше)
        )
    }
}
