package com.davidbugayov.financeanalyzer.core.achievements

import android.util.Log
import java.util.Calendar

/**
 * Хелпер для запуска триггеров достижений в разных частях приложения
 */
object AchievementTrigger {
    
    private const val TAG = "AchievementTrigger"
    
    /**
     * Вызывается при добавлении новой транзакции
     */
    fun onTransactionAdded() {
        Log.d(TAG, "🏆 Триггер: Транзакция добавлена")
        // TODO: Интегрировать с AchievementEngine
        
        // Проверяем время для специальных ачивок
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour < 7) {
            Log.d(TAG, "🌅 Триггер: Ранняя пташка (до 7 утра)")
        }
        if (hour >= 23) {
            Log.d(TAG, "🌙 Триггер: Ночная сова (после 23:00)")
        }
    }
    
    /**
     * Вызывается при создании бюджета
     */
    fun onBudgetCreated() {
        Log.d(TAG, "🏆 Триггер: Бюджет создан")
        // TODO: Интегрировать с AchievementEngine
    }
    
    /**
     * Вызывается при просмотре статистики
     */
    fun onStatisticsViewed() {
        Log.d(TAG, "🏆 Триггер: Статистика просмотрена")
        // TODO: Интегрировать с AchievementEngine
    }
    
    /**
     * Вызывается при посещении раздела приложения
     */
    fun onAppSectionVisited(sectionName: String) {
        Log.d(TAG, "🏆 Триггер: Посещен раздел $sectionName")
        // TODO: Интегрировать с AchievementEngine
    }
    
    /**
     * Вызывается при изменении баланса/накоплений
     */
    fun onSavingsChanged(newAmount: Long) {
        Log.d(TAG, "🏆 Триггер: Накопления изменились: $newAmount")
        // TODO: Интегрировать с AchievementEngine
    }
    
    /**
     * Вызывается при прогрессе по бюджету
     */
    fun onBudgetProgress(spentPercentage: Float) {
        Log.d(TAG, "🏆 Триггер: Прогресс бюджета: ${(spentPercentage * 100).toInt()}%")
        // TODO: Интегрировать с AchievementEngine
        
        if (spentPercentage < 0.8f) {
            Log.d(TAG, "💰 Триггер: Экономный (потрачено менее 80%)")
        }
    }
    
    /**
     * Вызывается при использовании новой категории
     */
    fun onCategoryUsed(categoryId: String) {
        Log.d(TAG, "🏆 Триггер: Использована категория $categoryId")
        // TODO: Интегрировать с AchievementEngine
    }
} 