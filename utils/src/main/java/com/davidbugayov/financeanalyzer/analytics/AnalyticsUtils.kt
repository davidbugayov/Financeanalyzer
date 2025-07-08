package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import com.davidbugayov.financeanalyzer.core.model.Money
import java.util.UUID
import org.koin.core.component.KoinComponent
import timber.log.Timber

/**
 * Утилиты для работы с аналитикой
 * Содержит только используемые методы
 */
object AnalyticsUtils : KoinComponent {
    private lateinit var analytics: IAnalytics
    private var sessionId: String = UUID.randomUUID().toString()
    private var appOpenTimestamp: Long = 0L

    fun init(analyticsImpl: IAnalytics) {
        analytics = analyticsImpl
        sessionId = UUID.randomUUID().toString()
        appOpenTimestamp = System.currentTimeMillis()
        Timber.d("AnalyticsUtils initialized with ${analyticsImpl.javaClass.simpleName}")
    }

    fun isInitialized(): Boolean = ::analytics.isInitialized

    fun getAnalytics(): IAnalytics {
        if (!isInitialized()) {
            analytics = NoOpAnalytics()
        }
        return analytics
    }

    fun logEvent(eventName: String) {
        if (isInitialized()) analytics.logEvent(eventName)
    }

    fun logEvent(
        eventName: String,
        params: Bundle,
    ) {
        if (isInitialized()) analytics.logEvent(eventName, params)
    }

    fun setUserProperty(
        name: String,
        value: String,
    ) {
        if (isInitialized()) analytics.setUserProperty(name, value)
    }

    // Жизненный цикл приложения

    fun logAppOpen() {
        appOpenTimestamp = System.currentTimeMillis()
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.APP_OPEN, params)
        Timber.d("App open logged, session: $sessionId")
    }

    fun logAppBackground() {
        val sessionDuration = System.currentTimeMillis() - appOpenTimestamp
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
                putLong(AnalyticsConstants.Params.USER_ENGAGEMENT_TIME, sessionDuration)
            }
        logEvent(AnalyticsConstants.Events.APP_BACKGROUND, params)
        Timber.d("App background logged, session: $sessionId, duration: $sessionDuration ms")
    }

    fun logAppForeground() {
        appOpenTimestamp = System.currentTimeMillis()
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.APP_FOREGROUND, params)
        Timber.d("App foreground logged, session: $sessionId")
    }

    // Экраны и навигация

    fun logScreenView(
        screenName: String,
        screenClass: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
                putString(AnalyticsConstants.Params.SCREEN_CLASS, screenClass)
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.SCREEN_VIEW, params)
        Timber.d("Screen view logged: $screenName ($screenClass)")
    }

    // Транзакции

    fun logTransactionEdited(
        amount: Money,
        category: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(
                    AnalyticsConstants.Params.TRANSACTION_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
                    },
                )
                putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
                putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EDITED, params)
        Timber.d("Transaction edited logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }

    fun logTransactionDeleted(
        amount: Money,
        category: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(
                    AnalyticsConstants.Params.TRANSACTION_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
                    },
                )
                putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
                putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_DELETED, params)
        Timber.d("Transaction deleted logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }

    // Категории

    fun logCategoryDeleted(
        category: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.CATEGORY_NAME, category)
                putString(
                    AnalyticsConstants.Params.CATEGORY_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
                    },
                )
            }
        logEvent(AnalyticsConstants.Events.CATEGORY_DELETED, params)
        Timber.d("Category deleted logged: $category, ${if (isExpense) "expense" else "income"}")
    }

    // Достижения

    /**
     * Логирует просмотр экрана достижений
     * @param totalCount Общее количество достижений
     * @param unlockedCount Количество разблокированных достижений
     * @param lockedCount Количество заблокированных достижений
     * @param totalCoinsEarned Общее количество заработанных монет
     */
    fun logAchievementsScreenViewed(
        totalCount: Int,
        unlockedCount: Int,
        lockedCount: Int,
        totalCoinsEarned: Int,
    ) {
        val params =
            Bundle().apply {
                putInt(AnalyticsConstants.Params.ACHIEVEMENTS_TOTAL_COUNT, totalCount)
                putInt(AnalyticsConstants.Params.ACHIEVEMENTS_UNLOCKED_COUNT, unlockedCount)
                putInt(AnalyticsConstants.Params.ACHIEVEMENTS_LOCKED_COUNT, lockedCount)
                putInt(AnalyticsConstants.Params.TOTAL_COINS_EARNED, totalCoinsEarned)
                putString(AnalyticsConstants.Params.SCREEN_NAME, "achievements")
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.ACHIEVEMENTS_SCREEN_VIEWED, params)
        Timber.d("Achievements screen viewed: $unlockedCount/$totalCount unlocked, $totalCoinsEarned coins earned")
    }

    /**
     * Логирует разблокировку достижения
     * @param achievementId ID достижения
     * @param achievementTitle Название достижения
     * @param achievementCategory Категория достижения
     * @param achievementRarity Редкость достижения
     * @param rewardCoins Количество монет за достижение
     */
    fun logAchievementUnlocked(
        achievementId: String,
        achievementTitle: String,
        achievementCategory: String,
        achievementRarity: String,
        rewardCoins: Int,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.ACHIEVEMENT_ID, achievementId)
                putString(AnalyticsConstants.Params.ACHIEVEMENT_TITLE, achievementTitle)
                putString(AnalyticsConstants.Params.ACHIEVEMENT_CATEGORY, achievementCategory)
                putString(AnalyticsConstants.Params.ACHIEVEMENT_RARITY, achievementRarity)
                putInt(AnalyticsConstants.Params.ACHIEVEMENT_REWARD_COINS, rewardCoins)
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.ACHIEVEMENT_UNLOCKED, params)
        Timber.d("Achievement unlocked: $achievementTitle ($achievementId), reward: $rewardCoins coins")
    }

    /**
     * Логирует изменение фильтра достижений
     * @param filterType Тип фильтра (all, unlocked, locked)
     * @param categoryFilter Выбранная категория (null если все категории)
     * @param resultCount Количество достижений после фильтрации
     */
    fun logAchievementFilterChanged(
        filterType: String,
        categoryFilter: String?,
        resultCount: Int,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.ACHIEVEMENT_FILTER_TYPE, filterType)
                categoryFilter?.let { putString(AnalyticsConstants.Params.ACHIEVEMENT_CATEGORY, it) }
                putInt(AnalyticsConstants.Params.ACHIEVEMENTS_TOTAL_COUNT, resultCount)
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.ACHIEVEMENT_FILTER_CHANGED, params)
        Timber.d("Achievement filter changed: $filterType${categoryFilter?.let { ", category: $it" } ?: ""}, results: $resultCount")
    }
}
