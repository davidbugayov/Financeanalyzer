package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.data.local.dao.DebtDao
import com.davidbugayov.financeanalyzer.data.local.entity.DebtEntity
import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository
import java.util.UUID

class DebtRepositoryImpl(
    private val dao: DebtDao,
) : DebtRepository {

    override suspend fun addDebt(debt: Debt): String {
        val id = if (debt.id.isBlank()) UUID.randomUUID().toString() else debt.id
        dao.insert(debt.toEntity(id))
        return id
    }

    override suspend fun updateDebt(debt: Debt) {
        dao.update(debt.toEntity(debt.id))
    }

    override suspend fun deleteDebt(id: String) {
        dao.deleteById(id)
    }

    override suspend fun getDebtById(id: String): Debt? = dao.getById(id)?.toDomain()

    override suspend fun getAllDebts(): List<Debt> = dao.getAll().map { it.toDomain() }

    override suspend fun getDebtsByType(type: DebtType): List<Debt> =
        dao.getByType(type.name).map { it.toDomain() }

    override suspend fun setDebtStatus(id: String, status: DebtStatus) {
        val existing = dao.getById(id) ?: return
        dao.update(existing.copy(status = status.name))
    }

    override suspend fun repayPart(id: String, amount: Money): Money {
        val existing = dao.getById(id) ?: return Money.zero(amount.currency)
        val newRemaining = existing.remaining - amount
        val clamped = if (newRemaining.amount < java.math.BigDecimal.ZERO) Money.zero(newRemaining.currency) else newRemaining
        val newStatus = if (clamped.amount.compareTo(java.math.BigDecimal.ZERO) == 0) DebtStatus.PAID.name else existing.status
        dao.update(existing.copy(remaining = clamped, status = newStatus))
        return clamped
    }
}

private fun Debt.toEntity(forcedId: String): DebtEntity =
    DebtEntity(
        idString = forcedId,
        title = title,
        counterparty = counterparty,
        type = type.name,
        status = status.name,
        principal = principal,
        remaining = remaining,
        createdAt = createdAt,
        dueAt = dueAt,
        note = note,
    )

private fun DebtEntity.toDomain(): Debt =
    Debt(
        id = idString,
        title = title,
        counterparty = counterparty,
        type = DebtType.valueOf(type),
        status = DebtStatus.valueOf(status),
        principal = principal,
        remaining = remaining,
        createdAt = createdAt,
        dueAt = dueAt,
        note = note,
    )


