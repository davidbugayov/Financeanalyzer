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
 * Реализация репозитория достижений с постоянным хранением в SharedPreferences
 */
class AchievementsRepositoryImpl(
    context: android.content.Context
) : AchievementsRepository {

    private val prefs = context.applicationContext.getSharedPreferences("achievements", android.content.Context.MODE_PRIVATE)
    
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
            id = "transaction_master",
            title = "Мастер транзакций",
            description = "Добавьте 100 транзакций",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 100,
            rewardCoins = 50
        ),
        Achievement(
            id = "data_analyst",
            title = "Аналитик данных",
            description = "Просмотрите статистику 10 раз",
            iconRes = 0,
            category = AchievementCategory.STATISTICS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 20
        ),
        Achievement(
            id = "first_budget",
            title = "Первый бюджет",
            description = "Создайте свой первый бюджет",
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "app_explorer",
            title = "Исследователь",
            description = "Посетите все разделы приложения",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 5,
            rewardCoins = 30
        ),
        Achievement(
            id = "category_organizer",
            title = "Организатор категорий",
            description = "Используйте 10 разных категорий",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 25
        ),
        Achievement(
            id = "early_bird",
            title = "Ранняя пташка",
            description = "Добавьте транзакцию до 7 утра",
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "night_owl",
            title = "Ночная сова",
            description = "Добавьте транзакцию после 23:00",
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "first_savings",
            title = "Первая копейка",
            description = "Накопите 1000 рублей",
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "emergency_fund",
            title = "Подушка безопасности",
            description = "Накопите сумму на 3 месяца расходов",
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.LEGENDARY,
            targetProgress = 1,
            rewardCoins = 200
        ),
        Achievement(
            id = "budget_saver",
            title = "Экономный",
            description = "Потратьте менее 80% от бюджета за месяц",
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 40
        ),
        Achievement(
            id = "consistent_user",
            title = "Постоянный пользователь",
            description = "Используйте приложение неделю подряд",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 50
        ),
        Achievement(
            id = "loyal_user",
            title = "Верный пользователь",
            description = "Используйте приложение месяц",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.EPIC,
            targetProgress = 1,
            rewardCoins = 100
        ),
        Achievement(
            id = "category_expert",
            title = "Эксперт категорий",
            description = "Используйте все доступные категории",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 60
        ),
        Achievement(
            id = "safety_first",
            title = "Безопасность прежде всего",
            description = "Создайте резервную копию данных",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 25
        ),
        
        // Достижения для импорта из банков
        Achievement(
            id = "tinkoff_importer",
            title = "Тинькоff-интегратор",
            description = "Импортируйте транзакции из Тинькофф",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "sberbank_importer", 
            title = "Сбер-коллекционер",
            description = "Импортируйте транзакции из Сбербанка",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "alfabank_importer",
            title = "Альфа-аналитик", 
            description = "Импортируйте транзакции из Альфа-Банка",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "ozon_importer",
            title = "OZON-агрегатор",
            description = "Импортируйте транзакции из OZON Банка",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "multi_bank_importer",
            title = "Мульти-банковский коллектор", 
            description = "Импортируйте данные из всех 4 банков",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.EPIC,
            targetProgress = 4,
            rewardCoins = 150
        ),
        
        // Достижения для экспорта
        Achievement(
            id = "export_master",
            title = "Мастер экспорта",
            description = "Экспортируйте транзакции в CSV",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "backup_enthusiast",
            title = "Энтузиаст резервных копий",
            description = "Создайте 5 экспортов данных",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 5,
            rewardCoins = 75
        ),
        
        // Достижение для импорта CSV
        Achievement(
            id = "csv_importer",
            title = "CSV-импортер",
            description = "Импортируйте транзакции из CSV-файла",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 25
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
            timber.log.Timber.e(e, "Ошибка при загрузке достижений")
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
            timber.log.Timber.d("🏆 Достижения сохранены в SharedPreferences")
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Ошибка при сохранении достижений")
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
        timber.log.Timber.d("🏆 Обновлено достижение: ${achievement.title} (прогресс: ${achievement.currentProgress}/${achievement.targetProgress})")
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