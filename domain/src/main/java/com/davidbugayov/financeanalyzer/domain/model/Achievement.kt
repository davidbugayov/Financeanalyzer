package com.davidbugayov.financeanalyzer.domain.model

/**
 * Модель достижения для системы геймификации
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val isUnlocked: Boolean = false,
    val dateUnlocked: Long? = null,
) 