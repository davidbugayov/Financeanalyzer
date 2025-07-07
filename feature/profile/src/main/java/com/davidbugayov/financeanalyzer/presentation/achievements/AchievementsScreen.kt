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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.profile.R
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar

/**
 * Экран достижений с современным дизайном
 */
@Composable
fun AchievementsScreen(
    achievements: List<Achievement>, 
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
    var selectedFilter by remember { mutableStateOf(AchievementFilter.ALL) }
    
    val filteredAchievements = remember(achievements, selectedCategory, selectedFilter) {
        achievements
            .filter { achievement ->
                // Фильтрация по категории
                selectedCategory?.let { category ->
                    achievement.category == category
                } ?: true
            }
            .filter { achievement ->
                // Фильтрация по статусу
                when (selectedFilter) {
                    AchievementFilter.ALL -> true
                    AchievementFilter.UNLOCKED -> achievement.isUnlocked
                    AchievementFilter.LOCKED -> !achievement.isUnlocked
                }
            }
            .filter { achievement ->
                // Скрываем скрытые ачивки если они не разблокированы
                if (achievement.isHidden && !achievement.isUnlocked) false else true
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.achievements),
            showBackButton = true,
            onBackClick = onBack,
        )
        
        // Статистика ачивок
        AchievementStatsCard(achievements = achievements)
        
        // Фильтры
        AchievementFilters(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        
        // Список ачивок
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = filteredAchievements,
                key = { it.id }
            ) { achievement ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
                ) {
                    ModernAchievementCard(achievement = achievement)
                }
            }
        }
    }
}

/**
 * Типы фильтров для ачивок
 */
enum class AchievementFilter {
    ALL, UNLOCKED, LOCKED
}

/**
 * Карточка со статистикой ачивок
 */
@Composable
private fun AchievementStatsCard(achievements: List<Achievement>) {
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCoins = achievements.filter { it.isUnlocked }.sumOf { it.rewardCoins }
    val progressPercentage = if (achievements.isNotEmpty()) {
        unlockedCount.toFloat() / achievements.size.toFloat()
    } else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage,
        animationSpec = tween(1000),
        label = "progress"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ваш прогресс",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$unlockedCount из ${achievements.size} разблокировано",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = totalCoins.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(progressPercentage * 100).toInt()}% завершено",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Фильтры для ачивок
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AchievementFilters(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit,
    selectedFilter: AchievementFilter,
    onFilterSelected: (AchievementFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Фильтры по статусу
        Text(
            text = "Фильтры",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AchievementFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            text = when (filter) {
                                AchievementFilter.ALL -> "Все"
                                AchievementFilter.UNLOCKED -> "Разблокированные"
                                AchievementFilter.LOCKED -> "Заблокированные"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (filter) {
                                AchievementFilter.ALL -> Icons.Filled.FilterList
                                AchievementFilter.UNLOCKED -> Icons.Filled.CheckCircle
                                AchievementFilter.LOCKED -> Icons.Filled.EmojiEvents
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Фильтры по категориям
        Text(
            text = "Категории",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Кнопка "Все"
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Все") }
            )
            
            AchievementCategory.values().forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = {
                        Text(
                            text = when (category) {
                                AchievementCategory.TRANSACTIONS -> "Транзакции"
                                AchievementCategory.BUDGET -> "Бюджет"
                                AchievementCategory.SAVINGS -> "Накопления"
                                AchievementCategory.HABITS -> "Привычки"
                                AchievementCategory.STATISTICS -> "Статистика"
                                AchievementCategory.MILESTONES -> "Вехи"
                                AchievementCategory.SPECIAL -> "Специальные"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Современная карточка достижения
 */
@Composable
private fun ModernAchievementCard(achievement: Achievement) {
    val rarityColors = getRarityColors(achievement.rarity)
    val categoryIcon = getCategoryIcon(achievement.category)
    
    val animatedProgress by animateFloatAsState(
        targetValue = achievement.progressPercentage,
        animationSpec = tween(1000),
        label = "achievement_progress"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (achievement.isUnlocked) 8.dp else 4.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (achievement.isUnlocked) {
                rarityColors.first().copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Заголовок с иконкой и редкостью
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Иконка категории в цветном круге
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.radialGradient(rarityColors),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (achievement.isUnlocked) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = achievement.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (achievement.isUnlocked) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Статус и награда
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (achievement.isUnlocked) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Разблокировано",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Заблокировано",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Награда
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = if (achievement.isUnlocked) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = achievement.rewardCoins.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (achievement.isUnlocked) Color(0xFFFFD700) else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Прогресс (если есть)
            if (achievement.targetProgress > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Прогресс: ${achievement.currentProgress}/${achievement.targetProgress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "${(achievement.progressPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (achievement.isUnlocked) {
                            Color(0xFF4CAF50)
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
 * Получает иконку для категории
 */
private fun getCategoryIcon(category: AchievementCategory): ImageVector {
    return when (category) {
        AchievementCategory.TRANSACTIONS -> Icons.Filled.Timeline
        AchievementCategory.BUDGET -> Icons.Filled.Wallet
        AchievementCategory.SAVINGS -> Icons.Filled.MonetizationOn
        AchievementCategory.HABITS -> Icons.Filled.LocalCafe
        AchievementCategory.STATISTICS -> Icons.Filled.TrendingUp
        AchievementCategory.MILESTONES -> Icons.Filled.Star
        AchievementCategory.SPECIAL -> Icons.Filled.EmojiEvents
    }
}

/**
 * Получает цвета для редкости
 */
private fun getRarityColors(rarity: AchievementRarity): List<Color> {
    return when (rarity) {
        AchievementRarity.COMMON -> listOf(Color(0xFF9E9E9E), Color(0xFF757575))
        AchievementRarity.RARE -> listOf(Color(0xFFFFD700), Color(0xFFFFA000))
        AchievementRarity.EPIC -> listOf(Color(0xFF9C27B0), Color(0xFF673AB7))
        AchievementRarity.LEGENDARY -> listOf(
            Color(0xFFFF5722), Color(0xFFFF9800),
            Color(0xFFFFEB3B), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFF9C27B0)
        )
    }
}
