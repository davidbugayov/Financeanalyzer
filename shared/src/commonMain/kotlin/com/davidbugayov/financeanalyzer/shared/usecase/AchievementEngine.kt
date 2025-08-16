package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Achievement
import com.davidbugayov.financeanalyzer.shared.repository.AchievementsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Движок для обработки логики достижений
 */
class AchievementEngine(
    private val achievementsRepository: AchievementsRepository,
    private val scope: CoroutineScope
) {
    
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
        } catch (e: Exception) {
            // Логирование ошибки
        }
    }

    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        scope.launch {
            transactionCount++
            
            // Обновляем прогресс достижений
            updateAchievementProgress("first_transaction", 1)
            updateAchievementProgress("transaction_master", transactionCount)
            
            // Проверяем время для специальных ачивок
            val currentTimeMillis = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = currentTimeMillis
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            
            // Ранняя пташка: до 7 утра (0-6 часов)
            val isEarlyBird = hour < 7
            checkAndUnlockAchievement("early_bird") { isEarlyBird }
            
            // Ночная сова: после 23:00 (23 час и позже)
            val isNightOwl = hour >= 23
            checkAndUnlockAchievement("night_owl") { isNightOwl }
        }
    }

    /**
     * Вызывается при просмотре статистики
     */
    fun onStatisticsViewed() {
        scope.launch {
            statisticsViewCount++
            updateAchievementProgress("data_analyst", statisticsViewCount)
        }
    }

    /**
     * Вызывается при посещении нового раздела приложения
     */
    fun onSectionVisited(sectionName: String) {
        scope.launch {
            if (sectionsVisited.add(sectionName)) {
                updateAchievementProgress("app_explorer", sectionsVisited.size)
            }
        }
    }

    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryName: String) {
        scope.launch {
            if (categoriesUsed.add(categoryName)) {
                updateAchievementProgress("category_organizer", categoriesUsed.size)
            }
        }
    }
    
    /**
     * Вызывается при экспорте транзакций
     */
    fun onTransactionExported() {
        scope.launch {
            // Триггер достижения за экспорт
            checkAndUnlockAchievement("export_master") { true }
            checkAndUnlockAchievement("backup_enthusiast") { true }
        }
    }
    
    /**
     * Вызывается при изменении сбережений
     */
    fun onSavingsChanged(balanceInKopecks: java.math.BigDecimal) {
        scope.launch {
            // Триггер достижения за сбережения
            if (balanceInKopecks > java.math.BigDecimal.ZERO) {
                checkAndUnlockAchievement("first_savings") { true }
            }
        }
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        scope.launch {
            // Триггер достижения за создание бюджета
            checkAndUnlockAchievement("first_budget") { true }
        }
    }
    
    /**
     * Вызывается при прогрессе бюджета
     */
    fun onBudgetProgress(progress: Float) {
        scope.launch {
            // Триггер достижения за прогресс бюджета
            if (progress > 0.1f && progress < 0.8f) {
                checkAndUnlockAchievement("budget_saver") { true }
            }
        }
    }
    
    /**
     * Вызывается при достижении вехи
     */
    fun onMilestoneReached(milestoneId: String) {
        scope.launch {
            // Триггер достижения за веху
            checkAndUnlockAchievement(milestoneId) { true }
        }
    }

    /**
     * Проверяет и разблокирует достижение на основе условия
     */
    private suspend fun checkAndUnlockAchievement(achievementId: String, condition: () -> Boolean) {
        if (condition()) {
            val achievement = achievementsRepository.getAchievementById(achievementId).first()
            if (achievement != null && !achievement.isUnlocked) {
                val updated = achievement.copy(
                    isUnlocked = true,
                    currentProgress = achievement.targetProgress,
                    dateUnlocked = System.currentTimeMillis()
                )
                achievementsRepository.updateAchievement(updated)
                _newAchievements.emit(updated)
            }
        }
    }

    /**
     * Обновляет прогресс достижения
     */
    private suspend fun updateAchievementProgress(achievementId: String, newProgress: Int) {
        val achievement = achievementsRepository.getAchievementById(achievementId).first() ?: return
        if (!achievement.isUnlocked) {
            val updatedProgress = minOf(newProgress, achievement.targetProgress)
            val shouldUnlock = updatedProgress >= achievement.targetProgress
            val updated = achievement.copy(
                currentProgress = updatedProgress,
                isUnlocked = shouldUnlock,
                dateUnlocked = if (shouldUnlock) System.currentTimeMillis() else achievement.dateUnlocked,
            )
            achievementsRepository.updateAchievement(updated)
            if (shouldUnlock && !achievement.isUnlocked) {
                _newAchievements.emit(updated)
            }
        }
    }
}


