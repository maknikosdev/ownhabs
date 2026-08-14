package com.ownhabs.app.data.local.dao

import androidx.room.*
import com.ownhabs.app.data.local.entity.StreakFreezeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakFreezeDao {

    @Query("SELECT * FROM streak_freezes WHERE habitId = :habitId ORDER BY dateEpochDay DESC")
    fun observeForHabit(habitId: String): Flow<List<StreakFreezeEntity>>

    @Query("SELECT * FROM streak_freezes WHERE habitId = :habitId")
    suspend fun getForHabit(habitId: String): List<StreakFreezeEntity>

    @Query("SELECT COUNT(*) FROM streak_freezes WHERE habitId = :habitId AND dateEpochDay BETWEEN :monthStart AND :monthEnd")
    suspend fun countInMonth(habitId: String, monthStart: Long, monthEnd: Long): Int

    @Query("SELECT * FROM streak_freezes WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun findForDate(habitId: String, dateEpochDay: Long): StreakFreezeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(freeze: StreakFreezeEntity)

    @Query("DELETE FROM streak_freezes WHERE id = :id")
    suspend fun deleteById(id: String)
}
