package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

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
 * Реализация репозитория достижений в памяти
 */
class AchievementsRepositoryImpl : AchievementsRepository {

    // Предустановленные достижения
    private val defaultAchievements = listOf(
        Achievement(
            id = "first_transaction",
            title = "Первые шаги",
            description = "Добавьте первую транзакцию",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 10
        ),
        Achievement(
            id = "week_no_coffee",
            title = "Неделя без кофе на вынос",
            description = "Не тратьте на кофе 7 дней подряд",
            iconRes = 0,
            category = AchievementCategory.HABITS,
            rarity = AchievementRarity.RARE,
            targetProgress = 7,
            rewardCoins = 35
        ),
    )

    private val _achievements = MutableStateFlow(defaultAchievements)
    
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
        _achievements.value = _achievements.value.map { existing ->
            if (existing.id == achievement.id) {
                achievement
            } else {
                existing
            }
        }
    }
    
    override suspend fun unlockAchievement(id: String) {
        _achievements.value = _achievements.value.map { achievement ->
            if (achievement.id == id && !achievement.isUnlocked) {
                achievement.copy(
                    isUnlocked = true,
                    dateUnlocked = System.currentTimeMillis(),
                )
            } else {
                achievement
            }
        }
    }
    
    override suspend fun initializeDefaultAchievements(achievements: List<Achievement>) {
        _achievements.value = achievements
    }
    
    override fun getTotalCoins(): Flow<Int> {
        return _achievements.map { achievements ->
            achievements.filter { it.isUnlocked }.sumOf { it.rewardCoins }
        }
    }
} 