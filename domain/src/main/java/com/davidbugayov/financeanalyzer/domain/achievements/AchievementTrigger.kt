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
        Timber.d("🏆 AchievementTrigger инициализирован")
    }
    
    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        Timber.d("🏆 Триггер: Транзакция добавлена")
        engine?.onTransactionAdded()
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        Timber.d("🏆 Триггер: Бюджет создан")
        engine?.onBudgetCreated()
    }
    
    /**
     * Вызывается при просмотре статистики
     */
    fun onStatisticsViewed() {
        Timber.d("🏆 Триггер: Статистика просмотрена")
        engine?.onStatisticsViewed()
    }
    
    /**
     * Вызывается при посещении раздела приложения
     */
    fun onAppSectionVisited(sectionName: String) {
        Timber.d("🏆 Триггер: Посещен раздел $sectionName")
        engine?.onAppSectionVisited(sectionName)
    }
    
    /**
     * Вызывается при изменении баланса/накоплений
     */
    fun onSavingsChanged(newAmount: Long) {
        Timber.d("🏆 Триггер: Накопления изменились: $newAmount")
        engine?.onSavingsChanged(newAmount)
    }
    
    /**
     * Вызывается при прогрессе по бюджету
     */
    fun onBudgetProgress(spentPercentage: Float) {
        Timber.d("🏆 Триггер: Прогресс бюджета: ${(spentPercentage * 100).toInt()}%")
        engine?.onBudgetProgress(spentPercentage)
    }
    
    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryId: String) {
        Timber.d("🏆 Триггер: Использована категория $categoryId")
        engine?.onCategoryUsed(categoryId)
    }
    
    /**
     * Вызывается при достижении определенных вех
     */
    fun onMilestoneReached(milestoneType: String) {
        Timber.d("🏆 Триггер: Достигнута веха $milestoneType")
        engine?.onMilestoneReached(milestoneType)
    }
} 