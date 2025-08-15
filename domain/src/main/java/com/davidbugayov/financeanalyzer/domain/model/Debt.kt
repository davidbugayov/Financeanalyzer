package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.core.model.Money

/**
 * Доменная модель долга.
 * Позволяет учитывать долги как выданные (вам должны), так и взятые (вы должны).
 *
 * @property id Строковый идентификатор долга (UUID)
 * @property title Краткое название долга (берется из ресурсов строк в UI)
 * @property counterparty Контрагент (кому должны или кто должен вам)
 * @property type Тип долга (взятый или выданный)
 * @property status Текущий статус долга
 * @property principal Исходная сумма долга
 * @property remaining Остаток долга к погашению
 * @property createdAt Дата создания (мс с эпохи)
 * @property dueAt Крайний срок оплаты (мс с эпохи), может быть null
 * @property note Примечание, может быть null
 */
data class Debt(
    val id: String,
    val title: String,
    val counterparty: String,
    val type: DebtType,
    val status: DebtStatus,
    val principal: Money,
    val remaining: Money,
    val createdAt: Long,
    val dueAt: Long? = null,
    val note: String? = null,
)

/** Тип долга: вы должны (BORROWED) или вам должны (LENT). */
enum class DebtType { BORROWED, LENT }

/** Статус долга. */
enum class DebtStatus { ACTIVE, PAID, OVERDUE, WRITEOFF }


