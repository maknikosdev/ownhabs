package com.ownhabs.app.data.local.dao

import androidx.room.*
import com.ownhabs.app.data.local.entity.BadgeEntity
import com.ownhabs.app.data.local.entity.UnlockedBadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges")
    fun observeBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges")
    suspend fun getAllBadges(): List<BadgeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Query("SELECT * FROM unlocked_badges")
    fun observeUnlocked(): Flow<List<UnlockedBadgeEntity>>

    @Query("SELECT * FROM unlocked_badges")
    suspend fun getAllUnlockedOnce(): List<UnlockedBadgeEntity>

    @Query("SELECT * FROM unlocked_badges WHERE habitId = :habitId AND badgeId = :badgeId LIMIT 1")
    suspend fun findUnlocked(habitId: String, badgeId: String): UnlockedBadgeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlocked(unlocked: UnlockedBadgeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedAll(unlocked: List<UnlockedBadgeEntity>)

    @Query("DELETE FROM unlocked_badges")
    suspend fun clearUnlocked()
}
