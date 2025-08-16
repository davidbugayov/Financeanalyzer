package com.davidbugayov.financeanalyzer.shared.model

data class Achievement(
    val id: String,
    val title: String = "",
    val description: String = "",
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val dateUnlocked: Long? = null,
    val category: AchievementCategory = AchievementCategory.GENERAL,
    val rarity: AchievementRarity = AchievementRarity.COMMON,
    val rewardCoins: Int = 0,
    val isHidden: Boolean = false,
)

enum class AchievementCategory {
    GENERAL,
    TRANSACTIONS,
    ANALYTICS,
    BUDGET,
    EXPORT_IMPORT,
    SECURITY,
    SOCIAL,
    SPECIAL
}

enum class AchievementRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}


