package com.habitpulse.app.data.local.dao

import androidx.room.*
import com.habitpulse.app.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun observeLogsForHabit(habitId: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND timestamp BETWEEN :dayStart AND :dayEnd")
    suspend fun getLogsForDay(habitId: String, dayStart: Long, dayEnd: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY timestamp ASC")
    suspend fun getAllForHabit(habitId: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllOnce(): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs")
    fun observeAll(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(logs: List<HabitLogEntity>)

    @Delete
    suspend fun delete(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId")
    suspend fun countForHabit(habitId: String): Int

    @Query("DELETE FROM habit_logs")
    suspend fun clearAll()
}
