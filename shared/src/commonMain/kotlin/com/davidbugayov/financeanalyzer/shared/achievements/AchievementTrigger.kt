package com.davidbugayov.financeanalyzer.shared.achievements

import com.davidbugayov.financeanalyzer.shared.usecase.AchievementEngine


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
    }
    
    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        engine?.onTransactionAdded()
    }
    
    /**
     * Вызывается при просмотре статистики
     */
    fun onStatisticsViewed() {
        engine?.onStatisticsViewed()
    }
    
    /**
     * Вызывается при посещении раздела приложения
     */
    fun onSectionVisited(sectionName: String) {
        engine?.onSectionVisited(sectionName)
    }
    
    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryName: String) {
        engine?.onCategoryUsed(categoryName)
    }
    
    /**
     * Вызывается при экспорте транзакций
     */
    fun onTransactionExported() {
        engine?.onTransactionExported()
    }
    
    /**
     * Вызывается при изменении сбережений
     */
    fun onSavingsChanged(balanceInKopecks: Double) {
        engine?.onSavingsChanged(balanceInKopecks)
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        engine?.onBudgetCreated()
    }
    
    /**
     * Вызывается при прогрессе бюджета
     */
    fun onBudgetProgress(progress: Float) {
        engine?.onBudgetProgress(progress)
    }
    
    /**
     * Вызывается при достижении вехи
     */
    fun onMilestoneReached(milestoneId: String) {
        engine?.onMilestoneReached(milestoneId)
    }
}
