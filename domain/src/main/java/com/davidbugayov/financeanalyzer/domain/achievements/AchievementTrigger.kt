package com.davidbugayov.financeanalyzer.domain.achievements

import com.davidbugayov.financeanalyzer.domain.usecase.AchievementEngine
import timber.log.Timber
import com.davidbugayov.financeanalyzer.domain.util.StringProvider

/**
 * Статический триггер для запуска достижений из разных частей приложения
 * Делегирует работу глобальному экземпляру AchievementEngine
 */
object AchievementTrigger {
    
    // Ссылка на движок достижений будет инициализирована в Application
    private var engine: AchievementEngine? = null
    
    /**
     * Инициализация с экземпляром движка
     */
    fun initialize(achievementEngine: AchievementEngine) {
        engine = achievementEngine
        // Timber.d(StringProvider.logAchievementTriggerInitialized)
    }
    
    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        // Timber.d(StringProvider.logAchievementTransactionAdded)
        engine?.onTransactionAdded()
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        // Timber.d(StringProvider.logAchievementBudgetCreated)
        engine?.onBudgetCreated()
    }
    
    /**
     * Вызывается при просмотре статистики
     */
    fun onStatisticsViewed() {
        // Timber.d(StringProvider.logAchievementStatisticsViewed)
        engine?.onStatisticsViewed()
    }
    
    /**
     * Вызывается при посещении раздела приложения
     */
    fun onAppSectionVisited(sectionName: String) {
        // Timber.d(StringProvider.logAchievementSectionVisited(sectionName))
        engine?.onAppSectionVisited(sectionName)
    }
    
    /**
     * Вызывается при изменении баланса/накоплений
     */
    fun onSavingsChanged(newAmount: Long) {
        Timber.d(StringProvider.logAchievementSavingsChanged(newAmount.toString()))
        engine?.onSavingsChanged(newAmount)
    }
    
    /**
     * Вызывается при прогрессе по бюджету
     */
    fun onBudgetProgress(spentPercentage: Float) {
        Timber.d(StringProvider.logAchievementBudgetProgress((spentPercentage * 100).toInt()))
        engine?.onBudgetProgress(spentPercentage)
    }
    
    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryId: String) {
        Timber.d(StringProvider.logAchievementCategoryUsed(categoryId))
        engine?.onCategoryUsed(categoryId)
    }
    
    /**
     * Вызывается при достижении определенных вех
     */
    fun onMilestoneReached(milestoneType: String) {
        Timber.d(StringProvider.logAchievementMilestoneReached(milestoneType))
        engine?.onMilestoneReached(milestoneType)
    }
} 