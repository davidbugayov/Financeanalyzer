package com.davidbugayov.financeanalyzer.domain.repository

import android.content.Context
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import com.davidbugayov.financeanalyzer.domain.util.StringProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
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
    
    private val prefs = context.applicationContext.getSharedPreferences("achievements", android.content.Context.MODE_PRIVATE)
    
    // Предустановленные достижения
    private val defaultAchievements = listOf(
        Achievement(
            id = "first_transaction",
            title = StringProvider.achievementFirstSteps,
            description = StringProvider.achievementFirstStepsDesc,
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 10
        ),
        Achievement(
            id = "transaction_master",
            title = StringProvider.achievementTransactionMaster,
            description = StringProvider.achievementTransactionMasterDesc,
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 100,
            rewardCoins = 50
        ),
        Achievement(
            id = "data_analyst",
            title = StringProvider.achievementDataAnalyst,
            description = StringProvider.achievementDataAnalystDesc,
            iconRes = 0,
            category = AchievementCategory.STATISTICS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 20
        ),
        Achievement(
            id = "first_budget",
            title = StringProvider.achievementFirstBudget,
            description = StringProvider.achievementFirstBudgetDesc,
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "app_explorer",
            title = StringProvider.achievementExplorer,
            description = StringProvider.achievementExplorerDesc,
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 5,
            rewardCoins = 30
        ),
        Achievement(
            id = "category_organizer",
            title = StringProvider.achievementCategoryOrganizer,
            description = StringProvider.achievementCategoryOrganizerDesc,
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 25
        ),
        Achievement(
            id = "early_bird",
            title = StringProvider.achievementEarlyBird,
            description = StringProvider.achievementEarlyBirdDesc,
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "night_owl",
            title = StringProvider.achievementNightOwl,
            description = StringProvider.achievementNightOwlDesc,
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "first_savings",
            title = StringProvider.achievementFirstSavings,
            description = StringProvider.achievementFirstSavingsDesc,
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "emergency_fund",
            title = StringProvider.achievementEmergencyFund,
            description = StringProvider.achievementEmergencyFundDesc,
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 100
        ),
        Achievement(
            id = "economical",
            title = StringProvider.achievementEconomical,
            description = StringProvider.achievementEconomicalDesc,
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "regular_user",
            title = StringProvider.achievementRegularUser,
            description = StringProvider.achievementRegularUserDesc,
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 7,
            rewardCoins = 25
        ),
        Achievement(
            id = "loyal_user",
            title = StringProvider.achievementLoyalUser,
            description = StringProvider.achievementLoyalUserDesc,
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.RARE,
            targetProgress = 30,
            rewardCoins = 50
        ),
        Achievement(
            id = "category_expert",
            title = StringProvider.achievementCategoryExpert,
            description = StringProvider.achievementCategoryExpertDesc,
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 40
        ),
        Achievement(
            id = "tinkoff_integrator",
            title = StringProvider.achievementTinkoffIntegrator,
            description = StringProvider.achievementTinkoffIntegratorDesc,
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "sber_collector",
            title = StringProvider.achievementSberCollector,
            description = StringProvider.achievementSberCollectorDesc,
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "alpha_analyst",
            title = StringProvider.achievementAlphaAnalyst,
            description = StringProvider.achievementAlphaAnalystDesc,
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "ozon_collector",
            title = StringProvider.achievementOzonCollector,
            description = StringProvider.achievementOzonCollectorDesc,
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "multi_bank_collector",
            title = StringProvider.achievementMultiBankCollector,
            description = StringProvider.achievementMultiBankCollectorDesc,
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.EPIC,
            targetProgress = 1,
            rewardCoins = 100
        ),
        Achievement(
            id = "export_master",
            title = StringProvider.achievementExportMaster,
            description = StringProvider.achievementExportMasterDesc,
            iconRes = 0,
            category = AchievementCategory.EXPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "backup_enthusiast",
            title = StringProvider.achievementBackupEnthusiast,
            description = StringProvider.achievementBackupEnthusiastDesc,
            iconRes = 0,
            category = AchievementCategory.EXPORT,
            rarity = AchievementRarity.RARE,
            targetProgress = 5,
            rewardCoins = 50
        ),
        Achievement(
            id = "csv_importer",
            title = StringProvider.achievementCsvImporter,
            description = StringProvider.achievementCsvImporterDesc,
            iconRes = 0,
            category = AchievementCategory.IMPORT,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        )
    )

    private val _achievements = MutableStateFlow(loadAchievements())
    
    /**
     * Загружает достижения из SharedPreferences
     */
    private fun loadAchievements(): List<Achievement> {
        return try {
            // Загружаем прогресс для каждого достижения отдельно
            defaultAchievements.map { achievement ->
                val progress = prefs.getInt("${achievement.id}_progress", 0)
                val isUnlocked = prefs.getBoolean("${achievement.id}_unlocked", false)
                val dateUnlocked = prefs.getLong("${achievement.id}_date", 0L).takeIf { it > 0 }
                
                achievement.copy(
                    currentProgress = progress,
                    isUnlocked = isUnlocked,
                    dateUnlocked = dateUnlocked
                )
            }
        } catch (e: Exception) {
            Timber.e(e, StringProvider.logErrorLoadingAchievements)
            defaultAchievements
        }
    }
    
    /**
     * Сохраняет достижения в SharedPreferences
     */
    private fun saveAchievements(achievements: List<Achievement>) {
        try {
            val editor = prefs.edit()
            achievements.forEach { achievement ->
                editor.putInt("${achievement.id}_progress", achievement.currentProgress)
                editor.putBoolean("${achievement.id}_unlocked", achievement.isUnlocked)
                achievement.dateUnlocked?.let { date ->
                    editor.putLong("${achievement.id}_date", date)
                }
            }
            editor.apply()
            Timber.d("🏆 Достижения сохранены в SharedPreferences")
        } catch (e: Exception) {
            Timber.e(e, StringProvider.logErrorSavingAchievements)
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
        Timber.d("🏆 Обновлено достижение: ${achievement.title} (прогресс: ${achievement.currentProgress}/${achievement.targetProgress})")
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