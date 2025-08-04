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
            
            // Timber.d("🏆 Загружен прогресс: транзакции=$transactionCount, статистика=$statisticsViewCount, разделы=${sectionsVisited.size}, категории=${categoriesUsed.size}")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке прогресса достижений")
        }
    }

    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        // Timber.d("🏆 Обработка: Транзакция добавлена")
        
        scope.launch {
            transactionCount++
            
            // Обновляем прогресс достижений
            updateAchievementProgress("first_transaction", 1)
            updateAchievementProgress("transaction_master", transactionCount)
            
            // Проверяем время для специальных ачивок - используем checkAndUnlockAchievement
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            // Timber.d("🏆 Текущий час: $hour")
            
            // Ранняя пташка: до 7 утра (0-6 часов)
            val isEarlyBird = hour < 7
            // Timber.d("🏆 Проверка early_bird: час=$hour, условие hour < 7 = $isEarlyBird")
            checkAndUnlockAchievement("early_bird") { isEarlyBird }
            
            // Ночная сова: после 23:00 (23 час и позже)
            val isNightOwl = hour >= 23
            // Timber.d("🏆 Проверка night_owl: час=$hour, условие hour >= 23 = $isNightOwl")
            checkAndUnlockAchievement("night_owl") { isNightOwl }
        }
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        
        scope.launch {
            updateAchievementProgress("first_budget", 1)
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
     * Вызывается при посещении раздела приложения
     */
    fun onAppSectionVisited(sectionName: String) {
        
        scope.launch {
            sectionsVisited.add(sectionName)
            updateAchievementProgress("app_explorer", sectionsVisited.size)
        }
    }

    /**
     * Вызывается при посещении экрана достижений
     * Также обновляет прогресс достижения "Исследователь"
     */
    fun onAchievementsScreenViewed() {
        Timber.d("🏆 Обработка: Посещен экран достижений")
        
        scope.launch {
            // Добавляем посещение экрана достижений к общему счетчику исследователя
            sectionsVisited.add("achievements")
            updateAchievementProgress("app_explorer", sectionsVisited.size)
        }
    }
    
    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryId: String) {
        
        scope.launch {
            categoriesUsed.add(categoryId)
            updateAchievementProgress("category_organizer", categoriesUsed.size)
        }
    }
    
    /**
     * Вызывается при изменении баланса/накоплений
     */
    fun onSavingsChanged(newAmount: Long) {
        
        scope.launch {
            // Конвертируем копейки в рубли для проверки
            val amountInRubles = newAmount / 100
            
            // Используем checkAndUnlockAchievement для немедленной разблокировки
            checkAndUnlockAchievement("first_savings") { amountInRubles >= 1000 }
            
            // Проверяем достижение подушки безопасности
            // Для упрощения считаем 3 месяца расходов = 100,000 рублей
            checkAndUnlockAchievement("emergency_fund") { amountInRubles >= 100000 }
            
            // Дополнительные уровни накоплений (если есть)
            checkAndUnlockAchievement("money_saver") { amountInRubles >= 10000 }
            checkAndUnlockAchievement("wealth_builder") { amountInRubles >= 500000 }
        }
    }
    
    /**
     * Вызывается при прогрессе по бюджету
     */
    fun onBudgetProgress(spentPercentage: Float) {
        
        scope.launch {
            // Ачивка "Экономный" разблокируется если пользователь активно тратит,
            // но держится в пределах 80% от бюджета в течение значимого времени
            if (spentPercentage > 0.5f && spentPercentage < 0.8f) {
                checkAndUnlockAchievement("budget_saver") { true }
            }
            
            // Ачивка "Скупердяй" для тех, кто тратит менее 50%
            if (spentPercentage > 0.2f && spentPercentage < 0.5f) {
                checkAndUnlockAchievement("penny_pincher") { true }
            }
        }
    }
    
    /**
     * Вызывается при достижении целей или вехах
     */
    fun onMilestoneReached(milestoneType: String) {
        
        scope.launch {
            when (milestoneType) {
                "week_streak" -> checkAndUnlockAchievement("consistent_user") { true }
                "month_active" -> checkAndUnlockAchievement("loyal_user") { true }
                "all_categories_used" -> checkAndUnlockAchievement("category_expert") { true }
                
                // Экспорт и импорт достижения
                "export_master" -> updateAchievementProgress("export_master", 1)
                "backup_enthusiast" -> {
                    val currentAchievement = achievementsRepository.getAchievementById("backup_enthusiast").first()
                    if (currentAchievement != null) {
                        updateAchievementProgress("backup_enthusiast", currentAchievement.currentProgress + 1)
                    }
                }
                
                // Банковские импорт достижения
                "tinkoff_importer" -> updateAchievementProgress("tinkoff_importer", 1)
                "sberbank_importer" -> updateAchievementProgress("sberbank_importer", 1)
                "alfabank_importer" -> updateAchievementProgress("alfabank_importer", 1)
                "ozon_importer" -> updateAchievementProgress("ozon_importer", 1)
                "csv_importer" -> updateAchievementProgress("csv_importer", 1)
                "multi_bank_importer" -> {
                    val currentAchievement = achievementsRepository.getAchievementById("multi_bank_importer").first()
                    if (currentAchievement != null) {
                        updateAchievementProgress("multi_bank_importer", currentAchievement.currentProgress + 1)
                    }
                }
            }
        }
    }
    
    /**
     * Обновляет прогресс достижения
     */
    private suspend fun updateAchievementProgress(achievementId: String, newProgress: Int) {
        try {
            val achievement = achievementsRepository.getAchievementById(achievementId).first()
            
            if (achievement != null) {
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
                        _newAchievements.emit(updatedAchievement)
                    }
                }
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
            
            if (achievement != null) {
                if (!achievement.isUnlocked) {
                    val conditionResult = condition()
                    
                    if (conditionResult) {
                        val unlockedAchievement = achievement.copy(
                            isUnlocked = true,
                            dateUnlocked = System.currentTimeMillis(),
                            currentProgress = achievement.targetProgress
                        )
                        
                        achievementsRepository.updateAchievement(unlockedAchievement)
                        _newAchievements.emit(unlockedAchievement)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при проверке достижения $achievementId")
        }
    }
} 