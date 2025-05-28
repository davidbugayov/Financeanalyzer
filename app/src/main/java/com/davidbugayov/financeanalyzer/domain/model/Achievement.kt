package com.davidbugayov.financeanalyzer.domain.model

import androidx.annotation.DrawableRes

/**
 * Модель достижения для системы геймификации
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val isUnlocked: Boolean = false,
    val dateUnlocked: Long? = null
) 