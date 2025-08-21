package com.davidbugayov.financeanalyzer.domain.repository

import android.content.Context
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider

// Избегаем зависимости на UI-модуль: будем получать строки по имени из пакета UI
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.context.GlobalContext
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

    // Предустановленные достижения
    private val rp: ResourceProvider = GlobalContext.get().get()

    // Получение строки из UI-модуля по имени ресурса, чтобы не тащить зависимость на :ui
    private fun uiString(name: String): String {
        return try {
            val resId = context.resources.getIdentifier(name, "string", "com.davidbugayov.financeanalyzer.ui")
            if (resId != 0) context.getString(resId) else name
        } catch (e: Exception) {
            Timber.w(e, "uiString lookup failed for %s", name)
            name
        }
    }

    private val defaultAchievements = listOf(
        Achievement(
            id = "first_transaction",
            title = uiString("achievement_first_steps"),
            description = uiString("achievement_first_steps_desc"),
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 10,
        ),
        Achievement(
            id = "transaction_master",
            title = uiString("achievement_transaction_master"),
            description = uiString("achievement_transaction_master_desc"),
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 100,
            rewardCoins = 50,
        ),
        Achievement(
            id = "data_analyst",
            title = uiString("achievement_data_analyst"),
            description = uiString("achievement_data_analyst_desc"),
            iconRes = 0,
            category = AchievementCategory.STATISTICS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 20,
        ),
        Achievement(
            id = "first_budget",
            title = uiString("achievement_first_budget"),
            description = uiString("achievement_first_budget_desc"),
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "app_explorer",
            title = uiString("achievement_explorer"),
            description = uiString("achievement_explorer_desc"),
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 5,
            rewardCoins = 30,
        ),
        Achievement(
            id = "category_organizer",
            title = uiString("achievement_category_organizer"),
            description = uiString("achievement_category_organizer_desc"),
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 25,
        ),
        Achievement(
            id = "early_bird",
            title = uiString("achievement_early_bird"),
            description = uiString("achievement_early_bird_desc"),
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "night_owl",
            title = uiString("achievement_night_owl"),
            description = uiString("achievement_night_owl_desc"),
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "first_savings",
            title = uiString("achievement_first_savings"),
            description = uiString("achievement_first_savings_desc"),
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "emergency_fund",
            title = uiString("achievement_emergency_fund"),
            description = uiString("achievement_emergency_fund_desc"),
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 100,
        ),
        Achievement(
            id = "economical",
            title = uiString("achievement_economical"),
            description = uiString("achievement_economical_desc"),
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30,
        ),
        Achievement(
            id = "regular_user",
            title = uiString("achievement_regular_user"),
            description = uiString("achievement_regular_user_desc"),
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 7,
            rewardCoins = 25,
        ),
        Achievement(
            id = "loyal_user",
            title = uiString("achievement_loyal_user"),
            description = uiString("achievement_loyal_user_desc"),
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.RARE,
            targetProgress = 30,
            rewardCoins = 50,
        ),
        Achievement(
            id = "category_expert",
            title = uiString("achievement_category_expert"),
            description = uiString("achievement_category_expert_desc"),
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 40,
        ),
        Achievement(
            id = "tinkoff_integrator",
            title = uiString("achievement_tinkoff_integrator"),
            description = uiString("achievement_tinkoff_integrator_desc"),
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "sber_collector",
            title = uiString("achievement_sber_collector"),
            description = uiString("achievement_sber_collector_desc"),
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "alpha_analyst",
            title = uiString("achievement_alpha_analyst"),
            description = uiString("achievement_alpha_analyst_desc"),
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "ozon_collector",
            title = uiString("achievement_ozon_collector"),
            description = uiString("achievement_ozon_collector_desc"),
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20,
        ),
        Achievement(
            id = "multi_bank_collector",
            title = uiString("achievement_multi_bank_collector"),
            description = uiString("achievement_multi_bank_collector_desc"),
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.EPIC,
            targetProgress = 1,
            rewardCoins = 100,
        ),
        Achievement(
            id = "export_master",
            title = uiString("achievement_export_master"),
            description = uiString("achievement_export_master_desc"),
            iconRes = 0,
            category = AchievementCategory.EXPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
        ),
        Achievement(
            id = "backup_enthusiast",
            title = uiString("achievement_backup_enthusiast"),
            description = uiString("achievement_backup_enthusiast_desc"),
            iconRes = 0,
            category = AchievementCategory.EXPORT,
            rarity = AchievementRarity.RARE,
            targetProgress = 5,
            rewardCoins = 50,
        ),
        Achievement(
            id = "csv_importer",
            title = uiString("achievement_csv_importer"),
            description = uiString("achievement_csv_importer_desc"),
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15,
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
                // Пусть UI-слой инициализирует локализованными строками
                emptyList()
            } else {
                // Загружаем прогресс для каждого достижения отдельно, базируясь на текущем списке по умолчанию
                defaultAchievements.map { achievement ->
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
            Timber.e(e, rp.getStringByName("log_error_loading_achievements"))
            emptyList()
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
            Timber.e(e, rp.getStringByName("log_error_saving_achievements"))
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
