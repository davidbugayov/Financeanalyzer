package com.davidbugayov.financeanalyzer.domain.achievements

import com.davidbugayov.financeanalyzer.domain.usecase.AchievementEngine
import timber.log.Timber

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
        // лог можно добавить через ResourceProvider при необходимости
    }
    
    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        // лог можно добавить через ResourceProvider при необходимости
        engine?.onTransactionAdded()
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        // лог можно добавить через ResourceProvider при необходимости
        engine?.onBudgetCreated()
    }
    
    /**
     * Вызывается при просмотре статистики
     */
    fun onStatisticsViewed() {
        // лог можно добавить через ResourceProvider при необходимости
        engine?.onStatisticsViewed()
    }
    
    /**
     * Вызывается при посещении раздела приложения
     */
    fun onAppSectionVisited(sectionName: String) {
        // лог можно добавить через ResourceProvider при необходимости
        engine?.onAppSectionVisited(sectionName)
    }
    
    /**
     * Вызывается при изменении баланса/накоплений
     */
    fun onSavingsChanged(newAmount: Long) {
        Timber.d("Savings changed: %s", newAmount.toString())
        engine?.onSavingsChanged(newAmount)
    }
    
    /**
     * Вызывается при прогрессе по бюджету
     */
    fun onBudgetProgress(spentPercentage: Float) {
        Timber.d("Budget progress: %d%%", (spentPercentage * 100).toInt())
        engine?.onBudgetProgress(spentPercentage)
    }
    
    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryId: String) {
        Timber.d("Category used: %s", categoryId)
        engine?.onCategoryUsed(categoryId)
    }
    
    /**
     * Вызывается при достижении определенных вех
     */
    fun onMilestoneReached(milestoneType: String) {
        Timber.d("Milestone reached: %s", milestoneType)
        engine?.onMilestoneReached(milestoneType)
    }
} 