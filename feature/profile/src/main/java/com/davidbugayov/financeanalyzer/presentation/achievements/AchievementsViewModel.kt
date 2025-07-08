package com.davidbugayov.financeanalyzer.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
        ),
        
        // Достижения для импорта из банков
        Achievement(
            id = "tinkoff_importer",
            title = "Тинькоff-интегратор",
            description = "Импортируйте транзакции из Тинькофф",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "sberbank_importer", 
            title = "Сбер-коллекционер",
            description = "Импортируйте транзакции из Сбербанка",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "alfabank_importer",
            title = "Альфа-аналитик", 
            description = "Импортируйте транзакции из Альфа-Банка",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "ozon_importer",
            title = "OZON-агрегатор",
            description = "Импортируйте транзакции из OZON Банка",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 30
        ),
        Achievement(
            id = "multi_bank_importer",
            title = "Мульти-банковский коллектор", 
            description = "Импортируйте данные из всех 4 банков",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.EPIC,
            targetProgress = 4,
            rewardCoins = 150
        ),
        
        // Достижения для экспорта
        Achievement(
            id = "export_master",
            title = "Мастер экспорта",
            description = "Экспортируйте транзакции в CSV",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.COMMON,
            targetProgress = 1,
            rewardCoins = 20
        ),
        Achievement(
            id = "backup_enthusiast",
            title = "Энтузиаст резервных копий",
            description = "Создайте 5 экспортов данных",
            iconRes = 0,
            category = AchievementCategory.MILESTONES,
            rarity = AchievementRarity.RARE,
            targetProgress = 5,
            rewardCoins = 75
        )
    )

    // Используем репозиторий как единственный источник истины
    val achievements: StateFlow<List<Achievement>> = achievementsRepository.getAllAchievements().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        // Инициализируем достижения в репозитории если их еще нет
        viewModelScope.launch {
            // Получаем текущие достижения из репозитория
            val existingAchievements = achievementsRepository.getAllAchievements().first()
            
            // Если репозиторий пустой, инициализируем дефолтными достижениями
            if (existingAchievements.isEmpty()) {
                defaultAchievements.forEach { achievement ->
                    achievementsRepository.updateAchievement(achievement)
                }
            }
        }
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
