package com.davidbugayov.financeanalyzer.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана достижений
 */
class AchievementsViewModel(
    private val achievementsRepository: AchievementsRepository
) : ViewModel() {

    // Предустановленные достижения с расширенными данными
    private val defaultAchievements = listOf(
        // Транзакции
        Achievement(
            id = "first_transaction",
            title = "Первые шаги",
            description = "Добавьте свою первую транзакцию",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 10
        ),
        Achievement(
            id = "transaction_master",
            title = "Мастер транзакций",
            description = "Добавьте 100 транзакций",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.RARE,
            targetProgress = 100,
            rewardCoins = 50
        ),
        Achievement(
            id = "daily_tracker",
            title = "Ежедневный трекер",
            description = "Добавляйте транзакции 7 дней подряд",
            iconRes = 0,
            category = AchievementCategory.HABITS,
            rarity = AchievementRarity.RARE,
            targetProgress = 7,
            rewardCoins = 30
        ),
        Achievement(
            id = "category_organizer",
            title = "Организатор категорий",
            description = "Используйте 10 разных категорий",
            iconRes = 0,
            category = AchievementCategory.TRANSACTIONS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 25
        ),

        // Бюджет
        Achievement(
            id = "first_budget",
            title = "Первый бюджет",
            description = "Создайте свой первый бюджет",
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "budget_keeper",
            title = "Хранитель бюджета",
            description = "Не превышайте бюджет 3 месяца подряд",
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.EPIC,
            targetProgress = 3,
            rewardCoins = 100
        ),
        Achievement(
            id = "budget_saver",
            title = "Экономный",
            description = "Потратьте менее 80% от бюджета за месяц",
            iconRes = 0,
            category = AchievementCategory.BUDGET,
            rarity = AchievementRarity.RARE,
            targetProgress = 1,
            rewardCoins = 40
        ),

        // Накопления
        Achievement(
            id = "first_savings",
            title = "Первая копейка",
            description = "Накопите 1000 рублей",
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "emergency_fund",
            title = "Подушка безопасности",
            description = "Накопите сумму на 3 месяца расходов",
            iconRes = 0,
            category = AchievementCategory.SAVINGS,
            rarity = AchievementRarity.LEGENDARY,
            targetProgress = 1,
            rewardCoins = 200
        ),

        // Привычки
        Achievement(
            id = "week_no_coffee",
            title = "Неделя без кофе",
            description = "Не тратьте на кофе 7 дней подряд",
            iconRes = 0,
            category = AchievementCategory.HABITS,
            rarity = AchievementRarity.RARE,
            targetProgress = 7,
            rewardCoins = 35
        ),
        Achievement(
            id = "healthy_spender",
            title = "Здоровые траты",
            description = "Тратьте на здоровье 5 дней подряд",
            iconRes = 0,
            category = AchievementCategory.HABITS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 5,
            rewardCoins = 25
        ),

        // Статистика
        Achievement(
            id = "data_analyst",
            title = "Аналитик данных",
            description = "Просмотрите статистику 10 раз",
            iconRes = 0,
            category = AchievementCategory.STATISTICS,
            rarity = AchievementRarity.COMMON,
            targetProgress = 10,
            rewardCoins = 20
        ),

        // Вехи
        Achievement(
            id = "app_explorer",
            title = "Исследователь",
            description = "Посетите все разделы приложения",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 5, // 5 разных экранов
            rewardCoins = 30
        ),
        Achievement(
            id = "month_user",
            title = "Постоянный пользователь",
            description = "Используйте приложение месяц",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.RARE,
            targetProgress = 30, // 30 дней
            rewardCoins = 75
        ),

        // Специальные
        Achievement(
            id = "early_bird",
            title = "Ранняя пташка",
            description = "Добавьте транзакцию до 7 утра",
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "night_owl",
            title = "Ночная сова",
            description = "Добавьте транзакцию после 23:00",
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 15
        ),
        Achievement(
            id = "perfectionist",
            title = "Перфекционист",
            description = "Заполните все поля в 50 транзакциях",
            iconRes = 0,
            category = AchievementCategory.SPECIAL,
            rarity = AchievementRarity.EPIC,
            targetProgress = 50,
            rewardCoins = 80,
            isHidden = true // Скрытое достижение
        )
    )

    private val _achievements = MutableStateFlow(defaultAchievements)

    val achievements: StateFlow<List<Achievement>> = _achievements.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        defaultAchievements
    )

    init {
        // Инициализируем достижения в репозитории
        viewModelScope.launch {
            achievementsRepository.initializeDefaultAchievements(defaultAchievements)
            
            // Подписываемся на изменения из репозитория
            achievementsRepository.getAllAchievements().collect { repositoryAchievements ->
                _achievements.value = repositoryAchievements
            }
        }
    }

    /**
     * Имитирует прогресс для демонстрации
     */
    fun simulateProgress() {
        viewModelScope.launch {
            val updated = _achievements.value.map { achievement ->
                when (achievement.id) {
                    "first_transaction" -> achievement.copy(currentProgress = 1, isUnlocked = true, dateUnlocked = System.currentTimeMillis())
                    "transaction_master" -> achievement.copy(currentProgress = 45)
                    "daily_tracker" -> achievement.copy(currentProgress = 3)
                    "category_organizer" -> achievement.copy(currentProgress = 7)
                    "data_analyst" -> achievement.copy(currentProgress = 6)
                    "app_explorer" -> achievement.copy(currentProgress = 3)
                    else -> achievement
                }
            }
            _achievements.value = updated
            
            // Сохраняем в репозиторий
            updated.forEach { achievement ->
                achievementsRepository.updateAchievement(achievement)
            }
        }
    }

    /**
     * Разблокирует случайное достижение для тестирования
     */
    fun unlockRandomAchievement() {
        viewModelScope.launch {
            val lockedAchievements = _achievements.value.filter { !it.isUnlocked }
            if (lockedAchievements.isNotEmpty()) {
                val randomAchievement = lockedAchievements.random()
                achievementsRepository.unlockAchievement(randomAchievement.id)
            }
        }
    }
}
