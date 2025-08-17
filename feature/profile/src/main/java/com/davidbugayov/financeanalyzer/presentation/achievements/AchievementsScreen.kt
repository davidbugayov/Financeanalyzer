package com.davidbugayov.financeanalyzer.presentation.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AchievementEngineProvider
import com.davidbugayov.financeanalyzer.ui.components.AchievementNotificationManager
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar

/**
 * Экран достижений с современным дизайном
 */
@Composable
fun AchievementsScreen(
    achievements: List<Achievement>,
    onBack: () -> Unit,
) {
    AchievementNotificationManager(
        achievementEngine = AchievementEngineProvider.get(),
        onAchievementUnlocked = { achievement ->
            // Логируем аналитику разблокировки достижения
            AnalyticsUtils.logAchievementUnlocked(
                achievementId = achievement.id,
                achievementTitle = achievement.title,
                achievementCategory = achievement.category.name.lowercase(),
                achievementRarity = achievement.rarity.name.lowercase(),
                rewardCoins = achievement.rewardCoins,
            )
        },
    ) {
        AchievementsScreenContent(
            achievements = achievements,
            onBack = onBack,
        )
    }
}

@Composable
private fun AchievementsScreenContent(
    achievements: List<Achievement>,
    onBack: () -> Unit,
) {
    LocalContext.current

    // Аналитика: отслеживаем посещение экрана достижений
    LaunchedEffect(achievements) {
        val unlockedCount = achievements.count { it.isUnlocked }
        val lockedCount = achievements.count { !it.isUnlocked }
        val totalCoinsEarned = achievements.filter { it.isUnlocked }.sumOf { it.rewardCoins }

        // Отправляем аналитику в AnalyticsUtils
        AnalyticsUtils.logAchievementsScreenViewed()

        // TODO: Добавить уведомление AchievementEngine о посещении экрана
    }

    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
    var selectedFilter by remember { mutableStateOf(AchievementFilter.ALL) }

    val filteredAchievements =
        remember(achievements, selectedCategory, selectedFilter) {
            achievements
                .filter { achievement ->
                    // Фильтрация по категории
                    selectedCategory?.let { category ->
                        achievement.category == category
                    } ?: true
                }.filter { achievement ->
                    // Фильтрация по статусу
                    when (selectedFilter) {
                        AchievementFilter.ALL -> true
                        AchievementFilter.UNLOCKED -> achievement.isUnlocked
                        AchievementFilter.LOCKED -> !achievement.isUnlocked
                    }
                }.filter { achievement ->
                    // Скрываем скрытые ачивки если они не разблокированы
                    if (achievement.isHidden && !achievement.isUnlocked) false else true
                }
        }

    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
    ) {
        AppTopBar(
            title = stringResource(UiR.string.achievements),
            showBackButton = true,
            onBackClick = onBack,
        )

        // Статистика ачивок
        ModernStatsCard(achievements = achievements)

        // Фильтры
        ModernFilters(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category

                // Аналитика: фильтр по категории
                val categoryFilter = category?.name?.lowercase()
                val resultCount =
                    achievements
                        .filter { achievement ->
                            category?.let { achievement.category == it } ?: true
                        }.size

                AnalyticsUtils.logAchievementFilterChanged(
                    filterType = AnalyticsConstants.Values.ACHIEVEMENT_FILTER_ALL,
                )
            },
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                selectedFilter = filter

                // Аналитика: фильтр по статусу
                val filterType =
                    when (filter) {
                        AchievementFilter.ALL -> AnalyticsConstants.Values.ACHIEVEMENT_FILTER_ALL
                        AchievementFilter.UNLOCKED -> AnalyticsConstants.Values.ACHIEVEMENT_FILTER_UNLOCKED
                        AchievementFilter.LOCKED -> AnalyticsConstants.Values.ACHIEVEMENT_FILTER_LOCKED
                    }

                val resultCount =
                    achievements
                        .filter { achievement ->
                            when (filter) {
                                AchievementFilter.ALL -> true
                                AchievementFilter.UNLOCKED -> achievement.isUnlocked
                                AchievementFilter.LOCKED -> !achievement.isUnlocked
                            }
                        }.size

                AnalyticsUtils.logAchievementFilterChanged(
                    filterType = filterType,
                )
            },
        )

        // Список ачивок
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            filteredAchievements.forEach { achievement ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
                ) {
                    UltraModernAchievementCard(achievement = achievement)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Типы фильтров для ачивок
 */
enum class AchievementFilter {
    ALL,
    UNLOCKED,
    LOCKED,
}

/**
 * Ультра-современная статистическая карточка
 */
@Composable
private fun ModernStatsCard(achievements: List<Achievement>) {
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCoins = achievements.filter { it.isUnlocked }.sumOf { it.rewardCoins }
    val progressPercentage =
        if (achievements.isNotEmpty()) {
            unlockedCount.toFloat() / achievements.size.toFloat()
        } else {
            0f
        }

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage,
        animationSpec = tween(1500),
        label = "progress",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color(0xFF6366F1),
                    spotColor = Color(0xFF6366F1),
                ),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Box {
            // Фоновый градиент
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            Color(0xFF6366F1).copy(alpha = 0.1f),
                                            Color(0xFF8B5CF6).copy(alpha = 0.05f),
                                        ),
                                ),
                            shape = RoundedCornerShape(24.dp),
                        ),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(UiR.string.achievements_your_achievements),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text =
                                stringResource(
                                    UiR.string.achievements_unlocked_of_total,
                                    unlockedCount,
                                    achievements.size,
                                ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Монеты с градиентом
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = Color(0xFFFFD700).copy(alpha = 0.1f),
                            ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = totalCoins.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8F00),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Круговой прогресс-бар с современным дизайном
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(UiR.string.achievements_overall_progress),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${(progressPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF6366F1),
                        trackColor = Color(0xFF6366F1).copy(alpha = 0.1f),
                    )
                }
            }
        }
    }
}

/**
 * Современные фильтры
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModernFilters(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit,
    selectedFilter: AchievementFilter,
    onFilterSelected: (AchievementFilter) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        // Статусные фильтры
        Text(
            text = stringResource(UiR.string.achievements_filters_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AchievementFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            text =
                                when (filter) {
                                    AchievementFilter.ALL -> stringResource(UiR.string.achievements_filter_all)
                                    AchievementFilter.UNLOCKED ->
                                        stringResource(
                                            UiR.string.achievements_filter_unlocked,
                                        )
                                    AchievementFilter.LOCKED -> stringResource(UiR.string.achievements_filter_locked)
                                },
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                when (filter) {
                                    AchievementFilter.ALL -> Icons.Filled.FilterList
                                    AchievementFilter.UNLOCKED -> Icons.Filled.CheckCircle
                                    AchievementFilter.LOCKED -> Icons.Filled.LockClock
                                },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Категорийные фильтры
        Text(
            text = stringResource(UiR.string.achievements_categories_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Кнопка "Все"
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(UiR.string.achievements_filter_all)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )

            AchievementCategory.values().forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = {
                        Text(
                            text = getCategoryDisplayName(category),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Ультра-современная карточка достижения
 */
@Composable
private fun UltraModernAchievementCard(achievement: Achievement) {
    val rarityColors = getRarityGradient(achievement.rarity)
    val achievementIcon = getAchievementIcon(achievement.id, achievement.category)

    val animatedProgress by animateFloatAsState(
        targetValue = achievement.progressPercentage,
        animationSpec = tween(1200),
        label = "achievement_progress",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (achievement.isUnlocked) 16.dp else 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = rarityColors.first().copy(alpha = 0.3f),
                    spotColor = rarityColors.first().copy(alpha = 0.5f),
                ),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Иконка достижения в стильном контейнере
                Box(
                    modifier =
                        Modifier
                            .size(64.dp)
                            .background(
                                brush =
                                    if (achievement.isUnlocked) {
                                        val iconColor =
                                            getAchievementIconColor(achievement.id, achievement.category, true)
                                        Brush.radialGradient(
                                            listOf(
                                                iconColor.copy(alpha = 0.15f),
                                                iconColor.copy(alpha = 0.05f),
                                            ),
                                        )
                                    } else {
                                        Brush.radialGradient(
                                            listOf(
                                                Color.Gray.copy(alpha = 0.3f),
                                                Color.Gray.copy(alpha = 0.1f),
                                            ),
                                        )
                                    },
                                shape = CircleShape,
                            ).clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = achievementIcon,
                        contentDescription = null,
                        tint = getAchievementIconColor(achievement.id, achievement.category, achievement.isUnlocked),
                        modifier = Modifier.size(32.dp),
                    )

                    // Анимированное кольцо для разблокированных
                    if (achievement.isUnlocked) {
                        Box(
                            modifier =
                                Modifier
                                    .size(68.dp)
                                    .background(
                                        Color.Transparent,
                                        CircleShape,
                                    ).clip(CircleShape),
                        ) {
                            // Добавим свечение
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Информация о достижении
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (achievement.isUnlocked) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            modifier = Modifier.weight(1f),
                        )

                        // Редкость бейджик
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        rarityColors.first().copy(
                                            alpha = if (achievement.isUnlocked) 0.15f else 0.05f,
                                        ),
                                ),
                        ) {
                            Text(
                                text = getRarityDisplayName(achievement.rarity),
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    if (achievement.isUnlocked) {
                                        rarityColors.first()
                                    } else {
                                        Color.Gray
                                    },
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (achievement.isUnlocked) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Награда
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (achievement.isUnlocked) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(UiR.string.achievements_reward_coins, achievement.rewardCoins),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (achievement.isUnlocked) Color(0xFFFF8F00) else Color.Gray,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Статус
                if (achievement.isUnlocked) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFF10B981).copy(alpha = 0.1f),
                                    CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(UiR.string.achievements_unlocked),
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            // Прогресс-бар (если нужен)
            if (achievement.targetProgress > 1) {
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(UiR.string.achievements_progress),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = "${achievement.currentProgress}/${achievement.targetProgress}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (achievement.isUnlocked) rarityColors.first() else Color.Gray,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        color =
                            if (achievement.isUnlocked) {
                                Color(0xFF10B981)
                            } else {
                                rarityColors.first()
                            },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Получает иконку для конкретного достижения
 */
private fun getAchievementIcon(
    achievementId: String,
    category: AchievementCategory,
): ImageVector =
    when (achievementId) {
        // Транзакции
        "first_transaction" -> Icons.Filled.FlightTakeoff
        "transaction_master" -> Icons.Filled.MilitaryTech
        "daily_tracker" -> Icons.Filled.Schedule
        "category_organizer" -> Icons.Filled.BusinessCenter
        "tinkoff_importer" -> Icons.Filled.CreditCard
        "sberbank_importer" -> Icons.Filled.AccountBalance
        "alfabank_importer" -> Icons.Filled.Psychology
        "ozon_importer" -> Icons.Filled.SportsEsports
        "csv_importer" -> Icons.Filled.FilterList
        "multi_bank_importer" -> Icons.Filled.Sync
        "export_master" -> Icons.Filled.Download
        "backup_enthusiast" -> Icons.Filled.Security

        // Бюджет
        "first_budget" -> Icons.Filled.Home
        "budget_keeper" -> Icons.Filled.Shield
        "budget_saver" -> Icons.Filled.Savings

        // Накопления
        "first_savings" -> Icons.Filled.MonetizationOn
        "emergency_fund" -> Icons.Filled.HealthAndSafety

        // Привычки
        "week_no_coffee" -> Icons.Filled.LocalCafe
        "healthy_spender" -> Icons.Filled.FavoriteBorder

        // Статистика
        "data_analyst" -> Icons.Filled.Analytics

        // Вехи
        "app_explorer" -> Icons.Filled.Explore
        "month_user" -> Icons.Filled.Groups

        // Специальные
        "early_bird" -> Icons.Filled.WbSunny
        "night_owl" -> Icons.Filled.NightsStay
        "perfectionist" -> Icons.Filled.AutoAwesome

        else -> getCategoryIcon(category)
    }

/**
 * Получает иконку для категории
 */
private fun getCategoryIcon(category: AchievementCategory): ImageVector =
    when (category) {
        AchievementCategory.TRANSACTIONS -> Icons.Filled.Timeline
        AchievementCategory.BUDGET -> Icons.Filled.Wallet
        AchievementCategory.SAVINGS -> Icons.Filled.Savings
        AchievementCategory.HABITS -> Icons.Filled.LocalFireDepartment
        AchievementCategory.STATISTICS -> Icons.AutoMirrored.Filled.TrendingUp
        AchievementCategory.MILESTONES -> Icons.Filled.Badge
        AchievementCategory.SPECIAL -> Icons.Filled.EmojiEvents
        AchievementCategory.IMPORT -> Icons.Filled.Download
        AchievementCategory.EXPORT -> Icons.Filled.Sync
    }

/**
 * Получает отображаемое имя категории
 */
@Composable
private fun getCategoryDisplayName(category: AchievementCategory): String =
    when (category) {
        AchievementCategory.TRANSACTIONS -> stringResource(UiR.string.achievements_category_transactions)
        AchievementCategory.BUDGET -> stringResource(UiR.string.achievements_category_budget)
        AchievementCategory.SAVINGS -> stringResource(UiR.string.achievements_category_savings)
        AchievementCategory.HABITS -> stringResource(UiR.string.achievements_category_habits)
        AchievementCategory.STATISTICS -> stringResource(UiR.string.achievements_category_statistics)
        AchievementCategory.MILESTONES -> stringResource(UiR.string.achievements_category_milestones)
        AchievementCategory.SPECIAL -> stringResource(UiR.string.achievements_category_special)
        AchievementCategory.IMPORT -> stringResource(UiR.string.achievements_category_import)
        AchievementCategory.EXPORT -> stringResource(UiR.string.achievements_category_export)
    }

/**
 * Получает отображаемое имя редкости
 */
@Composable
private fun getRarityDisplayName(rarity: AchievementRarity): String =
    when (rarity) {
        AchievementRarity.COMMON -> stringResource(UiR.string.achievements_rarity_common)
        AchievementRarity.RARE -> stringResource(UiR.string.achievements_rarity_rare)
        AchievementRarity.EPIC -> stringResource(UiR.string.achievements_rarity_epic)
        AchievementRarity.LEGENDARY -> stringResource(UiR.string.achievements_rarity_legendary)
    }

/**
 * Получает уникальный цвет иконки для конкретного достижения.
 * Для разблокированных ачивок используются индивидуальные цвета:
 * - Банковские импортеры: фирменные цвета банков
 * - Категории: тематические цвета (транзакции - синий, бюджет - зелёный и т.д.)
 * - Заблокированные: серый цвет
 */
private fun getAchievementIconColor(
    achievementId: String,
    category: AchievementCategory,
    isUnlocked: Boolean,
): Color =
    if (!isUnlocked) {
        Color.Gray
    } else {
        when (achievementId) {
            // Транзакции - разные оттенки синего/зелёного
            "first_transaction" -> Color(0xFF10B981) // Зелёный
            "transaction_master" -> Color(0xFFF59E0B) // Золотой
            "daily_tracker" -> Color(0xFF3B82F6) // Синий
            "category_organizer" -> Color(0xFF8B5CF6) // Фиолетовый

            // Банковские импортеры - фирменные цвета банков
            "tinkoff_importer" -> Color(0xFFFFDD2D) // Жёлтый Тинькофф
            "sberbank_importer" -> Color(0xFF21A038) // Зелёный Сбербанк
            "alfabank_importer" -> Color(0xFFEF3124) // Красный Альфа-Банк
            "ozon_importer" -> Color(0xFF005BFF) // Синий OZON
            "csv_importer" -> Color(0xFF06B6D4) // Cyan для CSV
            "multi_bank_importer" -> Color(0xFFEF4444) // Ярко-красный для мульти
            "export_master" -> Color(0xFF059669) // Тёмно-зелёный
            "backup_enthusiast" -> Color(0xFF7C3AED) // Фиолетовый

            // Бюджет - оттенки зелёного/золотого
            "first_budget" -> Color(0xFF10B981) // Зелёный
            "budget_keeper" -> Color(0xFF059669) // Тёмно-зелёный
            "budget_saver" -> Color(0xFFD97706) // Янтарный

            // Накопления - золотые/жёлтые оттенки
            "first_savings" -> Color(0xFFFFD700) // Золотой
            "emergency_fund" -> Color(0xFFF59E0B) // Янтарный

            // Привычки - тёплые цвета
            "week_no_coffee" -> Color(0xFF92400E) // Коричневый кофе
            "healthy_spender" -> Color(0xFFEC4899) // Розовый

            // Статистика - синие оттенки
            "data_analyst" -> Color(0xFF1E40AF) // Тёмно-синий

            // Вехи - фиолетовые оттенки
            "app_explorer" -> Color(0xFF7C3AED) // Фиолетовый
            "month_user" -> Color(0xFF9333EA) // Светло-фиолетовый

            // Специальные - яркие цвета
            "early_bird" -> Color(0xFFF59E0B) // Солнечный жёлтый
            "night_owl" -> Color(0xFF4338CA) // Ночной синий
            "perfectionist" -> Color(0xFFDC2626) // Красный для перфекциониста

            // Цвета по категориям для остальных
            else ->
                when (category) {
                    AchievementCategory.TRANSACTIONS -> Color(0xFF3B82F6) // Синий
                    AchievementCategory.BUDGET -> Color(0xFF10B981) // Зелёный
                    AchievementCategory.SAVINGS -> Color(0xFFFFD700) // Золотой
                    AchievementCategory.HABITS -> Color(0xFFEC4899) // Розовый
                    AchievementCategory.STATISTICS -> Color(0xFF1E40AF) // Тёмно-синий
                    AchievementCategory.MILESTONES -> Color(0xFF7C3AED) // Фиолетовый
                    AchievementCategory.SPECIAL -> Color(0xFFEF4444) // Красный
                    AchievementCategory.IMPORT -> Color(0xFF06B6D4) // Cyan
                    AchievementCategory.EXPORT -> Color(0xFF059669) // Тёмно-зелёный
                }
        }
    }

/**
 * Получает современные градиенты для редкости
 */
private fun getRarityGradient(rarity: AchievementRarity): List<Color> =
    when (rarity) {
        AchievementRarity.COMMON ->
            listOf(
                Color(0xFF64748B), // Slate
                Color(0xFF94A3B8),
            )
        AchievementRarity.RARE ->
            listOf(
                Color(0xFF3B82F6), // Blue
                Color(0xFF6366F1), // Indigo
            )
        AchievementRarity.EPIC ->
            listOf(
                Color(0xFF8B5CF6), // Violet
                Color(0xFFA855F7), // Purple
            )
        AchievementRarity.LEGENDARY ->
            listOf(
                Color(0xFFF59E0B), // Amber
                Color(0xFFEF4444), // Red
                Color(0xFF8B5CF6), // Violet
                Color(0xFF06B6D4), // Cyan
            )
    }
