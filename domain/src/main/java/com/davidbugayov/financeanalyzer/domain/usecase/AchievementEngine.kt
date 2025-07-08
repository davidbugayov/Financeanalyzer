package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

/**
 * Движок для обработки логики достижений
 */
class AchievementEngine(
    private val achievementsRepository: AchievementsRepository,
    private val scope: CoroutineScope
) {
    
    private val TAG = "AchievementEngine"
    
    // Поток для уведомлений о новых достижениях
    private val _newAchievements = MutableSharedFlow<Achievement>()
    val newAchievements: SharedFlow<Achievement> = _newAchievements.asSharedFlow()
    
    // Счетчики для отслеживания прогресса
    private var transactionCount = 0
    private var statisticsViewCount = 0
    private var sectionsVisited = mutableSetOf<String>()
    private var categoriesUsed = mutableSetOf<String>()
    
    init {
        // Загружаем текущий прогресс из репозитория при инициализации
        scope.launch {
            loadCurrentProgress()
        }
    }
    
    /**
     * Загружает текущий прогресс из репозитория при инициализации
     */
    private suspend fun loadCurrentProgress() {
        try {
            val achievements = achievementsRepository.getAllAchievements().first()
            
            // Восстанавливаем счетчики на основе сохраненного прогресса
            achievements.forEach { achievement ->
                when (achievement.id) {
                    "transaction_master" -> transactionCount = achievement.currentProgress
                    "data_analyst" -> statisticsViewCount = achievement.currentProgress
                    "app_explorer" -> {
                        // Восстанавливаем количество посещенных разделов
                        repeat(achievement.currentProgress) {
                            sectionsVisited.add("section_$it")
                        }
                    }
                    "category_organizer" -> {
                        // Восстанавливаем количество использованных категорий
                        repeat(achievement.currentProgress) {
                            categoriesUsed.add("category_$it")
                        }
                    }
                }
            }
            
            Timber.d("🏆 Загружен прогресс: транзакции=$transactionCount, статистика=$statisticsViewCount, разделы=${sectionsVisited.size}, категории=${categoriesUsed.size}")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке прогресса достижений")
        }
    }

    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        Timber.d("🏆 Обработка: Транзакция добавлена")
        
        scope.launch {
            transactionCount++
            
            // Обновляем прогресс достижений
            updateAchievementProgress("first_transaction", 1)
            updateAchievementProgress("transaction_master", transactionCount)
            
            // Проверяем время для специальных ачивок - используем checkAndUnlockAchievement
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            checkAndUnlockAchievement("early_bird") { hour < 7 }
            checkAndUnlockAchievement("night_owl") { hour >= 23 }
        }
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        Timber.d("🏆 Обработка: Бюджет создан")
        
        scope.launch {
            updateAchievementProgress("first_budget", 1)
        }
    }
    
    /**
     * Вызывается при просмотре статистики
     */
    fun onStatisticsViewed() {
        Timber.d("🏆 Обработка: Статистика просмотрена (текущий счетчик: $statisticsViewCount)")
        
        scope.launch {
            statisticsViewCount++
            Timber.d("🏆 Обработка: Увеличен счетчик статистики до $statisticsViewCount")
            updateAchievementProgress("data_analyst", statisticsViewCount)
        }
    }
    
    /**
     * Вызывается при посещении раздела приложения
     */
    fun onAppSectionVisited(sectionName: String) {
        Timber.d("🏆 Обработка: Посещен раздел $sectionName")
        
        scope.launch {
            sectionsVisited.add(sectionName)
            updateAchievementProgress("app_explorer", sectionsVisited.size)
        }
    }
    
    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryId: String) {
        Timber.d("🏆 Обработка: Использована категория $categoryId")
        
        scope.launch {
            categoriesUsed.add(categoryId)
            updateAchievementProgress("category_organizer", categoriesUsed.size)
        }
    }
    
    /**
     * Вызывается при изменении баланса/накоплений
     */
    fun onSavingsChanged(newAmount: Long) {
        Timber.d("🏆 Обработка: Накопления изменились: $newAmount")
        
        scope.launch {
            // Используем checkAndUnlockAchievement для немедленной разблокировки
            checkAndUnlockAchievement("first_savings") { newAmount >= 1000 }
            
            // Проверяем другие уровни накоплений
            checkAndUnlockAchievement("money_saver") { newAmount >= 10000 }
            checkAndUnlockAchievement("wealth_builder") { newAmount >= 100000 }
        }
    }
    
    /**
     * Вызывается при прогрессе по бюджету
     */
    fun onBudgetProgress(spentPercentage: Float) {
        Timber.d("🏆 Обработка: Прогресс бюджета: ${(spentPercentage * 100).toInt()}%")
        
        scope.launch {
            // Используем checkAndUnlockAchievement для условной разблокировки
            checkAndUnlockAchievement("budget_saver") { spentPercentage < 0.8f }
            checkAndUnlockAchievement("penny_pincher") { spentPercentage < 0.5f }
        }
    }
    
    /**
     * Вызывается при достижении целей или вехах
     */
    fun onMilestoneReached(milestoneType: String) {
        Timber.d("🏆 Обработка: Достигнута веха $milestoneType")
        
        scope.launch {
            when (milestoneType) {
                "week_streak" -> checkAndUnlockAchievement("consistent_user") { true }
                "month_active" -> checkAndUnlockAchievement("loyal_user") { true }
                "all_categories_used" -> checkAndUnlockAchievement("category_expert") { true }
                "backup_created" -> checkAndUnlockAchievement("safety_first") { true }
            }
        }
    }
    
    /**
     * Обновляет прогресс достижения
     */
    private suspend fun updateAchievementProgress(achievementId: String, newProgress: Int) {
        try {
            Timber.d("🏆 Попытка обновить прогресс $achievementId: $newProgress")
            val achievement = achievementsRepository.getAchievementById(achievementId).first()
            
            if (achievement != null) {
                Timber.d("🏆 Найдено достижение: ${achievement.title}, текущий прогресс: ${achievement.currentProgress}/${achievement.targetProgress}, разблокировано: ${achievement.isUnlocked}")
                
                if (!achievement.isUnlocked) {
                    val updatedProgress = minOf(newProgress, achievement.targetProgress)
                    val shouldUnlock = updatedProgress >= achievement.targetProgress
                    
                    val updatedAchievement = achievement.copy(
                        currentProgress = updatedProgress,
                        isUnlocked = shouldUnlock,
                        dateUnlocked = if (shouldUnlock) System.currentTimeMillis() else achievement.dateUnlocked
                    )
                    
                    achievementsRepository.updateAchievement(updatedAchievement)
                    
                    if (shouldUnlock && !achievement.isUnlocked) {
                        Timber.d("🏆 Разблокировано достижение: ${achievement.title}")
                        _newAchievements.emit(updatedAchievement)
                    } else {
                        Timber.d("🏆 Обновлен прогресс: ${achievement.title} - $updatedProgress/${achievement.targetProgress}")
                    }
                } else {
                    Timber.d("🏆 Достижение уже разблокировано: ${achievement.title}")
                }
            } else {
                Timber.w("🏆 Достижение $achievementId не найдено!")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при обновлении прогресса достижения $achievementId")
        }
    }
    
    /**
     * Проверяет условие и разблокирует достижение
     */
    private suspend fun checkAndUnlockAchievement(achievementId: String, condition: () -> Boolean) {
        try {
            val achievement = achievementsRepository.getAchievementById(achievementId).first()
            
            if (achievement != null && !achievement.isUnlocked && condition()) {
                Timber.d("🏆 Разблокировано достижение: ${achievement.title}")
                
                val unlockedAchievement = achievement.copy(
                    isUnlocked = true,
                    dateUnlocked = System.currentTimeMillis(),
                    currentProgress = achievement.targetProgress
                )
                
                achievementsRepository.updateAchievement(unlockedAchievement)
                _newAchievements.emit(unlockedAchievement)
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при проверке достижения $achievementId")
        }
    }
} 