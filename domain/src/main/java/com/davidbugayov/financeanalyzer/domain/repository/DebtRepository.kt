package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.DebtType

/**
 * Репозиторий долгов: CRUD-операции и бизнес-операции (погашение, изменение статуса).
 */
interface DebtRepository {
    /** Создать долг и вернуть его id. */
    suspend fun addDebt(debt: Debt): String

    /** Обновить долг. */
    suspend fun updateDebt(debt: Debt)

    /** Удалить долг по id. */
    suspend fun deleteDebt(id: String)

    /** Получить долг по id. */
    suspend fun getDebtById(id: String): Debt?

    /** Список всех долгов. */
    suspend fun getAllDebts(): List<Debt>

    /** Список долгов по типу. */
    suspend fun getDebtsByType(type: DebtType): List<Debt>

    /** Установить статус долга. */
    suspend fun setDebtStatus(id: String, status: DebtStatus)

    /** Частичное погашение долга на сумму amount. Возвращает обновленный остаток. */
    suspend fun repayPart(id: String, amount: Money): Money
}


