package com.davidbugayov.financeanalyzer.presentation.achievements

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.ui.R as UiR
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана достижений
 *
 * @param achievementsRepository Репозиторий для управления достижениями
 * @param context Контекст приложения для доступа к строковым ресурсам
 */
class AchievementsViewModel(
    private val achievementsRepository: AchievementsRepository,
    context: Context,
) : ViewModel() {
    // Используем applicationContext для избежания утечки памяти
    private val appContext = context.applicationContext

    // Предустановленные достижения с расширенными данными
    private val defaultAchievements =
        listOf(
            // Транзакции
            Achievement(
                id = "first_transaction",
                title = appContext.getString(UiR.string.achievement_first_transaction_title),
                description = appContext.getString(UiR.string.achievement_first_transaction_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 10,
            ),
            Achievement(
                id = "transaction_master",
                title = appContext.getString(UiR.string.achievement_transaction_master_title),
                description = appContext.getString(UiR.string.achievement_transaction_master_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.RARE,
                targetProgress = 100,
                rewardCoins = 50,
            ),
            Achievement(
                id = "daily_tracker",
                title = appContext.getString(UiR.string.achievement_daily_tracker_title),
                description = appContext.getString(UiR.string.achievement_daily_tracker_desc),
                iconRes = 0,
                category = AchievementCategory.HABITS,
                rarity = AchievementRarity.RARE,
                targetProgress = 7,
                rewardCoins = 30,
            ),
            Achievement(
                id = "category_organizer",
                title = appContext.getString(UiR.string.achievement_category_organizer_title),
                description = appContext.getString(UiR.string.achievement_category_organizer_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 10,
                rewardCoins = 25,
            ),
            // Бюджет
            Achievement(
                id = "first_budget",
                title = appContext.getString(UiR.string.achievement_first_budget_title),
                description = appContext.getString(UiR.string.achievement_first_budget_desc),
                iconRes = 0,
                category = AchievementCategory.BUDGET,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 15,
            ),
            Achievement(
                id = "budget_keeper",
                title = appContext.getString(UiR.string.achievement_budget_keeper_title),
                description = appContext.getString(UiR.string.achievement_budget_keeper_desc),
                iconRes = 0,
                category = AchievementCategory.BUDGET,
                rarity = AchievementRarity.EPIC,
                targetProgress = 3,
                rewardCoins = 100,
            ),
            Achievement(
                id = "budget_saver",
                title = appContext.getString(UiR.string.achievement_budget_saver_title),
                description = appContext.getString(UiR.string.achievement_budget_saver_desc),
                iconRes = 0,
                category = AchievementCategory.BUDGET,
                rarity = AchievementRarity.RARE,
                targetProgress = 1,
                rewardCoins = 40,
            ),
            // Накопления
            Achievement(
                id = "first_savings",
                title = appContext.getString(UiR.string.achievement_first_savings_title),
                description = appContext.getString(UiR.string.achievement_first_savings_desc),
                iconRes = 0,
                category = AchievementCategory.SAVINGS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 20,
            ),
            Achievement(
                id = "emergency_fund",
                title = appContext.getString(UiR.string.achievement_emergency_fund_title),
                description = appContext.getString(UiR.string.achievement_emergency_fund_desc),
                iconRes = 0,
                category = AchievementCategory.SAVINGS,
                rarity = AchievementRarity.LEGENDARY,
                targetProgress = 1,
                rewardCoins = 200,
            ),
            // Привычки
            Achievement(
                id = "week_no_coffee",
                title = appContext.getString(UiR.string.achievement_week_no_coffee_title),
                description = appContext.getString(UiR.string.achievement_week_no_coffee_desc),
                iconRes = 0,
                category = AchievementCategory.HABITS,
                rarity = AchievementRarity.RARE,
                targetProgress = 7,
                rewardCoins = 35,
            ),
            Achievement(
                id = "healthy_spender",
                title = appContext.getString(UiR.string.achievement_healthy_spender_title),
                description = appContext.getString(UiR.string.achievement_healthy_spender_desc),
                iconRes = 0,
                category = AchievementCategory.HABITS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 5,
                rewardCoins = 25,
            ),
            // Статистика
            Achievement(
                id = "data_analyst",
                title = appContext.getString(UiR.string.achievement_data_analyst_title),
                description = appContext.getString(UiR.string.achievement_data_analyst_desc),
                iconRes = 0,
                category = AchievementCategory.STATISTICS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 10,
                rewardCoins = 20,
            ),
            // Вехи
            Achievement(
                id = "app_explorer",
                title = appContext.getString(UiR.string.achievement_app_explorer_title),
                description = appContext.getString(UiR.string.achievement_app_explorer_desc),
                iconRes = 0,
                category = AchievementCategory.MILESTONES,
                rarity = AchievementRarity.COMMON,
                targetProgress = 5, // 5 разных экранов
                rewardCoins = 30,
            ),
            Achievement(
                id = "month_user",
                title = appContext.getString(UiR.string.achievement_month_user_title),
                description = appContext.getString(UiR.string.achievement_month_user_desc),
                iconRes = 0,
                category = AchievementCategory.MILESTONES,
                rarity = AchievementRarity.RARE,
                targetProgress = 30, // 30 дней
                rewardCoins = 75,
            ),
            // Специальные
            Achievement(
                id = "early_bird",
                title = appContext.getString(UiR.string.achievement_early_bird_title),
                description = appContext.getString(UiR.string.achievement_early_bird_desc),
                iconRes = 0,
                category = AchievementCategory.SPECIAL,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 15,
            ),
            Achievement(
                id = "night_owl",
                title = appContext.getString(UiR.string.achievement_night_owl_title),
                description = appContext.getString(UiR.string.achievement_night_owl_desc),
                iconRes = 0,
                category = AchievementCategory.SPECIAL,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 15,
            ),
            Achievement(
                id = "perfectionist",
                title = appContext.getString(UiR.string.achievement_perfectionist_title),
                description = appContext.getString(UiR.string.achievement_perfectionist_desc),
                iconRes = 0,
                category = AchievementCategory.SPECIAL,
                rarity = AchievementRarity.EPIC,
                targetProgress = 50,
                rewardCoins = 80,
                isHidden = true, // Скрытое достижение
            ),
            // Достижения для импорта из банков
            Achievement(
                id = "tinkoff_importer",
                title = appContext.getString(UiR.string.achievement_tinkoff_importer_title),
                description = appContext.getString(UiR.string.achievement_tinkoff_importer_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 30,
            ),
            Achievement(
                id = "sberbank_importer",
                title = appContext.getString(UiR.string.achievement_sberbank_importer_title),
                description = appContext.getString(UiR.string.achievement_sberbank_importer_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 30,
            ),
            Achievement(
                id = "alfabank_importer",
                title = appContext.getString(UiR.string.achievement_alfabank_importer_title),
                description = appContext.getString(UiR.string.achievement_alfabank_importer_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 30,
            ),
            Achievement(
                id = "ozon_importer",
                title = appContext.getString(UiR.string.achievement_ozon_importer_title),
                description = appContext.getString(UiR.string.achievement_ozon_importer_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 30,
            ),
            Achievement(
                id = "multi_bank_importer",
                title = appContext.getString(UiR.string.achievement_multi_bank_importer_title),
                description = appContext.getString(UiR.string.achievement_multi_bank_importer_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.EPIC,
                targetProgress = 4,
                rewardCoins = 150,
            ),
            // Достижения для экспорта
            Achievement(
                id = "export_master",
                title = appContext.getString(UiR.string.achievement_export_master_title),
                description = appContext.getString(UiR.string.achievement_export_master_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 20,
            ),
            Achievement(
                id = "backup_enthusiast",
                title = appContext.getString(UiR.string.achievement_backup_enthusiast_title),
                description = appContext.getString(UiR.string.achievement_backup_enthusiast_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.RARE,
                targetProgress = 5,
                rewardCoins = 75,
            ),
            // Достижение для импорта CSV
            Achievement(
                id = "csv_importer",
                title = appContext.getString(UiR.string.achievement_csv_importer_title),
                description = appContext.getString(UiR.string.achievement_csv_importer_desc),
                iconRes = 0,
                category = AchievementCategory.TRANSACTIONS,
                rarity = AchievementRarity.COMMON,
                targetProgress = 1,
                rewardCoins = 25,
            ),
        )

    // Используем репозиторий как единственный источник истины
    val achievements: StateFlow<List<Achievement>> =
        achievementsRepository.getAllAchievements().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    init {
        // Репозиторий теперь сам возвращает дефолтные ачивки
        // Дополнительная инициализация не нужна
    }

    /**
     * Разблокирует случайное достижение для тестирования
     */
    fun unlockRandomAchievement() {
        viewModelScope.launch {
            val lockedAchievements = achievements.first().filter { !it.isUnlocked }
            if (lockedAchievements.isNotEmpty()) {
                val randomAchievement = lockedAchievements.random()
                achievementsRepository.unlockAchievement(randomAchievement.id)
            }
        }
    }
}
