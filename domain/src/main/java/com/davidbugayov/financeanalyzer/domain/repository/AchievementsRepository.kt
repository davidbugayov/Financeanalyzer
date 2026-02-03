package com.davidbugayov.financeanalyzer.domain.repository

import android.content.Context
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Репозиторий для управления достижениями
 */
interface AchievementsRepository {

    /**
     * Получает все достижения
     */
    fun getAllAchievements(): Flow<List<Achievement>>

    /**
     * Получает достижение по ID
     */
    fun getAchievementById(id: String): Flow<Achievement?>

    /**
     * Получает достижения по категории
     */
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>>

    /**
     * Получает разблокированные достижения
     */
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    /**
     * Получает заблокированные достижения
     */
    fun getLockedAchievements(): Flow<List<Achievement>>

    /**
     * Обновляет достижение
     */
    suspend fun updateAchievement(achievement: Achievement)

    /**
     * Разблокирует достижение
     */
    suspend fun unlockAchievement(id: String)

    /**
     * Инициализирует достижения по умолчанию
     */
    suspend fun initializeDefaultAchievements(achievements: List<Achievement>)

    /**
     * Получает общее количество монет от разблокированных достижений
     */
    fun getTotalCoins(): Flow<Int>
}

/**
 * Реализация репозитория достижений, использующая SharedPreferences для хранения данных.
 * Предоставляет методы для работы с достижениями пользователя.
 *
 * @param context Контекст приложения для доступа к SharedPreferences.
 */
class AchievementsRepositoryImpl(private val context: Context) : AchievementsRepository {
    private val prefs = context.applicationContext.getSharedPreferences("achievements", Context.MODE_PRIVATE)

    // ...existing code...

    // Ленивая инициализация для получения строк с правильной локалью во время доступа
    private fun getDefaultAchievements(): List<Achievement> = listOf(
        Achievement(
            id = "first_transaction",
            titleResName = "achievement_first_steps_title",
            descriptionResName = "achievement_first_steps_description",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 10,
        ),
        Achievement(
            id = "transaction_master",
            titleResName = "achievement_transaction_master_title",
            descriptionResName = "achievement_transaction_master_desc",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 100,
            rewardCoins = 50,
        ),
        Achievement(
            id = "data_analyst",
            titleResName = "achievement_data_analyst_title",
            descriptionResName = "achievement_data_analyst_desc",
            iconRes = 0,
            category = AchievementCategory.STATISTICS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 20,
        ),
        Achievement(
            id = "first_budget",
            titleResName = "achievement_first_budget_title",
            descriptionResName = "achievement_first_budget_desc",
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "app_explorer",
            titleResName = "achievement_app_explorer_title",
            descriptionResName = "achievement_app_explorer_desc",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 5,
            rewardCoins = 30,
        ),
        Achievement(
            id = "category_organizer",
            titleResName = "achievement_category_organizer_title",
            descriptionResName = "achievement_category_organizer_desc",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 25,
        ),
        Achievement(
            id = "early_bird",
            titleResName = "achievement_early_bird_title",
            descriptionResName = "achievement_early_bird_desc",
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "night_owl",
            titleResName = "achievement_night_owl_title",
            descriptionResName = "achievement_night_owl_desc",
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "first_savings",
            titleResName = "achievement_first_savings_title",
            descriptionResName = "achievement_first_savings_desc",
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "emergency_fund",
            titleResName = "achievement_emergency_fund_title",
            descriptionResName = "achievement_emergency_fund_desc",
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 100,
        ),
        Achievement(
            id = "economical",
            titleResName = "achievement_economical_title",
            descriptionResName = "achievement_economical_desc",
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30,
        ),
        Achievement(
            id = "regular_user",
            titleResName = "achievement_regular_user_title",
            descriptionResName = "achievement_regular_user_desc",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 7,
            rewardCoins = 25,
        ),
        Achievement(
            id = "loyal_user",
            titleResName = "achievement_loyal_user_title",
            descriptionResName = "achievement_loyal_user_desc",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.RARE,
            targetProgress = 30,
            rewardCoins = 50,
        ),
        Achievement(
            id = "category_expert",
            titleResName = "achievement_category_expert_title",
            descriptionResName = "achievement_category_expert_desc",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 40,
        ),
        Achievement(
            id = "tinkoff_integrator",
            titleResName = "achievement_tinkoff_integrator_title",
            descriptionResName = "achievement_tinkoff_integrator_desc",
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "sber_collector",
            titleResName = "achievement_sber_collector_title",
            descriptionResName = "achievement_sber_collector_desc",
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "alpha_analyst",
            titleResName = "achievement_alpha_analyst_title",
            descriptionResName = "achievement_alpha_analyst_desc",
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "ozon_collector",
            titleResName = "achievement_ozon_collector_title",
            descriptionResName = "achievement_ozon_collector_desc",
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "multi_bank_collector",
            titleResName = "achievement_multi_bank_collector_title",
            descriptionResName = "achievement_multi_bank_collector_desc",
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.EPIC,
            targetProgress = 1,
            rewardCoins = 100,
        ),
        Achievement(
            id = "export_master",
            titleResName = "achievement_export_master_title",
            descriptionResName = "achievement_export_master_desc",
            iconRes = 0,
            category = AchievementCategory.EXPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "backup_enthusiast",
            titleResName = "achievement_backup_enthusiast_title",
            descriptionResName = "achievement_backup_enthusiast_desc",
            iconRes = 0,
            category = AchievementCategory.EXPORT,
            rarity = AchievementRarity.RARE,
            targetProgress = 5,
            rewardCoins = 50,
        ),
        Achievement(
            id = "csv_importer",
            titleResName = "achievement_csv_importer_title",
            descriptionResName = "achievement_csv_importer_desc",
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        // Shortcut
        Achievement(
            id = "shortcut_add_transaction",
            titleResName = "achievement_shortcut_add_title",
            descriptionResName = "achievement_shortcut_add_desc",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 10,
        ),
        // Widgets
        Achievement(
            id = "widget_small_added",
            titleResName = "achievement_widget_small_added_title",
            descriptionResName = "achievement_widget_small_added_desc",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 10,
        ),
        Achievement(
            id = "widget_large_added",
            titleResName = "achievement_widget_large_added_title",
            descriptionResName = "achievement_widget_large_added_desc",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 20,
        ),
    )

    private val _achievements = MutableStateFlow(loadAchievements())

    /**
     * Загружает достижения из SharedPreferences
     */
    private fun loadAchievements(): List<Achievement> {
        return try {
            val initialized = prefs.getBoolean("achievements_initialized", false)
            if (!initialized) {
                // Если не инициализированы, возвращаем дефолтные ачивки
                getDefaultAchievements()
            } else {
                // Загружаем прогресс для каждого достижения отдельно, базируясь на текущем списке по умолчанию
                getDefaultAchievements().map { achievement ->
                    val progress = prefs.getInt("${achievement.id}_progress", 0)
                    val isUnlocked = prefs.getBoolean("${achievement.id}_unlocked", false)
                    val dateUnlocked = prefs.getLong("${achievement.id}_date", 0L).takeIf { it > 0 }

                    achievement.copy(
                        currentProgress = progress,
                        isUnlocked = isUnlocked,
                        dateUnlocked = dateUnlocked,
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load achievements")
            getDefaultAchievements()
        }
    }

    /**
     * Сохраняет достижения в SharedPreferences
     */
    private fun saveAchievements(achievements: List<Achievement>) {
        try {
            prefs.edit {
                achievements.forEach { achievement ->
                    putInt("${achievement.id}_progress", achievement.currentProgress)
                    putBoolean("${achievement.id}_unlocked", achievement.isUnlocked)
                    achievement.dateUnlocked?.let { date ->
                        putLong("${achievement.id}_date", date)
                    }
                }
                putBoolean("achievements_initialized", true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save achievements")
        }
    }

    override fun getAllAchievements(): Flow<List<Achievement>> = _achievements.asStateFlow()

    override fun getAchievementById(id: String): Flow<Achievement?> {
        return _achievements.map { achievements ->
            achievements.find { it.id == id }
        }
    }

    override fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>> {
        return _achievements.map { achievements ->
            achievements.filter { it.category == category }
        }
    }

    override fun getUnlockedAchievements(): Flow<List<Achievement>> {
        return _achievements.map { achievements ->
            achievements.filter { it.isUnlocked }
        }
    }

    override fun getLockedAchievements(): Flow<List<Achievement>> {
        return _achievements.map { achievements ->
            achievements.filter { !it.isUnlocked }
        }
    }

    override suspend fun updateAchievement(achievement: Achievement) {
        val updatedList = _achievements.value.map { existing ->
            if (existing.id == achievement.id) {
                achievement
            } else {
                existing
            }
        }
        _achievements.value = updatedList
        saveAchievements(updatedList)
    }

    override suspend fun unlockAchievement(id: String) {
        val updatedList = _achievements.value.map { achievement ->
            if (achievement.id == id && !achievement.isUnlocked) {
                achievement.copy(
                    isUnlocked = true,
                    dateUnlocked = System.currentTimeMillis(),
                    currentProgress = achievement.targetProgress
                )
            } else {
                achievement
            }
        }
        _achievements.value = updatedList
        saveAchievements(updatedList)
    }

    override suspend fun initializeDefaultAchievements(achievements: List<Achievement>) {
        // Эта функция теперь не нужна, так как мы автоматически мержим с дефолтными
        // Но оставляем для совместимости
    }

    override fun getTotalCoins(): Flow<Int> {
        return _achievements.map { achievements ->
            achievements.filter { it.isUnlocked }.sumOf { it.rewardCoins }
        }
    }
}
