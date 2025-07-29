package com.davidbugayov.financeanalyzer.domain.model

/**
 * Категории достижений для группировки и фильтрации
 */
enum class AchievementCategory {
    TRANSACTIONS,    // Транзакции
    BUDGET,         // Бюджетирование  
    SAVINGS,        // Накопления
    HABITS,         // Привычки
    STATISTICS,     // Статистика
    MILESTONES,     // Важные вехи
    SPECIAL,        // Специальные события
    IMPORT,         // Импорт данных
    EXPORT          // Экспорт данных
}

/**
 * Редкость достижения влияет на дизайн и награды
 */
enum class AchievementRarity {
    COMMON,         // Обычное (серебро)
    RARE,           // Редкое (золото)
    EPIC,           // Эпическое (фиолетовое)
    LEGENDARY       // Легендарное (радужное)
}

/**
 * Модель достижения для системы геймификации
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val category: AchievementCategory,
    val rarity: AchievementRarity = AchievementRarity.COMMON,
    val isUnlocked: Boolean = false,
    val dateUnlocked: Long? = null,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val rewardCoins: Int = 10,  // Виртуальная валюта за достижение
    val isHidden: Boolean = false,  // Скрытые достижения (показываются только после разблокировки)
) {
    /**
     * Вычисляет прогресс достижения в процентах (0.0 - 1.0)
     */
    val progressPercentage: Float
        get() = if (targetProgress > 0) {
            (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
        } else 1f

    /**
     * Проверяет готово ли достижение к разблокировке
     */
    val isReadyToUnlock: Boolean
        get() = !isUnlocked && currentProgress >= targetProgress
} 