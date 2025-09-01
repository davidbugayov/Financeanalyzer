package com.davidbugayov.financeanalyzer.utils.kmp

import com.davidbugayov.financeanalyzer.domain.model.Achievement as DomainAchievement
import com.davidbugayov.financeanalyzer.domain.model.AchievementCategory as DomainCategory
import com.davidbugayov.financeanalyzer.domain.model.AchievementRarity as DomainRarity
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository as DomainRepo
import com.davidbugayov.financeanalyzer.shared.model.Achievement as SharedAchievement
import com.davidbugayov.financeanalyzer.shared.model.AchievementCategory as SharedCategory
import com.davidbugayov.financeanalyzer.shared.model.AchievementRarity as SharedRarity
import com.davidbugayov.financeanalyzer.shared.repository.AchievementsRepository as SharedRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedAchievementsRepositoryAdapter(
    private val domainRepo: DomainRepo,
) : SharedRepo {
    override fun getAllAchievements(): Flow<List<SharedAchievement>> =
        domainRepo.getAllAchievements().map { list -> list.map { it.toShared() } }

    override fun getAchievementById(id: String): Flow<SharedAchievement?> =
        domainRepo.getAchievementById(id).map { it?.toShared() }

    override suspend fun updateAchievement(achievement: SharedAchievement) {
        domainRepo.updateAchievement(achievement.toDomain())
    }

    override suspend fun clearAllAchievements() {
        // Not implemented - can be added later if needed
    }
}

private fun DomainAchievement.toShared(): SharedAchievement =
    SharedAchievement(
        id = this.id,
        title = this.title,
        description = this.description,
        currentProgress = this.currentProgress,
        targetProgress = this.targetProgress,
        isUnlocked = this.isUnlocked,
        dateUnlocked = this.dateUnlocked,
        category = this.category.toShared(),
        rarity = this.rarity.toShared(),
        rewardCoins = this.rewardCoins,
        isHidden = this.isHidden,
    )

private fun SharedAchievement.toDomain(): DomainAchievement =
    DomainAchievement(
        id = this.id,
        title = this.title,
        description = this.description,
        iconRes = 0,
        category = this.category.toDomain(),
        rarity = this.rarity.toDomain(),
        isUnlocked = this.isUnlocked,
        dateUnlocked = this.dateUnlocked,
        currentProgress = this.currentProgress,
        targetProgress = this.targetProgress,
        rewardCoins = this.rewardCoins,
        isHidden = this.isHidden,
    )

private fun DomainCategory.toShared(): SharedCategory =
    when (this) {
        DomainCategory.TRANSACTIONS -> SharedCategory.TRANSACTIONS
        DomainCategory.BUDGET -> SharedCategory.BUDGET
        DomainCategory.SAVINGS -> SharedCategory.GENERAL
        DomainCategory.HABITS -> SharedCategory.GENERAL
        DomainCategory.STATISTICS -> SharedCategory.ANALYTICS
        DomainCategory.MILESTONES -> SharedCategory.GENERAL
        DomainCategory.SPECIAL -> SharedCategory.SPECIAL
        DomainCategory.IMPORT, DomainCategory.EXPORT -> SharedCategory.EXPORT_IMPORT
    }

private fun SharedCategory.toDomain(): DomainCategory =
    when (this) {
        SharedCategory.TRANSACTIONS -> DomainCategory.TRANSACTIONS
        SharedCategory.BUDGET -> DomainCategory.BUDGET
        SharedCategory.ANALYTICS -> DomainCategory.STATISTICS
        SharedCategory.EXPORT_IMPORT -> DomainCategory.EXPORT
        SharedCategory.SECURITY -> DomainCategory.MILESTONES
        SharedCategory.SOCIAL -> DomainCategory.MILESTONES
        SharedCategory.SPECIAL -> DomainCategory.SPECIAL
        SharedCategory.GENERAL -> DomainCategory.MILESTONES
    }

private fun DomainRarity.toShared(): SharedRarity =
    when (this) {
        DomainRarity.COMMON -> SharedRarity.COMMON
        DomainRarity.RARE -> SharedRarity.RARE
        DomainRarity.EPIC -> SharedRarity.EPIC
        DomainRarity.LEGENDARY -> SharedRarity.LEGENDARY
    }

private fun SharedRarity.toDomain(): DomainRarity =
    when (this) {
        SharedRarity.COMMON -> DomainRarity.COMMON
        SharedRarity.RARE -> DomainRarity.RARE
        SharedRarity.EPIC -> DomainRarity.EPIC
        SharedRarity.LEGENDARY -> DomainRarity.LEGENDARY
    }
