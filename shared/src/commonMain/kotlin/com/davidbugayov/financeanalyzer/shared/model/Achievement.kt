package com.davidbugayov.financeanalyzer.shared.model

data class Achievement(
    val id: String,
    val title: String = "",
    val description: String = "",
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val dateUnlocked: Long? = null,
)


