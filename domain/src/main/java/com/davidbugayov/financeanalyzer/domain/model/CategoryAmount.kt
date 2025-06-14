package com.davidbugayov.financeanalyzer.domain.model

import java.math.BigDecimal

/**
 * Класс для хранения информации о категории и сумме транзакций
 */
data class CategoryAmount(
    val category: Category,
    val amount: BigDecimal,
    val percentage: Float
) 